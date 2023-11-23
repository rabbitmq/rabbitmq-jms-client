// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TtlIT extends AbstractITQueue {

    private static final String QUEUE_NAME = "test.queue." + TtlIT.class.getCanonicalName();
    private static final String AMQP_QUEUE_NAME = "test.queue.amqp." + TtlIT.class.getCanonicalName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second

    Connection connection;

    @BeforeEach
    public void initAmqp() throws Exception {
        connection = new ConnectionFactory().newConnection();
    }

    @AfterEach
    void tearDownAmqp() throws Exception {
        connection.close();
    }

    @Test
    public void withTtlExpirationShouldBeGreaterThanZero() throws Exception {
        Message message = publishConsume(true);
        assertTrue(message.getJMSExpiration() > 0);
    }

    @Test
    public void withNoTllExpirationShouldBeZero() throws Exception {
        Message message = publishConsume(false);
        assertEquals(0, message.getJMSExpiration());
    }

    @Test
    public void fromAmqpMessageWithExpirationShouldBeGreatherThanZero() throws Exception {
        Message message = publishAmqpConsumeJms(true);
        assertTrue(message.getJMSExpiration() > 0);
    }

    @Test
    public void fromAmqpMessageWithNoExpirationShouldBeGreatherThanZero() throws Exception {
        Message message = publishAmqpConsumeJms(false);
        assertEquals(0, message.getJMSExpiration());
    }

    private Message publishConsume(boolean ttl) throws Exception {
        String content = UUID.randomUUID().toString();
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);

            drainQueue(queueSession, queue);

            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(content);
            if (ttl) {
                queueSender.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 10 * 1000);
            } else {
                queueSender.send(message);
            }
        } finally {
            reconnect();
        }

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
        assertEquals(content, message.getText());
        return message;
    }

    private Message publishAmqpConsumeJms(boolean expiration) throws Exception {
        Channel channel = connection.createChannel();
        channel.queueDeclare(AMQP_QUEUE_NAME,
                false, // durable
                false, // non-exclusive
                true,  // autoDelete
                null   // options
        );

        Map<String, Object> hdrs = new HashMap<>();
        hdrs.put("JMSType", "TextMessage");  //

        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
        propsBuilder.headers(hdrs);

        if (expiration) {
            propsBuilder.expiration("10000");
        }

        String content = UUID.randomUUID().toString();
        channel.basicPublish("", AMQP_QUEUE_NAME, propsBuilder.build(), content.getBytes());

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = new RMQDestination(AMQP_QUEUE_NAME, null, null, AMQP_QUEUE_NAME);

        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);

        assertNotNull(message, "No message received");
        assertEquals(content, message.getText(), "Payload doesn't match");
        return message;
    }

}
