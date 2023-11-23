// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.*;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.rabbitmq.TestUtils.waitUntil;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for publisher confirms.
 */
public class PublisherConfirmsIT {

    private static final String QUEUE_NAME = "test.queue." + PublisherConfirmsIT.class.getCanonicalName();

    Connection connection;
    Map<String, Message> ackedMessages = new ConcurrentHashMap<>();

    @BeforeEach
    public void init() throws Exception {
        ackedMessages.clear();
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        connectionFactory.setConfirmListener(context -> {
            TextMessage textMessage = (TextMessage) context.getMessage();
            try {
                ackedMessages.put(textMessage.getText(), textMessage);
            } catch (JMSException e) {
                throw new RuntimeException(e);
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
        try (com.rabbitmq.client.Connection c = cf.newConnection()) {
            c.createChannel().queueDelete(QUEUE_NAME);
        }
    }

    @Test
    public void confirmListenerShouldBeCalledWhenConfirmListenerIsEnabled() throws Exception {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(queue);
        CountDownLatch receivedMessagesLatch = new CountDownLatch(2);
        consumer.setMessageListener(msg -> receivedMessagesLatch.countDown());

        TextMessage message = session.createTextMessage("hello1");
        MessageProducer producer = session.createProducer(queue);
        producer.send(message);
        assertThat(waitUntil(Duration.ofSeconds(5), () -> ackedMessages.size() == 1)).isTrue();
        assertThat(ackedMessages).containsKeys("hello1").containsValues(message);
        message = session.createTextMessage("hello2");
        producer.send(message);
        waitUntil(Duration.ofSeconds(5), () -> ackedMessages.size() == 2);
        assertThat(ackedMessages).containsKeys("hello2").containsValues(message);

        assertThat(receivedMessagesLatch.await(5, TimeUnit.SECONDS)).isTrue();
    }

}
