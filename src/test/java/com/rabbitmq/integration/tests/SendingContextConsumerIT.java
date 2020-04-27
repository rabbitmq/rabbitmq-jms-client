/* Copyright (c) 2018-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.client.SendingContext;
import com.rabbitmq.jms.client.SendingContextConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class SendingContextConsumerIT {

    private static final String QUEUE_NAME = "test.queue." + SendingContextConsumerIT.class.getCanonicalName();
    final AtomicInteger sentCount = new AtomicInteger(0);
    Connection connection;

    protected static void drainQueue(Session session, Queue queue) throws Exception {
        MessageConsumer receiver = session.createConsumer(queue);
        Message msg = receiver.receiveNoWait();
        while (msg != null) {
            msg = receiver.receiveNoWait();
        }
    }

    @BeforeEach
    public void init() throws Exception {
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
            .getConnectionFactory();
        connectionFactory.setSendingContextConsumer(new SendingContextConsumer() {

            @Override
            public void accept(SendingContext ctx) {
                sentCount.incrementAndGet();
            }
        });
        connection = connectionFactory.createConnection();
        connection.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
        com.rabbitmq.client.Connection c = cf.newConnection();
        try {
            c.createChannel().queueDelete(QUEUE_NAME);
        } finally {
            c.close();
        }
    }

    @Test
    public void sendingContextConsumerShouldBeCalledWhenSendingMessage() throws Exception {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        TextMessage message = session.createTextMessage("hello");
        MessageProducer producer = session.createProducer(session.createQueue(QUEUE_NAME));
        int initialCount = sentCount.get();
        producer.send(message);
        assertEquals(initialCount + 1, sentCount.get());
        producer.send(message);
        assertEquals(initialCount + 2, sentCount.get());
    }
}
