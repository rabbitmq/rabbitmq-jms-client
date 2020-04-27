/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.util.Shell;
import org.junit.jupiter.api.Test;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test
 */
public class SimpleTopicMessageIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.topic." + SimpleTopicMessageIT.class.getCanonicalName();
    private static final String WILD_TOPIC_NAME = "test.topic.#";
    private static final String MESSAGE_TEXT_1 = "Hello " + SimpleTopicMessageIT.class.getName();
    private static final String MESSAGE_TEXT_2 = "Wild hello " + SimpleTopicMessageIT.class.getName();

    static List<Shell.Binding> filterOnRoutingKey(List<Shell.Binding> bindings, String routingKey) {
        List<Shell.Binding> results = new ArrayList<Shell.Binding>();
        for (Shell.Binding binding : bindings) {
            if (binding.getRoutingKey().equals(routingKey)) {
                results.add(binding);
            }
        }
        return results;
    }

    static List<Shell.Binding> filterOnDestination(List<Shell.Binding> bindings, String destination) {
        List<Shell.Binding> results = new ArrayList<Shell.Binding>();
        for (Shell.Binding binding : bindings) {
            if (binding.getDestination().equals(destination)) {
                results.add(binding);
            }
        }
        return results;
    }

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

        // check only one binding to each subscriber AMQP queue is created
        // see https://github.com/rabbitmq/rabbitmq-jms-client/issues/72
        List<Shell.Binding> bindings = Shell.listBindings(false);
        List<Shell.Binding> bindingsToTopic = filterOnRoutingKey(bindings, TOPIC_NAME);
        assertEquals(2, bindingsToTopic.size());
        for (Shell.Binding b : bindingsToTopic) {
            String subscriberAmqpQueue = b.getDestination();
            List<Shell.Binding> subscriberAmqpQueueBinding = filterOnDestination(bindings, subscriberAmqpQueue);
            assertEquals(1, subscriberAmqpQueueBinding.size());
        }
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
        assertEquals(MESSAGE_TEXT_2, textMessage.getText(), "Failed to receive proper text message");
        assertEquals(explicitTopicDestination, (Topic) textMessage.getJMSDestination(), "Incorrect publisher destination on received message");
    }
}
