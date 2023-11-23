// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import javax.jms.*;
import com.rabbitmq.TestUtils.SkipIfDelayedMessageExchangePluginNotActivated;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test
 */
@SkipIfDelayedMessageExchangePluginNotActivated
public class DelayedTopicMessageIT extends AbstractITTopic {

    private static final String TOPIC_NAME = "delay.topic." + DelayedTopicMessageIT.class.getCanonicalName();
    private static final String MESSAGE_TEXT_1 = "Hello " + DelayedTopicMessageIT.class.getName();


    @Test
    public void testSendMessageToTopicWithDelayedPublisher() throws Exception {
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        sender.setDeliveryDelay(4000L);
        TopicSubscriber receiver = topicSession.createSubscriber(topic);

        // TODO probably ensure the topic is empty
        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE_TEXT_1);
        sender.send(message);

        assertNull(receiver.receive(2000L));
        TextMessage receivedMessage = (TextMessage)receiver.receive();
        assertTrue(receivedMessage.getJMSDeliveryTime() > 0);
        assertEquals(MESSAGE_TEXT_1, receivedMessage.getText());

    }


}
