/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;

import javax.jms.DeliveryMode;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.junit.Test;

/**
 * Integration test
 */
public class SimpleTopicMessageIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.topic." + SimpleTopicMessageIT.class.getCanonicalName();
    private static final String WILD_TOPIC_NAME = "test.topic.#";
    private static final String MESSAGE_TEXT_1 = "Hello " + SimpleTopicMessageIT.class.getName();
    private static final String MESSAGE_TEXT_2 = "Wild hello " + SimpleTopicMessageIT.class.getName();

    @Test
    public void testSendAndReceiveTextMessage() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        TopicSubscriber receiver1 = topicSession.createSubscriber(topic);
        TopicSubscriber receiver2 = topicSession.createSubscriber(topic);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
        sender.send(message);

        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver1.receive()).getText());
        assertEquals(MESSAGE_TEXT_1, ((TextMessage) receiver2.receive()).getText());
    }

    @Test
    public void testSendAndReceiveWildTopicTestMessage() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic explicitTopicDestination = topicSession.createTopic(TOPIC_NAME);
        Topic wildTopicDestination = topicSession.createTopic(WILD_TOPIC_NAME);

        TopicPublisher sender = topicSession.createPublisher(explicitTopicDestination);
        TopicSubscriber receiver = topicSession.createSubscriber(wildTopicDestination);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_2);
        sender.send(message);

        TextMessage textMessage = (TextMessage) receiver.receive();
        assertEquals("Failed to receive proper text message", MESSAGE_TEXT_2, textMessage.getText());
        assertEquals("Incorrect publisher destination on received message", explicitTopicDestination, (Topic) textMessage.getJMSDestination());
    }
}
