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
 * Implementation of a {@link Topic} and {@link Queue}. This implementation is
 * serializable as it can be stored in any JNDI naming context.
 */
public class RMQDestination implements Queue, Topic, Destination, Referenceable, Serializable, TemporaryQueue, TemporaryTopic {

    private static final long serialVersionUID = 596966152753718825L;
    private volatile boolean amqp;
    private volatile String name;
    private volatile RMQExchangeInfo exchangeInfo;
    private volatile String routingKey;
    private volatile String amqpQueueName;
    private volatile boolean isQueue;
    private volatile boolean isDeclared;
    private volatile boolean isTemporary;

    /**
     * Constructor used when object is deserialized using Java serialization
     */
    public RMQDestination() {
    }

    /**
     * Creates a destination for RJMS
     * @param destName the name of the topic or queue
     * @param isQueue true if this represent a queue
     * @param isTemporary true if this is a temporary destination
     */
    public RMQDestination(String destName, boolean isQueue, boolean isTemporary) {
        this(destName, queueOrTopicExchangeName(isQueue, isTemporary, destName), queueOrTopicExchangeType(isQueue, destName), destName, isQueue, false, isTemporary);
    }

    private static final String queueOrTopicExchangeName(boolean isQueue, boolean isTemporary, String destName) {
        if (isQueue & isTemporary)
            return RMQExchangeInfo.JMS_TEMP_QUEUE_EXCHANGE_NAME; // fixed queue exchange in RabbitMQ for jms traffic
        else if (isQueue & !isTemporary)
            return RMQExchangeInfo.JMS_DURABLE_QUEUE_EXCHANGE_NAME; // fixed queue exchange in RabbitMQ for jms traffic
        else if (!isQueue & isTemporary)
            return RMQExchangeInfo.JMS_TEMP_TOPIC_EXCHANGE_NAME; // fixed topic exchange in RabbitMQ for jms traffic
        else // if (!isQueue & !isTemporary)
            return RMQExchangeInfo.JMS_DURABLE_TOPIC_EXCHANGE_NAME; // fixed topic exchange in RabbitMQ for jms traffic
    }

    private static final String queueOrTopicExchangeType(boolean isQueue, String destName) {
        if (isQueue)
            return "direct"; // standard direct exchange type in RabbitMQ
        else
            return "topic"; // standard topic exchange type in RabbitMQ
    }

    /**
     * Creates a destination, either a queue or a topic, either mapped to a real amqp resource or not.
     *
     * @param destName - the name of the topic or the queue
     * @param exchangeName - the RabbitMQ exchange name we will publish to and bind to (which may be an amqp resource exchange)
     * @param exchangeType - the RabbitMQ type of exchange used (only used if it needs to be declared),
     * @param routingKey - the routing key used for this destination.
     * @param isQueue - true if this is a queue, false if this is a topic
     * @param isDeclared - <code>true</code> if we have called
     *            {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)}
     *            or
     *            {@link Channel#exchangeDeclare(String, String, boolean, boolean, boolean, java.util.Map)}
     *            to represent this queue/topic in the RabbitMQ broker. If
     *            creating a topic/queue to bind in JNDI, this value must be set
     *            to <code>false</code>.
     * @param isTemporary true if this is a temporary destination
     */
    private RMQDestination(String destName, String exchangeName, String exchangeType, String routingKey, boolean isQueue, boolean isDeclared, boolean isTemporary) {
        this.name = destName;
        this.amqp = false;
        this.exchangeInfo = new RMQExchangeInfo(exchangeName, exchangeType);
        this.routingKey = routingKey;
        this.isQueue = isQueue;
        this.isDeclared = isDeclared;
        this.isTemporary = isTemporary;
    }

    /**
     * Creates a destination for RJMS mapped onto an AMQP queue/destination.
     * <p>
     * <code>amqpExchangeName</code> and <code>amqpRoutingKey</code> must both be <code>null</code> if either is <code>null</code>, and <code>amqpQueueName</code> may be <code>null</code>, but at
     * least one of these three parameters must be non-<code>null</code>.
     * </p>
     *
     * @param destName the name of the topic or queue
     * @param amqpExchangeName - the exchange name for the mapped resource
     * @param amqpRoutingKey - the routing key for the mapped resource
     * @param amqpQueueName - the queue name of the mapped resource
     */
    public RMQDestination(String destName, String amqpExchangeName, String amqpRoutingKey, String amqpQueueName) {
        this.name = destName;
        this.amqp = true;
        this.exchangeInfo = new RMQExchangeInfo(amqpExchangeName, null);
        this.amqpQueueName = amqpQueueName;
        this.routingKey = amqpRoutingKey;
        this.isQueue = true;
        this.isDeclared = false;
        this.isTemporary = false;
    }

    /**
     * @return the name of the queue/topic
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the RabbitMQ Exchange information used to publish/send messages
     */
    public RMQExchangeInfo getExchangeInfo() {
        return this.exchangeInfo;
    }

    /**
     * @return the routingKey used to publish/send messages with
     */
    public String getRoutingKey() {
        return this.routingKey;
    }

    /**
     * @return true if this is a queue, false if it is a topic
     */
    public boolean isQueue() {
        return this.isQueue;
    }

    /**
     * Sets the name of the queue/topic - should only be used when binding into
     * JNDI
     *
     * @param name queue name or topic name
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setName(String name) {
        if (isDeclared())
            throw new IllegalStateException();
        this.name = name;
    }

    /**
     * Sets the exchange used in the RabbitMQ broker - should only be
     * used when binding into JNDI
     *
     * @param exchangeInfo name and type of exchange
     * @throws IllegalStateException if the destination has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setExchangeInfo(RMQExchangeInfo exchangeInfo) {
        if (isDeclared())
            throw new IllegalStateException();
        this.exchangeInfo = exchangeInfo;
    }

    /**
     * Sets the routing key when sending/receiving messages for this queue/topic
     * - should only be used when binding into JNDI
     *
     * @param routingKey routing key to use
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setRoutingKey(String routingKey) {
        if (isDeclared())
            throw new IllegalStateException();
        this.routingKey = routingKey;
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
        return this.name;
    }

    @Override
    public String getQueueName() throws JMSException {
        return this.name;
    }

    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(this.getClass().getCanonicalName());
        addStringProperty(ref, "destinationName", this.name);
        addBooleanProperty(ref, "amqp", this.amqp);
        if (this.amqp) {
            addStringProperty(ref, "amqpExchangeName", this.exchangeInfo.name());
            addStringProperty(ref, "amqpRoutingKey", this.routingKey);
            addStringProperty(ref, "amqpQueueName", this.amqpQueueName);
        }
        return ref;
    }
    /**
     * Adds a String valued property to a Reference (as a RefAddr)
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
     * Adds a boolean valued property to a Reference (as a RefAddr)
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static final void addBooleanProperty(Reference ref,
                                                 String propertyName,
                                                 boolean value) {
        if (propertyName==null) return;
        RefAddr ra = new StringRefAddr(propertyName, String.valueOf(value));
        ref.add(ra);
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
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
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

    @Override
    public void delete() throws JMSException {
        //delete is deferred until Session.close happens
        //TODO implement Channel.queueDelete
        //See RMQSession.close how we call Channel.queueDelete
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RMQDestination{");
        sb.append("name='").append(name)
//          .append("', routingKey='").append(routingKey)
          ;
        return sb.append("'}").toString();
    }
}
