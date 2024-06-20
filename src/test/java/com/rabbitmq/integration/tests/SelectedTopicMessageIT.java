// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2024 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.util.Shell;
import org.junit.jupiter.api.Test;

import jakarta.jms.*;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test
 */
public class SelectedTopicMessageIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.topic." + SelectedTopicMessageIT.class.getCanonicalName();

    @Test
    public void testSendAndReceiveTextMessages() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        TestConfiguration[] configurations = new TestConfiguration[]{
                new TestConfiguration(
                        topicSession.createSubscriber(topic, "boolProp", false),
                        "boolean property set to false",
                        message -> message.setBooleanProperty("boolProp", true)
                ),
                new TestConfiguration(
                        topicSession.createSubscriber(topic, "not boolProp", false),
                        "boolean property set to true",
                        message -> message.setBooleanProperty("boolProp", false)
                ),
                new TestConfiguration(
                        topicSession.createSubscriber(topic, "someField LIKE 'some%'", false),
                        "string property matching a like operator starting with 'some'",
                        message -> message.setStringProperty("someField", "some")
                ),
                new TestConfiguration(
                        topicSession.createSubscriber(topic, "someField LIKE 'test%'", false),
                        "string property matching a like operator starting with 'test'",
                        message -> message.setStringProperty("someField", "test")
                )
        };

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        for (TestConfiguration configuration : configurations) {
            TextMessage message = topicSession.createTextMessage(configuration.message);
            configuration.messageConfigurator.configure(message);
            sender.send(message);
        }

        for (TestConfiguration configuration : configurations) {
            TextMessage message = (TextMessage) configuration.subscriber.receive(1000);
            assertEquals(configuration.message, message.getText());
        }
    }

    @Test
    public void durableTopicSubscriberWithSelectorCreatesExchangesBetweenRestarts() throws Exception {
        String topicName = TOPIC_NAME + UUID.randomUUID().toString().substring(0, 10);
        int exchangeInitialCount = exchangeCount();
        String subscriberName = UUID.randomUUID().toString();
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(topicName);
        TopicSubscriber receiver = topicSession.createDurableSubscriber(
            topic, subscriberName, "boolProp", false);
        TopicPublisher sender = topicSession.createPublisher(topic);
        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage("hello true");
        message.setBooleanProperty("boolProp", true);
        sender.send(message);

        assertThat(receiver.receive(1000)).isNotNull();
        assertThat(exchangeCount()).isEqualTo(exchangeInitialCount + 1);

        sender.close();
        receiver.close();

        reconnect();

        topicConn.start();
        topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        topic = topicSession.createTopic(topicName);
        receiver = topicSession.createDurableSubscriber(
            topic, subscriberName, "boolProp", false);
        sender = topicSession.createPublisher(topic);
        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        message = topicSession.createTextMessage("hello true");
        message.setBooleanProperty("boolProp", true);
        sender.send(message);

        assertThat(receiver.receive(1000)).isNotNull();

        assertThat(exchangeCount()).isEqualTo(exchangeInitialCount + 2);
    }

    private static int exchangeCount() throws IOException {
        return Shell.listExchanges().size();
    }

    @FunctionalInterface
    private interface MessageConfigurator {

        void configure(Message message) throws Exception;

    }

    private static class TestConfiguration {

        TopicSubscriber subscriber;
        String message;
        MessageConfigurator messageConfigurator;

        public TestConfiguration(TopicSubscriber subscriber, String message, MessageConfigurator messageConfigurator) {
            this.subscriber = subscriber;
            this.message = message;
            this.messageConfigurator = messageConfigurator;
        }
    }
}
