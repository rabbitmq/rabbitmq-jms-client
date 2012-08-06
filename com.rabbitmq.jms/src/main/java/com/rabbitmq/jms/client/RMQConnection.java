//
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License
// at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
// the License for the specific language governing rights and
// limitations under the License.
//
// The Original Code is RabbitMQ.
//
// The Initial Developer of the Original Code is VMware, Inc.
// Copyright (c) 2012 VMware, Inc. All rights reserved.
//
package com.rabbitmq.jms.client;

import java.io.IOException;

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
    
    public RMQConnection(com.rabbitmq.client.Connection rabbitConnection) {
        this.rabbitConnection = rabbitConnection;
    }
    
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getClientID() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        // TODO Auto-generated method stub
        return null;
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
    
    public com.rabbitmq.client.Connection getRabbitConnection() {
        return rabbitConnection;
    }

    @Override
    public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionConsumer
            createConnectionConsumer(Topic topic, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return new RMQSession(this, transacted, acknowledgeMode);
    }

    @Override
    public ConnectionConsumer
            createConnectionConsumer(Queue queue, String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }
    
    

}
