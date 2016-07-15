/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.fail;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractITQueue {
    QueueConnectionFactory connFactory;
    protected QueueConnection queueConn;

    @Before
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }

    protected static void drainQueue(QueueSession session, Queue queue) throws Exception {
        QueueReceiver receiver = session.createReceiver(queue);
        int n=0;
        Message msg = receiver.receiveNoWait();
        while (msg != null) {
            ++n;
            msg = receiver.receiveNoWait();
        }
        if (n > 0) {
            System.out.println(">> INFO >> Drained messages (n=" + n + ") from queue " + queue + " prior to test.");
        }
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
