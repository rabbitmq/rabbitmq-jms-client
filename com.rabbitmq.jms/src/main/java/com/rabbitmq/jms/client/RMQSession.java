/* Copyright © 2013 VMware, Inc. All rights reserved. */
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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.impl.AMQCommand;
import com.rabbitmq.client.impl.AMQImpl;
import com.rabbitmq.client.impl.Method;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.admin.RMQExchangeInfo;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQMapMessage;
import com.rabbitmq.jms.client.message.RMQObjectMessage;
import com.rabbitmq.jms.client.message.RMQStreamMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import com.rabbitmq.jms.util.RJMSLogger;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.RMQJMSSelectorException;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ implementation of JMS {@link Session}
 */
public class RMQSession implements Session, QueueSession, TopicSession {

    private static final RJMSLogger LOGGER = new RJMSLogger("RMQSession");

    /** The connection that created this session */
    private final RMQConnection connection;
    /** Set to true if this session is transacted. */
    private final boolean transacted;
    /** The ack mode used when receiving message */
    private final int acknowledgeMode;
    /** The main RabbitMQ channel we use under the hood */
    private final Channel channel;
    /** A sacrificial channel we use for requests that might fail */
    private Channel sacrificialChannel = null; // @GuardedBy(sc_lock)
    private Object scLock = new Object();
    /** Set to true if close() has been called and completed */
    private volatile boolean closed = false;
    /** The message listener for this session. */
    private volatile MessageListener messageListener;
    /** A list of all the producers created by this session.
     * When a producer is closed, it will be removed from this list */
    private final ArrayList<RMQMessageProducer> producers = new ArrayList<RMQMessageProducer>();
    /** A list of all the consumer created by this session.
     * When a consumer is closed, it will be removed from this list */
    private final ArrayList<RMQMessageConsumer> consumers = new ArrayList<RMQMessageConsumer>();
    /** We keep an ordered set of the message tags (acknowledgement tags) for all messages received and unacknowledged.
     * Each message acknowledgement must ACK all (unacknowledged) messages received up to this point, and
     * we must never acknowledge a message more than once (nor acknowledge a message that doesn't exist). */
    private final SortedSet<Long> unackedMessageTags = Collections.synchronizedSortedSet(new TreeSet<Long>());

    /** List of all our durable subscriptions so we can track them */
    private final Map<String, RMQMessageConsumer> subscriptions;

    private final Object closeLock = new Object();

    /** Selector exchange for topic selection */
    private volatile String durableTopicSelectorExchange;
    /** Selector exchange for topic selection */
    private volatile String nonDurableTopicSelectorExchange;
    /** Selector exchange arg key for selector expression */
    private static final String RJMS_SELECTOR_ARG = "rjms_selector";

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
            this.channel = connection.createRabbitChannel();
            if (transacted) {
                /* Make the channel (RabbitMQ) transactional: this cannot be undone */
                this.channel.txSelect();
            }
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        LOGGER.log("createBytesMessage");
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
        LOGGER.log("createMapMessage");
        illegalStateExceptionIfClosed();
        return new RMQMapMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message createMessage() throws JMSException {
        LOGGER.log("createMessage");
        illegalStateExceptionIfClosed();
        return createTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        LOGGER.log("createObjectMessage");
        illegalStateExceptionIfClosed();
        return new RMQObjectMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        LOGGER.log("createObjectMessage", object);
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
        LOGGER.log("createStreamMessage");
        illegalStateExceptionIfClosed();
        return new RMQStreamMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage() throws JMSException {
        LOGGER.log("createTextMessage");
        illegalStateExceptionIfClosed();
        return new RMQTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        LOGGER.log("createTextMessage", text);
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
        LOGGER.log("getTransacted");
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
        LOGGER.log("getAcknowledgeMode");
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws JMSException {
        LOGGER.log("commit");
        illegalStateExceptionIfClosed();
        if (!this.transacted) throw new IllegalStateException("Session is not transacted");

        try {
            //call commit on the channel
            //this should ACK all messages
            //TODO: This does not ACK all messages (received) -- correct this?
            this.channel.txCommit();
        } catch (Exception x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws JMSException {
        LOGGER.log("rollback");
        illegalStateExceptionIfClosed();
        if (!this.transacted) throw new IllegalStateException("Session is not transacted");
        try {
            // rollback the RabbitMQ transaction which may cause some messages to become unacknowledged
            this.channel.txRollback();
            // requeue all unacknowledged messages (not automatically done by RabbitMQ)
            this.channel.basicRecover(true); // requeue
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws JMSException {
        LOGGER.log("close");
        this.getConnection().sessionClose(this);
    }

    void internalClose() throws JMSException {
        LOGGER.log("internal:internalClose");
        if (this.closed) return;

        synchronized (this.closeLock) {
            try {
                //start by rolling back anything not committed already
                if (this.getTransactedNoException()) {
                    this.rollback();
                }
                //close all consumers created by this session
                for (RMQMessageConsumer consumer : this.consumers) {
                    try {
                        consumer.internalClose();
                    } catch (JMSException x) {
                        x.printStackTrace(); //TODO logging implementation
                    }
                }
                this.consumers.clear();

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

    private void closeRabbitChannels() throws JMSException {
        LOGGER.log("closeRabbitChannels", "close:", this.channel);
        this.unsetSacrificialChannel(); // does not throw exceptions
        if (this.channel==null) return;
        try {
            this.channel.close();
        } catch (ShutdownSignalException x) {
            // nothing to do
        } catch (IOException x) {
            if (!(x.getCause() instanceof ShutdownSignalException)) {
                throw new RMQJMSException(x);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() throws JMSException {
        LOGGER.log("recover");
        illegalStateExceptionIfClosed();
        if (getTransactedNoException()) {
            throw new javax.jms.IllegalStateException("Session is transacted.");
        } else {
            synchronized (this.unackedMessageTags) {
                /* If we have messages to recover */
                if (!this.unackedMessageTags.isEmpty()) {
                    try {
                        this.channel.basicRecover(true); // requeue
                    }catch (IOException x) {
                        LOGGER.log("recover", x, "basicRecover");
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
        LOGGER.log("getMessageListener");
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
        LOGGER.log("createProducer", destination);
        illegalStateExceptionIfClosed();
        RMQDestination dest = (RMQDestination) destination;
        declareDestinationIfNecessary(dest);
        RMQMessageProducer producer = new RMQMessageProducer(this, dest);
        this.producers.add(producer);
        return producer;
    }

    void declareDestinationIfNecessary(RMQDestination destination) throws JMSException {
        if (destination != null && !destination.isDeclared()) {
            if (destination.isQueue()) {
                declareQueue(destination, null, false);
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
        LOGGER.log("createConsumer");
        illegalStateExceptionIfClosed();
        return createConsumerInternal((RMQDestination) destination, null, false, null);
    }

    /**
     * Creates a consumer for a destination. If this is a topic, we can specify the autoDelete flag.
     * @param dest internal destination object
     * @param uuidTag only used for topics, if null, one is generated as the queue name for this topic
     * @param durableSubscriber true if this is a durable topic subscription
     * @param jmsSelector selector expression - null if no selection required
     * @return {@link #createConsumer(Destination)}
     * @throws JMSException if destination is null or we fail to create the destination on the broker
     * @see #createConsumer(Destination)
     */
    private RMQMessageConsumer createConsumerInternal(RMQDestination dest, String uuidTag, boolean durableSubscriber, String jmsSelector) throws JMSException {
        LOGGER.log("internal:createConsumerInternal");
        String consumerTag = uuidTag != null ? uuidTag : Util.generateUUID("jms-topic-");

        declareDestinationIfNecessary(dest);

        if (!dest.isQueue()) {
            // This is a topic, we need to define a queue, and bind to it.
            // The queue name is a unique ID for each consumer.
            String queueName = consumerTag;
            try {
                // we never set auto delete; exclusive queues (used for topics and temporaries) are deleted on close anyway.
                this.declareQueue(dest, queueName, durableSubscriber);
                if (jmsSelector==null) {
                    // bind the queue to the exchange with the correct routing key
                    this.channel.queueBind(queueName, dest.getExchangeInfo().name(), dest.getRoutingKey());
                } else {
                    // get this session's topic selector exchange (name)
                    String selectionExchange = this.getSelectionExchange(durableSubscriber);
                    // bind it to the topic exchange with the topic routing key
                    this.channel.exchangeBind(selectionExchange, dest.getExchangeInfo().name(), dest.getRoutingKey());
                    this.bindSelectorQueue(dest, jmsSelector, queueName, selectionExchange);
                }
            } catch (IOException x) {
                throw new RMQJMSException("RabbitMQ Exception creating Consumer", x);
            }
        }
        RMQMessageConsumer consumer = new RMQMessageConsumer(this, dest, consumerTag, getConnection().isStopped(), jmsSelector);
        this.consumers.add(consumer);
        return consumer;
    }

    private void bindSelectorQueue(RMQDestination dest, String jmsSelector, String queueName, String selectionExchange)
            throws InvalidSelectorException, IOException {
        Map<String, Object> args = Collections.singletonMap(RJMS_SELECTOR_ARG, (Object)jmsSelector);
        try {
            // get a channel specifically to bind the selector queue
            Channel channel = this.getSacrificialChannel();
            // bind the queue to the topic selector exchange with the jmsSelector expression as argument
            channel.queueBind(queueName, selectionExchange, dest.getRoutingKey(), args);
        } catch (IOException ioe) {
            this.unsetSacrificialChannel();
            // Channel was closed early; check what sort of error this is
            if (this.checkPreconditionFailure(ioe)) {
                this.unbindFailedSelectorQueue(dest, jmsSelector, queueName, selectionExchange, args);
                throw new RMQJMSSelectorException(ioe);
            } else {
                throw ioe;
            }
        }
    }

    private Channel getSacrificialChannel() throws IOException {
        synchronized (this.scLock) {
            if (this.sacrificialChannel == null) {
                this.sacrificialChannel = this.getConnection().createRabbitChannel();
            }
            return this.sacrificialChannel;
        }
    }

    private void unsetSacrificialChannel() {
        synchronized (this.scLock) {
            if (this.sacrificialChannel != null) {
                try {
                    if (this.sacrificialChannel.isOpen())
                        this.sacrificialChannel.close();
                } catch (IOException e) {
                    // ignore any failures
                }
            }
            this.sacrificialChannel = null;
        }
    }

    private void unbindFailedSelectorQueue(RMQDestination dest, String jmsSelector, String queueName, String selectionExchange, Map<String, Object> args) {
        try {
            // get a channel specifically to unbind the selector queue
            Channel channel = this.getSacrificialChannel();
            channel.queueUnbind(queueName, selectionExchange, dest.getRoutingKey(), args);
        } catch (IOException ioe) {
            // ignore
        }
    }

    private boolean checkPreconditionFailure(IOException ioe) {
        Throwable cause = ioe.getCause();
        if (cause instanceof ShutdownSignalException) {
            ShutdownSignalException sse = (ShutdownSignalException) cause;
            Object reason = sse.getReason();
            if (reason instanceof AMQCommand) {
                Method meth = ((AMQCommand) reason).getMethod();
                if (meth instanceof AMQImpl.Channel.Close) {
                    AMQImpl.Channel.Close close = (AMQImpl.Channel.Close) meth;
                    if (close.getReplyCode() == AMQImpl.PRECONDITION_FAILED)
                        return true;
                }
            }
        }
        return false;
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
        this.channel.exchangeDeclare(this.durableTopicSelectorExchange, RMQExchangeInfo.JMS_TOPIC_SELECTOR_EXCHANGE_TYPE, true, true, null);
        return this.durableTopicSelectorExchange;
    }

    private String getNonDurableTopicSelectorExchange() throws IOException {
        if (this.nonDurableTopicSelectorExchange==null) {
            this.nonDurableTopicSelectorExchange = Util.generateUUID("jms-ndtop-slx-");
        }
        this.channel.exchangeDeclare(this.nonDurableTopicSelectorExchange, RMQExchangeInfo.JMS_TOPIC_SELECTOR_EXCHANGE_TYPE, false, true, null);
        return this.nonDurableTopicSelectorExchange;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        LOGGER.log("createConsumer");
        illegalStateExceptionIfClosed();
        if (messageSelector==null || messageSelector.trim().length()==0) {
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
        LOGGER.log("createConsumer");
        illegalStateExceptionIfClosed();
        if (messageSelector==null || messageSelector.trim().length()==0) {
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
        LOGGER.log("createQueue", queueName);
        illegalStateExceptionIfClosed();
        RMQDestination dest = new RMQDestination(queueName, true, false);
        declareQueue(dest, null, false);
        return dest;
    }

    /**
     * Invokes {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)} to define a queue on the RabbitMQ broker
     * this method invokes {@link RMQDestination#setDeclared(boolean)} with a true value
     * @param dest - the Queue object
     * @param temporary true if the queue is temporary
     * @throws JMSException if an IOException occurs in the {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)} call
     */
    private void declareQueue(RMQDestination dest, String queueNameOverride, boolean durableSubscriber) throws JMSException {
        LOGGER.log("declareQueue", dest, queueNameOverride, durableSubscriber);
        try {
            String queueName = queueNameOverride!=null ? queueNameOverride : dest.getQueueName();

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

            HashMap<String,Object> options = null; //new HashMap<String,Object>();

            this.channel.queueDeclare(
            /* name of the queue */   queueName,
            /* temporary destinations are not durable will not survive a server restart all JMS queues except temp
             * survive restarts only durable topic queues */
                                      durable,
            /* exclusive flag */      exclusive,
            /* We don't set auto delete to true ever that is cause exclusive(rabbit)==true automatically get deleted
             * when a Connection is closed */
                                      false,
            /* Queue properties */    options);

            /* Temporary or 'topic queues' are exclusive and therefore get deleted by RabbitMQ on close */
        } catch (Exception x) {
            throw new RMQJMSException(x);
        }
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic createTopic(String topicName) throws JMSException {
        LOGGER.log("createTopic", topicName);
        illegalStateExceptionIfClosed();
        RMQDestination dest = new RMQDestination(topicName, false, false);
        declareTopic(dest);
        return dest;
    }

    /**
     * Declares the topic exchange (and queue) in RabbitMQ.
     * @param dest the topic destination
     * @throws JMSException
     */
    private void declareTopic(RMQDestination dest) throws JMSException {
        LOGGER.log("internal:declareTopic", dest);
        if (RMQExchangeInfo.RABBITMQ_AMQ_TOPIC_EXCHANGE_NAME.equals(dest.getExchangeInfo().name())) {
            /* built-in exchange -- do not redeclare */
        }
        else
            try {
                this.channel.exchangeDeclare(/* the name of the exchange */
                                             dest.getExchangeInfo().name(),
                                             /* the type of exchange to use */
                                             dest.getExchangeInfo().type(),
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
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        LOGGER.log("createDurableSubscriber");
        RMQMessageConsumer result = (RMQMessageConsumer)createDurableSubscriber(topic, name, null, false);
        result.setDurable(true);
        this.subscriptions.put(name, result);
        return result;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException selectors not yet implemented
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        LOGGER.log("createDurableSubscriber");
        illegalStateExceptionIfClosed();
        if (messageSelector!=null && messageSelector.trim().length()==0)
            messageSelector = null;

        RMQDestination topicDest = (RMQDestination) topic;
        RMQMessageConsumer previousConsumer = this.subscriptions.get(name);
        if (previousConsumer!=null) {
            // we are changing subscription, or not, if called with the same topic
            if (previousConsumer.getDestination().equals(topicDest)) {
                if (previousConsumer.isClosed()) {
                    // They called TopicSubscriber.close but didn't unsubscribe
                    // and they are simply resubscribing with a new one
                } else {
                    throw new JMSException("Subscription with name["+name+"] and topic["+topicDest+"] already exists");
                }
            } else {
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
     * TODO Implement browser support when RabbitMQ broker supports it
     */
    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        LOGGER.log("createBrowser");
        illegalStateExceptionIfClosed();
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        LOGGER.log("createBrowser");
        illegalStateExceptionIfClosed();
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        LOGGER.log("createTemporaryQueue");
        illegalStateExceptionIfClosed();
        RMQDestination result = new RMQDestination(Util.generateUUID("jms-temp-queue-"), true, true);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        LOGGER.log("createTemporaryTopic");
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
        LOGGER.log("unsubscribe", name);
        illegalStateExceptionIfClosed();
        try {
            if (name != null && this.subscriptions.remove(name) != null) {
                // remove the queue
                this.channel.queueDelete(name);
            }
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue) throws JMSException {
        LOGGER.log("createReceiver", queue);
        illegalStateExceptionIfClosed();
        return (QueueReceiver) this.createConsumer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException {
        LOGGER.log("createReceiver", queue, messageSelector);
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
        LOGGER.log("createSender", queue);
        illegalStateExceptionIfClosed();
        return (QueueSender) this.createProducer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {
        LOGGER.log("createSubscriber", topic);
        illegalStateExceptionIfClosed();
        return (TopicSubscriber) this.createConsumer(topic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException {
        LOGGER.log("createSubscriber", topic, messageSelector, noLocal);
        illegalStateExceptionIfClosed();

        if (messageSelector!=null && messageSelector.trim().length()==0) messageSelector = null;

        RMQMessageConsumer consumer = createConsumerInternal((RMQDestination) topic, null, false, messageSelector);
        consumer.setNoLocal(noLocal);
        return consumer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicPublisher createPublisher(Topic topic) throws JMSException {
        LOGGER.log("createPublisher", topic);
        illegalStateExceptionIfClosed();
        return (TopicPublisher) this.createProducer(topic);
    }

    /**
     * Returns the connection this session is associated with
     * @return
     */
    public RMQConnection getConnection() {
        LOGGER.log("internal:getConnection");
        return this.connection;
    }

    /**
     * Returns the {@link Channel} this session has created
     * @return
     */
    public Channel getChannel() {
//        LOGGER.log("internal:getChannel");
        return this.channel;
    }

    void consumerClose(RMQMessageConsumer consumer) throws JMSException {
        LOGGER.log("internal:consumerClose");
        if (this.consumers.remove(consumer)) {
            //TODO: if (consumer.isDurable()) { don't cancel it? cancel it? -- decide }
            consumer.internalClose();
        }
    }

    public void removeProducer(RMQMessageProducer producer) {
        LOGGER.log("internal:removeProducer", producer);
        if (this.producers.remove(producer)) {
            producer.internalClose();
        }
    }

    public boolean isAutoAck() {
        LOGGER.log("isAutoAck");
        return (getAcknowledgeModeNoException()==Session.AUTO_ACKNOWLEDGE) || (getAcknowledgeModeNoException()==Session.DUPS_OK_ACKNOWLEDGE);
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
        LOGGER.log("internal:pause");
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.pause();
            } catch (InterruptedException x) {
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
        LOGGER.log("internal:resume");
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.resume();
            } catch (IllegalStateException x) {
                throw new RMQJMSException(x);
            }
        }
    }

    void unackedMessageReceived(RMQMessage message) {
        LOGGER.log("internal:unackedMessageReceived", message);
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
         // TODO: future functionality, allow group ack prior to the current tag
         // TODO: future functionality, allow ack of single message
        if (!isAutoAck() && !getTransacted() && !this.unackedMessageTags.isEmpty()) {
            /*
             * Per JMS specification Message.acknowledge(), if we ack
             * the last message in a group, we will ack all the ones prior received.
             * Spec 11.2.21 says:
             * "Note that the acknowledge method of Message acknowledges all messages
             * received on that message's session."
             */
            synchronized (this.unackedMessageTags) {
                /*
                 * ACK all messages unacknowledged (ACKed or NACKed) on this session.
                 */
                try {
                    getChannel().basicAck(this.unackedMessageTags.last(), // we ack the highest tag
                                          true);                          // and everything prior to that
                    this.unackedMessageTags.clear();
                } catch (IOException x) {
                    throw new RMQJMSException(x);
                }
            }
        }
    }
}
