// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Asynchronous and Synchronous simultaneously.  JMS 1.1 §4.4.6.
 */
public class MixedConsumerCheckIT extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue." + MixedConsumerCheckIT.class.getCanonicalName();
    private static final String QUEUE_NAME_TWO = QUEUE_NAME + ".two";

    /**
     * Listener with a Consumer. Uses message listener (asynchronous receive).
     * Try to receive() from different queue on same session.
     * @throws javax.jms.IllegalStateException if test succeeds.
     */
    @Test
    public void testAsyncThenSyncReceive() throws Exception {
        assertThrows(javax.jms.IllegalStateException.class, () -> {
            queueConn.start();

            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);

            Queue queue = queueSession.createQueue(QUEUE_NAME);
            Queue queueTwo = queueSession.createQueue(QUEUE_NAME_TWO);

            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            QueueReceiver queueReceiverTwo = queueSession.createReceiver(queueTwo);

            TestMessageListener listener = new TestMessageListener();
            queueReceiver.setMessageListener(listener);

            queueReceiverTwo.receiveNoWait(); // should throw an exception
        });
    }

    /**
     * Receive on another thread. Try to set a Listener (asynchronous receive).
     * @throws javax.jms.IllegalStateException if test succeeds.
     */
    @Test
    public void testSyncThenAsyncReceive() throws Exception {
        assertThrows(javax.jms.IllegalStateException.class, () -> {
            queueConn.start();

            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);

            Queue queue = queueSession.createQueue(QUEUE_NAME);
            Queue queueTwo = queueSession.createQueue(QUEUE_NAME_TWO);

            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            final QueueReceiver queueReceiverTwo = queueSession.createReceiver(queueTwo);

            new Thread(){
                @Override
                public void run() {
                    try {
                        queueReceiverTwo.receive(500); // half a sec’
                    } catch (JMSException e) { }
                }
            }.start();
            Thread.sleep(100); // tenth of a sec’

            queueReceiver.setMessageListener(new TestMessageListener()); // should throw exception
        });
    }

    private static class TestMessageListener implements MessageListener {
        @Override
        public void onMessage(Message message) {
        }
    }
}
