package com.rabbitmq.integration.tests;

import javax.jms.DeliveryMode;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import junit.framework.Assert;

import org.junit.Test;


/**
 * Integration test
 */
public class SimpleTopicMessageIT {
    final String TOPIC_NAME = "test.topic."+SimpleTopicMessageIT.class.getCanonicalName();
    @Test
    public void testSendAndReceiveTextMessage() throws Exception {
        final String MESSAGE2 = "Hello " + SimpleTopicMessageIT.class.getName();
        TopicConnectionFactory connFactory = (TopicConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        TopicConnection topicConn = connFactory.createTopicConnection();
        try {
            topicConn.start();
            TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(TOPIC_NAME);
            TopicPublisher sender = topicSession.createPublisher(topic);
            TopicSubscriber receiver1 = topicSession.createSubscriber(topic);
            TopicSubscriber receiver2 = topicSession.createSubscriber(topic);

            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = topicSession.createTextMessage(MESSAGE2);
            sender.send(message);

            Assert.assertEquals(MESSAGE2, ((TextMessage) receiver1.receive()).getText());
            Assert.assertEquals(MESSAGE2, ((TextMessage) receiver2.receive()).getText());
        } finally {
            topicConn.close();
        }

    }
}
