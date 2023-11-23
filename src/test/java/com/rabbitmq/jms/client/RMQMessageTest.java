// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQTextMessage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RMQMessageTest {

    RMQSession session;
    GetResponse getResponse;
    ReceivingContextConsumer consumer;

    @BeforeEach public void init() {
        session = mock(RMQSession.class);
        getResponse = mock(GetResponse.class);
        consumer = mock(ReceivingContextConsumer.class);

        when(session.getReplyToStrategy()).thenReturn(DefaultReplyToStrategy.INSTANCE);
    }


    @Test
    @DisplayName("RMQMessage::toAmqpHeaders should convert all properties to amqp headers")
    void convertsAllTypesToAmqpHeaders() throws JMSException, IOException {
        RMQMessage textMessage = new RMQTextMessage();
        textMessage.setJMSDeliveryMode(1);
        textMessage.setJMSPriority(1);
        textMessage.setStringProperty("string", "string");
        textMessage.setIntProperty("int", 42);
        textMessage.setFloatProperty("float", 1337f);
        textMessage.setDoubleProperty("double", 3.14d);
        textMessage.setLongProperty("long", 1L);
        textMessage.setShortProperty("short", (short) 4);
        textMessage.setByteProperty("byte", (byte) 2);
        textMessage.setBooleanProperty("boolean", true);

        Map<String, Object> amqpHeaders = textMessage.toAmqpHeaders();

        assertThat(amqpHeaders).contains(
                entry("string", "string"),
                entry("int", 42),
                entry("float", 1337f),
                entry("double", 3.14d),
                entry("long", 1L),
                entry("short", (short) 4),
                entry("byte", (byte) 2),
                entry("boolean", true)
        );
    }


    @Test
    @DisplayName("RMQMessage::convertMessage - amqp message - ensure JMS reply to is null with no replyto")
    void convertAMQPMessageWithNoReplyTo() throws JMSException {

        BasicProperties props = mock(BasicProperties.class);
        Envelope envelope = mock(Envelope.class);

        when(getResponse.getProps()).thenReturn(props);
        when(getResponse.getEnvelope()).thenReturn(envelope);
        when(envelope.isRedeliver()).thenReturn(false);

        RMQDestination destination = new RMQDestination("dest", "exch", "key", "queue");
        destination.setAmqp(true);

        RMQMessage result = RMQMessage.convertMessage(session, destination, getResponse, consumer);

        assertNull(result.getJMSReplyTo());
    }

    @Test
    @DisplayName("RMQMessage::convertMessage - amqp message - JMSXDeliveryCount is set to default if no header is available")
    void convertAMQPMessageWithRedeliverFlagAndNoHeaders() throws JMSException {

        BasicProperties props = mock(BasicProperties.class);
        Envelope envelope = mock(Envelope.class);

        when(getResponse.getProps()).thenReturn(props);
        when(props.getHeaders()).thenReturn(null);
        when(getResponse.getEnvelope()).thenReturn(envelope);
        when(envelope.isRedeliver()).thenReturn(true);

        RMQDestination destination = new RMQDestination("dest", "exch", "key", "queue");
        destination.setAmqp(true);

        RMQMessage result = RMQMessage.convertMessage(session, destination, getResponse, consumer);

        // if no header information (=null) is given, fall back to default delivery count
        assertEquals(2, result.getIntProperty("JMSXDeliveryCount"));
    }
    
    @Test
    @DisplayName("RMQMessage::convertMessage - amqp message - ensure JMS reply to is set to direct reply to")
    void convertAMQPMessageWithDirectReplyTo() throws JMSException {

        BasicProperties props = mock(BasicProperties.class);
        Envelope envelope = mock(Envelope.class);

        when(getResponse.getProps()).thenReturn(props);
        when(getResponse.getEnvelope()).thenReturn(envelope);
        when(envelope.isRedeliver()).thenReturn(false);
        when(session.getReplyToStrategy()).thenReturn(DefaultReplyToStrategy.INSTANCE);

        when(props.getReplyTo()).thenReturn("amq.rabbitmq.reply-to");

        RMQDestination destination = new RMQDestination("dest", "exch", "key", "queue");
        destination.setAmqp(true);

        RMQMessage result = RMQMessage.convertMessage(session, destination, getResponse, consumer);

        assertNotNull(result.getJMSReplyTo());

        RMQDestination expected = new RMQDestination("amq.rabbitmq.reply-to", "", "amq.rabbitmq.reply-to", "amq.rabbitmq.reply-to");
        assertEquals(expected, result.getJMSReplyTo());
    }

    @Test
    @DisplayName("RMQMessage::convertMessage - amqp message - ensure JMS reply to is set to direct reply to")
    void convertAMQPMessageWithDirectReplyToNoExplicityReplyToStrategy() throws JMSException {

        BasicProperties props = mock(BasicProperties.class);
        Envelope envelope = mock(Envelope.class);

        when(getResponse.getProps()).thenReturn(props);
        when(getResponse.getEnvelope()).thenReturn(envelope);
        when(envelope.isRedeliver()).thenReturn(false);

        when(props.getReplyTo()).thenReturn("amq.rabbitmq.reply-to");

        RMQDestination destination = new RMQDestination("dest", "exch", "key", "queue");
        destination.setAmqp(true);

        RMQMessage result = RMQMessage.convertMessage(session, destination, getResponse, consumer);

        assertNotNull(result.getJMSReplyTo());

        RMQDestination expected = new RMQDestination("amq.rabbitmq.reply-to", "", "amq.rabbitmq.reply-to", "amq.rabbitmq.reply-to");
        assertEquals(expected, result.getJMSReplyTo());
    }

    @Test
    @DisplayName("RMQMessage::convertMessage - amqp message - ensure JMS reply to is set to the forwarded direct reply to")
    void convertAMQPMessageWithForwardedDirectReplyTo() throws JMSException {

        BasicProperties props = mock(BasicProperties.class);
        Envelope envelope = mock(Envelope.class);

        when(getResponse.getProps()).thenReturn(props);
        when(getResponse.getEnvelope()).thenReturn(envelope);
        when(envelope.isRedeliver()).thenReturn(false);
        when(session.getReplyToStrategy()).thenReturn(DefaultReplyToStrategy.INSTANCE);
        when(props.getReplyTo()).thenReturn("amq.rabbitmq.reply-to-forwarded-id");

        RMQDestination destination = new RMQDestination("dest", "exch", "key", "queue");
        destination.setAmqp(true);

        RMQMessage result = RMQMessage.convertMessage(session, destination, getResponse, consumer);

        assertNotNull(result.getJMSReplyTo());

        RMQDestination expected = new RMQDestination("amq.rabbitmq.reply-to", "", "amq.rabbitmq.reply-to-forwarded-id", "amq.rabbitmq.reply-to-forwarded-id");
        assertEquals(expected, result.getJMSReplyTo());
    }

    @Test
    @DisplayName("RMQMessage::convertMessage - amqp message - ensure JMS reply to is set to the non direct reply to")
    void convertAMQPMessageWithNonDirectReplyTo() throws JMSException {

        BasicProperties props = mock(BasicProperties.class);
        Envelope envelope = mock(Envelope.class);

        when(getResponse.getProps()).thenReturn(props);
        when(getResponse.getEnvelope()).thenReturn(envelope);
        when(envelope.isRedeliver()).thenReturn(false);

        when(props.getReplyTo()).thenReturn("non-direct-replyto");

        RMQDestination destination = new RMQDestination("dest", "exch", "key", "queue");
        destination.setAmqp(true);

        RMQMessage result = RMQMessage.convertMessage(session, destination, getResponse, consumer);

        assertNull(result.getJMSReplyTo());
    }
}
