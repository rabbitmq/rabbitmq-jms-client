/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import javax.jms.QueueConnectionFactory;

import org.junit.Before;

public abstract class AbstractITQueueSSL extends AbstractITQueue {

    @Before
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getSslTestConnectionFactory()
                .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }
}
