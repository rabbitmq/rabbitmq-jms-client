package com.rabbitmq.jms.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQMapMessage;
import com.rabbitmq.jms.client.message.RMQObjectMessage;
import com.rabbitmq.jms.client.message.RMQStreamMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import com.rabbitmq.jms.util.CountUpAndDownLatch;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ implementation of JMS {@link Session}
 */
public class RMQSession implements Session, QueueSession, TopicSession {

    private final RMQConnection connection;
    private final boolean transacted;
    private final int acknowledgeMode;
    private volatile Channel channel;
    private volatile boolean closed = false;
    private volatile Long lastReceivedTag;
    private volatile MessageListener messageListener;
    private final ArrayList<RMQMessageProducer> producers = new ArrayList<RMQMessageProducer>();
    private final ArrayList<RMQMessageConsumer> consumers = new ArrayList<RMQMessageConsumer>();
    private final CountUpAndDownLatch runningListener = new CountUpAndDownLatch(0);
    
    private final ConcurrentHashMap<String, String> subscriptions = new ConcurrentHashMap<String, String>();

    /**
     * Creates a session object associated with a connection
     * @param connection the connection that we will send data on
     * @param transacted whether this session is transacted or not
     * @param mode the default ack mode
     * @throws JMSException if we fail to create a {@link Channel} object on the connection
     */
    public RMQSession(RMQConnection connection, boolean transacted, int mode) throws JMSException {
        assert (mode >= 0 && mode <= 3);
        this.connection = connection;
        this.transacted = transacted;
        this.acknowledgeMode = transacted ? Session.SESSION_TRANSACTED : mode;
        try {
            this.channel = connection.getRabbitConnection().createChannel();
            if (transacted) {
                this.channel.txSelect();
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        assert this.channel != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed");
        return new RMQBytesMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapMessage createMapMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed");
        return new RMQMapMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message createMessage() throws JMSException {
        return createTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed");
        return new RMQObjectMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        ObjectMessage message = createObjectMessage();
        message.setObject(object);
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed");
        return new RMQStreamMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed");
        return new RMQTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        TextMessage msg = createTextMessage();
        msg.setText(text);
        return msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getTransacted() throws JMSException {
        return getTransactedNoException();
    }

    /**
     * Same as {@link #getTransacted()}
     * but does not declare a JMSException in the throw clause
     * @return true if this session is transacted
     * @see {@link #getTransacted()}
     */
    public boolean getTransactedNoException() {
        return this.transacted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAcknowledgeMode() throws JMSException {
        return getAcknowledgeModeNoException();
    }

    /**
     * Same as {@link RMQSession#getAcknowledgeMode()} but without 
     * a declared exception in the throw clause
     * @return the acknowledge mode, one of {@link Session#AUTO_ACKNOWLEDGE},
     * {@link Session#CLIENT_ACKNOWLEDGE}, {@link Session#DUPS_OK_ACKNOWLEDGE} or 
     * {@link Session#SESSION_TRANSACTED}
     */
    public int getAcknowledgeModeNoException() {
        return this.acknowledgeMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed");
        if (!this.transacted)
            return;
        try {
            //call commit on the channel
            this.channel.txCommit();
            //this should ack all messages
            lastReceivedTag = null;
        } catch (Exception x) {
            Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed");
        if (!this.transacted)
            return;
        try {
            //call rollback
            this.channel.txRollback();
            //TODO if we have messages, do we need to NACK them?
            if (lastReceivedTag != null) {
                channel.basicNack(lastReceivedTag, true, true);
                lastReceivedTag = null;
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws JMSException {
        if (this.closed)
            return;
        
        try {
            //close all producers created by this session
            for (RMQMessageProducer producer : producers) {
                producer.internalClose();
            }
            producers.clear();

            //close all consumers created by this session
            for (RMQMessageConsumer consumer : consumers) {
                try {
                    consumer.internalClose();
                }catch (JMSException x) {
                    x.printStackTrace(); //TODO logging implementation

                }
            }
            consumers.clear();

            this.setMessageListener(null);

            if (this.getTransacted()) {
                this.rollback();
            }
            
            //close the channel itself
            try {
                this.channel.close();
            } catch (AlreadyClosedException x) {
                //nothing to do
            } catch (ShutdownSignalException x) {
                //nothing to do
            } catch (IOException x) {
                Util.util().handleException(x);
            } finally {
                //notify the connection that this session is closed
                //so that it can be removed from the list of sessions
                this.getConnection().sessionClose(this);
            }
        } finally {
            this.closed = true;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed");
        //each consumer contains a list of messages received and 
        //not acknowledged
        for (RMQMessageConsumer consumer : consumers) {
            try {
                consumer.recover();
            }catch (JMSException x) {
                x.printStackTrace(); //TODO logging implementation

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageListener getMessageListener() throws JMSException {
        return this.messageListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        this.messageListener = new MessageListenerWrapper(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        //this will not be implemented
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        RMQDestination dest = (RMQDestination) destination;
        if (!dest.isDeclared()) {
            if (dest.isQueue()) {
                declareQueue(dest,dest.isTemporary(),true);
            } else {
                declareTopic(dest);
            }
        }
        RMQMessageProducer producer = new RMQMessageProducer(this, (RMQDestination) destination);
        producers.add(producer);
        return producer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        return createConsumer(destination, true, null);
    }
    
    /**
     * Creates a consumer for a destination. If this is a topic, we can specify the autoDelete flag
     * @param destination
     * @param autoDelete true if the queue created should be autoDelete==true. This flag is ignored if the destination is a queue
     * @return {@link #createConsumer(Destination)}
     * @throws JMSException if destination is null or we fail to create the destination on the broker
     * @see {@link #createConsumer(Destination)}
     */
    public MessageConsumer createConsumer(Destination destination, boolean autoDelete, String uuidTag) throws JMSException {
        RMQDestination dest = (RMQDestination) destination;
        String consumerTag = uuidTag != null ? uuidTag : Util.util().generateUUIDTag();

        if (!dest.isDeclared()) {
            if (dest.isQueue()) {
                declareQueue(dest,dest.isTemporary(),true);
            } else {
                declareTopic(dest);
            }
        }

        if (!dest.isQueue()) {
            String queueName = consumerTag;
            // this is a topic, we need to define a queue, and bind to it
            // the queue name is a unique ID for each consumer 
            try {
                //we can set auto delete for a topic queue, since if the consumer disappears he is no longer 
                //participating in the topic.  
                this.channel.queueDeclare(queueName, true, dest.isTemporary(), autoDelete, new HashMap<String, Object>());
                //bind the queue to the exchange and routing key
                this.channel.queueBind(queueName, dest.getExchangeName(), dest.getRoutingKey());
            } catch (IOException x) {
                Util.util().handleException(x);
            }
        }
        RMQMessageConsumer consumer = new RMQMessageConsumer(this, (RMQDestination) destination, consumerTag);
        consumers.add(consumer);
        return consumer;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        // we are not implementing this method yet
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean NoLocal) throws JMSException {
        // we are not implementing this method yet
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue createQueue(String queueName) throws JMSException {
        RMQDestination dest = new RMQDestination(queueName, true, false);
        boolean durable = true;
        declareQueue(dest, dest.isTemporary(), durable);
        return dest;
    }

    /**
     * Invokes {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)} to define a queue on the RabbitMQ broker
     * this method invokes {@link RMQDestination#setDeclared(boolean)} with a true value
     * @param dest - the Queue object
     * @param temporary true if the queue is temporary
     * @param durable true if the queue should be durable
     * @throws JMSException if an IOException occurs in the {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)} call
     */
    protected void declareQueue(RMQDestination dest, boolean temporary, boolean durable) throws JMSException {
        try {

            this.channel.queueDeclare(dest.getQueueName(), // the name of the
                                                           // queue inside
                                                           // rabbit
                                      !temporary, // rabbit durable means
                                                  // survive server restart, all
                                                  // JMS destinations, except
                                                  // temp destination survive
                                                  // restarts
                                      temporary, // temporary destinations are
                                                 // rabbit exclusive
                                      !durable, // JMS durable means that we
                                                // don't want to auto delete the
                                                // destination
                                      new HashMap<String, Object>()); // rabbit
                                                                      // properties
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic createTopic(String topicName) throws JMSException {
        RMQDestination dest = new RMQDestination(topicName, false, false);
        declareTopic(dest);
        return dest;
    }

    /**
     * Invokes {@link Channel#exchangeDeclare(String, String, boolean, boolean, boolean, java.util.Map)} to define
     * the fanout exchange used to publish topic messages to
     * this method invokes {@link RMQDestination#setDeclared(boolean)} with a true value
     * @param dest the topic used to publish to
     * @throws JMSException
     */
    protected void declareTopic(RMQDestination dest) throws JMSException {
        try {
            this.channel.exchangeDeclare(dest.getExchangeName(), "fanout");
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        if (subscriptions.putIfAbsent(name, name)==null) {
            //this creates a durable subscription by setting autoDelete==false on the queue it binds 
            //to the fanout exchange
            RMQMessageConsumer result = (RMQMessageConsumer)createConsumer(topic, false, name);
            return result;
        } else {
            throw new JMSException("Subscription already exists["+name+"]");
        }
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException selectors not yet implemented
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        return new RMQDestination(Util.util().generateUUIDTag(), true, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        return new RMQDestination(Util.util().generateUUIDTag(), false, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(String name) throws JMSException {
        try {
            if (name!=null && name.equals(subscriptions.remove(name))) {
                //remove the queue from the fanout exchange
                this.channel.queueDelete(name);
            }
        }catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue) throws JMSException {
        return (QueueReceiver) this.createConsumer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException {
        return (QueueReceiver) this.createConsumer(queue, messageSelector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueSender createSender(Queue queue) throws JMSException {
        return (QueueSender) this.createProducer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {
        return (TopicSubscriber) this.createConsumer(topic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicPublisher createPublisher(Topic topic) throws JMSException {
        return (TopicPublisher) this.createProducer(topic);
    }

    /**
     * Returns the connection this session is associated with
     * @return
     */
    public RMQConnection getConnection() {
        return this.connection;
    }

    /**
     * Returns the {@link Channel} this session has created
     * @return
     */
    public Channel getChannel() {
        return this.channel;
    }

    /**
     * Invoked when the {@link RMQMessageConsumer} has received a message
     * so that we can track the last delivery tag
     * This is used to NACK messages when a transaction is rolled back
     * 
     * @param response
     * @see {@link Channel#basicNack(long, boolean, boolean)}
     * @see {@link Session#rollback()}
     */
    public void messageReceived(GetResponse response) {
        if (!transacted) return; //auto ack
        lastReceivedTag = response.getEnvelope().getDeliveryTag();
    }

    public void consumerClose(RMQMessageConsumer consumer) {
        this.consumers.remove(consumer);
    }

    public void producerClose(RMQMessageProducer producer) {
        this.producers.remove(producer);
    }

    public boolean isAutoAck() {
        return (getAcknowledgeModeNoException()==Session.AUTO_ACKNOWLEDGE) || (getAcknowledgeModeNoException()==Session.DUPS_OK_ACKNOWLEDGE);
    }

    /**
     * Stops all consumers from receiving messages. This is called by the
     * session indirectly after {@link javax.jms.Connection#stop()} has been
     * invoked. In this implementation, any async consumers will be cancelled,
     * only to be re-subscribed when
     * 
     * @throws {@link javax.jms.JMSException} if the thread is interrupted
     */
    public void pause() throws JMSException {
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.pause();
            } catch (IllegalStateException x) {
                Util.util().handleException(x);
            }
        }
    }

    /**
     * Resubscribes all async listeners and continues to receive messages
     * 
     * @see {@link javax.jms.Connection#stop()}
     * @throws {@link javax.jms.JMSException} if the thread is interrupted
     */
    public void resume() throws JMSException {
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.resume();
            } catch (IllegalStateException x) {
                Util.util().handleException(x);
            }
        }
    }
    
    private final class MessageListenerWrapper implements MessageListener {
        private final MessageListener listener;
        public MessageListenerWrapper(MessageListener listener) {
            this.listener = listener;
        }
        
        @Override
        public void onMessage(Message message) {
            runningListener.countUp();
            try {
                listener.onMessage(message);
            } finally {
                runningListener.countDown();
            }
            
        }
        
    }

}
