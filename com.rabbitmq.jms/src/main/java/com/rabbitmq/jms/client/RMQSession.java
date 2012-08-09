package com.rabbitmq.jms.client;

import java.io.IOException;
import java.io.Serializable;
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

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        return new RMQBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        return new RMQMapMessage();
    }

    @Override
    public Message createMessage() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        return new RMQObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        return new RMQStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");
        RMQTextMessage msg = new RMQTextMessage();
        msg.setText(text);
        return msg;
    }

    @Override
    public boolean getTransacted() throws JMSException {
        return this.transacted;
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return this.acknowledgeMode;
    }

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

    @Override
    public void close() throws JMSException {
        if (this.closed)
            return;
        this.closed = true;
        try {
            this.channel.close();
        } catch (IOException x) {
            Util.util().handleException(x);
        } finally {
            this.getConnection().sessionClose(this);
        }
    }

    @Override
    public void recover() throws JMSException {
        Util.util().checkClosed(this.closed, "Session has been closed");

        // TODO Auto-generated method stub

    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        // we are not implementing this optional method
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        // we are not implementing this optional method
        throw new UnsupportedOperationException();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        return new RMQMessageProducer(this, (RMQDestination) destination);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        RMQDestination dest = (RMQDestination) destination;
        String consumerTag = Util.util().generateUUIDTag();

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
        return consumer;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        // we are not implementing this method yet
        throw new UnsupportedOperationException();
    }

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
        RMQDestination dest = new RMQDestination(name, exchangeName, routingKey, true);
        boolean temporary = false;
        boolean durable = true;
        try {
            this.channel.queueDeclare(dest.getQueueName(), durable, temporary, !durable, new HashMap<String, Object>());
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        return dest;
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        String name = topicName, exchangeName = "topic." + topicName, routingKey = name;
        RMQDestination dest = new RMQDestination(name, exchangeName, routingKey, false);
        try {
            this.channel.exchangeDeclare(exchangeName, "fanout");
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        return dest;
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
        return (QueueReceiver) this.createConsumer(queue);
    }

    @Override
    public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException {
        return (QueueReceiver) this.createConsumer(queue, messageSelector);
    }

    @Override
    public QueueSender createSender(Queue queue) throws JMSException {
        return (QueueSender) this.createProducer(queue);
    }

    @Override
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {
        return (TopicSubscriber) this.createConsumer(topic);
    }

    @Override
    public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TopicPublisher createPublisher(Topic topic) throws JMSException {
        return (TopicPublisher) this.createProducer(topic);
    }

    public RMQConnection getConnection() {
        return this.connection;
    }

    public Channel getChannel() {
        return this.channel;
    }
    
    public void messageReceived(GetResponse response) {
        if (!transacted) return; //auto ack
        lastReceivedTag = response.getEnvelope().getDeliveryTag();
    }
    
    

}
