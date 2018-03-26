/* Copyright (c) 2018 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.AmqpPropertiesCustomiser;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test to test the AMQP properties customiser is applied correctly.
 */
public class AmqpPropertiesCustomiserIT extends AbstractAmqpITQueue {

    private static final String QUEUE_NAME = "test.queue."+AmqpPropertiesCustomiserIT.class.getCanonicalName();
    public static final String MESSAGE = "hello";
    public static final String TEXT_PLAIN = "text/plain";

    @Override
    protected void customise(RMQConnectionFactory connectionFactory) {
        connectionFactory.setAmqpPropertiesCustomiser(new AmqpPropertiesCustomiser() {

            @Override
            public AMQP.BasicProperties.Builder customise(AMQP.BasicProperties.Builder builder, Message jmsMessage) {
                builder.contentType(TEXT_PLAIN);
                return builder;
            }
        });
    }

    @Test
    public void customiserIsApplied() throws Exception {

        channel.queueDeclare(QUEUE_NAME,
                             false, // durable
                             true,  // exclusive
                             true,  // autoDelete
                             null   // options
                             );

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = new RMQDestination(QUEUE_NAME, "", QUEUE_NAME, null);  // write-only AMQP-mapped queue

        QueueSender queueSender = queueSession.createSender(queue);

        TextMessage message = queueSession.createTextMessage(MESSAGE);

        queueSender.send(message);
        queueConn.close();

        GetResponse response = channel.basicGet(QUEUE_NAME, false);
        assertNotNull("basicGet failed to retrieve a response", response);

        byte[] body = response.getBody();
        assertNotNull("body of response is null", body);

        assertEquals("body of response is not correct", MESSAGE, new String(body));

        assertEquals("body of response is not correct", TEXT_PLAIN, response.getProps().getContentType());
    }

}
