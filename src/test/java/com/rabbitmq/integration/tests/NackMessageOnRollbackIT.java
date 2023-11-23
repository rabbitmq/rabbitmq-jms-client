// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.RMQConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.jms.*;
import java.util.concurrent.CountDownLatch;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class NackMessageOnRollbackIT extends AbstractITQueue {
    private static final String EXCHANGE_NAME = "test.exchange." + NackMessageOnRollbackIT.class.getCanonicalName();
    private static final String EXCHANGE_DLX_NAME = "test.exchange." + NackMessageOnRollbackIT.class.getCanonicalName() + ".dlx";
    private static final String QUEUE_NAME = "test.queue." + NackMessageOnRollbackIT.class.getCanonicalName();
    private static final String QUEUE_DLX_NAME = "test.queue." + NackMessageOnRollbackIT.class.getCanonicalName() + ".dlx";
    private static final String MESSAGE = "Hello " + NackMessageOnRollbackIT.class.getName();
    private static final String ROUTING_KEY = "test";

    Connection connection;

    static Stream<Arguments> messageProviderArguments() {
        return Stream.of(
                Arguments.of(asynchronousMessageProvider()),
                Arguments.of(synchronousMessageProvider())
        );
    }

    private static MessageProvider synchronousMessageProvider() {
        return queueReceiver -> queueReceiver.receive(10_000L);
    }

    private static MessageProvider asynchronousMessageProvider() {
        return queueReceiver -> {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Message> messageReference = new AtomicReference<>();
            queueReceiver.setMessageListener(message -> {
                messageReference.set(message);
                latch.countDown();
            });
            latch.await(10, TimeUnit.SECONDS);
            return messageReference.get();
        };
    }

    @BeforeEach
    public void init() throws Exception {
        com.rabbitmq.client.ConnectionFactory connectionFactory = new ConnectionFactory();
        connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX_NAME);
        channel.queueDeclare(QUEUE_NAME, false, false, false, args);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        channel.exchangeDeclare(EXCHANGE_DLX_NAME, "direct");
        channel.queueDeclare(QUEUE_DLX_NAME, false, false, false, null);
        channel.queueBind(QUEUE_DLX_NAME, EXCHANGE_DLX_NAME, ROUTING_KEY);
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            Channel channel = connection.createChannel();
            channel.exchangeDelete(EXCHANGE_NAME);
            channel.queueDelete(QUEUE_NAME);
            channel.exchangeDelete(EXCHANGE_DLX_NAME);
            channel.queueDelete(QUEUE_DLX_NAME);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @ParameterizedTest
    @MethodSource("messageProviderArguments")
    public void nackParameterTrueCommitMessageShouldBeGone(MessageProvider messageProvider) throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection();
            QueueSession queueSession = connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            RMQDestination queue = new RMQDestination();
            queue.setAmqp(true);
            queue.setAmqpQueueName(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            Message message = messageProvider.message(queueReceiver);
            assertNotNull(message);
            queueSession.commit();

            // the message has been consumed, no longer in the queue
            queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueReceiver = queueSession.createReceiver(queue);
            message = queueReceiver.receive(1000L);
            assertNull(message);

            // the message has not been redelivered to the dead letter queue
            queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = new RMQDestination();
            queue.setAmqp(true);
            queue.setAmqpQueueName(QUEUE_DLX_NAME);
            queueReceiver = queueSession.createReceiver(queue);
            message = queueReceiver.receive(1000L);
            assertNull(message);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @ParameterizedTest
    @MethodSource("messageProviderArguments")
    public void nackParameterTrueRollbackMessageShouldBeInDlxQueue(MessageProvider messageProvider) throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection();
            QueueSession queueSession = connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            RMQDestination queue = new RMQDestination();
            queue.setAmqp(true);
            queue.setAmqpQueueName(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            Message message = messageProvider.message(queueReceiver);
            assertNotNull(message);
            queueSession.rollback();

            // the message has been consumed, no longer in the queue
            queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queueReceiver = queueSession.createReceiver(queue);
            message = queueReceiver.receive(1000L);
            assertNull(message);

            // the message has been redelivered to the dead letter queue
            queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = new RMQDestination();
            queue.setAmqp(true);
            queue.setAmqpQueueName(QUEUE_DLX_NAME);
            queueReceiver = queueSession.createReceiver(queue);
            message = queueReceiver.receive(1000L);
            assertNotNull(message);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void sendMessage() throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            RMQDestination queue = new RMQDestination();
            queue.setAmqp(true);
            queue.setAmqpQueueName(QUEUE_NAME);
            queue.setAmqpExchangeName(EXCHANGE_NAME);
            queue.setAmqpRoutingKey(ROUTING_KEY);
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
        connectionFactory.setNackOnRollback(true);
        QueueConnection queueConnection = connectionFactory.createQueueConnection();
        queueConnection.start();
        return queueConnection;
    }

    private interface MessageProvider {

        Message message(QueueReceiver queueReceiver) throws Exception;

    }
}
