package com.rabbitmq.jms.client;

import java.io.IOException;
import java.io.Serializable;

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
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ implementation of JMS {@link Session}
 */
public class RMQSession implements Session, QueueSession, TopicSession {

    private final RMQConnection connection;
    private final boolean transacted;
    private final int acknowledgeMode;
    private volatile Channel channel;

    public RMQSession(RMQConnection connection, boolean transacted, int mode) throws JMSException {
        assert (mode >= 0 && mode <= 3);
        this.connection = connection;
        this.transacted = transacted;
        this.acknowledgeMode = mode;
        try {
            this.channel = connection.getRabbitConnection().createChannel();
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        assert channel != null;
    }

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message createMessage() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        RMQMessage msg = new RMQMessage();
        msg.setText(text);
        return msg;
    }

    @Override
    public boolean getTransacted() throws JMSException {
        return transacted;
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return acknowledgeMode;
    }

    @Override
    public void commit() throws JMSException {
        assert transacted;

    }

    @Override
    public void rollback() throws JMSException {
        assert transacted;

    }

    @Override
    public void close() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void recover() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean NoLocal) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue createQueue(String queueName) throws JMSException {
        return new RMQDestination(this, queueName, true, true, false);
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unsubscribe(String name) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public QueueReceiver createReceiver(Queue queue) throws JMSException {
        assert queue instanceof RMQDestination;
        return new RMQMessageConsumer(this, (RMQDestination) queue);
    }

    @Override
    public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueSender createSender(Queue queue) throws JMSException {
        assert queue instanceof RMQDestination;
        return new RMQMessageProducer(this, (RMQDestination) queue);
    }

    @Override
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopicPublisher createPublisher(Topic topic) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    public RMQConnection getConnection() {
        return connection;
    }

    public Channel getChannel() {
        return channel;
    }

}
