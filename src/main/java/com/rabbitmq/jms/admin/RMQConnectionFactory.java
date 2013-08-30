/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.io.IOException;
import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.jms.client.RMQConnection;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.RMQJMSSecurityException;

/**
 * RabbitMQ Implementation of JMS {@link ConnectionFactory}
 * TODO - implement socket and SSL options
 */
public class RMQConnectionFactory implements ConnectionFactory, Referenceable, Serializable, QueueConnectionFactory,
                                 TopicConnectionFactory {
    private final Logger logger = LoggerFactory.getLogger(RMQConnectionFactory.class);

    private static final long serialVersionUID = -4953157213762979615L;

    /** Default username to RabbitMQ broker */
    private String username = "guest";
    /** Default password to RabbitMQ broker */
    private String password = "guest";
    /** Default virtualhost */
    private String virtualHost = "/";
    /** Default host to RabbitMQ broker */
    private String host = "localhost";
    /** Default port to RabbitMQ broker */
    private int port = 5672;
    /** The time to wait for threads/messages to terminate during {@link Connection#close()} */
    private volatile long terminationTimeout = Long.getLong("rabbit.jms.terminationTimeout", 15000);

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection createConnection() throws JMSException {
        return this.createConnection(username, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        logger.trace("Creating a connection for username '{}', password 'xxxxxxxx'.", userName);
        // Create a new factory and set the properties
        com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost(getVirtualHost());
        factory.setHost(getHost());
        factory.setPort(getPort());
        com.rabbitmq.client.Connection rabbitConnection = null;
        try {
            rabbitConnection = factory.newConnection();
        } catch (IOException x) {
            if (x.getMessage() != null) {
                if (x.getMessage().contains("authentication failure")) {
                    throw new RMQJMSSecurityException(x);
                } else if (x.getMessage().contains("Connection refused")) {
                    throw new RMQJMSException("Attempted to connect to RabbitMQ broker, but connection was refused. Is RabbitMQ running?", x);
                } else {
                    throw new RMQJMSException(x);
                }

            } else {
                throw new RMQJMSException(x);
            }
        }

        RMQConnection conn = new RMQConnection(rabbitConnection, getTerminationTimeout());
        logger.debug("Connection {} created.", conn);
        return conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(RMQConnectionFactory.class.getName());
        addStringProperty(ref, "username", this.username);
        addStringProperty(ref, "password", this.password);
        addStringProperty(ref, "virtualHost", this.virtualHost);
        addStringProperty(ref, "host", this.host);
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
     * {@inheritDoc}
     */
    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        return (TopicConnection) this.createConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        return (TopicConnection) this.createConnection(userName, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        return (QueueConnection) this.createConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        return (QueueConnection) this.createConnection(userName, password);
    }

    /**
     * Returns the configured username used when creating a connection If
     * {@link RMQConnectionFactory#setUsername(String)} has not been called the default value of 'guest' is returned.
     *
     * @return a string representing the username for a Rabbit connection
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username to be used when creating a connection to the RabbitMQ broker
     *
     * @param username - username to be used when creating a connection to the RabbitMQ broker
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the configured password used when creating a connection If
     * {@link RMQConnectionFactory#setPassword(String)} has not been called the default value of 'guest' is returned.
     *
     * @return a string representing the password for a Rabbit connection
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password to be used when creating a connection to the RabbitMQ broker
     *
     * @param password - password to be used when creating a connection to the RabbitMQ broker
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the configured virtual host used when creating a connection If
     * {@link RMQConnectionFactory#setVirtualHost(String)} has not been called the default value of '/' is returned.
     *
     * @return a string representing the virtual host for a Rabbit connection
     */
    public String getVirtualHost() {
        return virtualHost;
    }

    /**
     * Sets the virtual host to be used when creating a connection to the RabbitMQ broker
     *
     * @param virtualHost - virtual host to be used when creating a connection to the RabbitMQ broker
     */
    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    /**
     * Returns the host name of the RabbitMQ broker Host name can be in form of an IP address or a host name
     *
     * @return the host name of the RabbitMQ broker
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host name of the RabbitMQ broker The host name can be an IP address or a host name
     *
     * @param host ip address or a host name of the RabbitMQ broker
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the port the RabbitMQ broker listens to; this port is used to connect to the broker.
     *
     * @return the port the RabbitMQ broker listens to
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port that the RabbitMQ broker listens to; this port is used to connect to the broker.
     *
     * @param port a TCP port of the RabbitMQ broker
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * The time to wait in milliseconds when {@link Connection#close()} has been called for listeners and threads to
     * complete.
     *
     * @return the time in milliseconds the {@link Connection#close()} waits before continuing shutdown sequence
     */
    public long getTerminationTimeout() {
        return terminationTimeout;
    }

    /**
     * Sets the time in milliseconds a {@link Connection#close()} should wait for threads/tasks/listeners to complete
     *
     * @param terminationTimeout time in milliseconds
     */
    public void setTerminationTimeout(long terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
    }

}
