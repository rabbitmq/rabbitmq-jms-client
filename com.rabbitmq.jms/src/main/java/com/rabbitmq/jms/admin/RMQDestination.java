package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

@SuppressWarnings("serial")
public class RMQDestination implements Queue, Topic, Destination, Referenceable, Serializable {

    private volatile String name;
    private volatile String exchangeName;
    private volatile String routingKey;
    private volatile boolean queue;

    public RMQDestination() {
    }

    public RMQDestination(String name, String exchangeName, String routingKey, boolean queue) {
        this.name = name;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.queue = queue;
    }

    public String getName() {
        return name;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public boolean isQueue() {
        return queue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public void setQueue(boolean queue) {
        this.queue = queue;
    }

    @Override
    public String getTopicName() throws JMSException {
        return name;
    }

    @Override
    public String getQueueName() throws JMSException {
        return name;
    }

    @Override
    public Reference getReference() throws NamingException {
        return new Reference(this.getClass().getCanonicalName());
    }

}
