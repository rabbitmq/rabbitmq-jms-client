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
import com.rabbitmq.jms.util.PausableExecutorService;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ Implementation of JMS {@link ConnectionFactory}
 * TODO - implement socket and SSL options
 */
public class RMQConnectionFactory implements ConnectionFactory, Referenceable, Serializable, QueueConnectionFactory, TopicConnectionFactory {

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
    /** Number of threads each connection executor will have */
    private int threadsPerConnection = 2;
    /** The time to wait for threads/messages to terminate during {@link Connection#close()} */
    private volatile long terminationTimeout = Long.getLong("rabbit.jms.terminationTimeout",15000);
    /**
     * The thread prefix for threads created by the executor
     * If this is null, the value
     * <code>&quot;Rabbit JMS Connection[&quot;+rabbitConnection.getAddress()+&quot;]-&quot;</code>
     * will be used
     */
    private String threadPrefix = null;

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
        //Create a new factory and set the properties
        com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost(getVirtualHost());
        factory.setHost(getHost());
        factory.setPort(getPort());
        com.rabbitmq.client.Connection rabbitConnection = null;
        //Initialize the executor
        PausableExecutorService es = new PausableExecutorService(getThreadsPerConnection(), true);
        try {
            rabbitConnection = factory.newConnection(es);
        } catch (IOException x) {
            if (x.getMessage()!=null && x.getMessage().indexOf("authentication failure")>=0) {
                throw Util.handleSecurityException(x);
            } else {
                throw Util.handleException(x);
            }
        }
        //make sure the threads have a identifiable name
        if (getThreadPrefix()!=null) {
            es.setServiceId(getThreadPrefix());
        } else {
            es.setServiceId("Rabbit JMS Connection["+rabbitConnection.getAddress()+"]-");
        }
        RMQConnection conn = new RMQConnection(es, rabbitConnection);
        conn.setTerminationTimeout(getTerminationTimeout());
        return conn;
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

    /**
     * Returns the number of threads that are configured for each connection.
     * @return the number of threads that are used for each connection, default is 2
     */
    public int getThreadsPerConnection() {
        return threadsPerConnection;
    }

    /**
     * Configures how many threads should be used to receive messages for each TCP
     * connection that is established.
     * @param threadsPerConnection the number of threads for each executor service to handle a TCP connection
     */
    public void setThreadsPerConnection(int threadsPerConnection) {
        this.threadsPerConnection = threadsPerConnection;
    }

    /**
     * Returns the thread prefix used when creating threads in the executor
     * @return the thread prefix used
     */
    public String getThreadPrefix() {
        return threadPrefix;
    }

    /**
     * Sets the thread prefix to be used when threads are created in this system.
     * If this is null, a default prefix will be created
     * @param threadPrefix the prefix such as &quot;Rabbit JMS Thread #&quot;
     */
    public void setThreadPrefix(String threadPrefix) {
        this.threadPrefix = threadPrefix;
    }

    /**
     * The time to wait in milliseconds when {@link Connection#close()} has
     * been called for listeners and threads to complete.
     * @return the time in milliseconds the {@link Connection#close()} waits before continuing shutdown sequence
     */
    public long getTerminationTimeout() {
        return terminationTimeout;
    }

    /**
     * Sets the time in milliseconds a {@link Connection#close()} should wait for threads/tasks/listeners to complete
     * @param terminationTimeout time in milliseconds
     */
    public void setTerminationTimeout(long terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
    }

}
