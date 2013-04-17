/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import javax.jms.DeliveryMode;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import junit.framework.Assert;

import org.junit.Test;

import com.rabbitmq.jms.client.message.RMQTextMessage;

/**
 * Integration test
 */
public class SelectedTopicMessageIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.topic." + SelectedTopicMessageIT.class.getCanonicalName();
    private static final String MESSAGE1 = "Hello (1) " + SelectedTopicMessageIT.class.getName();
    private static final String MESSAGE2 = "Hello (2) " + SelectedTopicMessageIT.class.getName();

    @Test
    public void testSendAndReceiveTextMessages() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        TopicSubscriber receiver1 = topicSession.createSubscriber(topic, "boolProp", false);
        TopicSubscriber receiver2 = topicSession.createSubscriber(topic, "not boolProp", false);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        TextMessage message1 = topicSession.createTextMessage(MESSAGE1);
        TextMessage message2 = topicSession.createTextMessage(MESSAGE2);

        message1.setBooleanProperty("boolProp", true);
        message2.setBooleanProperty("boolProp", false);

        sender.send(message1);
        sender.send(message2);

        RMQTextMessage tmsg1 = (RMQTextMessage) receiver1.receive();
        RMQTextMessage tmsg2 = (RMQTextMessage) receiver2.receive();

        String t1 = tmsg1.getText();
        String t2 = tmsg2.getText();
        Assert.assertEquals(MESSAGE1, t1);
        Assert.assertEquals(MESSAGE2, t2);
    }
}
