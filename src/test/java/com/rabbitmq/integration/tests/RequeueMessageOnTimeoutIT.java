// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2017-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rabbitmq.TestUtils;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class RequeueMessageOnTimeoutIT extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue." + RequeueMessageOnTimeoutIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + RequeueMessageOnTimeoutIT.class.getName();
    private static final int REQUEUE_TIMEOUT_MS = 500;

    @BeforeEach public void init() throws Exception {
        Connection connection = null;
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            connection.createChannel().queueDelete(QUEUE_NAME);
        } finally {
            if (connection  != null) {
                connection.close();
            }
        }
    }

    @Test
    void requeueParameterTrueTimeoutMessageShouldBeNackedAndConsumerStillActive() throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection(true, true);
            QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            List<Message> receivedMessages = Collections.synchronizedList(new ArrayList<>());
            AtomicBoolean interrupted = new AtomicBoolean(false);
            queueReceiver.setMessageListener(message -> {
                receivedMessages.add(message);
                if (receivedMessages.size() == 1) {
                    try {
                        Thread.sleep(REQUEUE_TIMEOUT_MS * 2);
                    } catch (InterruptedException e) {
                      interrupted.set(true);
                    }
                }
            });

            TestUtils.waitUntil(Duration.ofSeconds(5), () -> receivedMessages.size() == 2);

            assertThat(interrupted.get()).isTrue();
            assertThat(receivedMessages).hasSize(2);
            assertThat(receivedMessages.get(0).getJMSRedelivered()).isFalse();
            assertThat(receivedMessages.get(1).getJMSRedelivered()).isTrue();
        } finally {
            if(connection != null) {
                connection.close();
            }
        }
    }

    @Test
    void requeueParameterFalseTimeoutConsumerIsClosed() throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection(true, false);
            QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            List<Message> receivedMessages = Collections.synchronizedList(new ArrayList<>());
            AtomicBoolean interrupted = new AtomicBoolean(false);
            queueReceiver.setMessageListener(message -> {
                receivedMessages.add(message);
                if (receivedMessages.size() == 1) {
                    try {
                        Thread.sleep(REQUEUE_TIMEOUT_MS * 2);
                    } catch (InterruptedException e) {
                        interrupted.set(true);
                    }
                }
            });

            TestUtils.waitUntil(Duration.ofSeconds(5), () -> receivedMessages.size() == 1);

            assertThat(receivedMessages).hasSize(1);
            assertThat(receivedMessages.get(0).getJMSRedelivered()).isFalse();

            // another consumer can consume the message
            queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueReceiver = queueSession.createReceiver(queue);
            Message message = queueReceiver.receive(1000L);
            assertNotNull(message);
            assertTrue(message.getJMSRedelivered());

            assertThat(interrupted.get()).isTrue();
        } finally {
            if(connection != null) {
                connection.close();
            }
        }
    }

    @Test
    void cannotRequeueOnTimeoutIfRequeueOnExceptionIsNotTrue() {
        assertThatThrownBy(() -> connection(false, true))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void requeueParameterTrueNoExceptionInListenerQueueShouldBeEmpty() throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection(true, false);
            QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            final CountDownLatch latch = new CountDownLatch(1);
            queueReceiver.setMessageListener(message -> latch.countDown());

            // the message has been consumed, no longer in the queue
            queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueReceiver = queueSession.createReceiver(queue);
            Message message = queueReceiver.receive(1000L);
            assertNull(message);
        } finally {
            if(connection != null) {
                connection.close();
            }
        }
    }

    private void sendMessage() throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
        } finally {
            reconnect();
        }
    }

    private QueueConnection connection(boolean requeueOnException, boolean requeueOnTimeout) throws Exception {
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
        connectionFactory.setRequeueOnMessageListenerException(requeueOnException);
        connectionFactory.setRequeueOnTimeout(requeueOnTimeout);
        connectionFactory.setOnMessageTimeoutMs(REQUEUE_TIMEOUT_MS);
        QueueConnection queueConnection = connectionFactory.createQueueConnection();
        queueConnection.start();
        return queueConnection;
    }



}
