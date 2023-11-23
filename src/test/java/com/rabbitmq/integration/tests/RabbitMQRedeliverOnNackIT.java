// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;


/**
 * Integration test for re-deliver on rollback.
 */
public class RabbitMQRedeliverOnNackIT  extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue."+RabbitMQRedeliverOnNackIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + RabbitMQRedeliverOnNackIT.class.getName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second

    /**
     * Test that Rabbit re-delivers a received message which has been rolled-back.
     * @throws Exception on test failure
     */
    @Test
    public void testRMQRedeliverOnNack() throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession( false /*not transacted*/
                                                                    , Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
        } finally {
            reconnect();
        }

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession( true /*transacted*/
                                                                , Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
        assertNotNull(message, "No message delivered initially");
        assertEquals(MESSAGE, message.getText(), "Wrong message delivered initially");

        queueSession.rollback();

        message = (TextMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);

        assertNotNull(message, "No message delivered after rollback");
        assertEquals(MESSAGE, message.getText(), "Wrong message redelivered");
        assertTrue(message.getJMSRedelivered(), "Message not marked 'redelivered'");

        queueSession.commit();
    }
}
