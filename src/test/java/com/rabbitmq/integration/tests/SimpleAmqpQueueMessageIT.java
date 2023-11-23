// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;

/**
 * Integration test for simple point-to-point messaging with target AMQP queue.
 */
public class SimpleAmqpQueueMessageIT extends AbstractAmqpITQueue {

    private static final String USER_JMS_TYPE_SETTING = "this is my type";
    private static final String USER_STRING_PROPERTY_NAME = "UserHdr";
    private static final String QUEUE_NAME = "test.queue."+SimpleAmqpQueueMessageIT.class.getCanonicalName();
    private static final String QUEUE_NAME_NON_EXCLUSIVE = "test-non-ex.queue."+SimpleAmqpQueueMessageIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + SimpleAmqpQueueMessageIT.class.getName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second
    private static final byte[] BYTE_ARRAY = { (byte) -2, (byte) -3, 4, 5, 34, (byte) -1 };
    private static final String STRING_PROP_VALUE = "the string property value";

    @Test
    public void testSendToAmqpAndReceiveTextMessage() throws Exception {

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
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        queueSender.setPriority(9);

        TextMessage message = queueSession.createTextMessage(MESSAGE);
        message.setStringProperty(USER_STRING_PROPERTY_NAME, STRING_PROP_VALUE);

        queueSender.send(message);
        queueConn.close();

        GetResponse response = channel.basicGet(QUEUE_NAME, false);
        assertNotNull(response, "basicGet failed to retrieve a response");

        byte[] body = response.getBody();
        assertNotNull(body, "body of response is null");

        { Map<String, Object> hdrs = response.getProps().getHeaders();

          assertEquals(new HashSet<>(
                  asList("JMSMessageID","JMSDeliveryMode","JMSPriority","JMSTimestamp",USER_STRING_PROPERTY_NAME))
                       , hdrs.keySet()
                       , "Some keys missing"
                      );

          assertEquals(9, hdrs.get("JMSPriority"), "Priority wrong");
          assertEquals("NON_PERSISTENT", hdrs.get("JMSDeliveryMode").toString(), "Delivery mode wrong"); // toString is a bit wiffy
          assertEquals(STRING_PROP_VALUE, hdrs.get(USER_STRING_PROPERTY_NAME).toString(), "String property wrong");
        }

        assertEquals(MESSAGE,  new String(body, "UTF-8"), "Message received not identical to message sent");
    }

    @Test
    public void testSendToAmqpAndReceiveBytesMessage() throws Exception {

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
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        queueSender.setPriority(9);

        BytesMessage message = queueSession.createBytesMessage();
        message.setStringProperty(USER_STRING_PROPERTY_NAME, STRING_PROP_VALUE);

        message.writeBytes(BYTE_ARRAY);

        queueSender.send(message);
        queueConn.close();

        GetResponse response = channel.basicGet(QUEUE_NAME, false);
        assertNotNull(response, "basicGet failed to retrieve a response");

        byte[] body = response.getBody();
        assertNotNull(body, "body of response is null");

        { Map<String, Object> hdrs = response.getProps().getHeaders();

          assertEquals(new HashSet<>(
                  asList("JMSMessageID", "JMSDeliveryMode", "JMSPriority", "JMSTimestamp",
                      USER_STRING_PROPERTY_NAME))
                       , hdrs.keySet()
                       , "Some keys missing"
                      );

          assertEquals(9, hdrs.get("JMSPriority"), "Priority wrong");
          assertEquals("NON_PERSISTENT", hdrs.get("JMSDeliveryMode").toString(), "Delivery mode wrong"); // toString is a bit wiffy
          assertEquals(STRING_PROP_VALUE, hdrs.get(USER_STRING_PROPERTY_NAME).toString(), "String property wrong");
        }
        assertArrayEquals(BYTE_ARRAY,  body, "Message received not identical to message sent");
    }

    @Test
    public void testSendFromAmqpAndReceiveBytesMessage() throws Exception {

        channel.queueDeclare(QUEUE_NAME_NON_EXCLUSIVE,
                             false, // durable
                             false, // non-exclusive
                             true,  // autoDelete
                             null   // options
                             );

        Map<String, Object> hdrs = new HashMap<String,Object>();
        hdrs.put(USER_STRING_PROPERTY_NAME, STRING_PROP_VALUE);
        hdrs.put("JMSType", USER_JMS_TYPE_SETTING);
        hdrs.put("JMSPriority", 21);
        hdrs.put("JMSDeliveryMode", 2);
        hdrs.put("DummyProp", 42);
        hdrs.put("rmq.jms.silly", "silly attempt");

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().deliveryMode(1).priority(6).headers(hdrs).build();

        channel.basicPublish("", QUEUE_NAME_NON_EXCLUSIVE, props, BYTE_ARRAY);

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = new RMQDestination(QUEUE_NAME_NON_EXCLUSIVE, null, null, QUEUE_NAME_NON_EXCLUSIVE);  // read-only AMQP-mapped queue

        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        BytesMessage message = (BytesMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);

        assertNotNull(message, "No message received");

        byte[] bytes = new byte[BYTE_ARRAY.length];
        int bytesIn = message.readBytes(bytes);
        assertEquals(BYTE_ARRAY.length, bytesIn, "Message payload not correct size");
        byte[] bytesTrunc = new byte[bytesIn];
        System.arraycopy(bytes, 0, bytesTrunc, 0, bytesIn);
        assertArrayEquals(BYTE_ARRAY, bytesTrunc, "Payload doesn't match");

        assertEquals(21, message.getJMSPriority(), "Priority incorrect"); // override should work
        assertEquals(1, message.getJMSDeliveryMode(), "Delivery mode incorrect"); // override should fail
        assertEquals(USER_JMS_TYPE_SETTING, message.getJMSType(), "JMSType not set correctly"); // override should work

        Enumeration<?> propNames = message.getPropertyNames();
        Set<String> propNameSet = new HashSet<String>();
        while (propNames.hasMoreElements()) {
            propNameSet.add((String) propNames.nextElement());
        }

        assertEquals(new HashSet<>(
                asList(USER_STRING_PROPERTY_NAME, "DummyProp", "JMSXDeliveryCount"))
                    , propNameSet
                    , "Headers not set correctly");

        assertEquals(STRING_PROP_VALUE, message.getStringProperty(USER_STRING_PROPERTY_NAME), "String property not transferred");
        assertEquals("42", message.getStringProperty("DummyProp"), "Numeric property not transferred");
        assertThat(message.getJMSTimestamp()).isZero();
    }

    @Test
    public void testSendFromAmqpAndReceiveTextMessage() throws Exception {

        channel.queueDeclare(QUEUE_NAME_NON_EXCLUSIVE,
                             false, // durable
                             false, // non-exclusive
                             true,  // autoDelete
                             null   // options
                             );

        Map<String, Object> hdrs = new HashMap<String,Object>();
        hdrs.put(USER_STRING_PROPERTY_NAME, STRING_PROP_VALUE);
        hdrs.put("JMSType", "TextMessage");  // To signal a JMS TextMessage
        hdrs.put("JMSPriority", 21);
        hdrs.put("JMSDeliveryMode", 2);
        hdrs.put("DummyProp", 42);
        hdrs.put("rmq.jms.silly", "silly attempt");

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().deliveryMode(1).priority(6).headers(hdrs).build();

        channel.basicPublish("", QUEUE_NAME_NON_EXCLUSIVE, props, MESSAGE.getBytes("UTF-8"));

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = new RMQDestination(QUEUE_NAME_NON_EXCLUSIVE, null, null, QUEUE_NAME_NON_EXCLUSIVE);  // read-only AMQP-mapped queue

        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);

        assertNotNull(message, "No message received");

        assertEquals(MESSAGE, message.getText(), "Payload doesn't match");

        assertEquals(21, message.getJMSPriority(), "Priority incorrect"); // override should work
        assertEquals(1, message.getJMSDeliveryMode(), "Delivery mode incorrect"); // override should fail
        assertEquals("TextMessage", message.getJMSType(), "JMSType not set correctly"); // override should work

        Enumeration<?> propNames = message.getPropertyNames();
        Set<String> propNameSet = new HashSet<String>();
        while (propNames.hasMoreElements()) {
            propNameSet.add((String) propNames.nextElement());
        }

        assertEquals(new HashSet<>(
                asList(USER_STRING_PROPERTY_NAME, "DummyProp", "JMSXDeliveryCount"))
                     , propNameSet
                     , "Headers not set correctly");

        assertEquals(STRING_PROP_VALUE, message.getStringProperty(USER_STRING_PROPERTY_NAME), "String property not transferred");
        assertEquals("42", message.getStringProperty("DummyProp"), "Numeric property not transferred");
        assertThat(message.getJMSTimestamp()).isZero();
    }

    @Test
    public void testSendFromJmsAndReceiveJmsTextMessage() throws Exception {
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName,
            false, // durable
            false, // non-exclusive
            true,  // autoDelete
            null   // options
        );

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = new RMQDestination(queueName, "", queueName, null); // write-only AMQP-mapped queue

        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        TextMessage message = queueSession.createTextMessage(MESSAGE);
        message.setStringProperty("JMSType", "TextMessage");
        message.setStringProperty(USER_STRING_PROPERTY_NAME, STRING_PROP_VALUE);

        queueSender.send(message);
        queueSession.close();

        queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        queue = new RMQDestination(queueName, null, null, queueName);  // read-only AMQP-mapped queue
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        message = (TextMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);

        channel.queueDelete(queueName);

        assertNotNull(message, "No message received");
        assertEquals(MESSAGE, message.getText(), "Payload doesn't match");
        assertEquals(STRING_PROP_VALUE, message.getStringProperty(USER_STRING_PROPERTY_NAME), "String property not transferred");
        assertThat(message.getJMSTimestamp()).isGreaterThan(0);
    }

}
