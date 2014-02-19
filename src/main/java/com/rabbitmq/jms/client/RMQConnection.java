/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.util.RMQJMSException;

/**
 * Implementation of the {@link Connection}, {@link QueueConnection} and {@link TopicConnection} interfaces.
 * A {@link RMQConnection} object holds a list of {@link RMQSession} objects as well as the actual
 * {link com.rabbitmq.client.Connection} object that represents the TCP connection to the RabbitMQ broker. <br/>
 * This implementation also holds a reference to the executor service that is used by the connection so that we
 * can pause incoming messages.
 *
 */
public class RMQConnection implements Connection, QueueConnection, TopicConnection {

    private final Logger logger = LoggerFactory.getLogger(RMQConnection.class);;

    /** the TCP connection wrapper to the RabbitMQ broker */
    private final com.rabbitmq.client.Connection rabbitConnection;
    /** Hard coded connection meta data returned in the call {@link #getMetaData()} call */
    private static final ConnectionMetaData connectionMetaData = new RMQConnectionMetaData();
    /** The client ID for this connection */
    private String clientID;
    /** The exception listener - TODO implement usage of exception listener */
    private ExceptionListener exceptionListener;
    /** The list of all {@link RMQSession} objects created by this connection */
    private final List<RMQSession> sessions = Collections.<RMQSession> synchronizedList(new ArrayList<RMQSession>());
    /** value to see if this connection has been closed */
    private volatile boolean closed = false;
    /** atomic flag to pause and unpause the connection consumers (see {@link #start()} and {@link #stop()} methods) */
    private final AtomicBoolean stopped = new AtomicBoolean(true);

    /** maximum time (in ms) to wait for close() to complete */
    private final long terminationTimeout;

    private static ConcurrentHashMap<String, String> CLIENT_IDS = new ConcurrentHashMap<String, String>();

    /** List of all our durable subscriptions so we can track them on a per connection basis (maintained by sessions).*/
    private final Map<String, RMQMessageConsumer> subscriptions = new ConcurrentHashMap<String, RMQMessageConsumer>();

    /** This is used for JMSCTS test cases, as ClientID should only be configurable right after the connection has been created */
    private volatile boolean canSetClientID = true;

    /**
     * Creates an RMQConnection object.
     * @param rabbitConnection the TCP connection wrapper to the RabbitMQ broker
     * @param terminationTimeout timeout for close in milliseconds
     */
    public RMQConnection(com.rabbitmq.client.Connection rabbitConnection, long terminationTimeout) {
        this.rabbitConnection = rabbitConnection;
        this.terminationTimeout = terminationTimeout;
    }

    private static final long FIFTEEN_SECONDS_MS = 15000;
    /**
     * Creates an RMQConnection object, with default termination timeout of 15 seconds.
     * @param rabbitConnection the TCP connection wrapper to the RabbitMQ broker
     */
    public RMQConnection(com.rabbitmq.client.Connection rabbitConnection) {
        this(rabbitConnection, FIFTEEN_SECONDS_MS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        logger.trace("transacted={}, acknowledgeMode={}", transacted, acknowledgeMode);
        illegalStateExceptionIfClosed();
        freezeClientID();
        RMQSession session = new RMQSession(this, transacted, acknowledgeMode, this.subscriptions);
        this.sessions.add(session);
        return session;
    }

    private void freezeClientID() {
        this.canSetClientID = false;
    }

    private void illegalStateExceptionIfClosed() throws IllegalStateException {
        if (this.closed) throw new IllegalStateException("Connection is closed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientID() throws JMSException {
        illegalStateExceptionIfClosed();
        return this.clientID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientID(String clientID) throws JMSException {
        logger.trace("set ClientID to '{}'", clientID);
        illegalStateExceptionIfClosed();
        if (!canSetClientID) throw new IllegalStateException("Client ID can only be set right after connection creation");
        if (this.clientID==null) {
            if (CLIENT_IDS.putIfAbsent(clientID, clientID)==null) {
                this.clientID = clientID;
            } else {
                throw new InvalidClientIDException(String.format("A connection with client ID «%s» already exists.", clientID));
            }
        } else {
            throw new IllegalStateException("Client ID already set.");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        illegalStateExceptionIfClosed();
        freezeClientID();
        return connectionMetaData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        illegalStateExceptionIfClosed();
        freezeClientID();
        return this.exceptionListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        logger.trace("set ExceptionListener ({})", listener);
        illegalStateExceptionIfClosed();
        freezeClientID();
        this.exceptionListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws JMSException {
        logger.trace("starting connection ({})", this);
        illegalStateExceptionIfClosed();
        freezeClientID();
        if (stopped.compareAndSet(true, false)) {
            for (RMQSession session : this.sessions) {
                session.resume();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws JMSException {
        logger.trace("stopping connection ({})", this);
        illegalStateExceptionIfClosed();
        freezeClientID();
        if (stopped.compareAndSet(false, true)) {
            for (RMQSession session : this.sessions) {
                session.pause();
            }
        }
    }

    /**
     * @return <code>true</code> if this connection is in a stopped state
     */
    public boolean isStopped() {
        return stopped.get();
    }

    /**
     * From the JMS Spec:
     * <blockquote>
     * <p>This call blocks until a
     * receive or message listener in progress has completed. A blocked message consumer receive call returns null when
     * this message consumer is closed.</p>
     * </blockquote>
     * <p/>{@inheritDoc}
     */
    @Override
    public void close() throws JMSException {
        logger.trace("closing connection ({})", this);
        if (this.closed) return;

        String cID = getClientID();
        this.closed = true;

        if (cID != null)
            CLIENT_IDS.remove(cID);

        Exception sessionException = null;
        for (RMQSession session : this.sessions) {
            try {
                session.internalClose();
            } catch (Exception e) {
                if (null==sessionException) {
                    sessionException = e;
                    logger.error("exception closing session ({})", session, e);
                }
            }
        }
        this.sessions.clear();

        try {
            this.rabbitConnection.close();
        } catch (ShutdownSignalException x) {
            //nothing to do
        } catch (IOException x) {
            if (!(x.getCause() instanceof ShutdownSignalException)) {
                throw new RMQJMSException(x);
            }
        }
    }

    Channel createRabbitChannel(boolean transactional) throws IOException {
        Channel channel = this.rabbitConnection.createChannel();
        if (transactional) {
            channel.txSelect();
        }
        return channel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return (TopicSession) this.createSession(transacted, acknowledgeMode);
    }

    /**
     * @throws UnsupportedOperationException - optional method not implemented
     */
    @Override
    public ConnectionConsumer
            createConnectionConsumer(Topic topic, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return (QueueSession) this.createSession(transacted, acknowledgeMode);
    }

    /**
     * @throws UnsupportedOperationException - optional method not implemented
     */
    @Override
    public ConnectionConsumer createConnectionConsumer(Queue queue,
                                                       String messageSelector,
                                                       ServerSessionPool sessionPool,
                                                       int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException - optional method not implemented
     */
    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination,
                                                       String messageSelector,
                                                       ServerSessionPool sessionPool,
                                                       int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException - optional method not implemented
     */
    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic,
                                                              String subscriptionName,
                                                              String messageSelector,
                                                              ServerSessionPool sessionPool,
                                                              int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /* Internal methods. */

    /** A connection must track all sessions that are created,
     * but when we call {@link RMQSession#close()} we must unregister this
     * session with the connection.
     * This method is called by {@link RMQSession#close()} and should not be called from anywhere else.
     *
     * @param session - the session that is being closed
     */
    void sessionClose(RMQSession session) throws JMSException {
        logger.trace("internal:sessionClose({})", session);
        if (this.sessions.remove(session)) {
            session.internalClose();
        }
    }

    long getTerminationTimeout() {
        return this.terminationTimeout;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RMQConnection{");
        sb.append("rabbitConnection=").append(this.rabbitConnection);
        sb.append(", stopped=").append(this.stopped.get());
        return sb.append('}').toString();
    }
}
