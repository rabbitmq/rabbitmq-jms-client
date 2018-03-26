/* Copyright (c) 2013-2018 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.junit.After;
import org.junit.Before;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public abstract class AbstractAmqpITQueue {
    private QueueConnectionFactory connFactory;
    protected QueueConnection queueConn;

    private ConnectionFactory rabbitConnFactory = new ConnectionFactory();
    protected Connection rabbitConn;
    protected Channel channel;

    @Before
    public void setUp() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        customise((RMQConnectionFactory) connFactory);
        this.queueConn = connFactory.createQueueConnection();

        this.rabbitConn = rabbitConnFactory.newConnection();
        this.channel = rabbitConn.createChannel();
    }

    protected void customise(RMQConnectionFactory connectionFactory) {

    }

    @After
    public void tearDown() throws Exception {
        if (this.queueConn != null) this.queueConn.close();
        if (this.channel != null) this.channel.close();
        if (this.rabbitConn != null) this.rabbitConn.close();
    }

}
