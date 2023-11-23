// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.Enumeration;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.junit.jupiter.api.Test;

/**
 * Integration test for browsing a queue with limits. Limit is fixed in {@link AbstractITLimitedQueue#QBR_MAX}.
 */
public class LimitedBrowseQueueMessageIT extends AbstractITLimitedQueue {

    private static final String QUEUE_NAME = "test.queue."+LimitedBrowseQueueMessageIT.class.getCanonicalName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second

    private static final int NUM_TO_SEND = AbstractITLimitedQueue.QBR_MAX + 2;
    private static final int NUM_TO_BROWSE = AbstractITLimitedQueue.QBR_MAX;

    private void messageTestBase(MessageTestType mtt, String selector, int numSend, int numExpected) throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);

            drainQueue(queueSession, queue);

            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            for (int i=0; i<numSend; ++i)
                queueSender.send(mtt.gen(queueSession, (Serializable)queue));
        } finally {
            reconnect();
        }

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);

        {// Browse queue before receiving message
            QueueBrowser queueBrowser = queueSession.createBrowser(queue, selector);
            Enumeration<?> e = queueBrowser.getEnumeration();

            int numE = 0;
            Message msg = null;
            while (e.hasMoreElements()) {
                ++numE;
                msg = (Message) e.nextElement();
            mtt.check(msg, (Serializable) queue);
        }
            assertEquals(numExpected, numE, "Wrong number of messages on browse queue");
        }
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);

        for (int i=0; i<numSend; ++i)
            mtt.check(queueReceiver.receive(TEST_RECEIVE_TIMEOUT), (Serializable) queue);
    }

    @Test
    public void testSendBrowseAndReceiveLongTextMessages() throws Exception {
        messageTestBase(MessageTestType.LONG_TEXT, null, NUM_TO_SEND, NUM_TO_BROWSE);
    }

    @Test
    public void testSendBrowseAndReceiveTextMessage() throws Exception {
        messageTestBase(MessageTestType.TEXT, null, NUM_TO_SEND, NUM_TO_BROWSE);
    }

    @Test
    public void testSendBrowseAndReceiveBytesMessage() throws Exception {
        messageTestBase(MessageTestType.BYTES, null, NUM_TO_SEND, NUM_TO_BROWSE);
    }

    @Test
    public void testSendBrowseAndReceiveMapMessage() throws Exception {
        messageTestBase(MessageTestType.MAP, null, NUM_TO_SEND, NUM_TO_BROWSE);
    }

    @Test
    public void testSendBrowseAndReceiveStreamMessage() throws Exception {
        messageTestBase(MessageTestType.STREAM, null, NUM_TO_SEND, NUM_TO_BROWSE);
    }

    @Test
    public void testSendBrowseAndReceiveObjectMessage() throws Exception {
        messageTestBase(MessageTestType.OBJECT, null, NUM_TO_SEND, NUM_TO_BROWSE);
    }

    @Test
    public void testBrowseWithSelectorFalse() throws Exception {
        messageTestBase(MessageTestType.TEXT, "false", NUM_TO_SEND, 0); // nothing selected
    }

    @Test
    public void testBrowseWithSelectorTrue() throws Exception {
        messageTestBase(MessageTestType.TEXT, "true", NUM_TO_SEND, NUM_TO_BROWSE);
    }
}
