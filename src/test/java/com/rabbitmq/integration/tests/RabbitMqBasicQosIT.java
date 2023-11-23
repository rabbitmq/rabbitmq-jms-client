// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.client.RMQConnection;
import org.junit.jupiter.api.Test;

import javax.jms.*;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rabbitmq.TestUtils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Asynchronous Consumer integration test.
 */
public class RabbitMqBasicQosIT extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue." + RabbitMqBasicQosIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + RabbitMqBasicQosIT.class.getName();

    @Test
    public void messagesAreDispatchedToMultipleListenersWithQos() throws Exception {
        sendMessages();
        QueueConnection connection = null;
        try {
            connection = connection(1);
            QueueSession queueSession = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);

            CountDownLatch latch = new CountDownLatch(1);
            CountDownLatch ackLatch = new CountDownLatch(1);

            MessageListener listener = new MessageListener(latch, ackLatch);
            queueReceiver.setMessageListener(listener);

            // waiting for message to be received
            latch.await(1000, TimeUnit.MILLISECONDS);
            // message is in first listener, but not yet ack-ed
            // no other message should be delivered to first listener

            // start another listener
            QueueSession queueSession2 = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            QueueReceiver queueReceiver2 = queueSession2.createReceiver(queue);
            CountDownLatch latch2 = new CountDownLatch(1);
            CountDownLatch ackLatch2 = new CountDownLatch(1);
            MessageListener listener2 = new MessageListener(latch2, ackLatch2);
            queueReceiver2.setMessageListener(listener2);

            // wait for second message in second listener
            latch2.await(1000L, TimeUnit.MILLISECONDS);
            // let's ack first message in first listener
            ackLatch.countDown();
            // let's ack second message in second listener
            ackLatch2.countDown();

            assertEquals(1, listener.getMessageCount());
            assertEquals(1, listener2.getMessageCount());
        } finally {
            if(connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void messagesAreNotDispatchedToMultipleListenersWithoutQos() throws Exception {
        sendMessages();
        QueueConnection connection = null;
        try {
            connection = connection(RMQConnection.NO_CHANNEL_QOS);
            QueueSession queueSession = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);

            CountDownLatch latch = new CountDownLatch(1);
            CountDownLatch ackLatch = new CountDownLatch(1);

            MessageListener listener = new MessageListener(latch, ackLatch);
            queueReceiver.setMessageListener(listener);

            // waiting for message to be received
            latch.await(1000, TimeUnit.MILLISECONDS);
            // message is in first listener, but not yet ack-ed
            // second message should arrive in first listener internal queue

            // start another listener, but it should see anything
            QueueSession queueSession2 = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            QueueReceiver queueReceiver2 = queueSession2.createReceiver(queue);
            CountDownLatch latch2 = new CountDownLatch(1);
            CountDownLatch ackLatch2 = new CountDownLatch(1);
            MessageListener listener2 = new MessageListener(latch2, ackLatch2);
            queueReceiver2.setMessageListener(listener2);

            // ack first message
            ackLatch.countDown();
            // second message should be delivered to first listener
            assertThat(waitUntil(Duration.ofSeconds(1), () -> new MessageListenerMessageCountCallable(listener).call() == 2)).isTrue();

            assertEquals(2, listener.getMessageCount());
            assertEquals(0, listener2.getMessageCount());
        } finally {
            if(connection != null) {
                connection.close();
            }
        }


    }

    private void sendMessages() throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
            queueSender.send(message);
        } finally {
            reconnect();
        }
    }

    private QueueConnection connection(int qos) throws Exception {
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
        connectionFactory.setChannelsQos(qos);
        QueueConnection queueConnection = connectionFactory.createQueueConnection();
        queueConnection.start();
        return queueConnection;
    }

    private static class MessageListener implements javax.jms.MessageListener {
        private final AtomicInteger messageCount = new AtomicInteger(0);
        private final CountDownLatch latch;
        private final CountDownLatch ackLatch;
        public MessageListener(CountDownLatch latch, CountDownLatch ackLatch) {
            this.latch = latch;
            this.ackLatch = ackLatch;
        }
        @Override
        public void onMessage(Message message) {
            messageCount.incrementAndGet();
            this.latch.countDown();
            try {
                this.ackLatch.await(1000L, TimeUnit.MILLISECONDS);
                message.acknowledge();
            } catch (Exception e) {
                throw new RuntimeException("Error in message processing", e);
            }

        }
        public int getMessageCount() {
            return this.messageCount.get();
        }
    }

    private static class MessageListenerMessageCountCallable implements Callable<Integer> {

        private final MessageListener listener;

        private MessageListenerMessageCountCallable(MessageListener listener) {
            this.listener = listener;
        }

        @Override
        public Integer call() throws Exception {
            return listener.getMessageCount();
        }
    }
}
