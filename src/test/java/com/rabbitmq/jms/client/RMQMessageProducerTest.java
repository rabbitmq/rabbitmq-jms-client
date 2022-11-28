// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2017-2022 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import jakarta.jms.CompletionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class RMQMessageProducerTest {

    RMQSession session;
    RMQDestination destination;

    @BeforeEach public void init() {
        session = mock(RMQSession.class);
        destination = mock(RMQDestination.class);
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
