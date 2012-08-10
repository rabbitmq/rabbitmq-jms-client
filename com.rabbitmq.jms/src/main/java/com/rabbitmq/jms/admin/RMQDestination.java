package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

public class RMQDestination implements Queue, Topic, Destination, Referenceable, Serializable {

    /** TODO */
    private static final long serialVersionUID = 596966152753718825L;
    private volatile String name;
    private volatile String exchangeName;
    private volatile String routingKey;
    private volatile boolean queue;
    private volatile boolean declared;

    public RMQDestination() {
    }

    public RMQDestination(String name, String exchangeName, String routingKey, boolean queue, boolean declared) {
        this.name = name;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.queue = queue;
        this.declared = declared;
    }

    public String getName() {
        return this.name;
    }

    public String getExchangeName() {
        return this.exchangeName;
    }

    public String getRoutingKey() {
        return this.routingKey;
    }

    public boolean isQueue() {
        return this.queue;
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

    public boolean isDeclared() {
        return declared;
    }

    public void setDeclared(boolean declared) {
        this.declared = declared;
    }
    
    

}
