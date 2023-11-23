// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import com.rabbitmq.jms.util.RMQJMSException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jms.CompletionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import java.io.IOException;
import java.util.function.BiFunction;

import static com.rabbitmq.jms.client.RMQMessage.*;

/**
 *
 */
public class RMQMessageProducer implements MessageProducer, QueueSender, TopicPublisher {

    private final Logger logger = LoggerFactory.getLogger(RMQMessageProducer.class);

    private static final String DIRECT_REPLY_TO = "amq.rabbitmq.reply-to";

    static final CompletionListener NO_OP_COMPLETION_LISTENER = new CompletionListener() {
        @Override
        public void onCompletion(Message message) {

        }

        @Override
        public void onException(Message message, Exception exception) {

        }
    };

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

    private long deliveryDelay = Message.DEFAULT_DELIVERY_DELAY;

    private final SendingStrategy sendingStrategy;

    private final BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser;

    private final SendingContextConsumer sendingContextConsumer;

    private final BeforePublishingCallback beforePublishingCallback;

    private final boolean keepTextMessageType;

    private final AtomicBoolean publishConfirmedEnabled = new AtomicBoolean(false);

    RMQMessageProducer(RMQSession session, RMQDestination destination, boolean preferProducerMessageProperty,
                              BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser,
                              SendingContextConsumer sendingContextConsumer,
                              PublishingListener publishingListener,
                              boolean keepTextMessageType) {
        this.session = session;
        this.destination = destination;
        if (preferProducerMessageProperty) {
            sendingStrategy = new PreferMessageProducerPropertySendingStategy();
        } else {
            sendingStrategy = new PreferMessagePropertySendingStrategy();
        }
        this.amqpPropertiesCustomiser = amqpPropertiesCustomiser == null ? (builder, message) -> builder : amqpPropertiesCustomiser;
        this.sendingContextConsumer = sendingContextConsumer == null ? ctx -> {} : sendingContextConsumer;
        if (publishingListener == null) {
            this.beforePublishingCallback = (message, completionListener, channel) -> {};
        } else {
            this.beforePublishingCallback = (message, completionListener, channel) ->
                publishingListener.publish(message, completionListener, channel.getNextPublishSeqNo());
        }
        this.keepTextMessageType = keepTextMessageType;
    }

    public RMQMessageProducer(RMQSession session, RMQDestination destination, boolean preferProducerMessageProperty,
            BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser,
            SendingContextConsumer sendingContextConsumer) {
        this(session, destination, preferProducerMessageProperty, amqpPropertiesCustomiser, sendingContextConsumer, null,
            false);
    }

    public RMQMessageProducer(RMQSession session, RMQDestination destination, boolean preferProducerMessageProperty,
        BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser) {
        this(session, destination, preferProducerMessageProperty, amqpPropertiesCustomiser, ctx -> {});
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
        this.sendingStrategy.send(this.destination, message, NO_OP_COMPLETION_LISTENER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        this.sendingStrategy.send(this.destination, message, NO_OP_COMPLETION_LISTENER,
            deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Destination destination, Message message) throws JMSException {
        this.checkUnidentifiedMessageProducer(destination);
        this.sendingStrategy.send(destination, message, NO_OP_COMPLETION_LISTENER);
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
        this.sendingStrategy.send(destination, message, NO_OP_COMPLETION_LISTENER,
            deliveryMode, priority, timeToLive);
    }

    private void internalSend(RMQDestination destination, Message message, CompletionListener completionListener,
                              int deliveryMode, int priority, long timeToLiveOrExpiration,
                              MessageExpirationType messageExpirationType,
                              DeliveryTimeSource deliveryTimeSource) throws JMSException {
        logger.trace("send/publish message({}) to destination({}) with properties deliveryMode({}), priority({}), timeToLive({}), deliveryTimeSource({})",
                message, destination, deliveryMode, priority, timeToLiveOrExpiration, deliveryTimeSource);

        this.sendingContextConsumer.accept(new SendingContext(destination, message));

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
        long deliveryDelay = getDeliveryDelayAndSetJMSDeliveryTimeIfNeeded(rmqMessage, deliveryTimeSource);

        /* Now send it */
        if (destination.isAmqp()) {
            sendAMQPMessage(destination, rmqMessage, message, completionListener,
                deliveryMode, priority, ttl, deliveryDelay);
        } else {
            sendJMSMessage(destination, rmqMessage, message, completionListener,
                deliveryMode, priority, ttl, deliveryDelay);
        }
    }
    private long getDeliveryDelayAndSetJMSDeliveryTimeIfNeeded(RMQMessage rmqMessage, DeliveryTimeSource deliveryTimeSource) throws JMSException {
        long deliveryDelay = 0L;
        long currentTime = System.currentTimeMillis();
        if (DeliveryTimeSource.MESSAGE.equals(deliveryTimeSource) || getDeliveryDelay() <= 0L) {
            deliveryDelay = rmqMessage.getJMSDeliveryTime() - currentTime;
        }else if (getDeliveryDelay() > 0L) {
            deliveryDelay = getDeliveryDelay();
            rmqMessage.setJMSDeliveryTime(currentTime + getDeliveryDelay());
        }
        return deliveryDelay;
    }

    private void sendAMQPMessage(RMQDestination destination, RMQMessage msg, Message originalMessage,
                                 CompletionListener completionListener, int deliveryMode,
                                 int priority, long timeToLive, long deliveryDelay) throws JMSException {
        if (!destination.isAmqpWritable()) {
            this.logger.error("Cannot write to AMQP destination {}", destination);
            throw new RMQJMSException("Cannot write to AMQP destination", new UnsupportedOperationException("MessageProducer.send to undefined AMQP resource"));
        }

        if (msg.isAmqpWritable()) {
            try {
                AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
                bob.contentType("application/octet-stream");
                bob.deliveryMode(RMQMessage.rmqDeliveryMode(deliveryMode));
                bob.priority(priority);
                bob.correlationId(msg.getJMSCorrelationID());
                bob.expiration(rmqExpiration(timeToLive));
                Map<String, Object> messageHeaders = msg.toAmqpHeaders();
                if (this.keepTextMessageType && msg instanceof RMQTextMessage) {
                    messageHeaders.put(RMQMessage.JMS_TYPE_HEADER,
                                       RMQMessage.TEXT_MESSAGE_HEADER_VALUE);
                }
                String targetAmqpExchangeName = session.delayMessage(destination, messageHeaders, deliveryDelay);
                bob.headers(messageHeaders);

                setReplyToProperty(bob, msg);

                bob = amqpPropertiesCustomiser.apply(bob, msg);

                byte[] data = msg.toAmqpByteArray();

                this.beforePublishingCallback.beforePublishing(originalMessage, completionListener, this.session.getChannel());
                this.session.getChannel().basicPublish(targetAmqpExchangeName, destination.getAmqpRoutingKey(), bob.build(), data);
            } catch (IOException x) {
                throw new RMQJMSException(x);
            }
        } else {
            this.logger.error("Unsupported message type {} for AMQP destination {}", msg.getClass().getName(), destination);
            throw new RMQJMSException("Unsupported message type for AMQP destination", new UnsupportedOperationException("MessageProducer.send to AMQP resource: Message not Text or Bytes"));
        }
    }

    // protected for testing
    protected void sendJMSMessage(RMQDestination destination, RMQMessage msg, Message originalMessage,
                                  CompletionListener completionListener,
                                  int deliveryMode, int priority, long timeToLive, long deliveryDelay) throws JMSException {
        this.session.declareDestinationIfNecessary(destination);
        try {
            AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
            bob.contentType("application/octet-stream");
            bob.deliveryMode(RMQMessage.rmqDeliveryMode(deliveryMode));
            bob.priority(priority);
            bob.correlationId(msg.getJMSCorrelationID());
            bob.expiration(rmqExpiration(timeToLive));
            Map<String, Object> headers = msg.toHeaders();
            String targetAmqpExchangeName = session.delayMessage(destination, headers, deliveryDelay);
            bob.headers(headers);

            setReplyToProperty(bob, msg);

            byte[] data = msg.toByteArray();

            this.beforePublishingCallback.beforePublishing(originalMessage, completionListener,
                this.session.getChannel());
            this.session.getChannel().basicPublish(targetAmqpExchangeName, destination.getAmqpRoutingKey(), bob.build(), data);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * Set AMQP reply-to property to reply-to if necessary.
     * <p>
     * <p>
     * Set the <code>reply-to</code> property to <code>amq.rabbitmq.reply-to</code>
     * if the <code>JMSReplyTo</code> header is set to a destination with that
     * name and does not have a routing key (which indicates this this is a forwarded
     * reply to destination).
     * <p>
     * Set the <code>reply-to</code> property to the amq routing key, if the routing key
     * if not null.
     * <p>
     * For outbound RPC request.
     *
     * @param builder
     * @param msg
     * @throws JMSException
     * @since 1.11.0
     */
    private static void setReplyToProperty(AMQP.BasicProperties.Builder builder, RMQMessage msg) throws JMSException {
        if (msg.getJMSReplyTo() != null && msg.getJMSReplyTo() instanceof RMQDestination) {
            RMQDestination replyTo = (RMQDestination) msg.getJMSReplyTo();
            if (DIRECT_REPLY_TO.equals(replyTo.getDestinationName()) && replyTo.getAmqpRoutingKey() == null) {
                builder.replyTo(DIRECT_REPLY_TO);
            } else {
                builder.replyTo(replyTo.getAmqpRoutingKey());
            }
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
        this.sendingStrategy.send(queue, message, NO_OP_COMPLETION_LISTENER,
            deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Queue queue, Message message) throws JMSException {
        this.sendingStrategy.send(queue, message, NO_OP_COMPLETION_LISTENER);
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
        this.sendingStrategy.send(this.getTopic(), message, NO_OP_COMPLETION_LISTENER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        this.sendingStrategy.send(this.getTopic(), message, NO_OP_COMPLETION_LISTENER,
            deliveryMode, priority, timeToLive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Topic topic, Message message) throws JMSException {
        this.sendingStrategy.send(topic, message, NO_OP_COMPLETION_LISTENER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        this.sendingStrategy.send(topic, message, NO_OP_COMPLETION_LISTENER,
            deliveryMode, priority, timeToLive);
    }

    /**
     * Strategy interface for sending messages.
     */
    private interface SendingStrategy {

        void send(Destination destination, Message message, CompletionListener completionListener)
            throws JMSException;

        void send(Destination destination, Message message, CompletionListener completionListener,
            int deliveryMode, int priority, long timeToLive) throws JMSException;

    }

    private enum DeliveryTimeSource {
        MESSAGE, /** the message carries the delivery time */
        PRODUCER /** the producer is configured with a delivery delay */
    }

    /**
     * This implementation ignores message properties (delivery mode, priority, and expiration)
     * in favor of the message producer's properties.
     */
    private class PreferMessageProducerPropertySendingStategy implements SendingStrategy {

        @Override
        public void send(Destination destination, Message message, CompletionListener completionListener)
            throws JMSException {
            internalSend((RMQDestination) destination, message, completionListener,
                getDeliveryMode(), getPriority(), getTimeToLive(), MessageExpirationType.TTL,
                    DeliveryTimeSource.PRODUCER);
        }

        @Override
        public void send(Destination destination, Message message, CompletionListener completionListener,
            int deliveryMode, int priority, long timeToLive) throws JMSException {
            internalSend((RMQDestination) destination, message, completionListener, deliveryMode, priority,
                    timeToLive, MessageExpirationType.TTL, DeliveryTimeSource.PRODUCER);
        }

    }

    /**
     * This implementation uses message properties (delivery mode, priority, and expiration)
     * if they've been set up. It falls back to the message producer's properties.
     */
    private class PreferMessagePropertySendingStrategy implements SendingStrategy {

        @Override
        public void send(Destination destination, Message message, CompletionListener completionListener)
            throws JMSException {
            internalSend((RMQDestination) destination, message, completionListener,
                message.propertyExists(JMS_MESSAGE_DELIVERY_MODE) ? message.getJMSDeliveryMode() : getDeliveryMode(),
                message.propertyExists(JMS_MESSAGE_PRIORITY) ? message.getJMSPriority() : getPriority(),
                message.propertyExists(JMS_MESSAGE_EXPIRATION) ? message.getJMSExpiration() : getTimeToLive(),
                message.propertyExists(JMS_MESSAGE_EXPIRATION) ? MessageExpirationType.EXPIRATION : MessageExpirationType.TTL,
                message.propertyExists(JMS_MESSAGE_DELIVERY_TIME) ? DeliveryTimeSource.MESSAGE : DeliveryTimeSource.PRODUCER);
        }

        @Override
        public void send(Destination destination, Message message, CompletionListener completionListener,
            int deliveryMode, int priority, long timeToLive) throws JMSException {
            internalSend((RMQDestination) destination, message, completionListener,
                deliveryMode, priority, timeToLive, MessageExpirationType.TTL, DeliveryTimeSource.MESSAGE);
        }

    }

    private enum MessageExpirationType {
        TTL, EXPIRATION
    }

    interface BeforePublishingCallback {

        void beforePublishing(Message message, CompletionListener completionListener, Channel channel);

    }

    @Override
    public void setDeliveryDelay(long deliveryDelay) {
        this.deliveryDelay = deliveryDelay;
    }

    @Override
    public long getDeliveryDelay() {
        return deliveryDelay;
    }

    @Override
    public void send(Message message, CompletionListener completionListener) throws JMSException {
        checkCompletionListenerNotNull(completionListener);
        enablePublishConfirm();
        this.sendingStrategy.send(this.destination, message, completionListener);
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive,
        CompletionListener completionListener) throws JMSException {
        checkCompletionListenerNotNull(completionListener);
        enablePublishConfirm();
        this.sendingStrategy.send(this.destination, message, completionListener,
            deliveryMode, priority, timeToLive);
    }

    @Override
    public void send(Destination destination, Message message,
        CompletionListener completionListener)
        throws JMSException {
        this.checkUnidentifiedMessageProducer(destination);
        checkCompletionListenerNotNull(completionListener);
        enablePublishConfirm();
        this.sendingStrategy.send(destination, message, completionListener);
    }

    @Override
    public void send(Destination destination, Message message, int deliveryMode, int priority,
        long timeToLive, CompletionListener completionListener) throws JMSException {
        this.checkUnidentifiedMessageProducer(destination);
        checkCompletionListenerNotNull(completionListener);
        enablePublishConfirm();
        this.sendingStrategy.send(destination, message, completionListener,
            deliveryMode, priority, timeToLive);
    }

    private static void checkCompletionListenerNotNull(CompletionListener completionListener) {
        if (completionListener == null) {
            throw new IllegalArgumentException("The completion listener cannot be null");
        }
    }

    private void enablePublishConfirm() throws JMSException {
        if (this.publishConfirmedEnabled.compareAndSet(false, true)) {
            try {
                this.session.enablePublishConfirmOnChannel();
            } catch (IOException e) {
                throw new RMQJMSException(e);
            }
        }
    }
}
