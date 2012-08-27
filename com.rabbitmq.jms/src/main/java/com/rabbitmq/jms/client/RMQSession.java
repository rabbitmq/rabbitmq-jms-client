package com.rabbitmq.jms.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQMapMessage;
import com.rabbitmq.jms.client.message.RMQObjectMessage;
import com.rabbitmq.jms.client.message.RMQStreamMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
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
    private ArrayList<RMQMessageProducer> producers = new ArrayList<RMQMessageProducer>();
    private ArrayList<RMQMessageConsumer> consumers = new ArrayList<RMQMessageConsumer>();

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
        Util.util().checkClosed(this.closed, "Session has been closed");
        return new RMQBytesMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapMessage createMapMessage() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
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
        Util.util().checkClosed(this.closed, "Session has been closed");
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
        Util.util().checkClosed(this.closed, "Session has been closed");
        return new RMQStreamMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
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
     * but does not declare a JMSException in the throw claus
     * @return
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
        return this.acknowledgeMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        if (!this.transacted)
            return;
        try {
            this.channel.txCommit();
            lastReceivedTag = null;
           
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        if (!this.transacted)
            return;
        try {
            this.channel.txRollback();
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
    public void close() throws JMSException {
        if (this.closed)
            return;
        this.closed = true;

        for (RMQMessageProducer producer : producers) {
            producer.internalClose();
        }
        producers.clear();
        for (RMQMessageConsumer consumer : consumers) {
            try {
                consumer.internalClose();
            }catch (JMSException x) {
                x.printStackTrace(); //TODO logging implementation
                
            }
        }
        consumers.clear();
        try {
            this.channel.close();
        } catch (IOException x) {
            Util.util().handleException(x);
        } finally {
            this.getConnection().sessionClose(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
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
        this.messageListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        RMQDestination dest = (RMQDestination) destination;
        if (!dest.isDeclared()) {
            if (dest.isQueue()) {
                //TODO for a late declared queue, fix this
                declareQueue(dest,false,true);
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
        RMQDestination dest = (RMQDestination) destination;
        String consumerTag = Util.util().generateUUIDTag();

        if (!dest.isDeclared()) {
            if (dest.isQueue()) {
                //TODO for a late declared queue, fix this
                declareQueue(dest,false,true);
            } else {
                declareTopic(dest);
            }
        }
        
        if (!dest.isQueue()) {
            String queueName = consumerTag;
            // this is a topic, we need to define a queue, and bind to it
            try {
                this.channel.queueDeclare(queueName, true, false, false, new HashMap<String, Object>());
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
     * @throws UnsupportedOperationException - method not implemented
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        // we are not implementing this method yet
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented
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
        String name = queueName, exchangeName = "", routingKey = name;
        RMQDestination dest = new RMQDestination(name, exchangeName, routingKey, true, true);
        boolean temporary = false;
        boolean durable = true;
        declareQueue(dest, temporary, durable);
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
            this.channel.queueDeclare(dest.getQueueName(), durable, temporary, !durable, new HashMap<String, Object>());
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
        String name = topicName, exchangeName = "topic." + topicName, routingKey = name;
        RMQDestination dest = new RMQDestination(name, exchangeName, routingKey, false, true);
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(String name) throws JMSException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue) throws JMSException {
        assert queue instanceof RMQDestination;
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
    
    public boolean isAutoAck() throws JMSException {
        return (getAcknowledgeMode()==Session.AUTO_ACKNOWLEDGE) || (getAcknowledgeMode()==Session.DUPS_OK_ACKNOWLEDGE);
    }
    

}
