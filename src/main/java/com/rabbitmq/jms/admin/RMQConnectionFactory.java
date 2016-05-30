/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import static com.rabbitmq.jms.util.UriCodec.encHost;
import static com.rabbitmq.jms.util.UriCodec.encSegment;
import static com.rabbitmq.jms.util.UriCodec.encUserinfo;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

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
import javax.net.ssl.SSLException;

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

    private static final int DEFAULT_RABBITMQ_SSL_PORT = com.rabbitmq.client.ConnectionFactory.DEFAULT_AMQP_OVER_SSL_PORT;

    private static final int DEFAULT_RABBITMQ_PORT = com.rabbitmq.client.ConnectionFactory.DEFAULT_AMQP_PORT;

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
    /** Default port NOT SET - determined by the type of connection (ssl or non-ssl) */
    private int port = -1;

    /** The maximum number of messages to read on a queue browser, which must be non-negative;
     *  0 means unlimited and is the default; negative values are interpreted as 0. */
    private int queueBrowserReadMax = Math.max(0, Integer.getInteger("rabbit.jms.queueBrowserReadMax", 0));

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
        setRabbitUri(logger, this, factory, this.getUri());
        com.rabbitmq.client.Connection rabbitConnection = getRabbitConnection(factory);

        RMQConnection conn = new RMQConnection(rabbitConnection, getTerminationTimeout(), getQueueBrowserReadMax());
        logger.debug("Connection {} created.", conn);
        return conn;
    }

    private com.rabbitmq.client.Connection getRabbitConnection(com.rabbitmq.client.ConnectionFactory factory) throws JMSException {
        try {
            return factory.newConnection();
        } catch (SSLException ssle) {
            throw new RMQJMSSecurityException("SSL Exception establishing RabbitMQ Connection", ssle);
        } catch (Exception x) {
            if (x instanceof IOException) {
                IOException ioe = (IOException) x;
                String msg = ioe.getMessage();
                if (msg!=null) {
                    if (msg.contains("authentication failure") || msg.contains("refused using authentication"))
                        throw new RMQJMSSecurityException(ioe);
                    else if (msg.contains("Connection refused"))
                        throw new RMQJMSException("RabbitMQ connection was refused. RabbitMQ broker may not be available.", ioe);
                }
                throw new RMQJMSException(ioe);
            } else if (x instanceof TimeoutException) {
                TimeoutException te = (TimeoutException) x;
                throw new RMQJMSException("Timed out establishing RabbitMQ Connection", te);
            } else {
                throw new RMQJMSException("Unexpected exception thrown by newConnection()", x);
            }
        }
    }

    /**
     * Returns the current factory connection parameters in a URI String.
     * @return URI for RabbitMQ connection (as a coded String)
     */
    public String getUri() {
        StringBuilder sb = new StringBuilder(scheme(isSsl())).append("://");
        sb.append(uriUInfoEscape(this.username, this.password)).append('@');
        sb.append(uriHostEscape(this.host)).append(':').append(this.getPort()).append("/");
        sb.append(uriVirtualHostEscape(this.virtualHost));

        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RMQConnectionFactory{");
        return (this.isSsl() ? sb.append("SSL, ") : sb)
        .append("user='").append(this.username)
        .append("', password").append(this.password!=null ? "=xxxxxxxx" : " not set")
        .append(", host='").append(this.host)
        .append("', port=").append(this.getPort())
        .append(", virtualHost='").append(this.virtualHost)
        .append("', queueBrowserReadMax=").append(this.queueBrowserReadMax)
        .append('}').toString();
    }

    /**
     * Set connection factory parameters by URI String.
     * @param uriString URI to use for instantiated connection
     * @throws JMSException
     */
    public void setUri(String uriString) throws JMSException {
        logger.trace("Set connection factory parameters by URI '{}'", uriString);
        // Create a temp factory and set the properties by uri
        com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
        setRabbitUri(logger, this, factory, uriString);
        // Now extract our properties from this factory, leaving the rest unchanged.
        this.host = factory.getHost();
        this.password = factory.getPassword();
        this.port = factory.getPort();
        this.ssl = factory.isSSL();
        this.username = factory.getUsername();
        this.virtualHost = factory.getVirtualHost();
    }

    private static final void setRabbitUri(Logger logger, RMQConnectionFactory rmqFactory, com.rabbitmq.client.ConnectionFactory factory, String uriString) throws RMQJMSException {
        if (uriString != null) { // we get the defaults if the uri is null
            try {
                factory.setUri(uriString);
            } catch (Exception e) {
                logger.error("Could not set URI on {}", rmqFactory, e);
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

    private static final String scheme(boolean isSsl) {
        return (isSsl ? "amqps" : "amqp");
    }

    private static final String uriUInfoEscape(String user, String pass) {
        if (null==user) return null;
        if (null==pass) return encUserinfo(user, "UTF-8");
        return encUserinfo(user + ":" + pass, "UTF-8");
    }

    private static final String uriHostEscape(String host) {
        return encHost(host, "UTF-8");
    }

    private static final String uriVirtualHostEscape(String vHost) {
        return encSegment(vHost, "UTF-8");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(RMQConnectionFactory.class.getName());
        addStringRefProperty(ref, "uri", this.getUri());
        addIntegerRefProperty(ref, "queueBrowserReadMax", this.getQueueBrowserReadMax());
        return ref;
    }

    /**
     * Adds a String valued property to a Reference (as a RefAddr)
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static final void addStringRefProperty(Reference ref,
                                                   String propertyName,
                                                   String value) {
        if (value==null || propertyName==null) return;
        RefAddr ra = new StringRefAddr(propertyName, value);
        ref.add(ra);
    }

    /**
     * Adds an integer valued property to a Reference (as a RefAddr).
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static final void addIntegerRefProperty(Reference ref,
                                                    String propertyName,
                                                    Integer value) {
        if (value==null || propertyName==null) return;
        RefAddr ra = new StringRefAddr(propertyName, String.valueOf(value));
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
     * @return a string representing the username for a RabbitMQ connection
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the <i>username</i> to be used when creating a connection to the RabbitMQ broker.
     * If the parameter is <code>null</code> the current <i>username</i> is not changed.
     *
     * @param username - username to be used when creating a connection to the RabbitMQ broker
     */
    public void setUsername(String username) {
        if (username != null) this.username = username;
        else this.logger.warn("Cannot set username to null (on {})", this);
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
     * Sets the <i>password</i> to be used when creating a connection to the RabbitMQ broker
     *
     * @param password - password to be used when creating a connection to the RabbitMQ broker
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the virtual host used when creating a connection.  If
     * {@link RMQConnectionFactory#setVirtualHost(String)} has not been called the default value of '/' is returned.
     *
     * @return a String representing the virtual host for a RabbitMQ connection
     */
    public String getVirtualHost() {
        return virtualHost;
    }

    /**
     * Sets the <i>virtualHost</i> to be used when creating a connection to the RabbitMQ broker.
     * If the parameter is <code>null</code> the current <i>virtualHost</i> is not changed.
     *
     * @param virtualHost - virtual host to be used when creating a connection to the RabbitMQ broker
     */
    public void setVirtualHost(String virtualHost) {
        if (virtualHost != null) this.virtualHost = virtualHost;
        else this.logger.warn("Cannot set virtualHost to null (on {})", this);
    }

    /**
     * Returns the host name to be used when creating a connection to the RabbitMQ broker.
     *
     * @return the host name of the RabbitMQ broker
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the <i>host</i> of the RabbitMQ broker. The host name can be an IP address or a host name.
     * If the parameter is <code>null</code> the current <i>host name</i> is not changed.
     *
     * @param host - IP address or a host name of the RabbitMQ broker, in String form
     */
    public void setHost(String host) {
        if (host != null) this.host = host;
        else this.logger.warn("Cannot set host to null (on {})", this);
    }

    /**
     * Returns the port the RabbitMQ broker listens to; this port is used to connect to the broker.
     * If the port has not been set (defaults to -1) then the default port for this type of connection is returned.
     *
     * @return the port the RabbitMQ broker listens to
     */
    public int getPort() {
        return this.port!=-1 ? this.port
             : isSsl() ? DEFAULT_RABBITMQ_SSL_PORT
             : DEFAULT_RABBITMQ_PORT;
    }

    /**
     * Set the <i>port</i> to be used when making a connection to the RabbitMQ broker.  This is the port number the broker will listen on.
     * Setting this to -1 means take the RabbitMQ default (which depends on the type of connection).
     *
     * @param port - a TCP port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the time to wait in milliseconds when {@link Connection#close()} has been called for listeners and threads to
     * complete.
     *
     * @return the duration in milliseconds for which the {@link Connection#close()} waits before continuing shutdown sequence
     */
    public long getTerminationTimeout() {
        return terminationTimeout;
    }

    /**
     * Sets <i>terminationTimeout</i>: the time in milliseconds a {@link Connection#close()} should wait for threads/tasks/listeners to complete
     *
     * @param terminationTimeout - duration in milliseconds
     */
    public void setTerminationTimeout(long terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
    }

    /**
     * Returns the maximum number of messages to read on a queue browser, or zero if there is no limit.
     *
     * @return the maximum number of messages to read on a queue browser
     */
    public int getQueueBrowserReadMax() {
        return this.queueBrowserReadMax;
    }

    /**
     * Sets <i>queueBrowserReadMax</i>: the maximum number of messages to read on a queue browser.
     * Non-positive values are set to zero, which is interpreted as no limit.
     *
     * @param queueBrowserReadMax - read no more than this number of messages on a queue browser.
     */
    public void setQueueBrowserReadMax(int queueBrowserReadMax) {
        this.queueBrowserReadMax = Math.max(0, queueBrowserReadMax);
    }
}
