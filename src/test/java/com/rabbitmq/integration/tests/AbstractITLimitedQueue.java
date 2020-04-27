/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

import javax.jms.QueueConnectionFactory;

import org.junit.jupiter.api.BeforeEach;

/** The setting queueBrowserReadMax is set to 5 on the connection in this test framework. */
public abstract class AbstractITLimitedQueue extends AbstractITQueue{
    protected final static int QBR_MAX = 5;

    @BeforeEach
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory(false, QBR_MAX)
                .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }
}
