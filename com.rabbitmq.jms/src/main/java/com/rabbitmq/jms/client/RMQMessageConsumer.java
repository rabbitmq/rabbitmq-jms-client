package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.CountUpAndDownLatch;
import com.rabbitmq.jms.util.PauseLatch;
import com.rabbitmq.jms.util.Util;

public class RMQMessageConsumer implements MessageConsumer, QueueReceiver, TopicSubscriber {

    private final RMQDestination destination;
    private final RMQSession session;
    private final String uuidTag;
    private final AtomicReference<MessageListenerConsumer> listener = new AtomicReference<MessageListenerConsumer>();
    private final PauseLatch pauseLatch = new PauseLatch(true);
    private volatile java.util.Queue<RMQMessage> receivedMessages = new ConcurrentLinkedQueue<RMQMessage>();
    private volatile java.util.Queue<RMQMessage> recoveredMessages = new ConcurrentLinkedQueue<RMQMessage>();
    private final CountUpAndDownLatch listenerRunning = new CountUpAndDownLatch(0);
    private final ConcurrentLinkedQueue<Thread> currentSynchronousReceiver = new ConcurrentLinkedQueue<Thread>();
    private volatile boolean closed = false;

    /**
     * Creates a RMQMessageConsumer object. Internal constructor used by {@link RMQSession}
     * @param session - the session object that created this consume 
     * @param destination - the destination for this consumer
     * @param uuidTag - when creating queues to a topic, we need a unique queue name for each consumer. This is the unique name
     */
    public RMQMessageConsumer(RMQSession session, RMQDestination destination, String uuidTag, boolean paused) {
        this.session = session;
        this.destination = destination;
        this.uuidTag = uuidTag;
        if (!paused) pauseLatch.resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue getQueue() throws JMSException {
        return destination;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException
     */
    @Override
    public String getMessageSelector() throws JMSException {
        //TODO implement getMessageSelector
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageListener getMessageListener() throws JMSException {
        MessageListenerWrapper wrapper = userListenerWrapper; 
        return wrapper==null?null:wrapper.getMessageListener();
    }
    private volatile MessageListenerWrapper userListenerWrapper =null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        try {
            userListenerWrapper  = listener==null?null : new MessageListenerWrapper(listener);
            MessageListenerConsumer previous = this.listener.get();
            if (listener == null && previous == null) {
                // do nothing - no previous
            } else if (listener == null && previous != null) {
                // unsubscribe
                if (this.listener.compareAndSet(previous, null)) {
                    this.basicCancel(previous.getConsumerTag());
                }
            } else if (previous != null) {
                // piggy back of the existing subscription, just swap the
                // listener
            } else {
                previous = this.wrap();
                if (this.listener.compareAndSet(null, previous)) {
                    // new subscription
                    String consumerTag = basicConsume(previous);
                    previous.setConsumerTag(consumerTag);
                }
            }
            MessageListenerWrapper wrapper = userListenerWrapper;
            if (wrapper!=null && recoveredMessages.size()>0) {
                wrapper.rwl.writeLock().lock();
                try {
                    RMQMessage msg;
                    while ((msg=recoveredMessages.poll()) != null) {
                        wrapper.onMessage(msg);
                    }
                } finally {
                    wrapper.rwl.writeLock().unlock();
                }
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message receive() throws JMSException {
        return receive(Long.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message receive(long timeout) throws JMSException {
        Util.util().checkTrue(closed, "Consumer has already been closed.");
        if (timeout == 0) {
            timeout = Long.MAX_VALUE;
        }
        
        long now = System.currentTimeMillis();

        Message msg = receiveNoWait(timeout);
        
        timeout = Math.max(timeout - (System.currentTimeMillis() - now), 0);

        
        if (msg != null) {
            // attempt instant receive first
            return msg;
        } else if (timeout==0) {
            return null;
        }
        
        String consumerTag = null;
        SynchronousConsumer sc = null;
        try {
            this.currentSynchronousReceiver.offer(Thread.currentThread());
            sc = new SynchronousConsumer(this.session.getChannel(), timeout, session.getAcknowledgeMode());
            consumerTag = basicConsume(sc);
            GetResponse response = sc.receive();
            return processMessage(response, isAutoAck());
        } catch (IOException x) {
            Util.util().handleException(x);
        } finally {
            this.currentSynchronousReceiver.remove(Thread.currentThread());
            if (consumerTag!=null && sc!=null) sc.cancel(consumerTag);
        }
        return null;
    }

    /**
     * Returns true if messages are auto acknowledged
     * @return
     */
    private boolean isAutoAck() throws JMSException {
        return (getSession().getAcknowledgeMode()==Session.DUPS_OK_ACKNOWLEDGE || getSession().getAcknowledgeMode()==Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Register an async listener with the Rabbit API
     * to receive messages
     * @param consumer - the consumer
     * @return the consumer tag created for this consumer
     * @throws IOException
     * @see {@link Channel#basicConsume(String, boolean, String, boolean, boolean, java.util.Map, Consumer)}
     */
    protected String basicConsume(Consumer consumer) throws IOException {
        String name = null;
        if (this.destination.isQueue()) {
            name = this.destination.getName();
        } else {
            name = this.getUUIDTag();
        }
        //never ack async messages automatically, only when we can deliver them
        return getSession().getChannel().basicConsume(name, false , consumer);
    }

    /**
     * Cancels an async consumer on a channel
     * @param consumerTag the tag to be cancelled
     * @throws IOException 
     * @see {@link Channel#basicCancel(String)}
     */
    protected void basicCancel(String consumerTag) throws IOException {
        if (consumerTag!=null) {
            getSession().getChannel().basicCancel(consumerTag);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message receiveNoWait() throws JMSException {
        return receiveNoWait(0);
    }
    
    public Message receiveNoWait(long timeout) throws JMSException {
        try {
            // the connection may be stopped
            if (!pauseLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                return null; // timeout happened before we got a chance to look
                             // for a message meaning we need to time out
            }
        } catch (InterruptedException x) {
            // TODO logging implementation
            return null;
        }

        
        RMQMessage message = recoveredMessages.poll();
        if (message!=null) {
            //we have recovered messages
            return message;
        }
        try {
            GetResponse response = null;
            if (this.destination.isQueue()) {
                response = this.getSession().getChannel().basicGet(this.destination.getQueueName(), isAutoAck());
            } else {
                response = this.getSession().getChannel().basicGet(this.getUUIDTag(), isAutoAck());
            }

            return processMessage(response, isAutoAck());
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        return null;
    }

    /**
     * Converts a {@link GetResponse} to a {@link Message}
     * @param response
     * @return
     * @throws JMSException
     */
    private Message processMessage(GetResponse response, boolean acknowledged) throws JMSException {
        try {
            if (response == null)
                return null;
            this.session.messageReceived(response);
            RMQMessage message = RMQMessage.fromMessage(response.getBody());
            message.setRabbitDeliveryTag(response.getEnvelope().getDeliveryTag());
            message.setRabbitConsumer(this);
            if (!acknowledged) {
                RMQMessage clone = RMQMessage.fromMessage(response.getBody());
                clone.setRabbitDeliveryTag(response.getEnvelope().getDeliveryTag());
                clone.setRabbitConsumer(this);
                receivedMessages.add(clone);
            }
            try {
                MessageListener listener = getSession().getMessageListener();
                if (listener != null) {
                    try {
                        listenerRunning.countUp();
                        listener.onMessage(message);
                    } finally {
                        listenerRunning.countDown();
                    }
                }
            } catch (JMSException x) {
                x.printStackTrace(); //TODO logging implementation
            }
            return message;
        } catch (IOException x) {
            Util.util().handleException(x);
        } catch (ClassNotFoundException x) {
            Util.util().handleException(x);
        } catch (IllegalAccessException x) {
            Util.util().handleException(x);
        } catch (InstantiationException x) {
            Util.util().handleException(x);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws JMSException {
        try {
            internalClose();
        } finally {
            getSession().consumerClose(this);
        }
    }

    /**
     * Method called internally or by the Session
     * when system is shutting down
     */
    protected void internalClose() throws JMSException {
        pauseLatch.resume();
        setMessageListener(null);
        try {
            Thread t = null;
            while ((t=currentSynchronousReceiver.poll())!=null) {
                t.interrupt();
            }
            long timeoutMillis = getSession().getConnection().getTerminationTimeout();
            listenerRunning.awaitZero(timeoutMillis, TimeUnit.SECONDS);
        }catch (InterruptedException x) {
            //do nothing
            //TODO log debug level message
        } finally {
            closed = true;
        }
        

    }

    /**
     * Returns the destination this message consumer is registered with
     * @return the destination this message consumer is registered with
     */
    public RMQDestination getDestination() {
        return this.destination;
    }

    /**
     * Returns the session this consumer was created by
     * @return the session this consumer was created by
     */
    public RMQSession getSession() {
        return this.session;
    }

    /**
     * The unique tag that this consumer holds
     * @return unique tag that this consumer holds
     */
    public String getUUIDTag() {
        return this.uuidTag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic getTopic() throws JMSException {
        if (this.getDestination().isQueue()) {
            throw new JMSException("Destination is of type Queue, not Topic");
        }
        return this.getDestination();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getNoLocal() throws JMSException {
        return false;
    }

    /**
     * Wraps a JMS {@link MessageListener} object with an internal object
     * that can receive messages, a {@link Consumer}
     * @param listener the {@link MessageListener} object 
     * @return a wrapper object 
     */
    protected MessageListenerConsumer wrap() {
        return new MessageListenerConsumer();
    }


    /**
     * Returns true if we are currently not receiving any messages
     * due to a connection pause. 
     * @see {@link javax.jms.Connection#stop()}
     * @return true if we are not receiving any messages at this time
     */
    public boolean isPaused() {
        return pauseLatch.isPaused();
    }

    /**
     * Stops this consumer from receiving messages.
     * This is called by the session indirectly after 
     * {@link javax.jms.Connection#stop()} has been invoked.
     * In this implementation, any async consumers will be 
     * cancelled, only to be re-subscribed when 
     * @throws {@link javax.jms.JMSException} if the thread is interrupted
     */
    public void pause() throws JMSException {
        pauseLatch.pause();
    }

    /** 
     * Resubscribes all async listeners
     * and continues to receive messages
     * @see {@link javax.jms.Connection#stop()}
     * @throws {@link javax.jms.JMSException} if the thread is interrupted
     */
    public void resume() throws JMSException  {
        pauseLatch.resume();
    }

    /**
     * Redelivers all the messages this 
     * consumer has received but not acknowledged
     * @see {@link javax.jms.Session#recover()}
     */
    public void recover() throws JMSException {
        java.util.Queue<RMQMessage> tmp = receivedMessages;
        receivedMessages = new ConcurrentLinkedQueue<RMQMessage>();
        recoveredMessages.addAll(tmp);
        for (RMQMessage msg : recoveredMessages) {
            msg.setJMSRedelivered(true);
        }

        RMQMessage message = null;
        MessageListener listener = getMessageListener(); 
        if (listener!=null) {
            while ((message = recoveredMessages.poll()) != null) {
                listener.onMessage(message);
            }
        }
    }

    /**
     * Acknowledges a message manually.
     * Invoked when the method 
     * {@link javax.jms.Message#acknowledge()}
     * @param message - the message to be acknowledged
     */
    public void acknowledge(RMQMessage message) throws JMSException{
        try {
            receivedMessages.remove(message);
            if ((!getSession().isAutoAck()) && (!getSession().getTransacted())) {
                getSession().getChannel().basicAck(message.getRabbitDeliveryTag(), false);
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    /**
     * Inner class that lets us lock the listener 
     * while recovering messages
     */
    protected class MessageListenerWrapper implements MessageListener {

        protected ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private final MessageListener listener;
        
        public MessageListenerWrapper(MessageListener listener) {
            this.listener = listener;
        }
        
        public MessageListener getMessageListener() {
            return listener;
        }
        
        @Override
        public void onMessage(Message message) {
            rwl.readLock().lock();
            try {
                listener.onMessage(message);
            } finally {
                rwl.readLock().unlock();
            }
        }
        
    }
    
    /**
     * Inner class to wrap MessageListener in order to consume 
     * messages and propagate them to the calling client
     */
    protected class MessageListenerConsumer implements Consumer {

        private volatile String consumerTag;


        public MessageListenerConsumer() {
        }

        public String getConsumerTag() {
            return consumerTag;
        }

        public void setConsumerTag(String consumerTag) {
            this.consumerTag = consumerTag;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleConsumeOk(String consumerTag) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleCancelOk(String consumerTag) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleCancel(String consumerTag) throws IOException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
            if (this.consumerTag==null) this.consumerTag = consumerTag;
            GetResponse response = new GetResponse(envelope, properties, body, 0);
            try {
                
                //this should not happen if it is paused, we need to figure that out
                //TODO
                try {
                    listenerRunning.countUp();
                    MessageListener listener = userListenerWrapper ;
                    if (listener!=null) {
                        boolean acked = isAutoAck();
                        if (isAutoAck()) {
                            try {
                                getSession().getChannel().basicAck(envelope.getDeliveryTag(), false);
                                acked = true;
                            } catch (AlreadyClosedException x) {
                                //TODO logging impl warn message
                                //this is problematic, we have a client, but we can't ack the message to the server
                                x.printStackTrace();
                            }
                        }
                        Message message = processMessage(response, acked);
                        listener.onMessage(message);
                    } else {
                        try {
                            getSession().getChannel().basicNack(envelope.getDeliveryTag(), false, true);
                        } catch (AlreadyClosedException x) {
                            //TODO logging impl debug message
                            //this is fine. we didn't ack the message in the first place
                        }
                    }
                } finally {
                    listenerRunning.countDown();
                }
            } catch (JMSException x) {
                x.printStackTrace(); //TODO logging implementation
                throw new IOException(x);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            // noop

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleRecoverOk(String consumerTag) {
            // noop

        }

    }

}
