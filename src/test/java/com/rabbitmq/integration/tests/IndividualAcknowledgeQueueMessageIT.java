// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import java.io.Serializable;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.junit.jupiter.api.Test;

import com.rabbitmq.jms.client.RMQSession;

/**
 * Integration test for simple browsing of a queue with selective acknowledgements; all message types.
 */
public class IndividualAcknowledgeQueueMessageIT extends AbstractITQueue {

    private static final String QUEUE_NAME = "test.queue."+IndividualAcknowledgeQueueMessageIT.class.getCanonicalName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second

    private void messageTestBase(MessageTestType mtt) throws Exception {
        int noToSend = 10;
        int noToAck = 5;
        try     { queueConn.start();
                  QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
                  Queue queue = queueSession.createQueue(QUEUE_NAME);

                  drainQueue(queueSession, queue);

                  QueueSender queueSender = queueSession.createSender(queue);
                  queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                  for (int i=0; i<noToSend; ++i) {
                      queueSender.send(mtt.gen(queueSession, (Serializable)queue));
                  }
                }
        finally { reconnect(); }

        try     { queueConn.start();
                  QueueSession queueSession = queueConn.createQueueSession(false, RMQSession.CLIENT_INDIVIDUAL_ACKNOWLEDGE);
                  Queue queue = queueSession.createQueue(QUEUE_NAME);
                  QueueReceiver queueReceiver = queueSession.createReceiver(queue);
                  int i = 0;
                  for (; i<noToSend-noToAck; ++i) {
                      Message msg = queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
                      mtt.check(msg,  (Serializable)queue);
                  }
                  for (; i<noToSend; ++i) {
                      Message msg = queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
                      mtt.check(msg,  (Serializable)queue);
                      msg.acknowledge();
                  }
                }
        finally { reconnect(); }    // requeues unacknowledged messages

        {   queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            for (int i=noToSend-noToAck; i<noToSend; ++i) {
                Message msg = queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
                mtt.check(msg,  (Serializable)queue);
            }
        }

    }

    @Test
    public void testSendBrowseAndReceiveLongTextMessage() throws Exception {
        messageTestBase(MessageTestType.LONG_TEXT);
    }

    @Test
    public void testSendBrowseAndReceiveTextMessage() throws Exception {
        messageTestBase(MessageTestType.TEXT);
    }

    @Test
    public void testSendBrowseAndReceiveBytesMessage() throws Exception {
        messageTestBase(MessageTestType.BYTES);
    }

    @Test
    public void testSendBrowseAndReceiveMapMessage() throws Exception {
        messageTestBase(MessageTestType.MAP);
    }

    @Test
    public void testSendBrowseAndReceiveStreamMessage() throws Exception {
        messageTestBase(MessageTestType.STREAM);
    }

    @Test
    public void testSendBrowseAndReceiveObjectMessage() throws Exception {
        messageTestBase(MessageTestType.OBJECT);
    }
}
