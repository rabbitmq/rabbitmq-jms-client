// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.TestUtils;
import com.rabbitmq.jms.util.Shell;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

import javax.jms.*;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test
 */
public class SimpleTopicMessageIT extends AbstractITTopic {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTopicMessageIT.class);

    private static final String TOPIC_NAME = "test.topic." + SimpleTopicMessageIT.class.getCanonicalName();
    private static final String WILD_TOPIC_NAME = "test.topic.#";
    private static final String MESSAGE_TEXT_1 = "Hello " + SimpleTopicMessageIT.class.getName();
    private static final String MESSAGE_TEXT_2 = "Wild hello " + SimpleTopicMessageIT.class.getName();

    @Test
    public void testSendAndReceiveTextMessage() throws Exception {
        log("Starting testSendAndReceiveTextMessage");
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        TopicSubscriber receiver1 = topicSession.createSubscriber(topic);
        TopicSubscriber receiver2 = topicSession.createSubscriber(topic);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
        sender.send(message);

        log("Sent message");

        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver1.receive()).getText());
        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver2.receive()).getText());

        // check only one binding to each subscriber AMQP queue is created
        // see https://github.com/rabbitmq/rabbitmq-jms-client/issues/72
        log("Listing bindings");
        List<Shell.Binding> bindings = Shell.listBindings(false);
        log("Bindings listed, checking them");
        List<Shell.Binding> bindingsToTopic = bindings.stream()
                .filter(b -> b.getRoutingKey().equals(TOPIC_NAME)) // bindings to the topic
                .collect(toList());
        assertEquals(2, bindingsToTopic.size());
        bindingsToTopic.forEach(b -> {
            String subscriberAmqpQueue = b.getDestination();
            List<Shell.Binding> subscriberAmqpQueueBinding = bindings.stream()
                    .filter(topicBinding -> topicBinding.getDestination().equals(subscriberAmqpQueue)) // bindings to this queue
                    .collect(Collectors.toList());
            assertEquals(1, subscriberAmqpQueueBinding.size());
        });
        log("Test done");
    }

    @Test
    public void testSendAndReceiveTextMessageOnSharedSubscription() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        String subscriptionName = "shared-subscription-name";
        MessageConsumer receiver1 = topicSession.createSharedConsumer(topic, subscriptionName);
        MessageConsumer receiver2 = topicSession.createSharedConsumer(topic, subscriptionName);

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

        receiver1.close();
        receiver2.close();
    }

    @Test
    public void testSendAndReceiveWildTopicTestMessage() throws Exception {
        log("Starting testSendAndReceiveWildTopicTestMessage");
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic explicitTopicDestination = topicSession.createTopic(TOPIC_NAME);
        Topic wildTopicDestination = topicSession.createTopic(WILD_TOPIC_NAME);

        TopicPublisher sender = topicSession.createPublisher(explicitTopicDestination);
        TopicSubscriber receiver = topicSession.createSubscriber(wildTopicDestination);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_2);
        sender.send(message);

        log("Sent message");

        TextMessage textMessage = (TextMessage) receiver.receive();
        assertEquals(MESSAGE_TEXT_2, textMessage.getText(), "Failed to receive proper text message");
        assertEquals(explicitTopicDestination, textMessage.getJMSDestination(), "Incorrect publisher destination on received message");
        log("Test done");
    }

    private static void log(String message) {
        LOGGER.debug(message);
    }
}
