// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static com.rabbitmq.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.TestUtils;
import com.rabbitmq.jms.admin.RMQDestination;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Integration test
 */
public class RecoverMessagesIT extends AbstractITQueue {

    private static final String QUEUE_NAME = "test.queue." + RecoverMessagesIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + RecoverMessagesIT.class.getName();

    @Test
    public void testRecoverTextMessageSync() throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        queueSender.send(message);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        Message msg1 = queueReceiver.receive();
        assertThat(msg1).isNotRedelivered().hasDeliveryCount(1);
        queueSession.recover();
        Message msg2 = queueReceiver.receive();
        assertThat(msg2).isEqualTo(msg1).isRedelivered().hasDeliveryCount(2);
        msg2.acknowledge();
        Message msg3 = queueReceiver.receiveNoWait();
        assertThat(msg3).isNull();
    }

    @Test
    public void testRecoverTextMessageSyncRedeliveryCountShouldBeSetWithQuorumQueue(TestInfo info) throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
        String queueName = TestUtils.queueName(info);
        Queue queue =
            new RMQDestination(
                queueName, true, false, Collections.singletonMap("x-queue-type", "quorum"));
        try {
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            Message msg1 = queueReceiver.receive();
            assertThat(msg1).isNotRedelivered().hasDeliveryCount(1);
            queueSession.recover();
            Message msg2 = queueReceiver.receive();
            assertThat(msg2).isEqualTo(msg1).isRedelivered().hasDeliveryCount(2);
            queueSession.recover();
            Message msg3 = queueReceiver.receiveNoWait();
            assertThat(msg3).isEqualTo(msg1).isRedelivered().hasDeliveryCount(3);
            msg3.acknowledge();
            Message tmsg4 = queueReceiver.receiveNoWait();
            assertThat(tmsg4).isNull();
        } finally {
           deleteQueue(queueName);
        }
    }

    @Test
    public void testRecoverTextMessageAsyncSync() throws Exception {
        List<Message> messages = new CopyOnWriteArrayList<>();

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        queueSender.send(message);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        final CountDownLatch firstMessageLatch = new CountDownLatch(1);
        final CountDownLatch secondMessageLatch = new CountDownLatch(2);
        queueReceiver.setMessageListener(message1 -> {
            messages.add(message1);
            firstMessageLatch.countDown();
            secondMessageLatch.countDown();
        });
        firstMessageLatch.await(5000, TimeUnit.MILLISECONDS);
        // we should have received one message
        assertThat(messages).hasSize(1);
        Message msg1 = messages.get(0);
        assertThat(msg1).isNotRedelivered().hasDeliveryCount(1);
        queueSession.recover();
        secondMessageLatch.await(5000, TimeUnit.MILLISECONDS);
        // we should have received two messages
        // There is no synchronisation so no guarantee we see the latest messages!
        assertThat(messages).hasSize(2);
        Message msg2 = messages.get(1);
        assertThat(msg2).isEqualTo(msg1).isRedelivered().hasDeliveryCount(2);
        msg2.acknowledge();
        queueReceiver.setMessageListener(null);
        Message msg3 = queueReceiver.receiveNoWait();
        assertThat(msg3).isNull();
    }

}
