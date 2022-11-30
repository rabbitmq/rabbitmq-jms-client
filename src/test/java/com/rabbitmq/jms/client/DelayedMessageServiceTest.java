package com.rabbitmq.jms.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import com.rabbitmq.jms.admin.RMQDestination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.rabbitmq.jms.client.DelayedMessageService.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DelayedMessageServiceTest {
    DelayedMessageService subject;
    ArgumentCaptor<Map<String, Object>> exchangeDeclarationArgs = ArgumentCaptor.forClass(Map.class);
    ArgumentCaptor<Map<String, Object>> exchangeBindingArgs = ArgumentCaptor.forClass(Map.class);
    Channel channel = Mockito.mock(Channel.class);
    RMQDestination jmsQueueDestination = new RMQDestination("some-queue", true, false);
    Map<String, Object> msgHeaders = new HashMap<>();

    @BeforeEach
    void setUp() {
        subject = new DelayedMessageService();
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
    void shouldReturnTheDelayedExchangeWhenDelayIsGreaterThanZero() throws IOException {
        String targetExchange = subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 1000L);
        assertEquals(X_DELAYED_JMS_EXCHANGE, targetExchange);
    }

    @Test
    void shouldAddDelayAndOriginalExchangeToMessageHeaders() throws IOException {
        subject.delayMessage(channel, jmsQueueDestination, msgHeaders, 1000L);
        assertEquals(1000L, msgHeaders.get(X_DELAY_HEADER));
        assertEquals(jmsQueueDestination.getAmqpExchangeName(), msgHeaders.get(X_DELAYED_JMS_EXCHANGE_HEADER));
    }

    @Test
    void shouldReturnTheTargetExchangeWhenDelayIsNotGreaterThanZero() throws IOException {
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