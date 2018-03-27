/* Copyright (c) 2013-2018 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.function.BiFunction;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import com.rabbitmq.jms.util.RMQJMSException;

import static com.rabbitmq.jms.client.RMQMessage.JMS_MESSAGE_DELIVERY_MODE;
import static com.rabbitmq.jms.client.RMQMessage.JMS_MESSAGE_EXPIRATION;
import static com.rabbitmq.jms.client.RMQMessage.JMS_MESSAGE_PRIORITY;

/**
 *
 */
public class RMQMessageProducer implements MessageProducer, QueueSender, TopicPublisher {

    private final Logger logger = LoggerFactory.getLogger(RMQMessageProducer.class);

    /**
     * The destination that we send our message to
     */
    private final RMQDestination destination;
    /**
     * The session this producer was created by
     */
    private final RMQSession session;
    /**
     * The default delivery mode used when a message is sent.
     * @see javax.jms.DeliveryMode
     */
    private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;
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
     * The default TTL value is 0 (zero), meaning indefinite (no time-out).
     */
    private long ttl = Message.DEFAULT_TIME_TO_LIVE;

    private final SendingStrategy sendingStrategy;

    private final BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser;

    public RMQMessageProducer(RMQSession session, RMQDestination destination, boolean preferProducerMessageProperty,
            BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser) {
        this.session = session;
        this.destination = destination;
        if (preferProducerMessageProperty) {
            sendingStrategy = new PreferMessageProducerPropertySendingStategy();
        } else {
            sendingStrategy = new PreferMessagePropertySendingStrategy();
        }
        this.amqpPropertiesCustomiser = amqpPropertiesCustomiser == null ? (builder, message) -> builder : amqpPropertiesCustomiser;
    }

    /**
     * Create a producer of messages.
     * @param session which this producer uses
     * @param destination to which this producer sends messages.
     * @param preferProducerMessageProperty properties take precedence over respective message properties
     */
    public RMQMessageProducer(RMQSession session, RMQDestination destination, boolean preferProducerMessageProperty) {
        this(session, destination, preferProducerMessageProperty, (builder, message) -> builder);
    }

    /**
     * Create a producer of messages.
     * @param session which this producer uses
     * @param destination to which this producer sends messages.
     */
    public RMQMessageProducer(RMQSession session, RMQDestination destination) {
        this(session, destination, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
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
        logger.trace("close producer for destination '{}'", this.destination);
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
        this.sendingStrategy.send(this.destination, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        this.sendingStrategy.send(this.destination, message, deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Destination destination, Message message) throws JMSException {
        this.checkUnidentifiedMessageProducer(destination);
        this.sendingStrategy.send(destination, message);
    }

    private void checkUnidentifiedMessageProducer(Destination destination) {
        if (destination != null && this.destination != null)
            throw new UnsupportedOperationException("Must not supply a destination unless MessageProducer is unidentified.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        this.checkUnidentifiedMessageProducer(destination);
        this.sendingStrategy.send(destination, message, deliveryMode, priority, timeToLive);
    }

    private void internalSend(RMQDestination destination, Message message, int deliveryMode, int priority, long timeToLiveOrExpiration, MessageExpirationType messageExpirationType) throws JMSException {
        logger.trace("send/publish message({}) to destination({}) with properties deliveryMode({}), priority({}), timeToLive({})", message, destination, deliveryMode, priority, timeToLiveOrExpiration);

        if (destination == null)
            destination = this.destination;
        if (destination == null)
            throw new InvalidDestinationException("No destination supplied, or implied.");
        if (deliveryMode != javax.jms.DeliveryMode.PERSISTENT)
            deliveryMode = javax.jms.DeliveryMode.NON_PERSISTENT;

        /* Normalise message to internal form */
        RMQMessage rmqMessage = RMQMessage.normalise(message);

        /* Set known JMS message properties that need to be set during this call */
        long currentTime = System.currentTimeMillis();
        long expiration;
        long ttl;
        if (messageExpirationType == MessageExpirationType.TTL) {
            expiration = timeToLiveOrExpiration == 0L ? 0L : currentTime + timeToLiveOrExpiration;
            ttl = timeToLiveOrExpiration;
        } else {
            expiration = timeToLiveOrExpiration;
            ttl = timeToLiveOrExpiration - currentTime;
        }

        rmqMessage.setJMSDeliveryMode(deliveryMode);
        rmqMessage.setJMSPriority(priority);
        rmqMessage.setJMSExpiration(expiration);
        rmqMessage.setJMSDestination(destination);
        rmqMessage.setJMSTimestamp(currentTime);
        rmqMessage.generateInternalID();

        /* Now send it */
        if (destination.isAmqp()) {
            sendAMQPMessage(destination, rmqMessage, deliveryMode, priority, ttl);
        } else {
            sendJMSMessage(destination, rmqMessage, deliveryMode, priority, ttl);
        }
    }

    private void sendAMQPMessage(RMQDestination destination, RMQMessage msg, int deliveryMode, int priority, long timeToLive) throws JMSException {
        if (!destination.amqpWritable()) {
            this.logger.error("Cannot write to AMQP destination {}", destination);
            throw new RMQJMSException("Cannot write to AMQP destination", new UnsupportedOperationException("MessageProducer.send to undefined AMQP resource"));
        }

        if (msg instanceof RMQBytesMessage || msg instanceof RMQTextMessage) {
            try {
                AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
                bob.contentType("application/octet-stream");
                bob.deliveryMode(RMQMessage.rmqDeliveryMode(deliveryMode));
                bob.priority(priority);
                bob.expiration(rmqExpiration(timeToLive));
                bob.headers(msg.toAmqpHeaders());

                bob = amqpPropertiesCustomiser.apply(bob, msg);

                byte[] data = msg.toAmqpByteArray();

                this.session.getChannel().basicPublish(destination.getAmqpExchangeName(), destination.getAmqpRoutingKey(), bob.build(), data);
            } catch (IOException x) {
                throw new RMQJMSException(x);
            }
        } else {
            this.logger.error("Unsupported message type {} for AMQP destination {}", msg.getClass().getName(), destination);
            throw new RMQJMSException("Unsupported message type for AMQP destination", new UnsupportedOperationException("MessageProducer.send to AMQP resource: Message not Text or Bytes"));
        }
    }

    // protected for testing
    protected void sendJMSMessage(RMQDestination destination, RMQMessage msg, int deliveryMode, int priority, long timeToLive) throws JMSException {
        this.session.declareDestinationIfNecessary(destination);
        try {
            AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
            bob.contentType("application/octet-stream");
            bob.deliveryMode(RMQMessage.rmqDeliveryMode(deliveryMode));
            bob.priority(priority);
            bob.expiration(rmqExpiration(timeToLive));
            bob.headers(msg.toHeaders());

            byte[] data = msg.toByteArray();

            this.session.getChannel().basicPublish(destination.getAmqpExchangeName(), destination.getAmqpRoutingKey(), bob.build(), data);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /** This is dictated by `erlang:send_after' on which rabbitmq depends to implement TTL:
     * <br/><code>-define(MAX_EXPIRY_TIMER, 4294967295)</code>.
     */
    private static final long MAX_TTL = 4294967295L;

    /**
     * Convert long time-to-live to String time-to-live for amqp protocol.
     * Constrain to limits: <code>0 &LT;= ttl &LT;= MAX_TTL</code>.
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
        this.sendingStrategy.send(queue, message, deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Queue queue, Message message) throws JMSException {
        this.sendingStrategy.send(queue, message);
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
        this.sendingStrategy.send(this.getTopic(), message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        this.sendingStrategy.send(this.getTopic(), message, deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Topic topic, Message message) throws JMSException {
        this.sendingStrategy.send(topic, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        this.sendingStrategy.send(topic, message, deliveryMode, priority, timeToLive);
    }

    /**
     * Strategy interface for sending messages.
     */
    private interface SendingStrategy {

        void send(Destination destination, Message message) throws JMSException;

        void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException;

    }

    /**
     * This implementation ignores message properties (delivery mode, priority, and expiration)
     * in favor of the message producer's properties.
     */
    private class PreferMessageProducerPropertySendingStategy implements SendingStrategy {

        @Override
        public void send(Destination destination, Message message) throws JMSException {
            internalSend((RMQDestination) destination, message, getDeliveryMode(), getPriority(), getTimeToLive(), MessageExpirationType.TTL);
        }

        @Override
        public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
            internalSend((RMQDestination) destination, message, deliveryMode, priority, timeToLive, MessageExpirationType.TTL);
        }

    }

    /**
     * This implementation uses message properties (delivery mode, priority, and expiration)
     * if they've been set up. It falls back to the message producer's properties.
     */
    private class PreferMessagePropertySendingStrategy implements SendingStrategy {

        @Override
        public void send(Destination destination, Message message) throws JMSException {
            internalSend((RMQDestination) destination, message,
                message.propertyExists(JMS_MESSAGE_DELIVERY_MODE) ? message.getJMSDeliveryMode() : getDeliveryMode(),
                message.propertyExists(JMS_MESSAGE_PRIORITY) ? message.getJMSPriority() : getPriority(),
                message.propertyExists(JMS_MESSAGE_EXPIRATION) ? message.getJMSExpiration() : getTimeToLive(),
                message.propertyExists(JMS_MESSAGE_EXPIRATION) ? MessageExpirationType.EXPIRATION : MessageExpirationType.TTL);
        }

        @Override
        public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
            internalSend((RMQDestination) destination, message, deliveryMode, priority, timeToLive, MessageExpirationType.TTL);
        }

    }

    private enum MessageExpirationType {
        TTL, EXPIRATION
    }

}
