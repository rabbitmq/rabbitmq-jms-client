// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.AbstractMetricsCollector;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.junit.jupiter.api.Test;

import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test to check metrics collection is properly set up.
 */
public class MetricsCollectionIT extends AbstractITQueue {

    private static final String QUEUE_NAME = "test.queue." + MetricsCollectionIT.class.getCanonicalName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second

    TestMetricsCollector metricsCollector = new TestMetricsCollector();

    @Override
    protected void customise(RMQConnectionFactory connectionFactory) {
        connectionFactory.setMetricsCollector(metricsCollector);
    }

    @Test
    public void testSendAndReceiveLongTextMessage() throws Exception {
        assertEquals(1, metricsCollector.connectionCount);
        assertEquals(0, metricsCollector.channelCount);
        assertEquals(0, metricsCollector.publishedMessageCount);
        assertEquals(0, metricsCollector.consumedMessageCount);
        sendAndConsumeMessage();
        assertEquals(1, metricsCollector.channelCount);
        assertEquals(1, metricsCollector.publishedMessageCount);
        assertEquals(1, metricsCollector.consumedMessageCount);
    }

    protected void sendAndConsumeMessage() throws Exception {
        MessageTestType mtt = MessageTestType.TEXT;
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);

            drainQueue(queueSession, queue);

            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            queueSender.send(mtt.gen(queueSession, (Serializable) queue));
        } finally {
            reconnect();
        }

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        mtt.check(queueReceiver.receive(TEST_RECEIVE_TIMEOUT), (Serializable) queue);
    }

    static class TestMetricsCollector extends AbstractMetricsCollector {

        volatile int connectionCount = 0;
        volatile int channelCount = 0;
        volatile int publishedMessageCount = 0;
        volatile int consumedMessageCount = 0;

        @Override
        protected void incrementConnectionCount(Connection connection) {
            connectionCount++;
        }

        @Override
        protected void decrementConnectionCount(Connection connection) {
            connectionCount--;
        }

        @Override
        protected void incrementChannelCount(Channel channel) {
            channelCount++;
        }

        @Override
        protected void decrementChannelCount(Channel channel) {
            channelCount--;
        }

        @Override
        protected void markPublishedMessage() {
            publishedMessageCount++;
        }

        @Override
        protected void markMessagePublishFailed() {
            publishedMessageCount++;
        }

        @Override
        protected void markConsumedMessage() {
            consumedMessageCount++;
        }

        @Override
        protected void markAcknowledgedMessage() {

        }

        @Override
        protected void markRejectedMessage() {

        }

        @Override
        protected void markMessagePublishAcknowledged() {

        }

        @Override
        protected void markMessagePublishNotAcknowledged() {

        }

        @Override
        protected void markPublishedMessageUnrouted() {

        }
    }
}
