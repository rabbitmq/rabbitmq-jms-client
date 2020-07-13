// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.fail;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractITQueue {
    QueueConnectionFactory connFactory;
    protected QueueConnection queueConn;

    @BeforeEach
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        customise((RMQConnectionFactory) connFactory);
        this.queueConn = connFactory.createQueueConnection();
    }

    protected void customise(RMQConnectionFactory connectionFactory) {

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

    @AfterEach
    public void afterTests() throws Exception {
        if (queueConn != null)
            queueConn.close();
    }
}
