// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.fail;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;

/**
 * Integration test for transaction commits in the presence of message listeners.
 */
public class MessageListenerTransactionIT extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue."+MessageListenerTransactionIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + MessageListenerTransactionIT.class.getName();

    /**
     * The destinations to create
     */
    private static final String[] DESTINATIONS = {"dest1", "dest2", "dest3"};

    private final int sendCount = 2;
    private final int expectedTotalCount = sendCount * DESTINATIONS.length;

    @Test
    public void testSerialInvocation() throws Exception {
        final long sleep = 100;

        TestListener listener = new TestListener(expectedTotalCount, sleep);

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession( true /*transacted*/
                                                                  , Session.CLIENT_ACKNOWLEDGE); /*ignored*/

        Queue[] queues = new Queue[DESTINATIONS.length];
        for (int i=0; i < DESTINATIONS.length; ++i) {
            queues[i] = queueSession.createQueue(QUEUE_NAME+DESTINATIONS[i]);
        }

        MessageConsumer[] consumers = new MessageConsumer[DESTINATIONS.length];
        for (int i = 0; i < DESTINATIONS.length; ++i) {
            consumers[i] = queueSession.createReceiver(queues[i]);
            consumers[i].setMessageListener(listener);
        }

        QueueSender[] senders = new QueueSender[DESTINATIONS.length];
        for (int i=0; i < DESTINATIONS.length; ++i) {
            senders[i] = queueSession.createSender(queues[i]);
            senders[i].setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE + DESTINATIONS[i]);
            for (int j=0; j < sendCount; ++j) senders[i].send(message);
        }

        for (int i=0; i<DESTINATIONS.length; ++i) senders[i].close();

        queueSession.commit();

        // wait for the messages to be received; max wait time expectedTotalCount*sleep*2
        long maxWait = expectedTotalCount * sleep * 2;
        synchronized (listener) {
            int rcvd;
            while (maxWait > 0 && (rcvd = listener.getReceived()) < expectedTotalCount) {
                long startWait = System.currentTimeMillis();
                listener.wait((expectedTotalCount-rcvd) * sleep);  // wait for a bit more
                maxWait -= (System.currentTimeMillis() - startWait);
            }
        }

        for (int i=0; i<DESTINATIONS.length; ++i) consumers[i].close();

        // acknowledge the messages
        Message last = listener.getMessage();
        if (last != null) {
            last.acknowledge();
        }

        if (listener.getFailures() != 0) {
            fail("The listener was not invoked serially");
        }
        if (listener.getReceived() != expectedTotalCount) {
            fail("Received " + listener.getReceived()
                 + " messages, but expected " + expectedTotalCount);
        }
    }

    /**
     * MessageListener implementation which checks if
     * onMessage() is invoked by multiple threads
     */
    private class TestListener implements MessageListener {
        /**
         * The no. of expected messages
         */
        private final int _expected;

        /**
         * The time to wait for in onMessage() in ms, before returning
         */
        private final long _sleep;

        /**
         * Determines if onMessage() has been invoked by another thread
         */
        private boolean _invoked = false;

        /**
         * The no. of times onMessage() has been invoked by multiple threads
         */
        private int _failures = 0;

        /**
         * The no. of received messages
         */
        private int _received = 0;

        /**
         * The last received message
         */
        private Message _message;


        /**
         * Construct a new <code>TestListener</code>
         *
         * @param expected the no.of messages to receive
         * @param sleep the time to wait in onMessage() in ms
         */
        public TestListener(int expected, long sleep) {
            _expected = expected;
            _sleep = sleep;
        }

        /**
         * Handle a message
         *
         * @param message the message
         */
        @Override
        public void onMessage(Message message) {
            synchronized (this) {
                _message = message;
                if (_invoked) {
                    _failures++;
                }
                _invoked = true;
                ++_received;
                if (_received == _expected) {
                    notify();
                }
            }
            try {
                Thread.sleep(_sleep);
            } catch (InterruptedException ignore) {
                ignore.printStackTrace();
            }
            synchronized (this) {
                _invoked = false;
            }
        }

        /**
         * Returns the no. of times {@link #onMessage} was invoked by
         * more than one thread
         *
         * @return the no. of failures
         */
        public int getFailures() {
            return _failures;
        }

        /**
         * Returns the no. of received messages
         *
         * @return the no. of received messages
         */
        public int getReceived() {
            return _received;
        }

        /**
         * Returns the last messsage received
         *
         * @return the last message received
         */
        public Message getMessage() {
            return _message;
        }
    }

}
