package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.IllegalStateException;
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
    /**
     * The destination that this consumer belongs to
     */
    private final RMQDestination destination;
    /**
     * The session that this consumer was created under
     */
    private final RMQSession session;
    /**
     * Unique tag, used when creating AMQP queues for a consumer that thinks it's a topic
     */
    private final String uuidTag;
    /**
     * The async listener that we use to subscribe to Rabbit messages
     */
    private final AtomicReference<MessageListenerConsumer> listener = new AtomicReference<MessageListenerConsumer>();
    /**
     * Pause latch, used by 
     * {@link javax.jms.Connection#start()} and {@link javax.jms.Connection#stop()}
     */
    private final PauseLatch pauseLatch = new PauseLatch(true);
    /**
     * We need to keep track of how many listeners are running.
     * The reason for this latch is to be able to 
     * not complete a {@link javax.jms.Connection#stop()} call
     * until all listeners have completed. This is spec requirement
     */
    private final CountUpAndDownLatch listenerRunning = new CountUpAndDownLatch(0);
    /**
     * We must track threads that are invoking {@link #receive()} and {@link #receive(long)}
     * cause we must be able to interrupt those threads if close or stop is called
     */
    private final ConcurrentLinkedQueue<Thread> currentSynchronousReceiver = new ConcurrentLinkedQueue<Thread>();
    /**
     * Is this consumer closed. this value should change to true, but never change back
     */
    private volatile boolean closed = false;
    /**
     * If this consumer is in the process of closing
     */
    private volatile boolean closing = false;
    /**
     * A wrapper around the message listener the user sets
     */
    private volatile MessageListenerWrapper userListenerWrapper =null;
    
    /**
     * Flag to check if we are a durable subscription
     */
    private volatile boolean durable = false;
    
    /**
     * Flag to check if we have noLocal set
     */
    private volatile boolean noLocal = false;
    
    /**
     * Only used internally
     */
    private RMQMessageConsumer() {
        this.session = null;
        this.destination = null;
        this.uuidTag = null;
    }
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
     * Note: This implementation always returns null
     */
    @Override
    public String getMessageSelector() throws JMSException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageListener getMessageListener() throws JMSException {
        MessageListenerWrapper wrapper = userListenerWrapper; 
        //if we have a listener, return the actual listener
        return wrapper==null?null:wrapper.getMessageListener();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        try {
            //reset the correct listener
            userListenerWrapper  = listener==null?null : new MessageListenerWrapper(listener);
            //see if we had already set a listener
            MessageListenerConsumer previous = this.listener.get();
            if (listener == null && previous == null) {
                // do nothing - no previous listener
            } else if (listener == null && previous != null) {
                /*
                 * The user called setMessageListener(null) which means we have 
                 * to unsubscribe the previous consumer
                 */
                if (this.listener.compareAndSet(previous, null)) {
                    this.basicCancel(previous.getConsumerTag());
                }
            } else if (previous != null) {
                /*
                 * The user called setMessageListener(new listener)
                 * to override the old one. We can keep our current subscription
                 * and just replace the variable userListenerWrapper
                 * like we did above 
                 */
            } else {
                /*
                 *  The user called setMessageListener(new listener)
                 *  and no previous listener was set, so we must 
                 *  create a subscription to the RabbitMQ channel
                 *  this is done using the basicConsume call
                 */
                previous = new MessageListenerConsumer();
                if (this.listener.compareAndSet(null, previous)) {
                    /*
                     * If we reached this point, we have a new subscription
                     * We will subscribe the consumer and set the reference to the 
                     * consumer tag so we have it for when we need to cancel the subscription
                     */
                    String consumerTag = basicConsume(previous);
                    previous.setConsumerTag(consumerTag);
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
        Util.util().checkTrue(closing, "Consumer is in the process of closing.");
        /*
         * The spec identifies 0 as infinite timeout
         */
        if (timeout == 0) {
            timeout = Long.MAX_VALUE;
        }
        /*
         * track when we started this call 
         */
        long now = System.currentTimeMillis();

        /*
         * Try to receive a message without waiting
         * This call can pause on the pauseLatch
         * if the Connection.stop method has been called
         */
        Message msg = null;
        /*this is a state that can be interrupted */
        this.currentSynchronousReceiver.offer(Thread.currentThread());
        try {
            msg = receiveNoWait(timeout);
        } finally {
            this.currentSynchronousReceiver.remove(Thread.currentThread());
        }
        
        if (Thread.currentThread().isInterrupted()) {
            /* if the thread has been interrupted waiting for the pause */
            return null;
        }
        
        /*
         * Calculate the new timeout
         */
        timeout = Math.max(timeout - (System.currentTimeMillis() - now), 0);

        
        if (msg != null) {
            /*
             * We received a message - return it
             */
            return msg;
        } else if (timeout==0) {
            /*
             * We timed out - calls to receive() and 
             * receive(long) can timeout when the Connection.stop 
             * is in effect. A timeout means we return null to the caller
             */
            return null;
        }
        
        /*
         * Currently there is no equivalent of receive(timeout) in 
         * RabbitMQ's Java API. So we emulate that behavior by creating a  
         * onetime subscription to the Channel.
         * We use the object SynchronousConsumer - this object supports 
         * both timeout and interrupts
         */
        String consumerTag = null;
        SynchronousConsumer sc = null;
        /*
         * If we reached here, we will be entering an async state and must notify our 
         * listener counter
         */
        this.listenerRunning.countUp();
        try {
            /*
             * Register the existing thread so we can interrupt it 
             * if we need to
             */
            this.currentSynchronousReceiver.offer(Thread.currentThread());
            /*
             * Create the consumer object - in here we specify the timeout too
             */
            sc = new SynchronousConsumer(this.session.getChannel(), timeout);
            /*
             * Subscribe the consumer object
             */
            consumerTag = basicConsume(sc);
            /*
             * Wait for an message to arrive
             * this returns null if we timeout
             */
            GetResponse response = sc.receive();
            /*
             * Process the result - even if it is null
             */
            RMQMessage message = (RMQMessage) processMessage(response, isAutoAck());
            /*
             * Now we must handle the case where Connection.stop has been called
             * while we were waiting for the message to arrive
             */
            if (message!=null) {
                /*
                 * If we reached here, means that the Connection.stop
                 * method was called before a message arrived
                 * But we are not allowed to return this message
                 * cause we are in a stop/pause state
                 * {@link Connection#stop}
                 * 
                 */
                timeout = Math.max(timeout - (System.currentTimeMillis() - now), 0);
                
                boolean isTimedout = true;
                try {
                    /*
                     * We need to wait for the Connection.start method to be called
                     * and we need to respect the timeout and return null if isTimedout==true
                     */
                    isTimedout = ! pauseLatch.await(timeout, TimeUnit.MILLISECONDS);
                }catch (InterruptedException x) {
                    /*
                     * Do nothing , keep the thread interrupt status intact
                     */
                }
                if (isTimedout) {
                    /*
                     * NACK message, cause the client timed out while the receiver was 
                     * paused
                     */
                    getSession().getChannel().basicNack(message.getRabbitDeliveryTag(), false, true);
                    return null;
                } else if (isAutoAck()) {
                    getSession().getChannel().basicAck(message.getRabbitDeliveryTag(), false);
                }
            }
            return message;
        } catch (IOException x) {
            Util.util().handleException(x);
        } finally {
            /*
             * Don't attempt to cancel if we are either closing or closed
             */
            if (consumerTag!=null && sc!=null && closing==false && closed==false) {
                try {
                    /*
                     * Cancel the subscription.
                     * This may have already be done in 
                     * that case this method returns immediately
                     */
                    sc.cancel(consumerTag);
                }finally {
                    /*
                     * Now the thread pool can shutdown
                     */
                    listenerRunning.countDown();
                }
            } else {
                /*
                 * Now the thread pool can shutdown
                 */
                listenerRunning.countDown();
            }
            /* we are not in an interrupt state anymore */
            this.currentSynchronousReceiver.remove(Thread.currentThread());
        }
        return null;
    }

    /**
     * Returns true if messages should be auto acknowledged upon arrival
     * @return true if {@link Session#getAcknowledgeMode()}=={@link Session#DUPS_OK_ACKNOWLEDGE} or 
     * {@link Session#getAcknowledgeMode()}=={@link Session#AUTO_ACKNOWLEDGE} 
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
            /*
             * javax.jms.Queue we share a single AMQP queue among all consumers
             * hence the name will the the name of the destination
             */
            name = this.destination.getName();
        } else {
            /*
             * javax.jms.Topic we create a unique AMQP queue for each consumer
             * and that name is unique for this consumer alone
             */
            name = this.getUUIDTag();
        }
        //never ack async messages automatically, only when we can deliver them
        //to the actual consumer so we pass in false as the auto ack mode
        //we must support setMessageListener(null) while messages are arriving
        //and those message we NACK
        return getSession().getChannel().basicConsume(/* 
                                                       * the name of the subscriber 
                                                       */
                                                      name, 
                                                      /* 
                                                       * autoack is ALWAYS false, otherwise we risk
                                                       * acking messages that are received to the client
                                                       * but the client listener(onMessage) has not yet been invoked
                                                       */
                                                      false,
                                                      /*
                                                       * the consumer tag, random plus an identifier
                                                       */
                                                      "jms-consumer-"+Util.util().generateUUIDTag(), //the consumer tag
                                                      /*
                                                       * we do support the noLocal for subscriptions 
                                                       */
                                                      this.getNoLocalNoException(), 
                                                      /* 
                                                       * exclusive will always be false
                                                       * exclusive consumer access, meaning only this consumer can access the queue. 
                                                       */
                                                      false, 
                                                      /*
                                                       * no custom arguments for the subscriber
                                                       */
                                                      new HashMap<String,Object>(), 
                                                      /*
                                                       * The callback object for handleDelivery()
                                                       */
                                                      consumer); 
    }

    /**
     * Cancels an async consumer on a channel
     * @param consumerTag the tag to be cancelled
     * @throws IOException 
     * @see {@link Channel#basicCancel(String)}
     */
    protected void basicCancel(String consumerTag) throws IOException {
        if (consumerTag!=null) {
            /*cancel a known subscription*/
            getSession().getChannel().basicCancel(consumerTag);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message receiveNoWait() throws JMSException {
        Util.util().checkTrue(closing, new IllegalStateException("Consumer in the process of closing"));
        Util.util().checkTrue(closed, new IllegalStateException("Consumer in the process of closing"));
        return receiveNoWait(0);
    }
    
    protected Message receiveNoWait(long timeout) throws JMSException {
        try {
            /*
             * If the connection is stopped, by Connection.stop
             * we will wait here until we either timeout
             * or Connection.start is called again
             */
            if (!pauseLatch.await(timeout, TimeUnit.MILLISECONDS)) {
                /*
                 *  timeout happened before we got a chance to look
                 *  for a message so we return null
                 */
                return null; 
            }
        } catch (InterruptedException x) {
            // this is normal since the implementation can interrupt
            // threads that are waiting
            return null;
        }
        
        /*
         * our result will be stored here 
         */
        RMQMessage message = null;
        
        try {
            GetResponse response = null;
            if (this.destination.isQueue()) {
                /*
                 * For queue, issue a basic.get on the queue name
                 */
                response = this.getSession().getChannel().basicGet(this.destination.getQueueName(), isAutoAck());
            } else {
                /*
                 * For topic, issue a basic.get on the unique queue name for the consumer
                 */
                response = this.getSession().getChannel().basicGet(this.getUUIDTag(), isAutoAck());
            }
            /*
             * convert the message and ack if we need to
             */
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
    protected Message processMessage(GetResponse response, boolean acknowledged) throws JMSException {
        try {
            if (response == null) {
                /*
                 * return null if the response is null
                 */
                return null;
            }
            /*
             * notify the session that the message has been
             * received. this call will store the delivery tag
             * if it is the greatest delivery tag so that it can be used
             * to nack messages upon rollback
             */
            this.session.messageReceived(response);
            /*
             * Deserialize the message from the byte[]
             */
            RMQMessage message = RMQMessage.fromMessage(response.getBody());
            /*
             * Received messages contain a reference to their delivery tag
             * this is used in Message.acknowledge
             */
            message.setRabbitDeliveryTag(response.getEnvelope().getDeliveryTag());
            /*
             * Set a reference to this session
             * this is used in Message.acknowledge
             */
            message.setSession(getSession());
            /*
             * Set the destination this message was received from
             */
            message.setJMSDestination(getDestination());
            /*
             * Initially the message is 
             * readOnly properties == true until clearProperties has been called
             * readOnly body == true until clearBody has been called
             */
            message.setReadonly(true);
            /*
             * Set the redelivered flag.
             * we inherit this from the RabbitMQ broker
             */
            message.setJMSRedelivered(response.getEnvelope().isRedeliver());
            if (!acknowledged) {
                /*
                 * If the message has not been acknowledged
                 * let the session know so that it can track 
                 * unacked messages
                 */
                getSession().unackedMessageReceived(message);
            }
            try {
                /*
                 * If the Session.setMessageListener() has been 
                 * set with a listener, we must invoke it at this time
                 */
                MessageListener listener = getSession().getMessageListener();
                if (listener != null) {
                    try {
                        /*
                         * Notify our consumer that a listener is running
                         */
                        listenerRunning.countUp();
                        listener.onMessage(message);
                    } finally {
                        /*
                         * Listener has completed
                         */
                        listenerRunning.countDown();
                    }
                }
            } catch (JMSException x) {
                /*
                 * We can not propagate this exception
                 * but it should be logged
                 */
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
     * returns true if {@link RMQMessageConsumer#close()} has been invoked and the call has completed.
     * returns false if the {@link RMQMessageConsumer#close()} has not been called or is in progress
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * Method called internally or by the Session
     * when system is shutting down
     */
    protected void internalClose() throws JMSException {
        /*
         * let the system know we are in the process of closing 
         */
        closing = true;
        /* 
         * if we are in a pause state, we must break that 
         * this will release all threads waiting on the pause latch
         * and effectively disable the use of the latch 
         */
        pauseLatch.finalResume();
        /*
         * cancel any subscriptions that we have
         * active at this time. there is no way to notify the 
         * listener, so all we can do is set it to null
         */
        setMessageListener(null);
        try {
            /*
             * Loop through all threads that are waiting in the 
             * receive() or receive(timeout) methods and interrupt them
             */
            Thread t = null;
            while ((t=currentSynchronousReceiver.poll())!=null) {
                t.interrupt();
            }
            /*
             * The timeout is configurable as part of the connection factory
             */
            long timeoutMillis = getSession().getConnection().getTerminationTimeout();
            /*
             * If there are listeners running, processing async messages
             * we must wait until those messages are complete
             * we can of course timeout, if a thread is stuck
             */
            listenerRunning.awaitZero(timeoutMillis, TimeUnit.MILLISECONDS);
        }catch (InterruptedException x) {
            //do nothing
            //TODO log debug level message
        } finally {
            closed = true;
            closing = false;
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
        return getNoLocalNoException();
    }
    
    /**
     * @see {@link #getNoLocal()}
     * @return true if the noLocal variable was set
     */
    public boolean getNoLocalNoException() {
        return noLocal;
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
     * Return true if durable
     * @return
     */
    public boolean isDurable() {
        return durable;
    }

    /**
     * Set durable status
     * @param durable
     */
    protected void setDurable(boolean durable) {
        this.durable = durable;
    }

    /**
     * Configures the no local for this consumer
     * This is currently only used when subscribing an async consumer
     * @param noLocal
     */
    public void setNoLocal(boolean noLocal) {
        this.noLocal = noLocal;
    }

    /**
     * Inner class that lets us lock the listener 
     * To stop this listener from processing
     * lock the write lock
     */
    protected class MessageListenerWrapper implements MessageListener {
        /**
         * This lock is used as a read lock when invoked async 
         * by the subscription we have with the RabbitMQ API.
         */
        protected ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        /**
         * The actual listener we want to deliver the message to
         */
        private final MessageListener listener;
        
        /**
         * Create a listener wrapper
         * @param listener the listener to invoke onMessage on, may NOT be null
         */
        public MessageListenerWrapper(MessageListener listener) {
            this.listener = listener;
        }
        
        /**
         * Returns the listener we are delivering messages to
         * @return
         */
        public MessageListener getMessageListener() {
            return listener;
        }
        
        /**
         * {@inheritDoc}
         */
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
     * this implements a RabbitMQ Consumer callback object
     */
    protected class MessageListenerConsumer implements Consumer {
        /**
         * The consumer tag for this RabbitMQ consumer
         */
        private volatile String consumerTag;


        /**
         * Constructor
         */
        public MessageListenerConsumer() {
        }

        /**
         * Returns the consumer tag used for this consumer
         * @return the consunmer tag for this consumer
         */
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
            /*
             * Assign the consumer tag, we are not reusing consumer objects for different subscriptions
             * this is a safe to do
             */
            if (this.consumerTag==null) this.consumerTag = consumerTag;
            /*
             * Wrap the incoming message in a GetResponse 
             */
            GetResponse response = new GetResponse(envelope, properties, body, 0);
            try {
                try {
                    /*
                     * Count up our latch, incase Connection.stop is called
                     * that call wont return until we are done processing the message
                     */
                    listenerRunning.countUp();
                    MessageListener listener = userListenerWrapper ;
                    if (listener!=null) {
                        boolean acked = isAutoAck();
                        if (isAutoAck()) {
                            try {
                                /*
                                 * Subscriptions we never auto ack, so we have a listener
                                 * and we know that we will deliver the message
                                 * ack it now
                                 */
                                getSession().getChannel().basicAck(envelope.getDeliveryTag(), false);
                                /*
                                 * Mark message as acked
                                 */
                                acked = true;
                            } catch (AlreadyClosedException x) {
                                //TODO logging impl warn message
                                //this is problematic, we have a client, but we can't ack the message to the server
                                x.printStackTrace();
                                //TODO should we deliver the message at this time, knowing that we can't ack it?
                                //My recommendation is that we bail out here and not proceed
                            }
                        }
                        /*
                         * Create a javax.jms.Message object
                         */
                        Message message = processMessage(response, acked);
                        /*
                         * Deliver it to the client
                         */
                        listener.onMessage(message);
                    } else {
                        try {
                            /*
                             * We are unable to deliver the message, nack it
                             */
                            getSession().getChannel().basicNack(envelope.getDeliveryTag(), false, true);
                        } catch (AlreadyClosedException x) {
                            //TODO logging impl debug message
                            //this is fine. we didn't ack the message in the first place
                        }
                    }
                } finally {
                    /*
                     * make sure we update our latch
                     */
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
