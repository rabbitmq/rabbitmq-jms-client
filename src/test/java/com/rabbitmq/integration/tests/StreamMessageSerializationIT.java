// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.client.message.RMQStreamMessage;
import com.rabbitmq.jms.client.message.TestMessages;
import com.rabbitmq.jms.util.RMQJMSException;
import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.StreamMessage;
import org.junit.jupiter.api.Test;

public class StreamMessageSerializationIT extends AbstractITQueue {

    private static final String QUEUE_NAME = "test.queue." + StreamMessageSerializationIT.class.getCanonicalName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second
    private static final java.util.List<String> TRUSTED_PACKAGES = Arrays.asList("java.lang", "com.rabbitmq.jms");

    @Override
    protected void customise(RMQConnectionFactory connectionFactory) {
        super.customise(connectionFactory);
        connectionFactory.setTrustedPackages(TRUSTED_PACKAGES);
    }

    protected void testReceiveStreamMessageWithValue(Object value) throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);

            drainQueue(queueSession, queue);

            QueueSender queueSender = queueSession.createSender(queue);
            StreamMessage message = (StreamMessage) MessageTestType.STREAM.gen(queueSession, null);

            // we simulate an attack from the sender by calling writeObject with a non-primitive value
            // (StreamMessage supports only primitive types)
            // the value is then sent to the destination and the consumer will have to
            // deserialize it and can potentially execute malicious code
            Method writeObjectMethod = RMQStreamMessage.class
                .getDeclaredMethod("writeObject", Object.class, boolean.class);
            writeObjectMethod.setAccessible(true);
            writeObjectMethod.invoke(message, value, true);

            queueSender.send(message);
        } finally {
            reconnect(Arrays.asList("java.lang", "com.rabbitmq.jms"));
        }

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        RMQStreamMessage m = (RMQStreamMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);
        MessageTestType.STREAM.check(m, null);
        assertEquals(m.readObject(), value);
    }

    @Test
    public void testReceiveStreamMessageWithPrimitiveValue() throws Exception {
        testReceiveStreamMessageWithValue(1024L);
        testReceiveStreamMessageWithValue("a string");
    }

    @Test
    public void testReceiveStreamMessageWithTrustedValue() throws Exception {
        testReceiveStreamMessageWithValue(new TestMessages.TestSerializable(8, "An object"));
    }

    @Test
    public void testReceiveStreamMessageWithUntrustedValue1() throws Exception {
        // StreamMessage cannot be used with a Map, unless the sender uses a trick
        // this is to simulate an attack from the sender
        // Note: java.util is not on the trusted package list
        assertThrows(RMQJMSException.class, () -> {
            Map<String, String> m = new HashMap<String, String>();
            m.put("key", "value");
            testReceiveStreamMessageWithValue(m);
        });
    }
    @Test
    public void testReceiveStreamMessageWithUntrustedValue2() throws Exception {
        // StreamMessage cannot be used with a Map, unless the sender uses a trick
        // this is to simulate an attack from the sender
        // java.awt is not on the trusted package list
        assertThrows(RMQJMSException.class, () -> {
            testReceiveStreamMessageWithValue(Color.WHITE);
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

