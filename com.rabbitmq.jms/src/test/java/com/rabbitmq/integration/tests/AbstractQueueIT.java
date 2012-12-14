package com.rabbitmq.integration.tests;

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractQueueIT {
    protected QueueConnectionFactory connFactory;
    protected QueueConnection queueConn;

    @Before
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }

    protected void reconnect() throws Exception {
        this.queueConn.close();
        this.queueConn = connFactory.createQueueConnection();
    }

    @After
    public void afterTests() throws Exception {
        queueConn.close();
    }

}
