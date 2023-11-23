// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import com.rabbitmq.jms.admin.RMQDestination;
import javax.jms.JMSException;
import org.assertj.core.api.Assertions;import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

public class RMQSessionTest {

    @Captor
    ArgumentCaptor<Map<String,Object>> delayedExchangeArguments;
    @Captor
    ArgumentCaptor<Map<String,Object>> destinationExchangeBindingArguments;
    @Mock
    RMQConnection connection;
    @Mock
    Channel channel;
    RMQSession session;

    RMQDestination destination;
    Map<String, Object> headers;
    private AutoCloseable mocks;

    @BeforeEach
    void init() throws JMSException, IOException {
        mocks = MockitoAnnotations.openMocks(this);
        doReturn(channel).when(connection).createRabbitChannel(false);
        session = new RMQSession(connection, false, 0, 0, new Subscriptions(), new DelayedMessageService());

        destination = new RMQDestination("some-exchange", true, false);
        headers = new HashMap<>();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void delayMessage() throws IOException {
        doReturn(new AMQImpl.Exchange.DeclareOk()).when(channel)
            .exchangeDeclare(eq(DelayedMessageService.X_DELAYED_JMS_EXCHANGE),
                eq("x-delayed-message"), eq(true), eq(false), eq(false),
                delayedExchangeArguments.capture());
        doReturn(new AMQImpl.Exchange.BindOk()).when(channel)
            .exchangeBind(eq(destination.getAmqpExchangeName()), eq(DelayedMessageService.X_DELAYED_JMS_EXCHANGE),
                eq(""), destinationExchangeBindingArguments.capture());

        assertEquals(DelayedMessageService.X_DELAYED_JMS_EXCHANGE,
                session.delayMessage(destination, headers, 1L));
        assertEquals(destination.getAmqpExchangeName(), headers.get(DelayedMessageService.X_DELAYED_JMS_EXCHANGE_HEADER));
        assertEquals(1L, headers.get(DelayedMessageService.X_DELAY_HEADER));

        assertEquals("headers", delayedExchangeArguments.getValue().get("x-delayed-type"));
        assertEquals(destination.getAmqpExchangeName(), destinationExchangeBindingArguments.getValue()
                .get(DelayedMessageService.X_DELAYED_JMS_EXCHANGE_HEADER));
    }

    @Test
    void skipDelayMessage() {
        assertEquals(destination.getAmqpExchangeName(), session.delayMessage(destination, headers, 0L));
        assertTrue(headers.isEmpty());
    }

    @Test
    void withReplyToStrategy() throws JMSException {

        SessionParams params = new SessionParams();
        params.setReplyToStrategy(HandleAnyReplyToStrategy.INSTANCE);
        params.setConnection(connection);

        RMQSession session = new RMQSession(params);

        assertSame(HandleAnyReplyToStrategy.INSTANCE, session.getReplyToStrategy());
    }

    @Test
    void withNoReplyToStrategy() throws JMSException {

        SessionParams params = new SessionParams();
        params.setConnection(connection);

        RMQSession session = new RMQSession(params);

        assertSame(DefaultReplyToStrategy.INSTANCE, session.getReplyToStrategy());
    }


    @Test
    void withNullReplyToStrategy() throws JMSException {

        SessionParams params = new SessionParams();
        params.setReplyToStrategy(null);
        params.setConnection(connection);

        RMQSession session = new RMQSession(params);

        assertThat(session.getReplyToStrategy()).isEqualTo(DefaultReplyToStrategy.INSTANCE);
    }
}
