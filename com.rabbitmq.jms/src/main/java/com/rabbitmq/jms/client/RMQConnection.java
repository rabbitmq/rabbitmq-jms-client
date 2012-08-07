package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private List<RMQSession> sessions = Collections.<RMQSession>synchronizedList(new ArrayList<RMQSession>());

    public RMQConnection(com.rabbitmq.client.Connection rabbitConnection) {
        this.rabbitConnection = rabbitConnection;
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        RMQSession session = new RMQSession(this, transacted, acknowledgeMode);
        sessions.add(session);
        return session;
    }

    @Override
    public String getClientID() throws JMSException {
        return clientID;
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        this.clientID = clientID;
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return connectionMetaData;
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return exceptionListener;
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        this.exceptionListener = listener;
    }

    @Override
    public void start() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws JMSException {
        try {
            rabbitConnection.close();
        } catch (IOException x) {
            Util.util().handleException(x);
        }

    }

    public com.rabbitmq.client.Connection getRabbitConnection() {
        return rabbitConnection;
    }

    @Override
    public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return (TopicSession) createSession(transacted, acknowledgeMode);
    }

    @Override
    public ConnectionConsumer
            createConnectionConsumer(Topic topic, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return (QueueSession) createSession(transacted, acknowledgeMode);
    }

    @Override
    public ConnectionConsumer
            createConnectionConsumer(Queue queue, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination,
                                                       String messageSelector,
                                                       ServerSessionPool sessionPool,
                                                       int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic,
                                                              String subscriptionName,
                                                              String messageSelector,
                                                              ServerSessionPool sessionPool,
                                                              int maxMessages) throws JMSException {
        throw new UnsupportedOperationException();
    }
    
    public void sessionClose(RMQSession session) {
        sessions.remove(session);
    }

}
