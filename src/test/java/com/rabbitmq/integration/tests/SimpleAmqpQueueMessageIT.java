/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;

/**
 * Integration test for simple point-to-point messaging with target AMQP queue.
 */
public class SimpleAmqpQueueMessageIT extends AbstractAmqpITQueue {

    private static final String QUEUE_NAME = "test.queue."+SimpleAmqpQueueMessageIT.class.getCanonicalName();
    private static final String QUEUE_NAME_NON_EX = "test-non-ex.queue."+SimpleAmqpQueueMessageIT.class.getCanonicalName();
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
        Queue queue = (Queue) new RMQDestination(QUEUE_NAME, "", QUEUE_NAME, null);  // write-only AMQP-mapped queue

        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        queueSender.setPriority(9);

        TextMessage message = queueSession.createTextMessage(MESSAGE);
        message.setStringProperty("StringProp", STRING_PROP_VALUE);

        queueSender.send(message);
        queueConn.close();

        GetResponse response = channel.basicGet(QUEUE_NAME, false);
        assertNotNull("basicGet failed to retrieve a response", response);

        byte[] body = response.getBody();
        assertNotNull("body of response is null", body);

        { Map<String, Object> hdrs = response.getProps().getHeaders();

          assertEquals("Some keys missing", new HashSet<String>(Arrays.asList("JMSMessageID","JMSDeliveryMode","JMSPriority","JMSTimestamp","StringProp"))
                                          , hdrs.keySet()
                      );

          assertEquals("Priority wrong", 9, hdrs.get("JMSPriority"));
          assertEquals("Delivery mode wrong", "NON_PERSISTENT", hdrs.get("JMSDeliveryMode").toString()); // toString is a bit wiffy
          assertEquals("String property wrong", STRING_PROP_VALUE, hdrs.get("StringProp").toString());
        }

        assertEquals("Message received not identical to message sent", MESSAGE,  new String(body, "UTF-8"));
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
        Queue queue = (Queue) new RMQDestination(QUEUE_NAME, "", QUEUE_NAME, null);  // write-only AMQP-mapped queue

        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        queueSender.setPriority(9);

        BytesMessage message = queueSession.createBytesMessage();
        message.setStringProperty("StringProp", STRING_PROP_VALUE);

        message.writeBytes(BYTE_ARRAY);

        queueSender.send(message);
        queueConn.close();

        GetResponse response = channel.basicGet(QUEUE_NAME, false);
        assertNotNull("basicGet failed to retrieve a response", response);

        byte[] body = response.getBody();
        assertNotNull("body of response is null", body);

        { Map<String, Object> hdrs = response.getProps().getHeaders();

          assertEquals("Some keys missing", new HashSet<String>(Arrays.asList("JMSMessageID","JMSDeliveryMode","JMSPriority","JMSTimestamp","StringProp"))
                                          , hdrs.keySet()
                      );

          assertEquals("Priority wrong", 9, hdrs.get("JMSPriority"));
          assertEquals("Delivery mode wrong", "NON_PERSISTENT", hdrs.get("JMSDeliveryMode").toString()); // toString is a bit wiffy
          assertEquals("String property wrong", STRING_PROP_VALUE, hdrs.get("StringProp").toString());
        }
        assertArrayEquals("Message received not identical to message sent", BYTE_ARRAY,  body);
    }

    @Test
    public void testSendFromAmqpAndReceiveBytesMessage() throws Exception {

        channel.queueDeclare(QUEUE_NAME_NON_EX,
                             false, // durable
                             false, // non-exclusive
                             true,  // autoDelete
                             null   // options
                             );

        Map<String, Object> hdrs = new HashMap<String,Object>();
        hdrs.put("UserHdr", "user hdr text");
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().deliveryMode(1).priority(6).headers(hdrs).build();
        channel.basicPublish("", QUEUE_NAME_NON_EX, props, BYTE_ARRAY);

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = (Queue) new RMQDestination(QUEUE_NAME_NON_EX, null, null, QUEUE_NAME_NON_EX);  // read-only AMQP-mapped queue

        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        BytesMessage message = (BytesMessage) queueReceiver.receive(TEST_RECEIVE_TIMEOUT);

        assertNotNull("No message received", message);

        byte[] bytes = new byte[BYTE_ARRAY.length+2];
        int bytesIn = message.readBytes(bytes);
        assertEquals("Message payload not correct size", BYTE_ARRAY.length, bytesIn);
        byte[] bytesTrunc = new byte[bytesIn];
        System.arraycopy(bytes, 0, bytesTrunc, 0, bytesIn);
        assertArrayEquals("Payload doesn't match", BYTE_ARRAY, bytesTrunc);

        fail("More to test");
    }

}
