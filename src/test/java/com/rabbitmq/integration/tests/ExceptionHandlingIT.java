// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2020 VMware, Inc. or its affiliates. All rights reserved.

package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQDestination;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Session;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * To make sure an exception is thrown when starting listening on
 * a non-existing destination.
 */
public class ExceptionHandlingIT {

    QueueConnectionFactory connFactory;
    QueueConnection queueConn;

    @BeforeEach
    public void beforeTests() throws Exception {
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
            .getConnectionFactory();
        this.queueConn = connFactory.createQueueConnection();
    }

    @AfterEach
    public void afterTests() throws Exception {
        if (queueConn != null)
            queueConn.close();
    }

    @Test
    public void consumerOnNonExistingDestination() throws Exception {
        queueConn.start();
        String queueName = ExceptionHandlingIT.class.getSimpleName() + " " + System.currentTimeMillis();
        RMQDestination queue = new RMQDestination(queueName, null, null, queueName);
        Session session = queueConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);

        assertThrows(JMSException.class, () -> messageConsumer.setMessageListener(msg -> {
        }));
    }
}
