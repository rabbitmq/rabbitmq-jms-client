/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    public void testSendAndReceiveAcrossConnections() throws Exception {
        topicConn.start();
        {
            TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(TOPIC_NAME);
            TopicPublisher sender = topicSession.createPublisher(topic);

            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
            sender.send(message);
        }

        reconnect();

        {
            TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(TOPIC_NAME);
            TopicSubscriber receiver = topicSession.createDurableSubscriber(topic, DURABLE_SUBSCRIBER_NAME);

            TextMessage tm = (TextMessage) receiver.receive(RECEIVE_TIMEOUT);
            assertNotNull("No message received", tm);
            assertEquals(MESSAGE_TEXT_1, tm.getText());

            topicSession.unsubscribe(DURABLE_SUBSCRIBER_NAME);
        }
    }

}
