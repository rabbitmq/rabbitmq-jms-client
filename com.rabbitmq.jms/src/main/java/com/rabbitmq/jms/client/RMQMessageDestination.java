package com.rabbitmq.jms.client;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

@SuppressWarnings("serial")
public class RMQMessageDestination implements Queue, Topic, Destination, Referenceable, Serializable {

    private volatile String name;
    private volatile String exchangeName;
    private volatile String routingKey;
    private volatile boolean queue;
    private volatile String consumerTag;

    public RMQMessageDestination() {
    }
    
    public RMQMessageDestination(String name, String exchangeName, String routingKey, boolean queue, String consumerTag) {
        this.name = name;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.queue = queue;
        this.consumerTag = consumerTag;
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



    public String getConsumerTag() {
        return consumerTag;
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

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
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
