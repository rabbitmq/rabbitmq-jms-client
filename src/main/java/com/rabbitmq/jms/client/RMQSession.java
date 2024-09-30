// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import com.rabbitmq.jms.admin.RMQDefaultDestinations;
import com.rabbitmq.jms.client.Subscription.Context;
import com.rabbitmq.jms.client.Subscription.PostAction;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.jms.util.WhiteListObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQMapMessage;
import com.rabbitmq.jms.client.message.RMQObjectMessage;
import com.rabbitmq.jms.client.message.RMQStreamMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ implementation of JMS {@link Session}
 */
public class RMQSession implements Session, QueueSession, TopicSession {

    private final Logger logger = LoggerFactory.getLogger(RMQSession.class);

    /** The connection that created this session */
    private final RMQConnection connection;
    /** Set to true if this session is transacted. */
    private final boolean transacted;

    /** This value must be the maximum allowed, and contiguous with valid values for {@link #acknowledgeMode}. */
    public static final int CLIENT_INDIVIDUAL_ACKNOWLEDGE = 4; // mode in {0, 1, 2, 3, 4} is valid
    /** The ack mode used when receiving message */
    private final int acknowledgeMode;
    /** Flag to remember if session is {@link #CLIENT_INDIVIDUAL_ACKNOWLEDGE} */
    private final boolean isIndividualAck;

    /**
     * Whether {@link MessageProducer} properties (delivery mode,
     * priority, TTL) take precedence over respective {@link Message}
     * properties or not.
     * Default is true.
     */
    private final boolean preferProducerMessageProperty;

    /**
     * Whether requeue message on {@link RuntimeException} in the
     * {@link javax.jms.MessageListener} or not.
     * Default is false.
     */
    private final boolean requeueOnMessageListenerException;

    private final boolean requeueOnTimeout;

    /**
     * Whether to commit nack on rollback or not.
     * Default is false.
     *
     * @since 1.14.0
     */
    private final boolean nackOnRollback;

    /**
     * Whether using auto-delete for server-named queues for non-durable topics.
     * If set to true, those queues will be deleted when the session is closed.
     * If set to false, queues will be deleted when the owning connection is closed.
     * Default is false.
     * @since 1.8.0
     */
    private final boolean cleanUpServerNamedQueuesForNonDurableTopics;

    /**
     * Callback to customise properties of outbound AMQP messages.
     *
     * @since 1.9.0
     */
    private final BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser;

    /**
     * Callback before sending a message.
     *
     * @since 1.11.0
     */
    private final SendingContextConsumer sendingContextConsumer;

    private final ReceivingContextConsumer receivingContextConsumer;

    private final PublishingListener publishingListener;

    /** The main RabbitMQ channel we use under the hood */
    private final Channel channel;
    /** Set to true if close() has been called and completed */
    private volatile boolean closed = false;
    /** The message listener for this session. */
    private volatile MessageListener messageListener;
    /** A list of all the producers created by this session.
     * When a producer is closed, it will be removed from this list */
    private final ArrayList<RMQMessageProducer> producers = new ArrayList<>();
    /** A list of all the consumers created by this session.
     * When a consumer is closed, it will be removed from this list */
    private final ArrayList<RMQMessageConsumer> consumers = new ArrayList<>();
    /** We keep an ordered set of the message tags (acknowledgement tags) for all messages received and unacknowledged.
     * Each message acknowledgement must ACK all (unacknowledged) messages received up to this point, and
     * we must never acknowledge a message more than once (nor acknowledge a message that doesn't exist). */
    private final SortedSet<Long> unackedMessageTags = Collections.synchronizedSortedSet(
        new TreeSet<>());

    /* Holds the uncommited tags to commit a nack on rollback */
    private final List<Long> uncommittedMessageTags = new ArrayList<>(); // GuardedBy("commitLock");

    /** List of all our topic subscriptions so we can track them */
    private final Subscriptions subscriptions;

    /** Lock for waiting for close */
    private final Object closeLock = new Object();

    /** Lock and parms for commit and rollback blocking of other commands */
    private final Object commitLock = new Object();
    private static final long COMMIT_WAIT_MAX = 2000L; // 2 seconds
    private boolean committing = false; // GuardedBy("commitLock");

    /** Client version obtained from compiled class. */
    private static final GenericVersion CLIENT_VERSION = new GenericVersion(RMQSession.class.getPackage().getImplementationVersion());

    static final String RJMS_CLIENT_VERSION = CLIENT_VERSION.toString();
    static {
        if (RJMS_CLIENT_VERSION.equals("0.0.0"))
            System.out.println("WARNING: Running test version of RJMS Client with no version information.");
    }

    /** Selector exchange for topic selection */
    private volatile String durableTopicSelectorExchange;
    /** Selector exchange for topic selection */
    private volatile String nonDurableTopicSelectorExchange;
    /** Selector exchange arg key for client version */
    private static final String RJMS_VERSION_ARG = "rjms_version";
    /** Selector exchange arguments */
    private static final Map<String, Object> RJMS_SELECTOR_EXCHANGE_ARGS
        = Collections.singletonMap(RJMS_VERSION_ARG, RJMS_CLIENT_VERSION);


    private static final String JMS_TOPIC_SELECTOR_EXCHANGE_TYPE = "x-jms-topic";

    private final DeliveryExecutor deliveryExecutor;

    /** The channels we use for browsing queues (there may be more than one in operation at a time) */
    private Set<Channel> browsingChannels = new HashSet<>(); // @GuardedBy(bcLock)
    private final Object bcLock = new Object();

    /**
     * Classes in these packages can be transferred via ObjectMessage.
     *
     * @see WhiteListObjectInputStream
     */
    private final List<String> trustedPackages;

    /**
     * Arguments to be used when declaring a queue while creating a producer
     *
     * @since 1.14.0
     */
    private Map<String, Object> queueDeclareArguments = null;

    private final boolean keepTextMessageType;

    private final SubscriptionNameValidator subscriptionNameValidator;

    private final AtomicBoolean confirmSelectCalledOnChannel = new AtomicBoolean(false);

    private final DelayedMessageService delayedMessageService;

    /**
     * The reply to strategy to use when dealing with received messages
     * with a reply to specified.
     *
     * @since 2.9.0
     */
    private final ReplyToStrategy replyToStrategy;


    static boolean validateSessionMode(int sessionMode) {
       return sessionMode >= 0 && sessionMode <= CLIENT_INDIVIDUAL_ACKNOWLEDGE;
    }

    /**
     * Creates a session object associated with a connection
     * @param sessionParams parameters for this session
     * @throws JMSException if we fail to create a {@link Channel} object on the connection, or if the acknowledgement mode is incorrect
     */
    public RMQSession(SessionParams sessionParams) throws JMSException {
        if (!validateSessionMode(sessionParams.getMode())) {
            throw new JMSException(String.format("cannot create session with acknowledgement mode = %d.", sessionParams.getMode()));
        }
        if (sessionParams.willRequeueOnTimeout() && !sessionParams.willRequeueOnMessageListenerException()) {
            throw new IllegalArgumentException("requeueOnTimeout can be true only if requeueOnMessageListenerException is true as well");
        }
        this.connection = sessionParams.getConnection();
        this.transacted = sessionParams.isTransacted();
        this.subscriptions = sessionParams.getSubscriptions();
        boolean deliveryExecutorCloseOnTimeout = !sessionParams.willRequeueOnTimeout();
        this.deliveryExecutor = new DeliveryExecutor(sessionParams.getOnMessageTimeoutMs(), deliveryExecutorCloseOnTimeout);
        this.preferProducerMessageProperty = sessionParams.willPreferProducerMessageProperty();
        this.requeueOnMessageListenerException = sessionParams.willRequeueOnMessageListenerException();
        this.nackOnRollback = sessionParams.willNackOnRollback();
        this.cleanUpServerNamedQueuesForNonDurableTopics = sessionParams.isCleanUpServerNamedQueuesForNonDurableTopics();
        this.amqpPropertiesCustomiser = sessionParams.getAmqpPropertiesCustomiser();
        this.sendingContextConsumer = sessionParams.getSendingContextConsumer();
        this.receivingContextConsumer = sessionParams.getReceivingContextConsumer() == null ?
            ReceivingContextConsumer.NO_OP : sessionParams.getReceivingContextConsumer();
        this.trustedPackages = sessionParams.getTrustedPackages();
        this.requeueOnTimeout = sessionParams.willRequeueOnTimeout();
        this.keepTextMessageType = sessionParams.isKeepTextMessageType();
        if (sessionParams.isValidateSubscriptionNames()) {
            this.subscriptionNameValidator = name -> {
                boolean subscriptionIsValid = Utils.SUBSCRIPTION_NAME_PREDICATE.test(name);
                if (!subscriptionIsValid) {
                    // the specification is not clear on which exception to throw when the
                    // subscription name is not valid, so throwing a JMSException.
                    throw new JMSException("This subscription name is not valid: " + name + ". "
                        + "It must not be more than 128 characters and should contain only "
                        + "Java letters, digits, '_', '.', and '-'.");
                }
            };
        } else {
            this.subscriptionNameValidator = name -> { };
        }
        this.delayedMessageService = sessionParams.getDelayedMessageService();

        this.replyToStrategy = sessionParams.getReplyToStrategy() == null ?
            DefaultReplyToStrategy.INSTANCE : sessionParams.getReplyToStrategy();

        if (transacted) {
            this.acknowledgeMode = Session.SESSION_TRANSACTED;
            this.isIndividualAck = false;
        } else if (sessionParams.getMode() == CLIENT_INDIVIDUAL_ACKNOWLEDGE) {
            this.acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
            this.isIndividualAck = true;
        } else {
            this.acknowledgeMode = sessionParams.getMode();
            this.isIndividualAck = false;
        }
        try {
            this.channel = connection.createRabbitChannel(transacted);
            if (sessionParams.getConfirmListener() != null) {
                enablePublishConfirmOnChannel();
                this.publishingListener = PublisherConfirmsUtils.configurePublisherConfirmsSupport(
                        this.channel, sessionParams.getConfirmListener()
                );
            } else {
                this.publishingListener = PublisherConfirmsUtils.configurePublisherConfirmsSupport(
                    this.channel, context -> { }
                );
            }
        } catch (Exception x) { // includes unchecked exceptions, e.g. ShutdownSignalException
            throw new RMQJMSException(x);
        }
    }

    /**
     * Creates a session object associated with a connection
     * @param connection the connection that we will send data on
     * @param transacted whether this session is transacted or not
     * @param onMessageTimeoutMs how long to wait for onMessage to return, in milliseconds
     * @param mode the (fixed) acknowledgement mode for this session
     * @param subscriptions the connection's subscriptions, shared with all sessions
     * @throws JMSException if we fail to create a {@link Channel} object on the connection, or if the acknowledgement mode is incorrect
     */
    public RMQSession(RMQConnection connection, boolean transacted, int onMessageTimeoutMs, int mode, Subscriptions subscriptions, DelayedMessageService delayedMessageService) throws JMSException {
        this(new SessionParams()
            .setConnection(connection)
            .setTransacted(transacted)
            .setOnMessageTimeoutMs(onMessageTimeoutMs)
            .setMode(mode)
            .setSubscriptions(subscriptions)
            .setDelayedMessageService(delayedMessageService)
            .setReplyToStrategy(connection.getReplyToStrategy())
        );
    }

    void enablePublishConfirmOnChannel() throws IOException {
        if (this.confirmSelectCalledOnChannel.compareAndSet(false, true)) {
            this.channel.confirmSelect();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        illegalStateExceptionIfClosed();
        return new RMQBytesMessage();
    }

    private void illegalStateExceptionIfClosed() throws IllegalStateException {
        if (this.closed) throw new IllegalStateException("Session is closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapMessage createMapMessage() throws JMSException {
        illegalStateExceptionIfClosed();
        return new RMQMapMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message createMessage() throws JMSException {
        illegalStateExceptionIfClosed();
        return createTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        illegalStateExceptionIfClosed();
        return new RMQObjectMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        illegalStateExceptionIfClosed();
        ObjectMessage message = createObjectMessage();
        message.setObject(object);
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        illegalStateExceptionIfClosed();
        return new RMQStreamMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage() throws JMSException {
        illegalStateExceptionIfClosed();
        return new RMQTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        illegalStateExceptionIfClosed();
        TextMessage msg = createTextMessage();
        msg.setText(text);
        return msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getTransacted() throws JMSException {
        illegalStateExceptionIfClosed();
        return getTransactedNoException();
    }

    /**
     * Same as {@link #getTransacted()}
     * but does not declare a JMSException in the throw clause
     * @return true if this session is transacted
     */
    private boolean getTransactedNoException() {
        return this.transacted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAcknowledgeMode() throws JMSException {
        illegalStateExceptionIfClosed();
        return getAcknowledgeModeNoException();
    }

    public List<String> getTrustedPackages() {
        return trustedPackages;
    }

    /**
     * Set arguments to be used when declaring a queue while creating a producer.
     * <p>
     * Use this method only when you need to customize the creation of an AMQP queue.
     * Note calling this method requires to cast the JMS {@link Session} to {@link RMQSession},
     * coupling the code to the JMS implementation. Common usage is to keep this call in
     * one place, not scattered in the application code.
     *
     * @param queueDeclareArguments
     * @since 1.14.0
     */
    public void setQueueDeclareArguments(Map<String, Object> queueDeclareArguments) { this.queueDeclareArguments = queueDeclareArguments; }

    /**
     * Same as {@link RMQSession#getAcknowledgeMode()} but without
     * a declared exception in the throws clause.
     * @return the acknowledge mode, one of {@link Session#AUTO_ACKNOWLEDGE},
     * {@link Session#CLIENT_ACKNOWLEDGE}, {@link Session#DUPS_OK_ACKNOWLEDGE} or
     * {@link Session#SESSION_TRANSACTED}
     */
    int getAcknowledgeModeNoException() {
        return this.acknowledgeMode;
    }

    private boolean enterCommittingBlock() {
        synchronized(this.commitLock){
            try {
                while(this.committing) {
                    this.commitLock.wait(COMMIT_WAIT_MAX);
                }
                this.committing = true;
                return true;
            } catch(InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    private void leaveCommittingBlock() {
        synchronized(this.commitLock){
            this.committing = false;
            this.commitLock.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws JMSException {
        logger.trace("commit transaction on session {}", this);
        illegalStateExceptionIfClosed();
        if (!this.transacted) throw new IllegalStateException("Session is not transacted");
        if (this.enterCommittingBlock()) {
            try {
                // Call commit on the channel.
                // All messages ought already to have been acked.
                this.channel.txCommit();
                this.clearUncommittedTags();
            } catch (Exception x) {
                this.logger.error("RabbitMQ exception on channel.txCommit() in session {}", this, x);
                throw new RMQJMSException(x);
            } finally {
                this.leaveCommittingBlock();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws JMSException {
        logger.trace("rollback transaction on session {}", this);
        illegalStateExceptionIfClosed();
        if (!this.transacted) throw new IllegalStateException("Session is not transacted");
        if (this.enterCommittingBlock()) {
            try {
                // rollback the RabbitMQ transaction which may cause some messages to become unacknowledged
                this.channel.txRollback();
                if (this.nackOnRollback && this.uncommittedMessageTags.size() > 0) {
                    for (Long dtag : this.uncommittedMessageTags) {
                        this.channel.basicNack(dtag, false, false);
                    }
                    this.channel.txCommit();
                    this.clearUncommittedTags();
                }
                // requeue all unacknowledged messages (not automatically done by RabbitMQ)
                this.channel.basicRecover(true); // requeue
            } catch (IOException x) {
                this.logger.error("RabbitMQ exception on channel.txRollback() or channel.basicRecover(true) in session {}",
                                  this, x);
                throw new RMQJMSException(x);
            } finally {
                this.leaveCommittingBlock();
            }
        }
    }

    void explicitAck(long deliveryTag) {
        if (this.enterCommittingBlock()) {
            try {
                this.channel.basicAck(deliveryTag, false);
            } catch (Exception x) {
                // this is problematic, we have received a message, but we can't ACK it to the server
                this.logger.error("Cannot acknowledge message received (dTag={})", deliveryTag, x);
                // TODO should we deliver the message at this time, knowing that we can't ack it?
                // My recommendation is that we bail out here and not proceed -- but how?
            } finally {
                this.leaveCommittingBlock();
            }
        }
    }

    void explicitNack(long deliveryTag) {
        if (this.enterCommittingBlock()) {
            try {
                this.channel.basicNack(deliveryTag, false, true);
            } catch (Exception x) {
                // TODO logging impl debug message
                this.logger.warn("Cannot reject/requeue message received (dTag={})", deliveryTag, x);
                // this is fine. we didn't ack the message in the first place
            } finally {
                this.leaveCommittingBlock();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws JMSException {
        this.getConnection().sessionClose(this);
    }

    void internalClose() throws JMSException {
        if (this.closed) return;
        logger.trace("close session {}", this);

        synchronized (this.closeLock) {
            try {
                // close consumers first (to prevent requeues being consumed)
                closeAllConsumers();

                // rollback anything not committed already
                if (this.getTransactedNoException()) {
                    // don't nack messages on close
                    this.clearUncommittedTags();
                    this.rollback();
                }

                //clear up potential executor
                this.deliveryExecutor.close();

                //close all producers created by this session
                for (RMQMessageProducer producer : this.producers) {
                    producer.internalClose();
                }
                this.producers.clear();

                //now commit anything done during close
                if (this.getTransactedNoException()) {
                    this.commit();
                }

                this.closeRabbitChannels();

            } finally {
                this.closed = true;
            }
        }
    }

    private void closeAllConsumers() {
        //close all consumers created by this session
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.internalClose();
            } catch (JMSException x) {
                this.logger.error("Consumer ({}) cannot be closed", consumer, x);
            }
        }
        this.consumers.clear();
    }

    void deliverMessage(RMQMessage rmqMessage, MessageListener messageListener) throws JMSException, InterruptedException {
        this.deliveryExecutor.deliverMessageWithProtection(rmqMessage, messageListener);
    }

    private void closeRabbitChannels() throws JMSException {
        this.clearBrowsingChannels(); // does not throw exception
        if (this.channel == null)
            return;
        try {
            this.channel.close();
        } catch (ShutdownSignalException x) {
            // nothing to do
        } catch (Exception x) {
            if (x instanceof IOException) {
                IOException ioe = (IOException) x;
                if (!(ioe.getCause() instanceof ShutdownSignalException)) {
                    this.logger.warn("RabbitMQ channel({}) failed to close on session {}", this.channel, this, ioe);
                    throw new RMQJMSException(ioe);
                }
            } else if (x instanceof TimeoutException) {
                TimeoutException te = (TimeoutException) x;
                this.logger.warn("RabbitMQ channel({}) timed out trying to close session {}", this.channel, this, te);
                throw new RMQJMSException(te);
            } else {
                throw new RMQJMSException("Unexpected exception from channel.close()", x);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() throws JMSException {
        illegalStateExceptionIfClosed();
        if (getTransactedNoException()) {
            throw new javax.jms.IllegalStateException("Session is transacted.");
        } else {
            synchronized (this.unackedMessageTags) {
                /* If we have messages to recover */
                if (!this.unackedMessageTags.isEmpty()) {
                    try {
                        this.channel.basicRecover(true); // requeue
                    } catch (IOException x) {
                        logger.warn("basicRecover on channel({}) failed", this.channel, x);
                        throw new RMQJMSException(x);
                    }
                    this.unackedMessageTags.clear();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageListener getMessageListener() throws JMSException {
        illegalStateExceptionIfClosed();
        return this.messageListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        // not implemented
        throw new UnsupportedOperationException();
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
     * <p>
     * <b>Note</b>: The destination may be null.
     * </p>
     */
    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        logger.trace("create producer for destination '{}' on session '{}'", destination, this);
        illegalStateExceptionIfClosed();
        RMQDestination dest = (RMQDestination) destination;
        declareDestinationIfNecessary(dest);
        RMQMessageProducer producer = new RMQMessageProducer(this, dest, this.preferProducerMessageProperty,
            this.amqpPropertiesCustomiser, this.sendingContextConsumer, this.publishingListener,
            this.keepTextMessageType);
        this.producers.add(producer);
        return producer;
    }

    void declareDestinationIfNecessary(RMQDestination destination) throws JMSException {
        if (destination != null && !destination.isAmqp() && !destination.isDeclared()) {
            if (destination.isQueue()) {
                declareRMQQueue(destination, null, false, true);
            } else {
                declareTopic(destination);
            }
        }
    }

    /**
     * Prepare a message for delayed delivery. If deliveryDelayMs > 0, it declares the exchange X_DELAYED_JMS_EXCHANGE
     * and binds to the target destination. It returns X_DELAYED_JMS_EXCHANGE and adds the following headers to the
     * headers map:
     * - x-delay : it has deliveryDelayMs
     * - destination : it has the name of the target exchange
     *
     * If deliveryDelayMs < 1, it returns the name of exchange associated to the target destination.
     *
     * @param destination
     * @param messageHeaders
     * @param deliveryDelayMs
     * @return
     */
    String delayMessage(RMQDestination destination, Map<String, Object> messageHeaders, long deliveryDelayMs) {
        return delayedMessageService.delayMessage(channel, destination, messageHeaders, deliveryDelayMs);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        illegalStateExceptionIfClosed();
        return createConsumerInternal((RMQDestination) destination, null, false, null);
    }

    /** All consumers on a session must be used all synchronously or all asynchronously */
    boolean syncAllowed() {
        // Return (None of the MessageConsumers have a (non-null) MessageListener set).
        for (RMQMessageConsumer mc : consumers) {
            if (mc.messageListenerIsSet()) return false;
        }
        return true;
    }
    boolean aSyncAllowed() {
        // Return (Number of receives is zero for all MessageConsumers.)
        for (RMQMessageConsumer mc : consumers) {
            if (0 != mc.getNumberOfReceives()) return false;
        }
        return true;
    }

    /**
     * Creates a consumer for a destination.
     * @param dest internal destination object
     * @param uuidTag only used for topics, if null, one is generated as the queue name for this topic
     * @param durableSubscriber true if this is a durable topic subscription
     * @param jmsSelector selector expression - null if no selection required
     * @return {@link #createConsumer(Destination)}
     * @throws JMSException if destination is null or we fail to create the destination on the broker
     * @see #createConsumer(Destination)
     */
    private RMQMessageConsumer createConsumerInternal(RMQDestination dest, String uuidTag, boolean durableSubscriber, String jmsSelector) throws JMSException {
        String consumerTag = uuidTag != null ? uuidTag : generateJmsConsumerQueueName();
        logger.trace("create consumer for destination '{}' with consumerTag '{}' and selector '{}'", dest, consumerTag, jmsSelector);
        declareDestinationIfNecessary(dest);
        if (!dest.isQueue()) {
            String subscriptionName = consumerTag;
            Subscription subscription = this.subscriptions.get(durableSubscriber, subscriptionName);
            if (subscription == null) {
                // it is unshared, non-durable, creating a transient subscription instance
                subscription = new Subscription(subscriptionName, subscriptionName, false, false, jmsSelector, false);
            }
            subscription.createTopology(dest, this, this.channel);
            consumerTag = subscription.queue();
        }
        RMQMessageConsumer consumer = new RMQMessageConsumer(this, dest, consumerTag, getConnection().isStopped(),
            jmsSelector, this.requeueOnMessageListenerException, this.receivingContextConsumer,
            this.requeueOnTimeout);
        this.consumers.add(consumer);
        return consumer;
    }

    private static String generateJmsConsumerQueueName() {
       return Util.generateUUID(RMQDefaultDestinations.getInstance().getConsumerQueueNamePrefix());
    }

    /**
     * The topic selector exchange may be created for this session (there are at most two per session).
     * @param durableSubscriber - set to true if we need to use a durable exchange, false otherwise.
     * @return this session's Selection Exchange
     * @throws IOException
     */
    String getSelectionExchange(boolean durableSubscriber) throws IOException {
        if (durableSubscriber) {
            return this.getDurableTopicSelectorExchange();
        } else {
            return this.getNonDurableTopicSelectorExchange();
        }
    }

    private String getDurableTopicSelectorExchange() throws IOException {
        if (this.durableTopicSelectorExchange==null) {
            this.durableTopicSelectorExchange = Util.generateUUID(RMQDefaultDestinations.getInstance().getDurableTopicSelectorExchangePrefix());
        }
        this.channel.exchangeDeclare(this.durableTopicSelectorExchange, JMS_TOPIC_SELECTOR_EXCHANGE_TYPE, true, true, RJMS_SELECTOR_EXCHANGE_ARGS);
        return this.durableTopicSelectorExchange;
    }

    private String getNonDurableTopicSelectorExchange() throws IOException {
        if (this.nonDurableTopicSelectorExchange==null) {
            this.nonDurableTopicSelectorExchange = Util.generateUUID(RMQDefaultDestinations.getInstance().getNonDurableTopicSelectorExchangePrefix());
        }
        this.channel.exchangeDeclare(this.nonDurableTopicSelectorExchange, JMS_TOPIC_SELECTOR_EXCHANGE_TYPE, false, true, RJMS_SELECTOR_EXCHANGE_ARGS);
        return this.nonDurableTopicSelectorExchange;
    }



    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        illegalStateExceptionIfClosed();
        if (nullOrEmpty(messageSelector)) {
            return createConsumer(destination);
        } else if (isTopic(destination)) {
            return createConsumerInternal((RMQDestination) destination, null, false, messageSelector);
        } else {
            // selectors are not supported for queues
            throw new UnsupportedOperationException();
        }
    }

    private static boolean nullOrEmpty(String str) {
        return str==null || str.trim().isEmpty();
    }

    private static boolean isTopic(Destination destination) {
        return !((RMQDestination) destination).isQueue();
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) throws JMSException {
        illegalStateExceptionIfClosed();
        if (nullOrEmpty(messageSelector)) {
            RMQMessageConsumer consumer = (RMQMessageConsumer)createConsumer(destination);
            consumer.setNoLocal(noLocal);
            return consumer;
        } else if (isTopic(destination)) {
            RMQMessageConsumer consumer = createConsumerInternal((RMQDestination) destination, null, false, messageSelector);
            consumer.setNoLocal(noLocal);
            return consumer;
        }  else {
            // selectors are not supported for queues
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue createQueue(String queueName) throws JMSException {
        illegalStateExceptionIfClosed();
        RMQDestination dest = new RMQDestination(queueName, true, false);
        declareRMQQueue(dest, null, false, true);
        return dest;
    }


    /**
     * Invokes {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)} to define a queue on the RabbitMQ broker
     * this method invokes {@link RMQDestination#setDeclared(boolean)} with a true value
     * @param dest - the Queue Destination object
     * @param queueNameOverride name of queue to declare (if different from destination name)
     * @param durableSubscriber - true if the subscriber ius
     * @throws JMSException if an IOException occurs in the {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)} call
     */
    void declareRMQQueue(RMQDestination dest, String queueNameOverride, boolean durableSubscriber, boolean bind) throws JMSException {
        logger.trace("declare RabbitMQ queue for destination '{}', explicitName '{}', durableSubscriber={}", dest, queueNameOverride, durableSubscriber);
        String queueName = queueNameOverride!=null ? queueNameOverride : dest.getQueueName();

        String exchangeName = dest.getAmqpExchangeName();
        String exchangeType = dest.getAmqpExchangeType();

        /*
         * We only want destinations to survive server restarts if
         * 1. They are durable topic subscriptions OR
         * 2. They are permanent queues
         */
        boolean durable = durableSubscriber || (dest.isQueue() & (!dest.isTemporary()));

        /*
         * A queue is exclusive, meaning it can only be accessed by the current connection
         * and will be deleted when the connection is closed if
         * 1. It's a temporary destination OR
         * 2. It's a non durable topic
         */
        boolean exclusive = dest.isTemporary() || ((!dest.isQueue()) && (!durableSubscriber));

        if (dest.isQueue()) {
            if (dest.noNeedToDeclareExchange()) {
                logger.warn("no need to declare built-in exchange for queue destination '{}'", dest);
            }
            else {
                logger.trace("declare RabbitMQ exchange for queue destinations '{}'", dest);
                try {
                    this.channel.exchangeDeclare(exchangeName, exchangeType, durable,
                                                 false, // autoDelete
                                                 false, // internal
                                                 null); // object properties
                } catch (Exception x) {
                    throw new RMQJMSException(x);
                }
            }
        }

        /* broker queues declared for a non-durable topic that have an auto-generated name must go down with
           consumer/producer or the broker will leak them until the connection is brought down
        */
        boolean autoDelete = cleanUpServerNamedQueuesForNonDurableTopics ?
            !durable && queueNameOverride != null && !dest.isQueue() : false;

        try { /* Declare the queue to RabbitMQ -- this creates it if it doesn't already exist */
            this.logger.debug("declare RabbitMQ queue name({}), durable({}), exclusive({}), auto-delete({}), arguments({} + {})",
                              queueName, durable, exclusive, false,
                              this.queueDeclareArguments, dest.getQueueDeclareArguments());
            Map<String, Object> arguments = merge(this.queueDeclareArguments, dest.getQueueDeclareArguments());
            this.channel.queueDeclare(queueName,
                                      durable,
                                      exclusive,
                                      autoDelete,
                                      arguments);

            /* Temporary or 'topic queues' are exclusive and therefore get deleted by RabbitMQ on close */
        } catch (Exception x) {
            this.logger.error("RabbitMQ exception on queue declare name({}), durable({}), exclusive({}), auto-delete({}), arguments({} + {})",
                              queueName, durable, exclusive, autoDelete, this.queueDeclareArguments,
                              dest.getQueueDeclareArguments(), x);
            throw new RMQJMSException(x);
        }

        if (bind) {
            try { /* Bind the queue to our exchange -- this allows publications to succeed. */
                this.logger.debug("bind queue name({}), to exchange({}), with r-key({}), no arguments",
                        queueName, exchangeName, queueName);
                this.channel.queueBind(queueName, exchangeName,
                        queueName, // routing key
                        null); // arguments
            } catch (Exception x) {
                this.logger.error("RabbitMQ exception on queue declare name({}), durable({}), exclusive({}), auto-delete({}), properties({})",
                        queueName, durable, exclusive, false, queueDeclareArguments, x);
                throw new RMQJMSException(x);
            }
        }
        dest.setDeclared(true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Topic createTopic(String topicName) throws JMSException {
        illegalStateExceptionIfClosed();
        RMQDestination dest = new RMQDestination(topicName, false, false);
        declareTopic(dest);
        return dest;
    }

    /**
     * Declares a topic exchange in RabbitMQ.
     * @param dest the topic destination
     * @throws JMSException
     */
    private void declareTopic(RMQDestination dest) throws JMSException {
        if (dest.noNeedToDeclareExchange()) {
            logger.warn("no need to declare built-in exchange for topic destination '{}'", dest);
        }
        else {
            logger.trace("declare RabbitMQ exchange for topic destination '{}'", dest);
            try {
                this.channel.exchangeDeclare(/* the name of the exchange */
                                             dest.getAmqpExchangeName(),
                                             /* the type of exchange to use */
                                             dest.getAmqpExchangeType(),
                                             /* durable for all except temporary topics */
                                             !dest.isTemporary(),
                                             // TODO: how do we delete exchanges used for temporary topics
                                             /* auto delete is always false */
                                             false,
                                             /* internal is false: JMS clients will want to publish directly to the exchange */
                                             false,
                                             /* object parameters */
                                             null);
            } catch (IOException x) {
                throw new RMQJMSException(x);
            }
        }
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        illegalStateExceptionIfClosed();
        return createBrowser(queue, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        illegalStateExceptionIfClosed();
        if (queue instanceof RMQDestination) {
            RMQDestination rmqDest = (RMQDestination) queue;
            if (rmqDest.isQueue()) {
                return new BrowsingMessageQueue(this, rmqDest, messageSelector,
                    this.connection.getQueueBrowserReadMax(), this.receivingContextConsumer);
            }
        }
        throw new UnsupportedOperationException("Unknown destination");
    }

    /**
     * Get a (new) channel for queue browsing.
     * @return channel for browsing queues
     * @throws JMSException if channel not available
     */
    Channel getBrowsingChannel() throws JMSException {
        try {
            synchronized (this.bcLock) {
                Channel chan = this.getConnection().createRabbitChannel(false); // not transactional
                this.browsingChannels.add(chan);
                return chan;
            }
        } catch (Exception e) { // includes unchecked exceptions, e.g. ShutdownSignalException
            throw new RMQJMSException("Cannot create browsing channel", e);
        }
    }

    private static Map<String, Object> merge(Map<String, Object> m1, Map<String, Object> m2) {
        if (m1 == null) {
            return m2;
        } else if (m2 == null) {
            return m1;
        } else {
            Map<String, Object> merged = new HashMap<>();
            merged.putAll(m1);
            merged.putAll(m2);
            return merged;
        }
    }

    /**
     * Silently close and discard browsing channels, if any.
     */
    private void clearBrowsingChannels() {
        synchronized (this.bcLock) {
            for (Channel chan : this.browsingChannels) {
                try {
                    if (chan.isOpen())
                        chan.close();
                } catch (Exception e) {
                    // ignore any failures, we are clearing up
                }
            }
            this.browsingChannels.clear();
        }
    }

    /**
     * Close a specific browsing channel.
     */
    void closeBrowsingChannel(Channel chan) {
        try {
            synchronized (this.bcLock) {
                if (this.browsingChannels.remove(chan)) {
                    if (chan.isOpen())
                        chan.close();
                }
            }
        } catch (Exception e) {
            // throw new RMQJMSException("Cannot close browsing channel", _);
            // ignore errors in clearing up
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        illegalStateExceptionIfClosed();
        return new RMQDestination(Util.generateUUID(RMQDefaultDestinations.getInstance().getTempQueuePrefix()), true, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        illegalStateExceptionIfClosed();
        return new RMQDestination(Util.generateUUID(RMQDefaultDestinations.getInstance().getTempTopicPrefix()), false, true);
    }

    /**
     * This is only available for topic subscriptions.
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(String name) throws JMSException {
        illegalStateExceptionIfClosed();
        try {
            if (name != null && this.subscriptions.remove(true, name) != null) {
                // remove the queue
                this.channel.queueDelete(name);
            } else {
                logger.warn("Cannot unsubscribe subscription named '{}'", name);
            }
        } catch (IOException x) {
            logger.error("RabbitMQ Queue delete for queue named '{}' failed", name, x);
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue) throws JMSException {
        illegalStateExceptionIfClosed();
        return (QueueReceiver) this.createConsumer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException {
        illegalStateExceptionIfClosed();
        return (QueueReceiver) this.createConsumer(queue, messageSelector);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note</b>: The queue may be null -- see {@link #createProducer}.
     * </p>
     */
    @Override
    public QueueSender createSender(Queue queue) throws JMSException {
        illegalStateExceptionIfClosed();
        return (QueueSender) this.createProducer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {
        illegalStateExceptionIfClosed();
        return (TopicSubscriber) this.createConsumer(topic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException {
        illegalStateExceptionIfClosed();
        RMQMessageConsumer consumer = createConsumerInternal((RMQDestination) topic, null, false, messageSelector);
        consumer.setNoLocal(noLocal);
        return consumer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicPublisher createPublisher(Topic topic) throws JMSException {
        illegalStateExceptionIfClosed();
        return (TopicPublisher) this.createProducer(topic);
    }

    /**
     * Returns the connection this session is associated with
     * @return
     */
    RMQConnection getConnection() {
        return this.connection;
    }

    /**
     * Returns the {@link Channel} this session has created
     * @return
     */
    Channel getChannel() {
        return this.channel;
    }

    void consumerClose(RMQMessageConsumer consumer) throws JMSException {
        if (this.consumers.remove(consumer)) {
            //TODO: if (consumer.isDurable()) { don't cancel it? cancel it? -- decide }
            consumer.internalClose();
        }
    }

    void removeProducer(RMQMessageProducer producer) {
        if (this.producers.remove(producer)) {
            producer.internalClose();
        }
    }

    boolean isAutoAck() {
        return (getAcknowledgeModeNoException()!=Session.CLIENT_ACKNOWLEDGE);  // only case when auto ack not required
    }

    /**
     * Stops all consumers from receiving messages. This is called by the
     * session indirectly after {@link javax.jms.Connection#stop()} has been
     * invoked. In this implementation, any async consumers will be cancelled,
     * only to be re-subscribed when
     *
     * @throws javax.jms.JMSException if the thread is interrupted
     */
    void pause() throws JMSException {
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.pause();
            } catch (JMSException e) {
                throw e;
            } catch (InterruptedException x) {
                logger.error("Consumer({}) pause interrupted", consumer, x);
                throw new RMQJMSException(x);
            } catch (Exception x) {
                logger.error("Error while pausing consumer({})", consumer, x);
                throw new RMQJMSException(x);
            }
        }
    }

    /**
     * Resubscribes all async listeners and continues to receive messages
     *
     * @see javax.jms.Connection#stop()
     * @throws javax.jms.JMSException if the thread is interrupted
     */
    void resume() throws JMSException {
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.resume();
            } catch (IllegalStateException x) {
                throw new RMQJMSException(x);
            }
        }
    }

    void unackedMessageReceived(long dTag) {
        if (!getTransactedNoException()) {
            synchronized (this.unackedMessageTags) {
                this.unackedMessageTags.add(dTag);
            }
        }
    }

    /**
     * Acknowledges messages in this session.
     * Invoked when the method {@link javax.jms.Message#acknowledge()} is called.
     * @param message - the message to be acknowledged, or the carrier to acknowledge all messages
     */
    void acknowledgeMessage(RMQMessage message) throws JMSException {
       this.acknowledge(message.getRabbitDeliveryTag());
    }

    void acknowledgeMessages() throws JMSException {
        try {
            Long lastMessageTag = this.unackedMessageTags.last();
            this.acknowledge(lastMessageTag);
        } catch (NoSuchElementException e) {
           // nothing to acknowledge
        }
    }


    private void acknowledge(long messageTag) throws JMSException {
        illegalStateExceptionIfClosed();

        boolean individualAck = this.getIndividualAck();
        boolean groupAck      = true;  // This assumption is new in RJMS 1.2.0 and is consistent with other implementations. It allows a form of group acknowledge.
        if (!isAutoAck() && !this.unackedMessageTags.isEmpty()) {
            /**
             * Per JMS specification of {@link Message#acknowledge()}, <i>if we ack the last message in a group, we will ack all the ones prior received</i>.
             * <p>But, JMS spec 11.2.21 says:</p>
             * <pre>"Note that the acknowledge method of Message acknowledges all messages
             * received on that message's session."</pre>
             * <p>
             * The groupAck option acknowledges all previous messages in this session (and this one, too, if not acknowledged already).
             * The individualAck option is set by session mode (CLIENT_INDIVIDUAL_ACKNOWLEDGE) and overrides groupAck (default) and acknowledges at most a single message.
             * </p>
             */
            synchronized (this.unackedMessageTags) {
                try {
                    if (individualAck) {
                        if (!this.unackedMessageTags.contains(messageTag)) return; // this message already acknowledged
                        /* ACK a single message */
                        this.getChannel().basicAck(messageTag, false); // we ack the single message with this tag
                        this.unackedMessageTags.remove(messageTag);
                    } else if (groupAck) {
                        /** The tags that precede the given one, and the given one, if unacknowledged */
                        SortedSet<Long> previousTags = this.unackedMessageTags.headSet(messageTag+1);
                        if (previousTags.isEmpty()) return; // no message to acknowledge
                        /* ack multiple message up until the existing tag */
                        this.getChannel().basicAck(previousTags.last(), // we ack the latest one (which might be this one, but might not be)
                                              true);               // and everything prior to that
                        // now remove all the tags <= messageTag
                        previousTags.clear();
                    } else {
                        // this block is no longer possible (groupAck == true) after RJMS 1.2.0
                        this.getChannel().basicAck(this.unackedMessageTags.last(), // we ack the highest tag
                                              true);                          // and everything prior to that
                        this.unackedMessageTags.clear();
                    }
                } catch (IOException x) {
                    this.logger.error("RabbitMQ exception on basicAck of message {}; on session '{}'", messageTag, this, x);
                    throw new RMQJMSException(x);
                }
            }
        }
    }

    private boolean getIndividualAck() {
        return this.isIndividualAck;
    }

    void addUncommittedTag(long deliveryTag) {
        if (this.nackOnRollback && this.getTransactedNoException()) {
            if (this.enterCommittingBlock()) {
                this.uncommittedMessageTags.add(deliveryTag);
                this.leaveCommittingBlock();
            }
        }
    }

    private void clearUncommittedTags() {
        if (this.nackOnRollback) {
            this.uncommittedMessageTags.clear();
        }
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
        return createDurableConsumer(topic, name, null, false);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector,
        boolean noLocal) throws JMSException {
        return this.createTopicConsumer(topic, name, true, false, messageSelector, noLocal);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
       return wrap(() -> this.createTopicConsumer(topic, sharedSubscriptionName, false, true, null, false));
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName,
        String messageSelector) {
        return wrap(() -> this.createTopicConsumer(topic, sharedSubscriptionName, false, true, messageSelector, false));
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name) {
        return wrap(() -> this.createTopicConsumer(topic, name, true, true, null, false));
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name,
        String messageSelector) {
        return wrap(() -> this.createTopicConsumer(topic, name, true, true, messageSelector, false));
    }

    private static MessageConsumer wrap(Callable<MessageConsumer> action) {
        try {
            return action.call();
        } catch (JMSException e) {
           throw new JMSRuntimeException(e.getMessage());
        } catch (Exception e) {
           throw new JMSRuntimeException(e.getMessage(), null, e);
        }
    }

    private MessageConsumer createTopicConsumer(Topic topic, String name, boolean durable, boolean shared,
        String messageSelector, boolean noLocal) throws JMSException {
        illegalStateExceptionIfClosed();

        this.subscriptionNameValidator.validate(name);

        RMQDestination topicDest = (RMQDestination) topic;
        String queueName = durable ? name : generateJmsConsumerQueueName();
        synchronized (this.subscriptions) {
            Subscription subscription = this.subscriptions.register(name, queueName, durable, shared,
                messageSelector, noLocal);
            PostAction postAction = subscription.validateNewConsumer(topic, durable, shared,
                messageSelector, noLocal);
            postAction.run(new Context(this, this.subscriptions));
            // look up the subscription again, the instance can have changed because topic/selector/noLocal
            // are not the same
            subscription = this.subscriptions.get(durable, name);
            // create the consumer
            RMQMessageConsumer consumer = createConsumerInternal(topicDest, name, durable, messageSelector);
            consumer.setDurable(durable);
            consumer.setNoLocal(noLocal);
            subscription.add(consumer);
            return consumer;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        return createDurableSubscriber(topic, name, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        return (TopicSubscriber) createDurableConsumer(topic, name, messageSelector, noLocal);
    }

    private interface SubscriptionNameValidator {

        void validate(String name) throws JMSException;

    }

    /**
     * Gets the reply to strategy that should be followed if as reply to is
     * found on a received message.
     *
     * @return  The reply to strategy.
     *
     * @since 2.9.0
     */
    public ReplyToStrategy getReplyToStrategy() {
        return replyToStrategy;
    }

}
