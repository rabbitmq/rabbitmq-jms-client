//
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License
// at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
// the License for the specific language governing rights and
// limitations under the License.
//
// The Original Code is RabbitMQ.
//
// The Initial Developer of the Original Code is VMware, Inc.
// Copyright (c) 2012 VMware, Inc. All rights reserved.
//
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
