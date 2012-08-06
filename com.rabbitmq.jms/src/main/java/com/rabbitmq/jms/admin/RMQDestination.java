package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.Assert;

/**
 * RabbitMQ implementation of JMS {@link Destination}
 */
public class RMQDestination implements Destination, Referenceable, Serializable, javax.jms.Queue {

    private volatile Queue queue;
    private RMQConnectionFactory factory;
    private RabbitTemplate template;
    private RabbitAdmin admin;

    public RMQDestination(RMQConnectionFactory factory, String name, boolean isQueue) {
        if (isQueue) {
            DirectExchange exchange = new DirectExchange(name);
            this.factory = factory;
            queue = new Queue(name);
            admin = new RabbitAdmin(factory.getAmqpFactory());
            admin.declareExchange(exchange);
            admin.declareQueue(queue);
            admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(name));
            template = new RabbitTemplate(factory.getAmqpFactory());
            template.setExchange(exchange.getName());
            template.setQueue(name);
            template.setRoutingKey(name);
        }
    }

    /** Default serializable uid */
    private static final long serialVersionUID = 1L;

    @Override
    public Reference getReference() throws NamingException {
        return new Reference(this.getClass().getCanonicalName());
    }

    @Override
    public String getQueueName() throws JMSException {
        Assert.notNull(queue, "This destination represents a topic.");
        return queue.getName();
    }

    public boolean isQueue() {
        return queue != null;
    }

    public Queue getQueue() {
        return queue;
    }

    public RMQConnectionFactory getFactory() {
        return factory;
    }

    public RabbitTemplate getTemplate() {
        return template;
    }

    public RabbitAdmin getAdmin() {
        return admin;
    }

}
