// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.rabbitmq.TestUtils;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.junit.jupiter.api.Test;

/**
 * Integration test
 */
public class SimpleDurableTopicMessageIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.durabletopic." + SimpleDurableTopicMessageIT.class.getCanonicalName();

    private static final String DURABLE_CONSUMER_NAME = "test.durabletopic.consumername";
    private static final String DURABLE_SUBSCRIBER_NAME = "test.durabletopic.subname";

    private static final int RECEIVE_TIMEOUT = 1000; // ms

    private static final String MESSAGE_TEXT_1 = "Hello " + SimpleDurableTopicMessageIT.class.getName();

    @Override
    protected void customise(RMQConnectionFactory connectionFactory) {
       super.customise(connectionFactory);
       connectionFactory.setValidateSubscriptionNames(true);
    }

    @Test
    public void testSendAndReceiveTextMessage() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        MessageConsumer receiver1 = topicSession.createDurableConsumer(topic, DURABLE_CONSUMER_NAME);
        TopicSubscriber receiver2 = topicSession.createSubscriber(topic);
        TopicSubscriber receiver3 = topicSession.createDurableSubscriber(topic, DURABLE_SUBSCRIBER_NAME);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
        sender.send(message);

        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver1.receive()).getText());
        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver2.receive()).getText());
        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver3.receive()).getText());

        topicSession.unsubscribe(DURABLE_CONSUMER_NAME);
        topicSession.unsubscribe(DURABLE_SUBSCRIBER_NAME);
    }

    @Test
    public void testSendAndReceiveTextMessagesOnSharedSubscription() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        MessageConsumer receiver1 = topicSession.createSharedDurableConsumer(topic, DURABLE_CONSUMER_NAME);
        MessageConsumer receiver2 = topicSession.createSharedDurableConsumer(topic, DURABLE_CONSUMER_NAME);

        int messageCount = 100;
        CountDownLatch latch = new CountDownLatch(messageCount);
        Set<String> messages1 = ConcurrentHashMap.newKeySet(messageCount / 2);
        Set<String> messages2 = ConcurrentHashMap.newKeySet(messageCount / 2);
        receiver1.setMessageListener(message -> {
            messages1.add(TestUtils.text(message));
            latch.countDown();
        });
        receiver2.setMessageListener(message -> {
            messages2.add(TestUtils.text(message));
            latch.countDown();
        });

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        for (int i = 0; i < messageCount; i++) {
            sender.send(topicSession.createTextMessage("hello " + (i % 2 + 1)));
        }

        assertThat(latch.await(10, SECONDS)).isTrue();
        // checking round-robin dispatching
        assertThat(messages1).hasSize(1).containsExactly("hello 1");
        assertThat(messages2).hasSize(1).containsExactly("hello 2");

        topicSession.unsubscribe(DURABLE_CONSUMER_NAME);
    }

    @Test
    public void testSendAndReceiveAfterReconnect() throws Exception {
        topicConn.start();
        {
            TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(TOPIC_NAME);
            TopicPublisher sender = topicSession.createPublisher(topic);
            topicSession.createDurableConsumer(topic, DURABLE_SUBSCRIBER_NAME); // ignore receiver

            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
            sender.send(message);
        }

        reconnect();

        topicConn.start();
        {
            TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(TOPIC_NAME);
            MessageConsumer receiver = topicSession.createDurableConsumer(topic, DURABLE_SUBSCRIBER_NAME);

            TextMessage tm = (TextMessage) receiver.receive(RECEIVE_TIMEOUT);
            assertNotNull(tm, "No message received");
            assertEquals(MESSAGE_TEXT_1, tm.getText());

            topicSession.unsubscribe(DURABLE_SUBSCRIBER_NAME);
        }
    }

    @Test
    public void subscriptionShouldFailWhenSubscriptionNameIsNotValid() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        assertThatThrownBy(() -> topicSession.createDurableConsumer(topic, "invalid?subscription?name"))
            .isInstanceOf(JMSException.class)
            .hasMessageContainingAll("This subscription name is not valid");
    }
}
