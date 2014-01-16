/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidSelectorException;
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
import com.rabbitmq.jms.parse.sql.SqlExpressionType;
import com.rabbitmq.jms.parse.sql.SqlParseTree;
import com.rabbitmq.jms.parse.sql.SqlParser;
import com.rabbitmq.jms.parse.sql.SqlTokenStream;
import com.rabbitmq.jms.parse.sql.SqlTypeChecker;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.RMQJMSSelectorException;
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
    /** The ack mode used when receiving message */
    private final int acknowledgeMode;
    /** The main RabbitMQ channel we use under the hood */
    private final Channel channel;
    /** Set to true if close() has been called and completed */
    private volatile boolean closed = false;
    /** The message listener for this session. */
    private volatile MessageListener messageListener;
    /** A list of all the producers created by this session.
     * When a producer is closed, it will be removed from this list */
    private final ArrayList<RMQMessageProducer> producers = new ArrayList<RMQMessageProducer>();
    /** A list of all the consumers created by this session.
     * When a consumer is closed, it will be removed from this list */
    private final ArrayList<RMQMessageConsumer> consumers = new ArrayList<RMQMessageConsumer>();
    /** We keep an ordered set of the message tags (acknowledgement tags) for all messages received and unacknowledged.
     * Each message acknowledgement must ACK all (unacknowledged) messages received up to this point, and
     * we must never acknowledge a message more than once (nor acknowledge a message that doesn't exist). */
    private final SortedSet<Long> unackedMessageTags = Collections.synchronizedSortedSet(new TreeSet<Long>());

    /** List of all our durable subscriptions so we can track them */
    private final Map<String, RMQMessageConsumer> subscriptions;

    /** Lock for waiting for close */
    private final Object closeLock = new Object();

    /** Lock and parms for commit and rollback blocking of other commands */
    private final Object commitLock = new Object();
    private final static long COMMIT_WAIT_MAX = 2000L; // 2 seconds
    private boolean committing = false; // GuardedBy("commitLock");

    /** Selector exchange for topic selection */
    private volatile String durableTopicSelectorExchange;
    /** Selector exchange for topic selection */
    private volatile String nonDurableTopicSelectorExchange;
    /** Selector exchange arg key for selector expression */
    private static final String RJMS_SELECTOR_ARG = "rjms_selector";
    /** Name of argument on exchange create; used to specify identifier types */
    private static final String RJMS_TYPE_INFO_ARG = "rjms_type_info";
    /** Name of argument on exchange create; used to determine selection policy */
    private static final String RJMS_POLICY_ARG = "rjms_selection_policy";

    private static Map<String, SqlExpressionType> generateJMSTypeIdents() {
        Map<String, SqlExpressionType> map = new HashMap<String, SqlExpressionType>(6);  // six elements only
        // [ {<<"JMSDeliveryMode">>,  [<<"PERSISTENT">>, <<"NON_PERSISTENT">>]}
        // , {<<"JMSPriority">>,      number}
        // , {<<"JMSMessageID">>,     string}
        // , {<<"JMSTimestamp">>,     number}
        // , {<<"JMSCorrelationID">>, string}
        // , {<<"JMSType">>,          string}
        // ]
        map.put("JMSDeliveryMode",  SqlExpressionType.STRING);
        map.put("JMSPriority",      SqlExpressionType.ARITH );
        map.put("JMSMessageID",     SqlExpressionType.STRING);
        map.put("JMSTimestamp",     SqlExpressionType.ARITH );
        map.put("JMSCorrelationID", SqlExpressionType.STRING);
        map.put("JMSType",          SqlExpressionType.STRING);
        return Collections.unmodifiableMap(map);
    }

    private static final Map<String, SqlExpressionType> JMS_TYPE_IDENTS = generateJMSTypeIdents();

    private static Map<String, Object> generateJMSTypeInfoMap() {
        Map<String, Object> map = new HashMap<String, Object>(6);  // six elements only
        // [ {<<"JMSDeliveryMode">>,  [<<"PERSISTENT">>, <<"NON_PERSISTENT">>]}
        // , {<<"JMSPriority">>,      number}
        // , {<<"JMSMessageID">>,     string}
        // , {<<"JMSTimestamp">>,     number}
        // , {<<"JMSCorrelationID">>, string}
        // , {<<"JMSType">>,          string}
        // ]
        map.put("JMSDeliveryMode",  "string");
        map.put("JMSPriority",      "number");
        map.put("JMSMessageID",     "string");
        map.put("JMSTimestamp",     "number");
        map.put("JMSCorrelationID", "string");
        map.put("JMSType",          "string");

        return Collections.unmodifiableMap(map);
    }

    private static final Map<String, Object> JMS_TYPE_INFO_ARGUMENTS = generateJMSTypeInfoMap();

    private static final Map<String, Object> RJMS_TOPIC_SELECTOR_EXCHANGE_ARGUMENTS = generateJMSExchangeArgs("jms-topic");
//    private static final Map<String, Object> RJMS_QUEUE_SELECTOR_EXCHANGE_ARGUMENTS = generateJMSExchangeArgs("jms-queue");

    private static Map<String, Object> generateJMSExchangeArgs(String policy) {
        Map<String, Object> map = new HashMap<String, Object>(2);  // pair of elements only
        map.put(RJMS_TYPE_INFO_ARG, (Object) JMS_TYPE_INFO_ARGUMENTS);
        map.put(RJMS_POLICY_ARG, (Object) policy);
        return Collections.unmodifiableMap(map);
    }

    private static final String JMS_TOPIC_SELECTOR_EXCHANGE_TYPE = "x-jms-topic";

    private static final long ON_MESSAGE_EXECUTOR_TIMEOUT_MS = 2000; // 2 seconds
    private final DeliveryExecutor deliveryExecutor = new DeliveryExecutor(ON_MESSAGE_EXECUTOR_TIMEOUT_MS);

    /**
     * Creates a session object associated with a connection
     * @param connection the connection that we will send data on
     * @param transacted whether this session is transacted or not
     * @param mode the default ACK mode
     * @param subscriptions the connection's subscriptions, shared with all sessions
     * @throws JMSException if we fail to create a {@link Channel} object on the connection
     */
    public RMQSession(RMQConnection connection, boolean transacted, int mode, Map<String, RMQMessageConsumer> subscriptions) throws JMSException {
        assert (mode >= 0 && mode <= 3);
        this.connection = connection;
        this.transacted = transacted;
        this.subscriptions = subscriptions;
        this.acknowledgeMode = transacted ? Session.SESSION_TRANSACTED : mode;
        try {
            this.channel = connection.createRabbitChannel(transacted);
        } catch (IOException x) {
            throw new RMQJMSException(x);
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
     * @see  #getTransacted()
     */
    public boolean getTransactedNoException() {
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
        if (this.channel==null) return;
        try {
            this.channel.close();
        } catch (ShutdownSignalException x) {
            // nothing to do
        } catch (IOException x) {
            if (!(x.getCause() instanceof ShutdownSignalException)) {
                this.logger.warn("RabbitMQ channel({}) failed to close on session {}", this.channel, this, x);
                throw new RMQJMSException(x);
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
        RMQMessageProducer producer = new RMQMessageProducer(this, dest);
        this.producers.add(producer);
        return producer;
    }

    void declareDestinationIfNecessary(RMQDestination destination) throws JMSException {
        if (destination != null && !destination.isAmqp() && !destination.isDeclared()) {
            if (destination.isQueue()) {
                declareRMQQueue(destination, null, false);
            } else {
                declareTopic(destination);
            }
        }
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
        String consumerTag = uuidTag != null ? uuidTag : Util.generateUUID("jms-cons-");
        logger.trace("create consumer for destination '{}' with consumerTag '{}' and selector '{}'", dest, consumerTag, jmsSelector);

        declareDestinationIfNecessary(dest);

        if (!dest.isQueue()) {
            // This is a topic, we need to define a queue, and bind to it.
            // The queue name is distinct for each consumer.
            try {
                String queueName = consumerTag;
                this.declareRMQQueue(dest, queueName, durableSubscriber);
                if (jmsSelector==null) {
                    // bind the queue to the exchange with the correct routing key
                    this.channel.queueBind(queueName, dest.getAmqpExchangeName(), dest.getAmqpRoutingKey());
                } else {
                    // get this session's topic selector exchange (name)
                    String selectionExchange = this.getSelectionExchange(durableSubscriber);
                    // bind it to the topic exchange with the topic routing key
                    this.channel.exchangeBind(selectionExchange, dest.getAmqpExchangeName(), dest.getAmqpRoutingKey());
                    this.bindSelectorQueue(dest, jmsSelector, queueName, selectionExchange);
                }
            } catch (IOException x) {
                logger.error("consumer with tag '{}' could not be created", consumerTag, x);
                throw new RMQJMSException("RabbitMQ Exception creating Consumer", x);
            }
        }
        RMQMessageConsumer consumer = new RMQMessageConsumer(this, dest, consumerTag, getConnection().isStopped(), jmsSelector);
        this.consumers.add(consumer);
        return consumer;
    }

    private void bindSelectorQueue(RMQDestination dest, String jmsSelector, String queueName, String selectionExchange)
            throws InvalidSelectorException, IOException {
        if (selectorIsValid(jmsSelector)) {
            Map<String, Object> args = Collections.singletonMap(RJMS_SELECTOR_ARG, (Object)jmsSelector);
            // bind the queue to the topic selector exchange with the jmsSelector expression as argument
            this.channel.queueBind(queueName, selectionExchange, dest.getAmqpRoutingKey(), args);
        } else {
            throw new RMQJMSSelectorException(String.format("Selector expression: \"%s\" is not type-valid or is incorrectly formed.", jmsSelector));
        }
    }

    /**
     * Can the selector string be parsed and is it type-valid for JMS message selection?
     * @param jmsSelector - the selector string
     * @return <code>true</code> if it is valid, <code>false</code> if not.
     */
    private static boolean selectorIsValid(String jmsSelector) {
        SqlTokenStream tokenStream = new SqlTokenStream(jmsSelector);
        if (!"".equals(tokenStream.getResidue())) return false;
        SqlParser sp = new SqlParser(tokenStream);
        SqlParseTree spt = sp.parse();
        if (!sp.parseOk()) return false;
        return canBeBool(SqlTypeChecker.deriveExpressionType(spt, JMS_TYPE_IDENTS));
    }

    private static boolean canBeBool(SqlExpressionType set) {
        return (set == SqlExpressionType.BOOL || set == SqlExpressionType.ANY);
    }

    /**
     * The topic selector exchange may be created for this session (there are at most two per session).
     * @param durableSubscriber - set to true if we need to use a durable exchange, false otherwise.
     * @return this session's Selection Exchange
     * @throws IOException
     */
    private String getSelectionExchange(boolean durableSubscriber) throws IOException {
        if (durableSubscriber) {
            return this.getDurableTopicSelectorExchange();
        } else {
            return this.getNonDurableTopicSelectorExchange();
        }
    }

    private String getDurableTopicSelectorExchange() throws IOException {
        if (this.durableTopicSelectorExchange==null) {
            this.durableTopicSelectorExchange = Util.generateUUID("jms-dutop-slx-");
        }
        this.channel.exchangeDeclare(this.durableTopicSelectorExchange, JMS_TOPIC_SELECTOR_EXCHANGE_TYPE, true, true, RJMS_TOPIC_SELECTOR_EXCHANGE_ARGUMENTS);
        return this.durableTopicSelectorExchange;
    }

    private String getNonDurableTopicSelectorExchange() throws IOException {
        if (this.nonDurableTopicSelectorExchange==null) {
            this.nonDurableTopicSelectorExchange = Util.generateUUID("jms-ndtop-slx-");
        }
        this.channel.exchangeDeclare(this.nonDurableTopicSelectorExchange, JMS_TOPIC_SELECTOR_EXCHANGE_TYPE, false, true, RJMS_TOPIC_SELECTOR_EXCHANGE_ARGUMENTS);
        return this.nonDurableTopicSelectorExchange;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        illegalStateExceptionIfClosed();
        if (messageSelector==null || messageSelector.trim().isEmpty()) {
            return createConsumer(destination);
        } else {
            // we are not implementing this method yet
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) throws JMSException {
        illegalStateExceptionIfClosed();
        if (messageSelector==null || messageSelector.trim().isEmpty()) {
            RMQMessageConsumer consumer = (RMQMessageConsumer)createConsumer(destination);
            consumer.setNoLocal(noLocal);
            return consumer;
        } else {
            // we are not implementing this method yet
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
        declareRMQQueue(dest, null, false);
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
    private void declareRMQQueue(RMQDestination dest, String queueNameOverride, boolean durableSubscriber) throws JMSException {
        logger.trace("declare RabbitMQ queue for destination '{}', explicitName '{}', durableSubscriber={}", dest, queueNameOverride, durableSubscriber);
        String queueName = queueNameOverride!=null ? queueNameOverride : dest.getQueueName();

        String exchangeName = dest.getAmqpExchangeName();
        String exchangeType = dest.amqpExchangeType();

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

        Map<String,Object> options = null; //new HashMap<String,Object>();

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

        try { /* Declare the queue to RabbitMQ -- this creates it if it doesn't already exist */
            this.logger.debug("declare RabbitMQ queue name({}), durable({}), exclusive({}), auto-delete({}), properties({})",
                              queueName, durable, exclusive, false, options);
            this.channel.queueDeclare(queueName,
                                      durable,
                                      exclusive,
                                      false,    // autoDelete - exclusive takes care of this
                                      options); // object properties

            /* Temporary or 'topic queues' are exclusive and therefore get deleted by RabbitMQ on close */
        } catch (Exception x) {
            this.logger.error("RabbitMQ exception on queue declare name({}), durable({}), exclusive({}), auto-delete({}), properties({})",
                              queueName, durable, exclusive, false, options, x);
            throw new RMQJMSException(x);
        }

        try { /* Bind the queue to our exchange -- this allows publications to succeed. */
            this.logger.debug("bind queue name({}), to exchange({}), with r-key({}), no arguments",
                              queueName, exchangeName, queueName);
            this.channel.queueBind(queueName, exchangeName,
                                   queueName, // routing key
                                   null); // arguments
        } catch (Exception x) {
            this.logger.error("RabbitMQ exception on queue declare name({}), durable({}), exclusive({}), auto-delete({}), properties({})",
                              queueName, durable, exclusive, false, options, x);
            throw new RMQJMSException(x);
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
                                             dest.amqpExchangeType(),
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
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        return createDurableSubscriber(topic, name, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        illegalStateExceptionIfClosed();
        if (messageSelector!=null && messageSelector.trim().isEmpty())
            messageSelector = null;

        RMQDestination topicDest = (RMQDestination) topic;
        RMQMessageConsumer previousConsumer = this.subscriptions.get(name);
        if (previousConsumer!=null) {
            // we are changing subscription, or not, if called with the same topic
            RMQDestination prevDest = previousConsumer.getDestination();
            if (prevDest.equals(topicDest)) {
                if (previousConsumer.isClosed()) {
                    // They called TopicSubscriber.close but didn't unsubscribe
                    // and they are simply resubscribing with a new one
                    logger.warn("Re-subscribing to topic '{}' with name '{}'", topicDest, name);
                } else {
                    logger.error("Subscription with name '{}' for topic '{}' already exists", name, topicDest);
                    throw new JMSException("Subscription with name["+name+"] and topic["+topicDest+"] already exists");
                }
            } else {
                logger.warn("Previous subscription with name '{}' was for topic '{}' and is replaced by one for topic '{}'", name, prevDest, topicDest);
                unsubscribe(name);
            }
        }
        // Create a new subscription
        RMQMessageConsumer consumer = (RMQMessageConsumer)createConsumerInternal(topicDest, name, true, messageSelector);
        consumer.setDurable(true);
        consumer.setNoLocal(noLocal);
        this.subscriptions.put(name, consumer);
        return consumer;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException in version 1.0
     */
    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        illegalStateExceptionIfClosed();
        throw new UnsupportedOperationException("Browsing not supported in version 1.0");
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException in version 1.0
     */
    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        illegalStateExceptionIfClosed();
        throw new UnsupportedOperationException("Browsing not supported in version 1.0");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        illegalStateExceptionIfClosed();
        RMQDestination result = new RMQDestination(Util.generateUUID("jms-temp-queue-"), true, true);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        illegalStateExceptionIfClosed();
        RMQDestination result = new RMQDestination(Util.generateUUID("jms-temp-topic-"), false, true);
        return result;
    }

    /**
     * This is only available for topic subscriptions.
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(String name) throws JMSException {
        illegalStateExceptionIfClosed();
        try {
            if (name != null && this.subscriptions.remove(name) != null) {
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

        if (messageSelector!=null && messageSelector.trim().isEmpty()) messageSelector = null;

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

    void consumerClose(RMQMessageConsumer consumer) throws JMSException {
        if (this.consumers.remove(consumer)) {
            //TODO: if (consumer.isDurable()) { don't cancel it? cancel it? -- decide }
            consumer.internalClose();
        }
    }

    public void removeProducer(RMQMessageProducer producer) {
        if (this.producers.remove(producer)) {
            producer.internalClose();
        }
    }

    private boolean isAutoAck() {
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
    public void pause() throws JMSException {
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.pause();
            } catch (InterruptedException x) {
                logger.error("Consumer({}) pause interrupted", consumer, x);
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
    public void resume() throws JMSException {
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.resume();
            } catch (IllegalStateException x) {
                throw new RMQJMSException(x);
            }
        }
    }

    void unackedMessageReceived(RMQMessage message) {
        if (!getTransactedNoException()) {
            synchronized (this.unackedMessageTags) {
                this.unackedMessageTags.add(message.getRabbitDeliveryTag());
            }
        }
    }

    /**
     * Acknowledges all unacknowledged messages for this session.
     * Invoked when the method {@link RMQMessage#acknowledge()} is called.
     */
    void acknowledgeMessages() throws JMSException {
        illegalStateExceptionIfClosed();
        if (!isAutoAck() && !this.unackedMessageTags.isEmpty()) {
            /*
             * Per JMS specification Message.acknowledge(), if we ack
             * the last message in a group, we will ack all the ones prior received.
             * Spec 11.2.21 says:
             * "Note that the acknowledge method of Message acknowledges all messages
             * received on that message's session."
             */
            synchronized (this.unackedMessageTags) {
                long lastTag = this.unackedMessageTags.last();
                /* ACK all messages unacknowledged (ACKed or NACKed) on this session. */
                try {
                    getChannel().basicAck(lastTag, // we ack the highest tag
                                          true);   // and everything prior to that
                    this.unackedMessageTags.clear();
                } catch (IOException x) {
                    logger.error("RabbitMQ exception on basicAck of message with tag '{}' on session '{}'", lastTag, this, x);
                    throw new RMQJMSException(x);
                }
            }
        }
    }
}
