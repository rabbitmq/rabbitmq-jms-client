// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.util.Shell;
import javax.jms.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test
 */
public class DelayedQueueMessageIT extends AbstractITQueue {

    private static final String MESSAGE = "Hello " + DelayedQueueMessageIT.class.getName();

    private static final String QUEUE_NAME = "delay.queue." + DelayedQueueMessageIT.class.getCanonicalName();

    @BeforeEach
    public void beforeMethod() throws IOException {
        try {
            Shell.rabbitmqPlugins("is_enabled rabbitmq_delayed_message_exchange");
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Skipped DelayedMessageIT. Plugin rabbitmq_delayed_message_exchange is not enabled");
            Assumptions.assumeTrue(false);
        }
    }


    @Test
    public void testSendMessageToJMSTemporaryQueue() throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createTemporaryQueue();
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryDelay(4000L);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        queueSender.send(message);

        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        assertNull(queueReceiver.receive(2000L));
        TextMessage receivedMessage = (TextMessage) queueReceiver.receive(3000L);
        assertNotNull(receivedMessage);
        assertTrue(receivedMessage.getJMSDeliveryTime() > 0);
        assertEquals(MESSAGE, receivedMessage.getText());
    }


    @Test
    public void testSendDelayedMessageToJMSQueue() throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueSender queueSender = queueSession.createSender(queue);

        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        message.setJMSDeliveryTime(System.currentTimeMillis() + 4000L);
        queueSender.send(message);
        
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        assertNull(queueReceiver.receive(2000L));
        TextMessage receivedMessage = (TextMessage)queueReceiver.receive(2000L);
        assertNotNull(receivedMessage);
        assertEquals(message.getJMSDeliveryTime(), receivedMessage.getJMSDeliveryTime());
        assertEquals(MESSAGE, receivedMessage.getText());
    }

    @Test
    public void testSendMessageViaDelayedProducerToJMSQueue() throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryDelay(4000L);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        queueSender.send(message);

        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        assertNull(queueReceiver.receive(2000L));
        TextMessage receivedMessage = (TextMessage)queueReceiver.receive(3000L);
        assertNotNull(receivedMessage);
        assertEquals(message.getJMSDeliveryTime(), receivedMessage.getJMSDeliveryTime());
        assertEquals(MESSAGE, receivedMessage.getText());
    }

}
