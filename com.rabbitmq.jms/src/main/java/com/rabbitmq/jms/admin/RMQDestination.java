package com.rabbitmq.jms.admin;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import com.rabbitmq.jms.client.RMQSession;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ implementation of JMS {@link Destination}
 */
@SuppressWarnings("serial")
public class RMQDestination implements Queue, Destination, Referenceable, Serializable, Topic {

    private final RMQSession session;
    private final String name;
    private final String exchangeName;
    private final String routingKey;
    private final boolean queue;
    private final String consumerTag;

    public RMQDestination(RMQSession session, String name, boolean queue, boolean durable, boolean temporary) throws JMSException {
        super();
        this.session = session;
        this.name = name;
        this.queue = queue;
        exchangeName = "";
        routingKey = name;
        consumerTag = name + "." + System.identityHashCode(this);
        try {
            if (queue) {
                session.getChannel().queueDeclare(name, durable, temporary, !durable, new HashMap<String,Object>());
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
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


    @Override
    public Reference getReference() throws NamingException {
        return new Reference(this.getClass().getCanonicalName());
    }

    @Override
    public String getQueueName() throws JMSException {
        assert queue == true;
        return name;
    }
    
    

    @Override
    public String getTopicName() throws JMSException {
        assert queue == false;
        return name;
    }



    public RMQSession getSession() {
        return session;
    }
    
    

}
