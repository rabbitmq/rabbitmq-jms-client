// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;


/**
 * Asynchronous Consumer integration test.
 */
public class AsyncConsumerIT extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue." + AsyncConsumerIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + AsyncConsumerIT.class.getName();

    /**
     * Basic send and receive with a Consumer. Uses serial send followed by async receive.
     * @throws Exception if test error.
     */
    @Test
    public void testSendAndAsyncReceiveTextMessage() throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
        } finally {
            reconnect();
        }
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        CountDownLatch latch = new CountDownLatch(1);

        MessageListener listener = new MessageListener(latch);
        queueReceiver.setMessageListener(listener);

        latch.await(1000, TimeUnit.MILLISECONDS);
        TextMessage message = (TextMessage) listener.getLastMessage();
        assertEquals(MESSAGE, message.getText());
        assertEquals(1, listener.getMessageCount());
        assertTrue(listener.isSuccess());
    }

    private static class MessageListener implements javax.jms.MessageListener {
        private volatile boolean success = false;
        private volatile Message lastMessage;
        private final AtomicInteger messageCount = new AtomicInteger(0);
        private final CountDownLatch latch;
        public MessageListener(CountDownLatch latch) {
            this.latch = latch;
        }
        @Override
        public void onMessage(Message message) {
            this.success = (this.messageCount.incrementAndGet() == 1);
            this.lastMessage = message;
            this.latch.countDown();
        }
        public boolean isSuccess() {
            return this.success;
        }
        public Message getLastMessage() {
            return this.lastMessage;
        }
        public int getMessageCount() {
            return this.messageCount.get();
        }
    }
}
