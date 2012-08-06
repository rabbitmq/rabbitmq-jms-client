/**
 * The Initial Developer of the Original Code is VMware, Inc. Copyright (c) 2012
 * VMware, Inc. All rights reserved.
 */
package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import org.springframework.util.Assert;

import com.rabbitmq.jms.client.RMQConnection;

/**
 * RabbitMQ Implementation of JMS {@link javax.jms.ConnectionFactory}
 */
@SuppressWarnings("serial")
public class RMQConnectionFactory implements javax.jms.ConnectionFactory, javax.jms.QueueConnectionFactory, Referenceable, Serializable {

    private org.springframework.amqp.rabbit.connection.ConnectionFactory amqpFactory;

    public RMQConnectionFactory() {
    }

    public RMQConnectionFactory(org.springframework.amqp.rabbit.connection.ConnectionFactory amqpFactory) {
        this.amqpFactory = amqpFactory;
    }

    public org.springframework.amqp.rabbit.connection.ConnectionFactory getAmqpFactory() {
        return amqpFactory;
    }

    public void setAmqpFactory(org.springframework.amqp.rabbit.connection.ConnectionFactory amqpFactory) {
        this.amqpFactory = amqpFactory;
    }

    @Override
    public Connection createConnection() throws JMSException {
        Assert.notNull(amqpFactory);
        return new RMQConnection(this);
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        Assert.notNull(amqpFactory);
        // TODO implement username/password
        return new RMQConnection(this);

    }

    @Override
    public Reference getReference() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        Assert.notNull(amqpFactory);
        return new RMQConnection(this);
    }

    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        Assert.notNull(amqpFactory);
        // TODO implement username/password
        return new RMQConnection(this);
    }

}
