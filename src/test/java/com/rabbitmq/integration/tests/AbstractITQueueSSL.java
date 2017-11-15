/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import javax.jms.QueueConnectionFactory;

import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractITQueueSSL extends AbstractITQueue {

    @BeforeEach
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory(true, 0)
                .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }
}
