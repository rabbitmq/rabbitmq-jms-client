/**
 * The Initial Developer of the Original Code is VMware, Inc. Copyright (c) 2012
 * VMware, Inc. All rights reserved.
 */
package com.rabbitmq.jms.client;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

import junit.framework.Assert;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

@SuppressWarnings("serial")
public class RMQConnection implements Connection, javax.jms.QueueConnection {

    private RMQConnectionFactory factory;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private String clientID;

    public RMQConnection(RMQConnectionFactory factory) {
        super();
        this.factory = factory;
    }

    public RMQConnectionFactory getFactory() {
        return factory;
    }

    public void setFactory(RMQConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        // TODO Auto-generated method stub
        return null;
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
        return new RMQConnectionMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void start() throws JMSException {
        Assert.assertTrue(started.compareAndSet(false, true));

    }

    @Override
    public void stop() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination,
                                                       String messageSelector,
                                                       ServerSessionPool sessionPool,
                                                       int maxMessages) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic,
                                                              String subscriptionName,
                                                              String messageSelector,
                                                              ServerSessionPool sessionPool,
                                                              int maxMessages) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return new RMQSession(this, transacted, acknowledgeMode);
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Queue queue,
                                                       String messageSelector,
                                                       ServerSessionPool sessionPool,
                                                       int maxMessages) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

}
