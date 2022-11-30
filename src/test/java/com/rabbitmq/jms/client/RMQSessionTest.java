package com.rabbitmq.jms.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import com.rabbitmq.jms.admin.RMQDestination;
import jakarta.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

public class RMQSessionTest {

    RMQSession session;
    RMQConnection connection;
    Channel channel;

    RMQDestination destination;
    Map<String, Object> headers;

    @BeforeEach
    public void init() throws JMSException, IOException {
        connection = Mockito.mock(RMQConnection.class);
        channel = Mockito.mock(Channel.class);
        Mockito.doReturn(channel).when(connection).createRabbitChannel(false);
        session = new RMQSession(connection, false, 0, 0, new Subscriptions(), new DelayedMessageService());

        destination = new RMQDestination("some-exchange", true, false);
        headers = new HashMap<>();
    }

    @Test
    void delayMessage() throws IOException {
        ArgumentCaptor<Map<String,Object>> delayedExchangeArguments = delayedExchangeShouldBeDeclared();
        ArgumentCaptor<Map<String,Object>> destinationExchangeBindingArguments = targetDestinationShouldBeBoundToDelayedExchange();

        assertEquals(DelayedMessageService.X_DELAYED_JMS_EXCHANGE,
                session.delayMessage(destination, headers, 1L));
        assertEquals(destination.getAmqpExchangeName(), headers.get(DelayedMessageService.X_DELAYED_JMS_EXCHANGE_HEADER));
        assertEquals(1L, headers.get(DelayedMessageService.X_DELAY_HEADER));

        assertEquals("headers", delayedExchangeArguments.getValue().get("x-delayed-type"));
        assertEquals(destination.getAmqpExchangeName(), destinationExchangeBindingArguments.getValue()
                .get(DelayedMessageService.X_DELAYED_JMS_EXCHANGE_HEADER));
    }

    private ArgumentCaptor<Map<String,Object>> delayedExchangeShouldBeDeclared() throws IOException {
        ArgumentCaptor<Map<String,Object>> argumentCaptor = ArgumentCaptor.forClass(Map.class);

        Mockito.doReturn(new AMQImpl.Exchange.DeclareOk()).when(channel)
                .exchangeDeclare(eq(DelayedMessageService.X_DELAYED_JMS_EXCHANGE),
                eq("x-delayed-message"), eq(true), eq(false), eq(false),
                argumentCaptor.capture()); // object proper

        return argumentCaptor;
    }
    private ArgumentCaptor<Map<String,Object>> targetDestinationShouldBeBoundToDelayedExchange() throws IOException {
        ArgumentCaptor<Map<String,Object>> argumentCaptor = ArgumentCaptor.forClass(Map.class);

        Mockito.doReturn(new AMQImpl.Exchange.BindOk()).when(channel)
                .exchangeBind(eq(destination.getAmqpExchangeName()), eq(DelayedMessageService.X_DELAYED_JMS_EXCHANGE),
                        eq(""), argumentCaptor.capture());
        return argumentCaptor;
    }

    @Test
    void skipDelayMessage() {
        assertEquals(destination.getAmqpExchangeName(), session.delayMessage(destination, headers, 0L));
        assertTrue(headers.isEmpty());
    }

}
