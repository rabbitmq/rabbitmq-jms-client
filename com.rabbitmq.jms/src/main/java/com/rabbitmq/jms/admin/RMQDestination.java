/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.client.RMQSession;

/**
 * Implementation of a {@link Topic} and {@link Queue}. This implementation is
 * serializable as it can be stored in any JNDI naming context.
 */
public class RMQDestination implements Queue, Topic, Destination, Referenceable, Serializable, TemporaryQueue, TemporaryTopic {

    private static final long serialVersionUID = 596966152753718825L;
    private volatile String name;
    private volatile RMQExchangeInfo exchangeInfo;
    private volatile String routingKey;
    private volatile boolean queue;
    private volatile boolean declared;
    private volatile boolean temporary;

    /**
     * Constructor used when object is deserialized using Java serialization
     */
    public RMQDestination() {
    }

    /**
     * Creates a destination in RabbitMQ
     * @param name the name of the topic or queue
     * @param queue true if this represent a queue
     * @param temporary true if this is a temporary destination
     */
    public RMQDestination(String name, boolean queue, boolean temporary) {
        this(name, queueOrTopicExchangeName(queue, temporary, name), queueOrTopicExchangeType(queue, name), name, queue, false, temporary);
    }

    private static final String queueOrTopicExchangeName(boolean queue, boolean temporary, String name) {
        if (queue)
            return ""; // default exchange in RabbitMQ (direct)
        else if (temporary)
            return RMQExchangeInfo.JMS_TEMP_TOPIC_EXCHANGE_NAME; // fixed topic exchange in RabbitMQ for jms traffic
        else
            return RMQExchangeInfo.JMS_DURABLE_TOPIC_EXCHANGE_NAME; // fixed topic exchange in RabbitMQ for jms traffic
    }

    private static final String queueOrTopicExchangeType(boolean queue, String name) {
        if (queue)
            return ""; // default exchange type in RabbitMQ (direct)
        else
            return "topic"; // standard topic exchange type in RabbitMQ
    }

    /**
     * Creates a destination, either a queue or a topic, initialising
     * all the values appropriately.
     *
     * @param name - the name of the topic or the queue
     * @param exchangeName - the RabbitMQ exchange name we will publish to and bind queues to.
     * @param exchangeType - the RabbitMQ type of exchange used (only used if it needs to be declared)
     * @param routingKey - the routing key used for this destination. the
     *            routingKey should be the same value as the name parameter.
     * @param queue - true if this is a queue, false if this is a topic
     * @param declared - <code>true</code> if we have called
     *            {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)}
     *            or
     *            {@link Channel#exchangeDeclare(String, String, boolean, boolean, boolean, java.util.Map)}
     *            to represent this queue/topic in the RabbitMQ broker. If
     *            creating a topic/queue to bind in JNDI, this value must be set
     *            to <code>false</code>.
     * @param temporary true if this is a temporary destination
     */
    private RMQDestination(String name, String exchangeName, String exchangeType, String routingKey, boolean queue, boolean declared, boolean temporary) {
        this.name = name;
        this.exchangeInfo = new RMQExchangeInfo(exchangeName, exchangeType);
        this.routingKey = routingKey;
        this.queue = queue;
        this.declared = declared;
        this.temporary = temporary;
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
        return this.queue;
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
     * @param queue <code>true</code> if this is a queue, <code>false</code> otherwise
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setQueue(boolean queue) {
        if (isDeclared())
            throw new IllegalStateException();
        this.queue = queue;
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
        return new Reference(this.getClass().getCanonicalName());
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
        return declared;
    }

    /**
     * Should only be used internally by {@link RMQSession}
     *
     * @param declared - set to true if the queue/topic has been defined in the
     *            RabbitMQ broker
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     * @see #isDeclared()
     */
    public void setDeclared(boolean declared) {
        this.declared = declared;
    }

    /**
     * @return <code>true</code> if this is a temporary destination, <code>false</code> otherwise
     */
    public boolean isTemporary() {
        return temporary;
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
