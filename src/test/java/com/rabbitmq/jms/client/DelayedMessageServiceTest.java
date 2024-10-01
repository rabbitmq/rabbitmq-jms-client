// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import com.rabbitmq.jms.admin.RMQDestination;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.mockito.MockitoAnnotations;

import static com.rabbitmq.jms.client.DelayedMessageService.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DelayedMessageServiceTest {
    DelayedMessageService subject;

    @Captor
    ArgumentCaptor<Map<String, Object>> exchangeDeclarationArgs;
    @Captor
    ArgumentCaptor<Map<String, Object>> exchangeBindingArgs;

    @Mock
    Channel channel;
    AutoCloseable mocks;
    RMQDestination jmsQueueDestination = new RMQDestination("some-queue", true, false);
    Map<String, Object> msgHeaders = new HashMap<>();

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        subject = new DelayedMessageService();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldDeclareDelayedExchangeOnce() throws IOException {

        shouldDeclareDelayedExchangeOfTypeHeaders();

        // when we try to send the message again,
        reset(channel);

        subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 1000L);

        verifyNoInteractions(channel);
    }

    @Test
    void shouldDeclareDelayedExchangeOfTypeHeaders() throws IOException {

        shouldDeclareDelayedExchange();

        subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 1000L);

        assertEquals("headers", exchangeDeclarationArgs.getValue().get("x-delayed-type"));
    }


    @Test
    void shouldBindTargetExchangeToDelayedExchangeOnce() throws IOException {

        shouldBindTargetExchangeToDelayedExchange();

        reset(channel);
        subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 1000L);

        verifyNoInteractions(channel);
    }

    @Test
    void shouldBindTargetExchangeToDelayedExchange() throws IOException {

        shouldBindTargetExchangeToDelayExchange();

        subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 1000L);

        assertEquals(jmsQueueDestination.getAmqpExchangeName(), exchangeBindingArgs.getValue().get(X_DELAYED_JMS_EXCHANGE_HEADER));

    }


    @Test
    void shouldReturnTheDelayedExchangeWhenDelayIsGreaterThanZero() {
        String targetExchange = subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 1000L);
        assertEquals(X_DELAYED_JMS_EXCHANGE, targetExchange);
    }

    @Test
    void shouldAddDelayAndOriginalExchangeToMessageHeaders() {
        subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 1000L);
        assertEquals(1000L, msgHeaders.get(X_DELAY_HEADER));
        assertEquals(jmsQueueDestination.getAmqpExchangeName(), msgHeaders.get(X_DELAYED_JMS_EXCHANGE_HEADER));
    }

    @Test
    void shouldReturnTheTargetExchangeWhenDelayIsNotGreaterThanZero() {
        String targetExchange = subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 0L);
        assertEquals(jmsQueueDestination.getAmqpExchangeName(), targetExchange);
    }

    private void shouldDeclareDelayedExchange() throws IOException {
        doReturn(new AMQImpl.Exchange.DeclareOk()).when(channel).exchangeDeclare(
                eq(X_DELAYED_JMS_EXCHANGE),
                eq("x-delayed-message"),
                eq(true),
                eq(false),
                eq(false),
                exchangeDeclarationArgs.capture());
    }

    private void shouldBindTargetExchangeToDelayExchange() throws IOException {
        doReturn(new AMQImpl.Exchange.BindOk()).when(channel).exchangeBind(
                eq(jmsQueueDestination.getAmqpExchangeName()),
                eq(X_DELAYED_JMS_EXCHANGE),
                eq(""),
                exchangeBindingArgs.capture());
    }
}