/* Copyright (c) 2017 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.RMQConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.*;
import java.util.concurrent.CountDownLatch;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class CommitNackMessageOnRollbackIT extends AbstractITQueue {
    private static final String EXCHANGE_NAME = "test.exchange." + CommitNackMessageOnRollbackIT.class.getCanonicalName();
    private static final String EXCHANGE_DLX_NAME = "test.exchange." + CommitNackMessageOnRollbackIT.class.getCanonicalName() + ".dlx";
    private static final String QUEUE_NAME = "test.queue." + CommitNackMessageOnRollbackIT.class.getCanonicalName();
    private static final String QUEUE_DLX_NAME = "test.queue." + CommitNackMessageOnRollbackIT.class.getCanonicalName() + ".dlx";
    private static final String MESSAGE = "Hello " + CommitNackMessageOnRollbackIT.class.getName();
    private static final String ROUTING_KEY = "test";

    @BeforeEach public void init() throws Exception {
        Connection connection = null;
        try {
            com.rabbitmq.client.ConnectionFactory connectionFactory = new ConnectionFactory();
            connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDelete(EXCHANGE_NAME);
            channel.queueDelete(QUEUE_NAME);
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            HashMap<String, Object> args = new HashMap<String, Object>();
            args.put("x-dead-letter-exchange", EXCHANGE_DLX_NAME);
            channel.queueDeclare(QUEUE_NAME, false, false, false, args);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            channel.exchangeDelete(EXCHANGE_DLX_NAME);
            channel.queueDelete(QUEUE_DLX_NAME);
            channel.exchangeDeclare(EXCHANGE_DLX_NAME, "direct");
            channel.queueDeclare(QUEUE_DLX_NAME, false, false, false, null);
            channel.queueBind(QUEUE_DLX_NAME, EXCHANGE_DLX_NAME, ROUTING_KEY);
        } finally {
            if (connection  != null) {
                connection.close();
            }
        }
    }

    @Test
    public void commitNackParameterTrueCommitMessageShouldBeGone() throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection();
            QueueSession queueSession = connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            RMQDestination queue = new RMQDestination();
            queue.setAmqp(true);
            queue.setAmqpQueueName(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            Message message = queueReceiver.receive();
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
            if(connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void commitNackParameterTrueRollbackMessageShouldBeInDlxQueue() throws Exception {
        sendMessage();
        QueueConnection connection = null;
        try {
            connection = connection();
            QueueSession queueSession = connection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
            RMQDestination queue = new RMQDestination();
            queue.setAmqp(true);
            queue.setAmqpQueueName(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            Message message = queueReceiver.receive();
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
            if(connection != null) {
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
        connectionFactory.setCommitNackOnRollback(true);
        QueueConnection queueConnection = connectionFactory.createQueueConnection();
        queueConnection.start();
        return queueConnection;
    }
}
