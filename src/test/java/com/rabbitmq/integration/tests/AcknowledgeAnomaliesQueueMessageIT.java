// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;

import javax.jms.DeliveryMode;
import javax.jms.IllegalStateException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.junit.jupiter.api.Test;

import com.rabbitmq.jms.client.RMQSession;

/**
 * Integration test for acknowledgement anomalies: repeated ack and ack out of session.
 */
public class AcknowledgeAnomaliesQueueMessageIT extends AbstractITQueue {

    private static final String QUEUE_NAME = "test.queue."+AcknowledgeAnomaliesQueueMessageIT.class.getCanonicalName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second

    private void messageTestBase(MessageTestType mtt) throws Exception {
        try     { queueConn.start();
                  QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
                  Queue queue = queueSession.createQueue(QUEUE_NAME);

                  drainQueue(queueSession, queue);

                  QueueSender queueSender = queueSession.createSender(queue);
                  queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                  for (int i=0; i<2; ++i) { // send two messages
                      queueSender.send(mtt.gen(queueSession, (Serializable)queue));
                  }
                }
        finally { reconnect(); }

        Message msgNotAcked = null;
        try     { queueConn.start();
                  QueueSession queueSession = queueConn.createQueueSession(false, RMQSession.CLIENT_INDIVIDUAL_ACKNOWLEDGE);
                  Queue queue = queueSession.createQueue(QUEUE_NAME);
                  QueueReceiver queueReceiver = queueSession.createReceiver(queue);

                  msgNotAcked = queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
                  mtt.check(msgNotAcked,  (Serializable)queue);

                  Message msg2 = queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
                  mtt.check(msg2,  (Serializable)queue);
                  msg2.acknowledge();

                  // repeat acknowledgement: should be a no-op

                  msg2.acknowledge();
                }
        finally { reconnect(); }    // requeues unacknowledged messages

        // No session here

        try {
            msgNotAcked.acknowledge();  // should get an illegal state exception
        } catch (IllegalStateException ise) {
            // brilliant
        } catch (Exception e) {
            fail(String.format("Message acknowledged outside session gave wrong exception: %s", e));
        }

        {   queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE); // automatically acknowledged
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            Message msg = queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
            mtt.check(msg,  (Serializable)queue);

            msg.acknowledge(); // repeat ack: should be a no-op
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
