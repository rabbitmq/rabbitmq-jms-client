package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import com.rabbitmq.jms.util.Util;

/**
 *
 */
public class RMQConnection implements Connection, QueueConnection, TopicConnection {

    private final com.rabbitmq.client.Connection rabbitConnection;
    private static final ConnectionMetaData connectionMetaData = new RMQConnectionMetaData();
    private String clientID;
    private ExceptionListener exceptionListener;
    private final List<RMQSession> sessions = Collections.<RMQSession> synchronizedList(new ArrayList<RMQSession>());
    private volatile boolean closed = false;
    private final AtomicBoolean stopped = new AtomicBoolean(true);

    public RMQConnection(com.rabbitmq.client.Connection rabbitConnection) {
        this.rabbitConnection = rabbitConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        Util.util().checkClosed(closed, "Connection is closed.");
        RMQSession session = new RMQSession(this, transacted, acknowledgeMode);
        this.sessions.add(session);
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientID() throws JMSException {
        Util.util().checkClosed(closed, "Connection is closed.");
        return this.clientID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientID(String clientID) throws JMSException {
        Util.util().checkClosed(closed, "Connection is closed.");
        this.clientID = clientID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        Util.util().checkClosed(closed, "Connection is closed.");
        return connectionMetaData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        Util.util().checkClosed(closed, "Connection is closed.");
        return this.exceptionListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        Util.util().checkClosed(closed, "Connection is closed.");
        this.exceptionListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws JMSException {
        Util.util().checkClosed(closed, "Connection is closed.");
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
        Util.util().checkClosed(closed, "Connection is closed.");
        if (stopped.compareAndSet(false, true)) {
            for (RMQSession session : this.sessions) {
                session.pause();
            }
        }
    }

    /**
     * Returns true if this connection is in a stopped state
     * @return
     */
    public boolean isStopped() {
        return stopped.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws JMSException {
        if (closed) return;
        closed = true;
        try {
            this.rabbitConnection.close();
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    public com.rabbitmq.client.Connection getRabbitConnection() {
        return this.rabbitConnection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
        Util.util().checkClosed(closed, "Connection is closed.");
        return (TopicSession) this.createSession(transacted, acknowledgeMode);
    }

    /**
     * @throws UnsupportedOperationException - method not implemented
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
        Util.util().checkClosed(closed, "Connection is closed.");
        return (QueueSession) this.createSession(transacted, acknowledgeMode);
    }

    /**
     * @throws UnsupportedOperationException - method not implemented
     */
    @Override
    public ConnectionConsumer
    createConnectionConsumer(Queue queue, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException - method not implemented
     */
    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination,
                                                       String messageSelector,
                                                       ServerSessionPool sessionPool,
                                                       int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException - method not implemented
     */
    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic,
                                                              String subscriptionName,
                                                              String messageSelector,
                                                              ServerSessionPool sessionPool,
                                                              int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    /**
     * Internal methods. A connection must track all sessions
     * that are created, but when we call {@link RMQSession#close()}
     * we must unregister this session with the connection
     * This method is called by {@link RMQSession#close()}
     * and should not be called from anywhere else
     * @param session - the session that is being closed
     */
    protected void sessionClose(RMQSession session) {
        this.sessions.remove(session);
    }

}
