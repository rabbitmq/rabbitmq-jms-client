/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.fail;

import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractITQueue {
    protected QueueConnectionFactory connFactory;
    protected QueueConnection queueConn;

    @Before
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }

    protected void reconnect() throws Exception {
        if (queueConn != null) {
            this.queueConn.close();
            this.queueConn = connFactory.createQueueConnection();
        } else {
            fail("Cannot reconnect");
        }
    }

    @After
    public void afterTests() throws Exception {
        if (queueConn != null)
            queueConn.close();
    }
}
