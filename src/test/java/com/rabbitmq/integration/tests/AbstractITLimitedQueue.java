/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import javax.jms.QueueConnectionFactory;

import org.junit.Before;

/** The setting queueBrowserReadMax is set to 5 on the connection in this test framework. */
public abstract class AbstractITLimitedQueue extends AbstractITQueue{
    protected final static int QBR_MAX = 5;

    @Before
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory(false, QBR_MAX)
                .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }
}
