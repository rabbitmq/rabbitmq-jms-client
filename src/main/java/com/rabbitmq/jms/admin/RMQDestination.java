/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.client.RMQSession;

/**
 * Implementation of a {@link Topic} and {@link Queue} {@link Destination}.
 * <p>
 * This implementation is {@link Serializable} so it can be stored in a JNDI naming context. It is also
 * {@link Referenceable} for the same purpose.
 * </p>
 */
public class RMQDestination implements Queue, Topic, Destination, Referenceable, Serializable, TemporaryQueue, TemporaryTopic {

    private static final long serialVersionUID = 596966152753718825L;

    private static final String RABBITMQ_AMQ_TOPIC_EXCHANGE_NAME = "amq.topic";
    private static final String RABBITMQ_AMQ_TOPIC_EXCHANGE_TYPE = "topic";             // standard topic exchange type in RabbitMQ
    private static final String JMS_DURABLE_TOPIC_EXCHANGE_NAME = "jms.durable.topic";  // fixed topic exchange in RabbitMQ for jms traffic
    private static final String JMS_TEMP_TOPIC_EXCHANGE_NAME = "jms.temp.topic";        // fixed topic exchange in RabbitMQ for jms traffic

    private static final String RABBITMQ_AMQ_DIRECT_EXCHANGE_NAME = "amq.direct";
    private static final String RABBITMQ_AMQ_DIRECT_EXCHANGE_TYPE = "direct";           // standard direct exchange type in RabbitMQ
    private static final String JMS_DURABLE_QUEUE_EXCHANGE_NAME = "jms.durable.queues"; // fixed queue exchange in RabbitMQ for jms traffic
    private static final String JMS_TEMP_QUEUE_EXCHANGE_NAME = "jms.temp.queues";       // fixed queue exchange in RabbitMQ for jms traffic

    private volatile String destinationName;
    /** <code>true</code> if maps JMS destination to AMQP resource in RabbitMQ server */
    private volatile boolean amqp;
    private volatile String amqpExchangeName;
    private volatile String amqpExchangeType;
    private volatile String amqpRoutingKey;
    private volatile String amqpQueueName;
    private volatile boolean isQueue;
    private volatile boolean isDeclared;
    private volatile boolean isTemporary;

    /**
     * Constructor used for Java serialisation
     */
    public RMQDestination() {
    }

    /**
     * Creates a destination for RJMS
     * @param destinationName the name of the topic or queue
     * @param isQueue true if this represent a queue
     * @param isTemporary true if this is a temporary destination
     */
    public RMQDestination(String destName, boolean isQueue, boolean isTemporary) {
        this(destName, false, queueOrTopicExchangeName(isQueue, isTemporary, destName), queueOrTopicExchangeType(isQueue, destName), destName, destName, isQueue, isTemporary);
    }

    private static final String queueOrTopicExchangeName(boolean isQueue, boolean isTemporary, String destName) {
        if (isQueue & isTemporary)              return JMS_TEMP_QUEUE_EXCHANGE_NAME;
        else if (isQueue & !isTemporary)        return JMS_DURABLE_QUEUE_EXCHANGE_NAME;
        else if (!isQueue & isTemporary)        return JMS_TEMP_TOPIC_EXCHANGE_NAME;
        else /* if (!isQueue & !isTemporary) */ return JMS_DURABLE_TOPIC_EXCHANGE_NAME;
    }

    private static final String queueOrTopicExchangeType(boolean isQueue, String destName) {
        if (isQueue) return RABBITMQ_AMQ_DIRECT_EXCHANGE_TYPE;
        else         return RABBITMQ_AMQ_TOPIC_EXCHANGE_TYPE;
    }

    /**
     * Creates a destination for RJMS mapped onto an AMQP queue/destination.
     * <p>
     * <code>amqpExchangeName</code> and <code>amqpRoutingKey</code> must both be <code>null</code> if either is <code>null</code>, and <code>amqpQueueName</code> may be <code>null</code>, but at
     * least one of these three parameters must be non-<code>null</code>.
     * </p>
     *
     * @param destinationName the name of the topic or queue
     * @param amqpExchangeName - the exchange name for the mapped resource
     * @param amqpRoutingKey - the routing key for the mapped resource
     * @param amqpQueueName - the queue name of the mapped resource
     */
    public RMQDestination(String destName, String amqpExchangeName, String amqpRoutingKey, String amqpQueueName) {
        this(destName, true, amqpExchangeName, null, amqpRoutingKey, amqpQueueName, true, false);
    }

    /**
     * Creates a destination, either a queue or a topic; not mapped to a real amqp resource.
     *
     * @param destinationName - the name of the topic or the queue
     * @param amqp - <code>true</code> if this is bound to an amqp resource, <code>false</code> if it is a RJMS resource
     * @param exchangeName - the RabbitMQ exchange name we will publish to and bind to (which may be an amqp resource exchange)
     * @param exchangeType - the RabbitMQ type of exchange used (only used if it needs to be declared),
     * @param routingKey - the routing key used for this destination (if it is a topic)
     * @param isQueue - <code>true</code> if this is a queue, <code>false</code> if this is a topic
     * @param isTemporary true if this is a temporary destination
     */
    private RMQDestination(String destName, boolean amqp, String exchangeName, String exchangeType, String routingKey, String queueName, boolean isQueue, boolean isTemporary) {
        this.destinationName = destName;
        this.amqp = amqp;
        this.amqpExchangeName = exchangeName;
        this.amqpExchangeType = exchangeType;
        this.amqpRoutingKey = routingKey;
        this.amqpQueueName = queueName;
        this.isQueue = isQueue;
        this.isDeclared = false;
        this.isTemporary = isTemporary;
    }

    public boolean isAmqp() {
        return this.amqp;
    }
    public void setAmqp(boolean amqp) {
        if (this.isDeclared())
            throw new IllegalStateException();
        this.amqp = amqp;
    }
    public String getAmqpQueueName() {
        return this.amqpQueueName;
    }
    public void setAmqpQueueName(String amqpQueueName) {
        if (this.isDeclared())
            throw new IllegalStateException();
        this.amqpQueueName = amqpQueueName;
    }
    public String getAmqpExchangeName() {
        return this.amqpExchangeName;
    }
    public void setAmqpExchangeName(String amqpExchangeName) {
        if (this.isDeclared())
            throw new IllegalStateException();
        this.amqpExchangeName = amqpExchangeName;
    }
    public String getAmqpExchangeType() {
        return this.amqpExchangeType;
    }
    public void setAmqpExchangeType(String amqpExchangeType) {
        if (this.isDeclared())
            throw new IllegalStateException();
        this.amqpExchangeType = amqpExchangeType;
    }
    /**
     * @return the name of the queue/topic
     */
    public String getDestinationName() {
        return this.destinationName;
    }
    /**
     * Sets the name of the queue/topic - for binding into JNDI
     *
     * @param destinationName name of destination
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setDestinationName(String destinationName) {
        if (isDeclared())
            throw new IllegalStateException();
        this.destinationName = destinationName;
    }

    public boolean noNeedToDeclareExchange() {
        return RABBITMQ_AMQ_TOPIC_EXCHANGE_NAME .equals(this.amqpExchangeName)
            || RABBITMQ_AMQ_DIRECT_EXCHANGE_NAME.equals(this.amqpExchangeName);
    }

    /**
     * @return the amqpRoutingKey used to publish/send messages with
     */
    public String getAmqpRoutingKey() {
        return this.amqpRoutingKey;
    }

    /**
     * @return true if this is a queue, false if it is a topic
     */
    public boolean isQueue() {
        return this.isQueue;
    }

    /**
     * Sets the routing key when sending/receiving messages for this queue/topic
     * - should only be used when binding into JNDI
     *
     * @param amqpRoutingKey routing key to use
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setAmqpRoutingKey(String routingKey) {
        if (isDeclared())
            throw new IllegalStateException();
        this.amqpRoutingKey = routingKey;
    }

    /**
     * Set to true if this is a queue, false if this is a topic - should only be
     * used when binding into JNDI
     *
     * @param isQueue <code>true</code> if this is a queue, <code>false</code> otherwise
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setQueue(boolean isQueue) {
        if (isDeclared())
            throw new IllegalStateException();
        this.isQueue = isQueue;
    }

    @Override
    public String getTopicName() throws JMSException {
        return this.destinationName;
    }

    @Override
    public String getQueueName() throws JMSException {
        return this.destinationName;
    }

    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(this.getClass().getCanonicalName());
        addStringProperty(ref, "destinationName", this.destinationName);
        addBooleanProperty(ref, "amqp", this.amqp);
        if (this.amqp) {
            addStringProperty(ref, "amqpExchangeName", this.amqpExchangeName);
            addStringProperty(ref, "amqpRoutingKey", this.amqpRoutingKey);
            addStringProperty(ref, "amqpQueueName", this.amqpQueueName);
        }
        return ref;
    }

    /**
     * Adds a String valued property to a Reference (as a RefAddr) if it is non-<code>null</code>.
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static final void addStringProperty(Reference ref,
                                                String propertyName,
                                                String value) {
        if (value==null || propertyName==null) return;
        RefAddr ra = new StringRefAddr(propertyName, value);
        ref.add(ra);
    }

    /**
     * Adds a boolean valued property to a Reference (as a StringRefAddr) if the value is <code>true</code>
     * (default <code>false</code> on read assumed).
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static final void addBooleanProperty(Reference ref,
                                                 String propertyName,
                                                 boolean value) {
        if (propertyName==null) return;
        if (value) {
            RefAddr ra = new StringRefAddr(propertyName, String.valueOf(value));
            ref.add(ra);
        }
    }

    /**
     * @return true if we have called
     *         {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)}
     *         or
     *         {@link Channel#exchangeDeclare(String, String, boolean, boolean, boolean, java.util.Map)}
     *         to represent this queue/topic in the RabbitMQ broker. If creating
     *         a topic/queue to bind in JNDI, this value will be set to false
     *         until the queue/topic has been setup in the RabbitMQ broker
     */
    public boolean isDeclared() {
        return isDeclared;
    }

    /**
     * Should only be used internally by {@link RMQSession}
     *
     * @param isDeclared - set to true if the queue/topic has been defined in the
     *            RabbitMQ broker
     * @see #isDeclared()
     */
    public void setDeclared(boolean isDeclared) {
        this.isDeclared = isDeclared;
    }

    /**
     * @return <code>true</code> if this is a temporary destination, <code>false</code> otherwise
     */
    public boolean isTemporary() {
        return isTemporary;
    }

    /**
     * This method is for {@link TemporaryQueue}s only — deletion currently occurs automatically on {@link RMQSession#close}.
     * <p/>{@inheritDoc}
     */
    @Override
    public void delete() throws JMSException {
        //TODO implement delete by Channel.queueDelete for TemporaryQueues only
        //See RMQSession.close how we call Channel.queueDelete
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RMQDestination{");
        sb.append("destinationName='").append(destinationName)
//          .append("', amqpRoutingKey='").append(amqpRoutingKey)
          ;
        return sb.append("'}").toString();
    }
}
