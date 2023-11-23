// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class AsyncConsumerOnMessageHangIT extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue." + AsyncConsumerOnMessageHangIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + AsyncConsumerOnMessageHangIT.class.getName();

    /**
     * Basic send and receive with a Consumer. Uses serial send followed by async receive.
     * @throws Exception if test error.
     */
    @Test
    public void testSendAndAsyncReceiveTextMessageWithHang() throws Exception {
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

        MessageListener listener = new MessageListener();
        queueReceiver.setMessageListener(listener);

        Thread.sleep(4000);

        TextMessage message = (TextMessage) listener.getLastMessage();
        assertEquals(MESSAGE, message.getText());
        assertEquals(1, listener.getMessageCount());
        assertTrue(listener.isSuccess());
        assertTrue(listener.isInterrupted());
    }

    private static class MessageListener implements javax.jms.MessageListener {
        private volatile boolean success = false;
        private volatile boolean interrupted = false;
        private volatile Message lastMessage = null;
        private final AtomicInteger messageCount = new AtomicInteger(0);
        public MessageListener() {
        }
        @Override
        public void onMessage(Message message) {
            this.messageCount.incrementAndGet();
            this.lastMessage = message;
            this.success = true;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException _ignored) {
                // this must happen
                this.interrupted = true;
            }
        }
        public boolean isInterrupted() {
            return this.interrupted;
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
