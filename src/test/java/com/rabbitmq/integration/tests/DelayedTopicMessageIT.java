// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.util.Shell;
import javax.jms.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test
 */
public class DelayedTopicMessageIT extends AbstractITTopic {

    private static final String MESSAGE = "Hello " + DelayedTopicMessageIT.class.getName();

    private static final String TOPIC_NAME = "delay.topic." + DelayedTopicMessageIT.class.getCanonicalName();
    private static final String WILD_TOPIC_NAME = "delay.topic.#";
    private static final String MESSAGE_TEXT_1 = "Hello " + DelayedTopicMessageIT.class.getName();
    private static final String MESSAGE_TEXT_2 = "Wild hello " + DelayedTopicMessageIT.class.getName();

    @BeforeEach
    public void beforeMethod() throws IOException {
        try {
            Shell.rabbitmqPlugins("is_enabled rabbitmq_delayed_message_exchange");
        }catch(Exception e) {
            System.out.println("Skipped DelayedMessageIT. Plugin rabbitmq_delayed_message_exchange is not enabled");
            Assumptions.assumeTrue(false);
        }
    }
    @Test
    public void testSendMessageToTopicWithDelayedPublisher() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        sender.setDeliveryDelay(4000L);
        TopicSubscriber receiver1 = topicSession.createSubscriber(topic);

        // TODO probably ensure the topic is empty
        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
        sender.send(message);

        assertNull(receiver1.receive(2000L));
        TextMessage receivedMessage = (TextMessage)receiver1.receive();
        assertTrue(receivedMessage.getJMSDeliveryTime() > 0);
        assertEquals(MESSAGE_TEXT_1, receivedMessage.getText());

    }


}
