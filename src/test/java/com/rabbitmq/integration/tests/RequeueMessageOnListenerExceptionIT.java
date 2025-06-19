// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2017-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.client.RMQConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.jms.*;
import java.util.concurrent.CountDownLatch;

import static com.rabbitmq.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class RequeueMessageOnListenerExceptionIT extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue." + RequeueMessageOnListenerExceptionIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + RequeueMessageOnListenerExceptionIT.class.getName();

    @BeforeEach public void init() throws Exception {
        Connection connection = null;
        try {
            com.rabbitmq.client.ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            connection.createChannel().queueDelete(QUEUE_NAME);
        } finally {
            if (connection  != null) {
                connection.close();
            }
        }
    }

    @Test
    public void requeueParameterTrueRuntimeExceptionInListenerMessageShouldBeNacked() throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection();
            QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            CountDownLatch latch = new CountDownLatch(1);
            queueReceiver.setMessageListener(message -> {
                latch.countDown();
                throw new RuntimeException("runtime exception in message listener");
            });
            assertThat(latch).completes();
            queueSession.close();

            // another consumer can consume the message
            queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueReceiver = queueSession.createReceiver(queue);
            Message message = queueReceiver.receive(1000L);
            assertNotNull(message);
            assertTrue(message.getJMSRedelivered());
        } finally {
            if(connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void requeueParameterTrueNoExceptionInListenerQueueShouldBeEmpty() throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection();
            QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            final CountDownLatch latch = new CountDownLatch(1);
            queueReceiver.setMessageListener(message -> latch.countDown());
            assertThat(latch).completes();
            queueSession.close();

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

    @Test
    public void messageShouldBeAckedWhenRequeueOnMessageListenerExceptionIsTrue() throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection();
            QueueSession queueSession = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            final CountDownLatch latch = new CountDownLatch(1);
            queueReceiver.setMessageListener(message -> {
                try {
                    message.acknowledge();
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
                latch.countDown();
            });
            assertThat(latch).completes();
            queueSession.close();

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

    private QueueConnection connection() throws Exception {
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
        connectionFactory.setChannelsQos(RMQConnection.NO_CHANNEL_QOS);
        connectionFactory.setRequeueOnMessageListenerException(true);
        QueueConnection queueConnection = connectionFactory.createQueueConnection();
        queueConnection.start();
        return queueConnection;
    }

}
