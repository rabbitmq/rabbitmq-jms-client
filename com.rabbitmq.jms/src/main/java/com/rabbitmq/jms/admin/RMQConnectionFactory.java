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
import javax.naming.Reference;
import javax.naming.Referenceable;

import com.rabbitmq.jms.client.RMQConnection;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ Implementation of JMS {@link ConnectionFactory}
 */
public class RMQConnectionFactory implements ConnectionFactory, Referenceable, Serializable, QueueConnectionFactory, TopicConnectionFactory {

    /** TODO */
    private static final long serialVersionUID = -4953157213762979615L;

    private String username = "guest";
    private String password = "guest";
    private String virtualHost = "/";
    private String host = "localhost";
    private int port = 5672;

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
            Util.util().handleException(x);
        }
        return new RMQConnection(rabbitConnection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getReference() throws NamingException {
        return new Reference(RMQConnectionFactory.class.getName());
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
     * {@link RMQConnectionFactory#setUsername(String)} has not been called the
     * default value of 'guest' is returned.
     * 
     * @return a string representing the username for a Rabbit connection
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username to be used when creating a connection to the RabbitMQ
     * broker
     * 
     * @param username - username to be used when creating a connection to the
     *            RabbitMQ broker
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the configured password used when creating a connection If
     * {@link RMQConnectionFactory#setPassword(String)} has not been called the
     * default value of 'guest' is returned.
     * 
     * @return a string representing the password for a Rabbit connection
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password to be used when creating a connection to the RabbitMQ
     * broker
     * 
     * @param password - password to be used when creating a connection to the
     *            RabbitMQ broker
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the configured virtual host used when creating a connection If
     * {@link RMQConnectionFactory#setVirtualHost(String)} has not been called the
     * default value of '/' is returned.
     * 
     * @return a string representing the virtual host for a Rabbit connection
     */
    public String getVirtualHost() {
        return virtualHost;
    }

    /**
     * Sets the virtual host to be used when creating a connection to the RabbitMQ
     * broker
     * 
     * @param virtualHost - virtual host to be used when creating a connection to the
     *            RabbitMQ broker
     */
    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    /**
     * Returns the host name of the RabbitMQ broker
     * Host name can be in form of an IP address or a host name
     * @return the host name of the RabbitMQ broker
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host name of the RabbitMQ broker
     * The host name can be an IP address or a host name
     * @param host ip address or a host name of the RabbitMQ broker
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the port the RabbitMQ broker listens to
     * This port is used to connect to the broker
     * @return the port the RabbitMQ broker listens to
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port that the RabbitMQ broker listens to
     * This port is used to connect to the broker
     * @param port a TCP port of the RabbitMQ broker
     */
    public void setPort(int port) {
        this.port = port;
    }

}
