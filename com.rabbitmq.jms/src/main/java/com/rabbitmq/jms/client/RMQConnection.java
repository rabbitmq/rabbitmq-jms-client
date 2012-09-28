package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.util.PausableExecutorService;
import com.rabbitmq.jms.util.Util;

/**
 * Implementation of the {@link Connection}, {@link QueueConnection} and {@link TopicConnection} interfaces.
 * A {@link RMQConnection} object holds a list of {@link RMQSession} objects as well as the actual
 * {link com.rabbitmq.client.Connection} object that represents the TCP connection to the RabbitMQ broker. <br/>
 * This implementation also holds a reference to the executor service that is used by the connection so that we
 * can pause incoming messages.
 *
 */
public class RMQConnection implements Connection, QueueConnection, TopicConnection {

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
    /** atomic flag to pause and unpause the connection by calling the {@link #start()} and {@link #stop()} methods */
    private final AtomicBoolean stopped = new AtomicBoolean(true);
    /** The thread pool that receives incoming messages */
    private final PausableExecutorService threadPool;

    private volatile long terminationTimeout = 15000;

    private static ConcurrentHashMap<String, String> CLIENT_IDS = new ConcurrentHashMap<String, String>();

    /**
     * List of all our durable subscriptions so we can track them on a per connection basis
     */
    private static final ConcurrentHashMap<String, RMQMessageConsumer> subscriptions = new ConcurrentHashMap<String, RMQMessageConsumer>();


    /** This is used for JMSCTS test cases, as ClientID should only be configurable right after the connection has been created */
    private volatile boolean canSetClientID = true;
    /**
     * Creates an RMQConnection object
     * @param threadPool the thread pool that was used to create the rabbit connection {@link com.rabbitmq.client.Connection} object
     * @param rabbitConnection the TCP connection wrapper to the RabbitMQ broker
     */
    public RMQConnection(PausableExecutorService threadPool, com.rabbitmq.client.Connection rabbitConnection) {
        this.rabbitConnection = rabbitConnection;
        this.threadPool = threadPool;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        canSetClientID = false;
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
        RMQSession session = new RMQSession(this, transacted, acknowledgeMode, subscriptions);
        this.sessions.add(session);
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientID() throws JMSException {
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
        return this.clientID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientID(String clientID) throws JMSException {
        Util.checkTrue(!canSetClientID, "Client ID can only be set right after connection creation", IllegalStateException.class);
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
        if (this.clientID==null) {
            if (CLIENT_IDS.putIfAbsent(clientID, clientID)==null) {
                this.clientID = clientID;
            } else {
                throw new InvalidClientIDException("A connection with that client ID alreayd exists.["+clientID+"]");
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
        canSetClientID = false;
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
        return connectionMetaData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        canSetClientID = false;
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
        return this.exceptionListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        canSetClientID = false;
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
        this.exceptionListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws JMSException {
        canSetClientID = false;
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
        if (stopped.compareAndSet(true, false)) {
            this.threadPool.resume();
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
        canSetClientID = false;
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
        if (stopped.compareAndSet(false, true)) {
            try {
                this.threadPool.pause();
            } catch (InterruptedException x) {
                stopped.set(false);
                throw Util.handleException(x);
            }
            for (RMQSession session : this.sessions) {
                session.pause();
            }
        }
    }

    /**
     * Returns true if this connection is in a stopped state
     *
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
        if (closed)
            return;
        String clientID = getClientID();
        closed = true;
        if (clientID!=null) CLIENT_IDS.remove(clientID);
        try {
            while (this.sessions.size()>0) {
                RMQSession session = sessions.get(0);
                session.close();
            }
            this.threadPool.shutdown();
            this.threadPool.awaitTermination(getTerminationTimeout(), TimeUnit.MILLISECONDS);
        } catch(InterruptedException x) {
            //do nothing - proceed
        }

        try {
            this.rabbitConnection.close();
        } catch (AlreadyClosedException x) {
            //nothing to do
        } catch (ShutdownSignalException x) {
            //nothing to do
        } catch (IOException x) {
            throw Util.handleException(x);
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
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
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
        Util.checkTrue(closed, "Connection is closed.", IllegalStateException.class);
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
     * Internal methods. A connection must track all sessions that are created,
     * but when we call {@link RMQSession#close()} we must unregister this
     * session with the connection This method is called by
     * {@link RMQSession#close()} and should not be called from anywhere else
     *
     * @param session - the session that is being closed
     */
    protected void sessionClose(RMQSession session) {
        this.sessions.remove(session);
    }

    public long getTerminationTimeout() {
        return terminationTimeout;
    }

    public void setTerminationTimeout(long terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
    }



}
