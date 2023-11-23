// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.jms.DeliveryMode;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.junit.jupiter.api.Test;

import com.rabbitmq.jms.client.message.RMQTextMessage;

/**
 * Integration test
 */
public class SelectedTopicMessageIdentifierIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.topic." + SelectedTopicMessageIdentifierIT.class.getCanonicalName();
    private static final String MESSAGE1 = "Hello (1) " + SelectedTopicMessageIdentifierIT.class.getName();
    private static final String MESSAGE2 = "Hello (2) " + SelectedTopicMessageIdentifierIT.class.getName();

    @Test
    public void testSendAndReceiveTextMessagesWithSelectorsAndPeriodIdentifiers() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        TopicSubscriber receiver1 = topicSession.createSubscriber(topic, "period.bool.prop", false);
        TopicSubscriber receiver2 = topicSession.createSubscriber(topic, "not period.bool.prop", false);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        TextMessage message1 = topicSession.createTextMessage(MESSAGE1);
        TextMessage message2 = topicSession.createTextMessage(MESSAGE2);

        message1.setBooleanProperty("period.bool.prop", true);
        message2.setBooleanProperty("period.bool.prop", false);

        sender.send(message1);
        sender.send(message2);

        RMQTextMessage tmsg1 = (RMQTextMessage) receiver1.receive();
        RMQTextMessage tmsg2 = (RMQTextMessage) receiver2.receive();

        String t1 = tmsg1.getText();
        String t2 = tmsg2.getText();
        assertEquals(MESSAGE1, t1);
        assertEquals(MESSAGE2, t2);
    }
}
