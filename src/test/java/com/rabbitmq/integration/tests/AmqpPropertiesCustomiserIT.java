// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import org.junit.jupiter.api.Test;

import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test to test the AMQP properties customiser is applied correctly.
 */
public class AmqpPropertiesCustomiserIT extends AbstractAmqpITQueue {

    public static final String MESSAGE = "hello";
    public static final String TEXT_PLAIN = "text/plain";
    private static final String QUEUE_NAME = "test.queue." + AmqpPropertiesCustomiserIT.class.getCanonicalName();

    @Override
    protected void customise(RMQConnectionFactory connectionFactory) {
        connectionFactory.setAmqpPropertiesCustomiser((builder, message) -> builder.contentType(TEXT_PLAIN));
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
        assertNotNull(response, "basicGet failed to retrieve a response");

        byte[] body = response.getBody();
        assertNotNull(body, "body of response is null");

        assertEquals(new String(body), MESSAGE, "body of response is not correct");

        assertEquals(response.getProps().getContentType(), TEXT_PLAIN, "body of response is not correct");
    }
}
