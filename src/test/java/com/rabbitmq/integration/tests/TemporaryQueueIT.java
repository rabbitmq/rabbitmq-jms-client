// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.jms.DeliveryMode;
import jakarta.jms.Queue;
import jakarta.jms.QueueReceiver;
import jakarta.jms.QueueSender;
import jakarta.jms.QueueSession;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import org.junit.jupiter.api.Test;

/**
 * Integration test
 */
public class TemporaryQueueIT extends AbstractITQueue {

    private static final String MESSAGE = "Hello " + TemporaryQueueIT.class.getName();

    @Test
    public void testQueueSendAndReceiveSingleSession() throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createTemporaryQueue();
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        queueSender.send(message);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        message = (TextMessage) queueReceiver.receive(1000);
        assertNotNull(message);
        assertEquals(MESSAGE, message.getText());
    }

    @Test
    public void testQueueSendAndReceiveTwoSessions() throws Exception {
        queueConn.start();
        QueueSession queueSession1 = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        QueueSession queueSession2 = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession1.createTemporaryQueue();
        QueueSender queueSender = queueSession1.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession1.createTextMessage(MESSAGE);
        queueSender.send(message);
        QueueReceiver queueReceiver = queueSession2.createReceiver(queue);
        message = (TextMessage) queueReceiver.receive(1000);
        assertNotNull(message);
        assertEquals(MESSAGE, message.getText());
    }
}
