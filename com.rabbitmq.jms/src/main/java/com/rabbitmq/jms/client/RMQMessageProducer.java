package com.rabbitmq.jms.client;

import java.io.IOException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.RJMSLogger;
import com.rabbitmq.jms.util.RMQJMSException;

/**
 *
 */
public class RMQMessageProducer implements MessageProducer, QueueSender, TopicPublisher {

    private final RJMSLogger LOGGER = new RJMSLogger(new RJMSLogger.LogTemplate(){
        @Override
        public String template() {
            return "RMQMessageProducer("+RMQMessageProducer.this.destination+")";
        }
    });

    /**
     * The destination that we send our message to
     */
    private final RMQDestination destination;
    /**
     * The session this producer was created by
     */
    private final RMQSession session;
    /**
     * Delivery mode
     * @see javax.jms.DeliveryMode
     */
    private int deliveryMode;
    /**
     * Should we use message IDs or not.
     * In this implementation, this flag is ignored and we will
     * always use message IDs
     */
    private boolean disableMessageID = false;
    /**
     * Should we disable timestamps
     * In this implementation, this flag is ignored and we will
     * always use message timestamps
     */
    private boolean disableMessageTimestamp = false;
    /**
     * The default priority for a message
     */
    private int priority = Message.DEFAULT_PRIORITY;
    /**
     * RabbitMQ doesn't support TTL for individual messages but when it does
     * we can use this
     */
    private long ttl = Message.DEFAULT_TIME_TO_LIVE;

    /**
     * Create a producer of messages.
     * @param session which this producer uses
     * @param destination to which this producer sends messages.
     */
    public RMQMessageProducer(RMQSession session, RMQDestination destination) {
        this.session = session;
        this.destination = destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
        LOGGER.log("setDisableMessageID", value);
        this.disableMessageID = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDisableMessageID() throws JMSException {
        return this.disableMessageID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisableMessageTimestamp(boolean value) throws JMSException {
        LOGGER.log("setDisableMessageTimestamp", value);
        this.disableMessageTimestamp = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        return this.disableMessageTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        LOGGER.log("setDeliveryMode", deliveryMode);
        this.deliveryMode = deliveryMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDeliveryMode() throws JMSException {
        return this.deliveryMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPriority(int defaultPriority) throws JMSException {
        LOGGER.log("setPriority", defaultPriority);
        this.priority = defaultPriority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() throws JMSException {
        return this.priority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeToLive(long timeToLive) throws JMSException {
        LOGGER.log("setTimeToLive", timeToLive);
        this.ttl = timeToLive;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimeToLive() throws JMSException {
        return this.ttl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Destination getDestination() throws JMSException {
        return this.destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws JMSException {
        LOGGER.log("close");
        this.session.removeProducer(this);
    }

    /**
     * Method called internally or by the Session
     * when system is shutting down
     */
    protected void internalClose() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Message message) throws JMSException {
        LOGGER.log("send", message);
        this.internalSend(this.destination, message, this.getDeliveryMode(), this.getPriority(), this.getTimeToLive());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        LOGGER.log("send", message, deliveryMode, priority, timeToLive);
        this.internalSend(this.destination, message, deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Destination destination, Message message) throws JMSException {
        LOGGER.log("send", destination, message);
        this.internalSend(destination, message, this.getDeliveryMode(), this.getPriority(), this.getTimeToLive());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        LOGGER.log("send", destination, message, deliveryMode, priority, timeToLive);
        this.internalSend(destination, message, deliveryMode, priority, timeToLive);
    }

    private void internalSend(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        try {
            if (deliveryMode != javax.jms.DeliveryMode.PERSISTENT) {
                deliveryMode = javax.jms.DeliveryMode.NON_PERSISTENT;
            }
            /*
             * Set known JMS message properties that need to be set during this call
             */
            long currentTime = System.currentTimeMillis();
            long expiration = timeToLive == 0L ? 0L : currentTime + timeToLive;

            RMQMessage msg = (RMQMessage) message;
            msg.setJMSDeliveryMode(deliveryMode);
            msg.setJMSPriority(priority);
            msg.setJMSExpiration(expiration);
            msg.setJMSDestination(destination);
            msg.setJMSTimestamp(currentTime);
            msg.generateInternalID();

            RMQDestination dest = (RMQDestination) destination;

            /*
             * Configure the send settings
             */
            AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
            bob.contentType("application/octet-stream");
            bob.deliveryMode(rmqDeliveryMode(deliveryMode));
            bob.priority(priority);
            bob.expiration(rmqExpiration(timeToLive));
            bob.headers(RMQMessage.toHeaders(msg));

            byte[] data = RMQMessage.toMessage(msg);
            /*
             * Send the message
             */
            this.session.getChannel().basicPublish(dest.getExchangeInfo().name(), dest.getRoutingKey(), bob.build(), data);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /** This is dictated by `erlang:send_after' on which rabbitmq depends to implement TTL:
     * <br/><code>-define(MAX_EXPIRY_TIMER, 4294967295)</code>.
     */
    private final static long MAX_TTL = 4294967295L;

    /**
     * Convert long time-to-live to String time-to-live for amqp protocol.
     * Also constrain to limits: <code>0 <= ttl <= MAX_EXPIRY_TIMER</code>.
     * @param ttl JMS time-to-live long integer
     * @return RabbitMQ message expiration setting (null if expiration==0L)
     */
    private static final String rmqExpiration(long ttl) {
        if (ttl == 0L) return null;

        return String.valueOf( ttl < 0L      ? 0L
                             : ttl > MAX_TTL ? MAX_TTL
                             :                 ttl
                             );
    }

    /**
     * @param deliveryMode JMS delivery mode value
     * @return RabbitMQ delivery mode value
     */
    private static final int rmqDeliveryMode(int deliveryMode) {
        return (deliveryMode == javax.jms.DeliveryMode.PERSISTENT ? 2 : 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue getQueue() throws JMSException {
        return this.destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Queue queue, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        LOGGER.log("send", queue, message, deliveryMode, priority, timeToLive);
        this.internalSend((Destination) queue, message, deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Queue queue, Message message) throws JMSException {
        LOGGER.log("send", queue, message);
        this.internalSend((Destination) queue, message, this.getDeliveryMode(), this.getPriority(), this.getTimeToLive());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic getTopic() throws JMSException {
        return this.destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Message message) throws JMSException {
        LOGGER.log("publish", message);
        this.internalSend(this.getTopic(), message, this.getDeliveryMode(), this.getPriority(), this.getTimeToLive());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        LOGGER.log("publish", message, deliveryMode, priority, timeToLive);
        this.internalSend(this.getTopic(), message, deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Topic topic, Message message) throws JMSException {
        LOGGER.log("publish", topic, message);
        this.internalSend(topic, message, this.getDeliveryMode(), this.getPriority(), this.getTimeToLive());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        LOGGER.log("publish", topic, message, deliveryMode, priority, timeToLive);
        this.internalSend(topic, message, deliveryMode, priority, timeToLive);
    }

}
