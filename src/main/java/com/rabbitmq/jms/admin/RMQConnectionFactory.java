/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

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
 * TODO - implement SslContext option
 */
public class RMQConnectionFactory implements ConnectionFactory, Referenceable, Serializable, QueueConnectionFactory,
                                 TopicConnectionFactory {
    private final Logger logger = LoggerFactory.getLogger(RMQConnectionFactory.class);

    private static final long serialVersionUID = -4953157213762979615L;

    /** Default not to use ssl */
    private boolean ssl = false;
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
    public Connection createConnection(String username, String password) throws JMSException {
        logger.trace("Creating a connection for username '{}', password 'xxxxxxxx'.", username);
        this.username = username;
        this.password = password;
        // Create a new factory and set the properties
        com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
        resetSsl(factory);
        setRabbitUri(factory, this.getUri());
        com.rabbitmq.client.Connection rabbitConnection = getRabbitConnection(factory);

        RMQConnection conn = new RMQConnection(rabbitConnection, getTerminationTimeout());
        logger.debug("Connection {} created.", conn);
        return conn;
    }

    private com.rabbitmq.client.Connection getRabbitConnection(com.rabbitmq.client.ConnectionFactory factory) throws JMSException {
        try {
            return factory.newConnection();
        } catch (IOException x) {
            if (x.getMessage() != null) {
                if (x.getMessage().contains("authentication failure")) {
                    throw new RMQJMSSecurityException(x);
                } else if (x.getMessage().contains("Connection refused")) {
                    throw new RMQJMSException("Attempted to connect to RabbitMQ broker, but connection was refused. RabbitMQ broker may not be available.", x);
                } else {
                    throw new RMQJMSException(x);
                }

            } else {
                throw new RMQJMSException(x);
            }
        }
    }

    public URI getUri() {
        try {
            return new URI(scheme(isSsl()), uriUInfoEscape(this.username, this.password), this.host, this.port, uriEncodeVirtualHost(this.virtualHost), null, null);
        } catch (URISyntaxException use) {
            this.logger.error("Exception creating URI from {}", this, use);
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RMQConnectionFactory{");
        return (this.isSsl() ? sb.append("SSL, ") : sb)
        .append("user='").append(this.username)
        .append("', password").append(this.password!=null ? "=xxxxxxxx" : " not set")
        .append(", host='").append(this.host)
        .append("', port=").append(this.port)
        .append(", virtualHost='").append(this.virtualHost)
        .append("'}").toString();
    }

    public void setUri(String uriString) throws JMSException {
        logger.trace("Set connection factory parameters by URI '{}'", uriString);
        // Create a temp factory and set the properties by uri
        com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
        setRabbitUri(factory, createAmqpUri(uriString));
        // Now extract our properties from this factory, leaving the rest unchanged.
        this.host = factory.getHost();
        this.password = factory.getPassword();
        this.port = factory.getPort();
        this.ssl = factory.isSSL();
        this.username = factory.getUsername();
        this.virtualHost = factory.getVirtualHost();
    }

    private void setRabbitUri(com.rabbitmq.client.ConnectionFactory factory, URI uri) throws RMQJMSException {
        if (uri != null) { // we get the defaults if the uri is null
            try {
                factory.setUri(uri);
            } catch (Exception e) {
                this.logger.error("Could not set URI on {}", this, e);
                throw new RMQJMSException("Could not set URI on RabbitMQ connection factory.", e);
            }
        }
    }

    private void resetSsl(com.rabbitmq.client.ConnectionFactory factory) {
        if (this.ssl)
        try {
            factory.useSslProtocol();
        } catch (Exception e1) {
            this.logger.warn("Could not set SSL protocol on connection factory, {}. SSL set off.", this, e1);
            this.ssl=false;
        }
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    private final URI createAmqpUri(String uriString) {
        try {
            return new URI(uriString);
        } catch (URISyntaxException use) {
            this.logger.debug("Failed creating URI for RabbitMQ ConnectionFactory with string '{}'", uriString);
        }
        return null;
    }

    private static final String scheme(boolean isSsl) {
        return (isSsl ? "amqps" : "amqp");
    }

    private static final String uriEncodeVirtualHost(String virtualHost) {
        if (virtualHost==null) return null;
        return "/" + virtualHost.replace("/", "%2F"); // avoid embedded slashes
    }

    private static final String uriUInfoEscape(String user, String pass) {
        if (null==user) return null;
        if (null==pass) return uriStrEscape(user);
        return uriStrEscape(user) + ":" + uriStrEscape(pass);
    }

    private static final String uriStrEscape(String str) {
        return str.replace(":", "%3A"); // quoted colon
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(RMQConnectionFactory.class.getName());
        addStringProperty(ref, "uri", this.getUri().toString());
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

//    /**
//     * Adds a boolean valued property to a Reference (as a RefAddr)
//     * @param ref - the reference to contain the value
//     * @param propertyName - the name of the property
//     * @param value - the value to store with the property
//     */
//    private static final void addBooleanProperty(Reference ref,
//                                                 String propertyName,
//                                                 boolean value) {
//        if (propertyName==null) return;
//        RefAddr ra = new StringRefAddr(propertyName, String.valueOf(value));
//        ref.add(ra);
//    }

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
