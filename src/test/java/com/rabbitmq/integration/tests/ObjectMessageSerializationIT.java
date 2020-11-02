/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.client.message.RMQObjectMessage;
import com.rabbitmq.jms.client.message.TestMessages;
import com.rabbitmq.jms.util.RMQJMSException;
import org.junit.jupiter.api.Test;

import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectMessageSerializationIT extends AbstractITQueue {

    private static final String QUEUE_NAME = "test.queue." + ObjectMessageSerializationIT.class.getCanonicalName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second
    private static final java.util.List<String> TRUSTED_PACKAGES = Arrays.asList("java.lang", "com.rabbitmq.jms");

    @Override
    protected void customise(RMQConnectionFactory connectionFactory) {
        super.customise(connectionFactory);
        connectionFactory.setTrustedPackages(TRUSTED_PACKAGES);
    }

    protected void testReceiveObjectMessageWithPayload(Object payload) throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);

            drainQueue(queueSession, queue);

            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.send(MessageTestType.OBJECT.gen(queueSession, (Serializable) payload));
        } finally {
            reconnect(Arrays.asList("java.lang", "com.rabbitmq.jms"));
        }

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        RMQObjectMessage m = (RMQObjectMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
        assertEquals(m.getObject(), payload);
        assertEquals(m.getObject(TRUSTED_PACKAGES), payload);
    }

    @Test
    public void testReceiveObjectMessageWithPrimitivePayload() throws Exception {
        testReceiveObjectMessageWithPayload(1024L);
        testReceiveObjectMessageWithPayload("a string");
    }

    @Test
    public void testReceiveObjectMessageWithTrustedPayload() throws Exception {
        testReceiveObjectMessageWithPayload(new TestMessages.TestSerializable(8, "An object"));
    }

    @Test
    public void testReceiveObjectMessageWithUntrustedPayload1() throws Exception {
        // It makes little sense to use ObjectMessage for maps
        // but someone somewhere certainly does it.
        // Note: java.util is not on the trusted package list
        assertThrows(RMQJMSException.class, () -> {
            Map<String, String> m = new HashMap<String, String>();
            m.put("key", "value");
            testReceiveObjectMessageWithPayload(m);
        });
    }
    @Test
    public void testReceiveObjectMessageWithUntrustedPayload2() throws Exception {
        // java.awt is not on the trusted package list
        assertThrows(RMQJMSException.class, () -> {
            testReceiveObjectMessageWithPayload(Color.WHITE);
        });
    }

    protected void reconnect(java.util.List<String> trustedPackages) throws Exception {
        if (queueConn != null) {
            this.queueConn.close();
            ((RMQConnectionFactory) connFactory).setTrustedPackages(trustedPackages);
            this.queueConn = connFactory.createQueueConnection();
        } else {
            fail("Cannot reconnect");
        }
    }
}

