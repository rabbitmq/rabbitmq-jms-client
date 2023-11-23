// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
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
