/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

import org.junit.jupiter.api.Test;

import javax.jms.DeliveryMode;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test
 */
public class SimpleDurableTopicMessageIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.durabletopic." + SimpleDurableTopicMessageIT.class.getCanonicalName();

    private static final String DURABLE_SUBSCRIBER_NAME = "test.durabletopic.subname";

    private static final int RECEIVE_TIMEOUT = 1000; // ms

    private static final String MESSAGE_TEXT_1 = "Hello " + SimpleDurableTopicMessageIT.class.getName();

    @Test
    public void testSendAndReceiveTextMessage() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        TopicSubscriber receiver1 = topicSession.createDurableSubscriber(topic, DURABLE_SUBSCRIBER_NAME);
        TopicSubscriber receiver2 = topicSession.createSubscriber(topic);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
        sender.send(message);

        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver1.receive()).getText());
        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver2.receive()).getText());

        topicSession.unsubscribe(DURABLE_SUBSCRIBER_NAME);
    }

    @Test
    public void testSendAndReceiveAfterReconnect() throws Exception {
        topicConn.start();
        {
            TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(TOPIC_NAME);
            TopicPublisher sender = topicSession.createPublisher(topic);
            topicSession.createDurableSubscriber(topic, DURABLE_SUBSCRIBER_NAME); // ignore receiver

            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
            sender.send(message);
        }

        reconnect();

        topicConn.start();
        {
            TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(TOPIC_NAME);
            TopicSubscriber receiver = topicSession.createDurableSubscriber(topic, DURABLE_SUBSCRIBER_NAME);

            TextMessage tm = (TextMessage) receiver.receive(RECEIVE_TIMEOUT);
            assertNotNull(tm, "No message received");
            assertEquals(MESSAGE_TEXT_1, tm.getText());

            topicSession.unsubscribe(DURABLE_SUBSCRIBER_NAME);
        }
    }

}
