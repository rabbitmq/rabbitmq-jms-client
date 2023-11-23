// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import javax.jms.*;
import com.rabbitmq.TestUtils.SkipIfDelayedMessageExchangePluginNotActivated;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test
 */
@SkipIfDelayedMessageExchangePluginNotActivated
public class DelayedQueueMessageIT extends AbstractITQueue {

    private static final String MESSAGE = "Hello " + DelayedQueueMessageIT.class.getName();

    private static final String QUEUE_NAME = "delay.queue.";

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
        Queue queue = queueSession.createQueue(QUEUE_NAME + System.currentTimeMillis());
        QueueSender queueSender = queueSession.createSender(queue);

        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        message.setJMSDeliveryTime(System.currentTimeMillis() + 4000L);
        queueSender.send(message);

        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        assertNull(queueReceiver.receive(2000L));
        TextMessage receivedMessage = (TextMessage)queueReceiver.receive();
        assertNotNull(receivedMessage);
        assertEquals(message.getJMSDeliveryTime(), receivedMessage.getJMSDeliveryTime());
        assertEquals(MESSAGE, receivedMessage.getText());
    }

    @Test
    public void testSendMessageViaDelayedProducerToJMSQueue() throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME + System.currentTimeMillis());
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
