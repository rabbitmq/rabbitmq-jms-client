// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import jakarta.jms.QueueConnectionFactory;

import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractITQueueSSL extends AbstractITQueue {

    @BeforeEach
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory(true, 0)
                .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }
}
