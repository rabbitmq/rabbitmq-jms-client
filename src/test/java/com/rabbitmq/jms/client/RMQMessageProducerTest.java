// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2017-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import javax.jms.CompletionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class RMQMessageProducerTest {

    RMQSession session;
    RMQDestination destination;
    Channel channel;

    @BeforeEach public void init() {
        session = mock(RMQSession.class);
        destination = mock(RMQDestination.class);
        channel = mock(Channel.class);
    }

    @Test public void preferProducerPropertyNoMessagePropertySpecified() throws Exception {
        StubRMQMessageProducer producer = new StubRMQMessageProducer(
            session, destination, true
        );
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setPriority(9);
        producer.setTimeToLive(1000L);
        producer.setDeliveryDelay(5000L);
        RMQTextMessage message = new RMQTextMessage();
        producer.send(message);

        assertEquals(DeliveryMode.NON_PERSISTENT, message.getJMSDeliveryMode());
        assertEquals(9, message.getJMSPriority());
        assertTrue(message.getJMSExpiration() > System.currentTimeMillis());
        assertTrue(message.getJMSDeliveryTime() > producer.getDeliveryDelay());
    }



    @Test public void preferProducerPropertyMessagePropertiesSpecified() throws Exception {
        StubRMQMessageProducer producer = new StubRMQMessageProducer(
            session, destination, true
        );
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setPriority(9);
        producer.setTimeToLive(1000L);
        producer.setDeliveryDelay(5000L);
        RMQTextMessage message = new RMQTextMessage();
        message.setJMSPriority(1);
        message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
        message.setJMSExpiration(System.currentTimeMillis() + 10 * 1000);
        long unexpectedDeliveryTime = System.currentTimeMillis() + producer.getDeliveryDelay() * 2;
        message.setJMSDeliveryTime(unexpectedDeliveryTime);
        producer.send(message);

        assertEquals(DeliveryMode.NON_PERSISTENT, message.getJMSDeliveryMode());
        assertEquals(9, message.getJMSPriority());
        assertTrue(message.getJMSExpiration() > System.currentTimeMillis());
        assertTrue(message.getJMSDeliveryTime() > producer.getDeliveryDelay());
        assertTrue(message.getJMSDeliveryTime() < unexpectedDeliveryTime);
    }

    @Test public void preferMessagePropertyNoMessagePropertySpecified() throws Exception {
        StubRMQMessageProducer producer = new StubRMQMessageProducer(
            session, destination, false
        );
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setPriority(9);
        producer.setTimeToLive(1000L);
        producer.setDeliveryDelay(5000L);
        RMQTextMessage message = new RMQTextMessage();
        producer.send(message);

        assertEquals(DeliveryMode.NON_PERSISTENT, message.getJMSDeliveryMode());
        assertEquals(9, message.getJMSPriority());
        assertTrue(message.getJMSExpiration() > System.currentTimeMillis());
        assertTrue(message.getJMSDeliveryTime() > System.currentTimeMillis());
    }

    @Test public void preferMessagePropertyMessagePropertiesSpecified() throws Exception {
        StubRMQMessageProducer producer = new StubRMQMessageProducer(
            session, destination, false
        );
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setPriority(9);
        producer.setTimeToLive(1000L);
        RMQTextMessage message = new RMQTextMessage();
        message.setJMSPriority(1);
        message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
        long expiration = System.currentTimeMillis() + 10 * 1000;
        message.setJMSExpiration(expiration);
        long deliveryTime = expiration + 1000;
        message.setJMSDeliveryTime(deliveryTime);
        producer.send(message);

        assertEquals(DeliveryMode.PERSISTENT, message.getJMSDeliveryMode());
        assertEquals(1, message.getJMSPriority());
        assertEquals(expiration, message.getJMSExpiration());
        assertEquals(deliveryTime, message.getJMSDeliveryTime());
    }

    @Test public void sendDelayedMessageToAMQPDestination() throws JMSException, IOException {
        RMQBytesMessage bytesMessage = new RMQBytesMessage();        //RMQBytesMessage || RMQTextMessage
        RMQDestination amqpDestination = new RMQDestination("some-direct-exchange", "some-direct-exchange",
                "some-routing-key", "some-queue");
        assertTrue(amqpDestination.isAmqp());

        doReturn("x").when(session).delayMessage(eq(amqpDestination), anyMap(), eq(100L));
        Channel channel = Mockito.mock(Channel.class);
        doReturn(channel).when(session).getChannel();
        RMQMessageProducer producer = new RMQMessageProducer(session, amqpDestination, false);
        producer.setDeliveryDelay(100L);
        producer.send(bytesMessage);

        verify(channel).basicPublish(eq("x"), anyString(), any(AMQP.BasicProperties.class), any(byte[].class));
    }

    @Test public void sendDelayedMessageToJMSDestination() throws JMSException, IOException {
        RMQBytesMessage bytesMessage = new RMQBytesMessage();        //RMQBytesMessage || RMQTextMessage
        RMQDestination jmsQueueDestination = new RMQDestination("some-queue", true, false);
        assertFalse(jmsQueueDestination.isAmqp());

        doReturn("x").when(session).delayMessage(eq(jmsQueueDestination), anyMap(), eq(100L));
        Channel channel = Mockito.mock(Channel.class);
        doReturn(channel).when(session).getChannel();
        RMQMessageProducer producer = new RMQMessageProducer(session, jmsQueueDestination, false);
        producer.setDeliveryDelay(100L);
        producer.send(bytesMessage);

        verify(session).declareDestinationIfNecessary(jmsQueueDestination);
        verify(channel).basicPublish(eq("x"), anyString(), any(AMQP.BasicProperties.class), any(byte[].class));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("RMQMessageProducer::send should ensure that an AMQP message has the correlation id on the rabbit message")
    public void sendAMQPMessageWithCorrelationIdCopy() throws JMSException, IOException {

        try (RMQMessageProducer producer = new RMQMessageProducer(session, destination)) {
            RMQTextMessage message = new RMQTextMessage();
            message.setText("Test message");
            message.setJMSCorrelationID("TESTID");

            when(session.getChannel()).thenReturn(channel);
            when(session.delayMessage(eq(destination), any(Map.class), any(Long.class))).thenReturn("targetExch");

            when(destination.isAmqp()).thenReturn(true);
            when(destination.isAmqpWritable()).thenReturn(true);
            when(destination.getAmqpRoutingKey()).thenReturn("key");

            producer.send(message);

            ArgumentCaptor<com.rabbitmq.client.AMQP.BasicProperties> propCapture = ArgumentCaptor.forClass(com.rabbitmq.client.AMQP.BasicProperties.class);

            verify(channel).basicPublish(eq("targetExch"), eq("key"), propCapture.capture(), eq("Test message".getBytes()));

            assertEquals("TESTID", propCapture.getValue().getCorrelationId());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("RMQMessageProducer::send should ensure that a JMS message has the correlation id on the rabbit message")
    public void sendJMSMessageWithCorrelationIdCopy() throws JMSException, IOException {

        try (RMQMessageProducer producer = new RMQMessageProducer(session, destination)) {
            RMQMessage message = mock(RMQMessage.class);

            when(message.toByteArray()).thenReturn("Test message".getBytes());
            when(message.getJMSCorrelationID()).thenReturn("TESTID");

            when(session.getChannel()).thenReturn(channel);
            when(session.delayMessage(eq(destination), any(Map.class), any(Long.class))).thenReturn("targetExch");

            when(destination.isAmqp()).thenReturn(false);
            when(destination.isAmqpWritable()).thenReturn(true);
            when(destination.getAmqpRoutingKey()).thenReturn("key");

            producer.send(message);

            ArgumentCaptor<com.rabbitmq.client.AMQP.BasicProperties> propCapture = ArgumentCaptor.forClass(com.rabbitmq.client.AMQP.BasicProperties.class);

            verify(channel).basicPublish(eq("targetExch"), eq("key"), propCapture.capture(), eq("Test message".getBytes()));

            assertEquals("TESTID", propCapture.getValue().getCorrelationId());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("RMQMessageProducer::send should ensure that an AMQP message with a direct reply to is handled correctly")
    public void sendAMQPMessageWithDirectReplyTo() throws JMSException, IOException {

        try (RMQMessageProducer producer = new RMQMessageProducer(session, destination)) {
            RMQTextMessage message = new RMQTextMessage();
            message.setText("Test message");
            message.setJMSCorrelationID("TESTID");
            message.setJMSReplyTo(new RMQDestination("amq.rabbitmq.reply-to", "", "amq.rabbitmq.reply-to", "amq.rabbitmq.reply-to"));

            when(session.getChannel()).thenReturn(channel);
            when(session.delayMessage(eq(destination), any(Map.class), any(Long.class))).thenReturn("targetExch");

            when(destination.isAmqp()).thenReturn(true);
            when(destination.isAmqpWritable()).thenReturn(true);
            when(destination.getAmqpRoutingKey()).thenReturn("key");

            producer.send(message);

            ArgumentCaptor<com.rabbitmq.client.AMQP.BasicProperties> propCapture = ArgumentCaptor.forClass(com.rabbitmq.client.AMQP.BasicProperties.class);

            verify(channel).basicPublish(eq("targetExch"), eq("key"), propCapture.capture(), eq("Test message".getBytes()));

            assertEquals("amq.rabbitmq.reply-to", propCapture.getValue().getReplyTo());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("RMQMessageProducer::send should ensure that an AMQP message with a forwarded direct reply to is handled correctly")
    public void sendAMQPMessageWithDirectReplyToForwardedId() throws JMSException, IOException {

        try (RMQMessageProducer producer = new RMQMessageProducer(session, destination)) {
            RMQTextMessage message = new RMQTextMessage();
            message.setText("Test message");
            message.setJMSCorrelationID("TESTID");
            message.setJMSReplyTo(new RMQDestination("amq.rabbitmq.reply-to", "", "amq.rabbitmq.reply-to-replyToID", "amq.rabbitmq.reply-to"));

            when(session.getChannel()).thenReturn(channel);
            when(session.delayMessage(eq(destination), any(Map.class), any(Long.class))).thenReturn("targetExch");

            when(destination.isAmqp()).thenReturn(true);
            when(destination.isAmqpWritable()).thenReturn(true);
            when(destination.getAmqpRoutingKey()).thenReturn("key");

            producer.send(message);

            ArgumentCaptor<com.rabbitmq.client.AMQP.BasicProperties> propCapture = ArgumentCaptor.forClass(com.rabbitmq.client.AMQP.BasicProperties.class);

            verify(channel).basicPublish(eq("targetExch"), eq("key"), propCapture.capture(), eq("Test message".getBytes()));

            assertEquals("amq.rabbitmq.reply-to-replyToID", propCapture.getValue().getReplyTo());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("RMQMessageProducer::send should ensure that an AMQP message with a non-direct reply to is handled correctly")
    public void sendAMQPMessageWithGenericReplyTo() throws JMSException, IOException {

        try (RMQMessageProducer producer = new RMQMessageProducer(session, destination)) {
            RMQTextMessage message = new RMQTextMessage();
            message.setText("Test message");
            message.setJMSCorrelationID("TESTID");
            message.setJMSReplyTo(new RMQDestination("other-replyto", "exch", "other-replyto", "other-replyto"));

            when(session.getChannel()).thenReturn(channel);
            when(session.delayMessage(eq(destination), any(Map.class), any(Long.class))).thenReturn("targetExch");

            when(destination.isAmqp()).thenReturn(true);
            when(destination.isAmqpWritable()).thenReturn(true);
            when(destination.getAmqpRoutingKey()).thenReturn("key");

            producer.send(message);

            ArgumentCaptor<com.rabbitmq.client.AMQP.BasicProperties> propCapture = ArgumentCaptor.forClass(com.rabbitmq.client.AMQP.BasicProperties.class);

            verify(channel).basicPublish(eq("targetExch"), eq("key"), propCapture.capture(), eq("Test message".getBytes()));

            assertEquals("other-replyto", propCapture.getValue().getReplyTo());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("RMQMessageProducer::send should ensure that a JMS message with a direct reply to is handled correctly")
    public void sendJMSMessageWithDirectReplyTo() throws JMSException, IOException {

        try (RMQMessageProducer producer = new RMQMessageProducer(session, destination)) {
            RMQMessage message = mock(RMQMessage.class);

            when(message.toByteArray()).thenReturn("Test message".getBytes());
            when(message.getJMSCorrelationID()).thenReturn("TESTID");
            when(message.getJMSReplyTo()).thenReturn(new RMQDestination("amq.rabbitmq.reply-to", "", "amq.rabbitmq.reply-to", "amq.rabbitmq.reply-to"));


            when(session.getChannel()).thenReturn(channel);
            when(session.delayMessage(eq(destination), any(Map.class), any(Long.class))).thenReturn("targetExch");

            when(destination.isAmqp()).thenReturn(false);
            when(destination.isAmqpWritable()).thenReturn(true);
            when(destination.getAmqpRoutingKey()).thenReturn("key");

            producer.send(message);

            ArgumentCaptor<com.rabbitmq.client.AMQP.BasicProperties> propCapture = ArgumentCaptor.forClass(com.rabbitmq.client.AMQP.BasicProperties.class);

            verify(channel).basicPublish(eq("targetExch"), eq("key"), propCapture.capture(), eq("Test message".getBytes()));

            assertEquals("amq.rabbitmq.reply-to", propCapture.getValue().getReplyTo());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("RMQMessageProducer::send should ensure that a JMS message with a forwarded direct reply to is handled correctly")
    public void sendJMSMessageWithDirectReplyToWithForwardedId() throws JMSException, IOException {

        try (RMQMessageProducer producer = new RMQMessageProducer(session, destination)) {
            RMQMessage message = mock(RMQMessage.class);

            when(message.toByteArray()).thenReturn("Test message".getBytes());
            when(message.getJMSCorrelationID()).thenReturn("TESTID");
            when(message.getJMSReplyTo()).thenReturn(new RMQDestination("amq.rabbitmq.reply-to", "", "amq.rabbitmq.reply-to-forwarded-id", "amq.rabbitmq.reply-to"));


            when(session.getChannel()).thenReturn(channel);
            when(session.delayMessage(eq(destination), any(Map.class), any(Long.class))).thenReturn("targetExch");

            when(destination.isAmqp()).thenReturn(false);
            when(destination.isAmqpWritable()).thenReturn(true);
            when(destination.getAmqpRoutingKey()).thenReturn("key");

            producer.send(message);

            ArgumentCaptor<com.rabbitmq.client.AMQP.BasicProperties> propCapture = ArgumentCaptor.forClass(com.rabbitmq.client.AMQP.BasicProperties.class);

            verify(channel).basicPublish(eq("targetExch"), eq("key"), propCapture.capture(), eq("Test message".getBytes()));

            assertEquals("amq.rabbitmq.reply-to-forwarded-id", propCapture.getValue().getReplyTo());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("RMQMessageProducer::send should ensure that a JMS message with a non-direct reply to is handled correctly")
    public void sendJMSMessageWithGenericReplyTo() throws JMSException, IOException {

        try (RMQMessageProducer producer = new RMQMessageProducer(session, destination)) {
            RMQMessage message = mock(RMQMessage.class);

            when(message.toByteArray()).thenReturn("Test message".getBytes());
            when(message.getJMSCorrelationID()).thenReturn("TESTID");
            when(message.getJMSReplyTo()).thenReturn(new RMQDestination("other-reply-to", "", "other-reply-to", "other-reply-to"));


            when(session.getChannel()).thenReturn(channel);
            when(session.delayMessage(eq(destination), any(Map.class), any(Long.class))).thenReturn("targetExch");

            when(destination.isAmqp()).thenReturn(false);
            when(destination.isAmqpWritable()).thenReturn(true);
            when(destination.getAmqpRoutingKey()).thenReturn("key");

            producer.send(message);

            ArgumentCaptor<com.rabbitmq.client.AMQP.BasicProperties> propCapture = ArgumentCaptor.forClass(com.rabbitmq.client.AMQP.BasicProperties.class);

            verify(channel).basicPublish(eq("targetExch"), eq("key"), propCapture.capture(), eq("Test message".getBytes()));

            assertEquals("other-reply-to", propCapture.getValue().getReplyTo());
        }
    }


    static class StubRMQMessageProducer extends RMQMessageProducer {

        RMQMessage message;

        public StubRMQMessageProducer(RMQSession session, RMQDestination destination, boolean preferProducerMessageProperty) {
            super(session, destination, preferProducerMessageProperty);
        }

        @Override
        protected void sendJMSMessage(RMQDestination destination, RMQMessage msg, Message originalMessage,
            CompletionListener completionListener,
            int deliveryMode, int priority, long timeToLive, long deliveryDelay) throws JMSException {
            this.message = msg;
        }
    }

}
