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
 * Implementation of a {@link Topic} and {@link Queue} This implementation is
 * serializable as it can be stored in a JNDI tree.
 */
public class RMQDestination implements Queue, Topic, Destination, Referenceable, Serializable, TemporaryQueue, TemporaryTopic {

    private static final long serialVersionUID = 596966152753718825L;
    private volatile String name;
    private volatile String exchangeName;
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
        if (queue) {
            String exchangeName = "", routingKey = name;
            init(name, exchangeName, routingKey, true, false, temporary);
        } else {
            String topicName = name, exchangeName = "topic." + topicName, routingKey = name;
            init(name, exchangeName, routingKey, false, false, temporary);
        }
    }

    /**
     * Creates a destination, either a queue or a topic, this method initializes
     * all the values appropriately
     * Unless used appropriately, this is an internal method.
     *
     * @param name - the name of the topic or the queue
     * @param exchangeName - the exchange we will publish to and bind queues to.
     *            If this is a queue, then set this value to an empty string
     *            <code>&quot;&quot;</code>. If this is a topic, the
     *            exchangeName should be <code>&quot;topic.&quot;+name</code>
     * @param routingKey - the routing key used for this destination. the
     *            routingKey should be the same value as the name parameter.
     * @param queue - true if this is a queue, false if this is a topic
     * @param declared - true if we have called
     *            {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)}
     *            or
     *            {@link Channel#exchangeDeclare(String, String, boolean, boolean, boolean, java.util.Map)
     *            to represent this queue/topic in the RabbitMQ broker. If
     *            creating a topic/queue to bind in JNDI, this value must be set
     *            to FALSE
     * @param temporary true if this is a temporary destination
     */
    protected void init(String name, String exchangeName, String routingKey, boolean queue, boolean declared, boolean temporary) {
        this.name = name;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.queue = queue;
        this.declared = declared;
        this.temporary = temporary;
    }

    /**
     * Returns the name of the queue/topic
     *
     * @return the name of the queue/topic
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the name of the RabbitMQ Exchange used to publish/send messages
     * to
     *
     * @return the name of the RabbitMQ Exchange used to publish/send messages
     *         to
     */
    public String getExchangeName() {
        return this.exchangeName;
    }

    /**
     * Returns the routingKey used to publish/send messages with
     *
     * @return the routingKey used to publish/send messages with
     */
    public String getRoutingKey() {
        return this.routingKey;
    }

    /**
     * Returns true if this is a queue, false if it is a topic
     *
     * @return true if this is a queue, false if it is a topic
     */
    public boolean isQueue() {
        return this.queue;
    }

    /**
     * Sets the name of the queue/topic - should only be used when binding into
     * JNDI
     *
     * @param name
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setName(String name) {
        if (isDeclared())
            throw new IllegalStateException();
        this.name = name;
    }

    /**
     * Sets the name of the exchange in the RabbitMQ broker - should only be
     * used when binding into JNDI
     *
     * @param exchangeName
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setExchangeName(String exchangeName) {
        if (isDeclared())
            throw new IllegalStateException();
        this.exchangeName = exchangeName;
    }

    /**
     * Sets the routing key when sending/receiving messages for this queue/topic
     * - should only be used when binding into JNDI
     *
     * @param routingKey
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
     * @param queue
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setQueue(boolean queue) {
        if (isDeclared())
            throw new IllegalStateException();
        this.queue = queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTopicName() throws JMSException {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueueName() throws JMSException {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getReference() throws NamingException {
        return new Reference(this.getClass().getCanonicalName());
    }

    /**
     * @return true if we have called
     *         {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)}
     *         or
     *         {@link Channel#exchangeDeclare(String, String, boolean, boolean, boolean, java.util.Map)
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
     * Returns true if this is a temporary destination
     * @return true if this is a temporary destination
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




}
