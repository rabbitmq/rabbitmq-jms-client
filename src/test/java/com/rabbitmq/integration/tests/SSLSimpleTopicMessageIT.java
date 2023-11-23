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
import com.rabbitmq.TestUtils.SkipIfTlsNotActivated;

import org.junit.jupiter.api.Test;

/**
 * Integration test
 */
@SkipIfTlsNotActivated
public class SSLSimpleTopicMessageIT extends AbstractITTopicSSL {
    private static final String TOPIC_NAME = "test.topic." + SSLSimpleTopicMessageIT.class.getCanonicalName();

    @Test
    public void testSendAndReceiveTextMessage() throws Exception {
        final String MESSAGE2 = "Hello " + SSLSimpleTopicMessageIT.class.getName();
        topicConn.start();
        TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topic = topicSession.createTopic(TOPIC_NAME);
        TopicPublisher sender = topicSession.createPublisher(topic);
        TopicSubscriber receiver1 = topicSession.createSubscriber(topic);
        TopicSubscriber receiver2 = topicSession.createSubscriber(topic);

        sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = topicSession.createTextMessage(MESSAGE2);
        sender.send(message);

        assertEquals(MESSAGE2, ((TextMessage) receiver1.receive()).getText());
        assertEquals(MESSAGE2, ((TextMessage) receiver2.receive()).getText());
    }
}
