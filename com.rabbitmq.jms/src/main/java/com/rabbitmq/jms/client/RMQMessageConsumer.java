package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.PauseLatch;
import com.rabbitmq.jms.util.Util;

public class RMQMessageConsumer implements MessageConsumer, QueueReceiver, TopicSubscriber {

    private final RMQDestination destination;
    private final RMQSession session;
    private final String uuidTag;
    private AtomicReference<MessageListenerWrapper> listener = new AtomicReference<MessageListenerWrapper>();
    private final PauseLatch pauseLatch = new PauseLatch(false);

    /**
     * Creates a RMQMessageConsumer object. Internal constructor used by {@link RMQSession}
     * @param session - the session object that created this consume 
     * @param destination - the destination for this consumer
     * @param uuidTag - when creating queues to a topic, we need a unique queue name for each consumer. This is the unique name
     */
    public RMQMessageConsumer(RMQSession session, RMQDestination destination, String uuidTag) {
        this.session = session;
        this.destination = destination;
        this.uuidTag = uuidTag;
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
        MessageListenerWrapper wrapper = this.listener.get();
        if (wrapper != null) {
            return wrapper.getMessageListener();
        }
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        try {
            MessageListenerWrapper wrapper = listener==null?null:this.wrap(listener);
            MessageListenerWrapper previous = this.listener.getAndSet(wrapper);
            if (previous != null) {
                this.basicCancel(previous.getConsumerTag());
            }
            if (wrapper!=null) {
                String consumerTag = basicConsume(wrapper);
                wrapper.setConsumerTag(consumerTag);
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
        Message msg = receiveNoWait();
        if (msg != null) {
            // attempt instant receive first
            return msg;
        }
        if (timeout == 0) {
            timeout = Long.MAX_VALUE;
        }

        try {
            SynchronousConsumer sc = new SynchronousConsumer(this.session.getChannel(), timeout, session.getAcknowledgeMode());
            basicConsume(sc);
            GetResponse response = sc.receive();
            return processMessage(response);
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        return null;
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
        
        return getSession().getChannel().basicConsume(name, !getSession().getTransactedNoException(), consumer);
    }

    /**
     * Cancels an async consumer on a channel
     * @param consumerTag the tag to be cancelled
     * @throws IOException 
     * @see {@link Channel#basicCancel(String)}
     */
    protected void basicCancel(String consumerTag) throws IOException {
        getSession().getChannel().basicCancel(consumerTag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message receiveNoWait() throws JMSException {
        try {
            GetResponse response = null;
            if (this.destination.isQueue()) {
                response = this.getSession().getChannel().basicGet(this.destination.getQueueName(), !this.getSession().getTransacted());
            } else {
                response = this.getSession().getChannel().basicGet(this.getUUIDTag(), !this.getSession().getTransacted());
            }

            return processMessage(response);
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
    private Message processMessage(GetResponse response) throws JMSException {
        try {
            if (response == null)
                return null;
            this.session.messageReceived(response);
            RMQMessage message = RMQMessage.fromMessage(response.getBody());
            try {
                MessageListener listener = getSession().getMessageListener();
                if (listener!=null) listener.onMessage(message);
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
        getSession().consumerClose(this);
        internalClose();
    }
    
    /**
     * Method called internally or by the Session
     * when system is shutting down
     */
    protected void internalClose() throws JMSException {
        setMessageListener(null);        
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
    protected MessageListenerWrapper wrap(MessageListener listener) {
        return new MessageListenerWrapper(listener);
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
     */
    public void pause() throws JMSException {
        try {
            pauseLatch.pause();
        } catch (IllegalStateException x) {
            Util.util().handleException(x);
        }
    }
    
    /** 
     * Resubscribes all async listeners
     * and continues to receive messages
     * @see {@link javax.jms.Connection#stop()}
     */
    public void resume() throws JMSException  {
        try {
            pauseLatch.resume();
        } catch (IllegalStateException x) {
            Util.util().handleException(x);
        }
    }
    
    /**
     * Redelivers all the messages this 
     * consumer has received but not acknowledged
     * @see {@link javax.jms.Session#recover()}
     */
    public void recover() throws JMSException {
        
    }
    
    /**
     * Acknowledges a message manually.
     * Invoked when the method 
     * {@link javax.jms.Message#acknowledge()}
     * @param message - the message to be acknowledged
     */
    public void acknowledge(RMQMessage message) {
        
    }
    
    /**
     * Inner class to wrap MessageListener in order to consume 
     * messages and propagate them to the calling client
     */
    protected class MessageListenerWrapper implements Consumer {

        private MessageListener listener;
        private volatile String consumerTag;
        

        public MessageListenerWrapper(MessageListener listener) {
            this.listener = listener;
        }

        public String getConsumerTag() {
            return consumerTag;
        }

        public void setConsumerTag(String consumerTag) {
            this.consumerTag = consumerTag;
        }

        public MessageListener getMessageListener() {
            return listener;
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
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
            if (this.consumerTag==null) this.consumerTag = consumerTag;
            GetResponse response = new GetResponse(envelope, properties, body, 0);
            try {
                Message message = processMessage(response);
                this.listener.onMessage(message);
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
            // TODO Auto-generated method stub

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleRecoverOk(String consumerTag) {
            // TODO Auto-generated method stub

        }

    }

}
