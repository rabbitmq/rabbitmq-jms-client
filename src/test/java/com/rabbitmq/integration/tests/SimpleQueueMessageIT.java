/* Copyright (c) 2013, 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.junit.Test;

import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.TestMessages;

/**
 * Integration test for simple browsing of a queue.
 */
public class SimpleQueueMessageIT extends AbstractITQueue {

    private static final String QUEUE_NAME = "test.queue."+SimpleQueueMessageIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + SimpleQueueMessageIT.class.getName();
    private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second

    private static final char[] CHAR = new char[]{' ', ';', '…', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int CHARLEN = CHAR.length;
    private static final String LONG_TEXT_BODY = generateLongString(65535);

    private final static String generateLongString(int len) {
        StringBuilder sb = new StringBuilder("Long String length>="+len);
        for (int i=0; i<len; ++i) {
            sb.append(CHAR[i % CHARLEN]);
        }
        return sb.toString();
    }

    private enum MessageTestType {
        LONG_TEXT {
            @Override
            Message gen(QueueSession s, Queue q) throws Exception { return s.createTextMessage(LONG_TEXT_BODY); }
            @Override
            void check(Message m, Queue q) throws Exception { assertEquals(LONG_TEXT_BODY, ((TextMessage) m).getText()); }
        },
        TEXT {
            @Override
            Message gen(QueueSession s, Queue q) throws Exception { return s.createTextMessage(MESSAGE); }
            @Override
            void check(Message m, Queue q) throws Exception { assertEquals(MESSAGE, ((TextMessage) m).getText()); }
        },
        BYTES {
            @Override
            Message gen(QueueSession s, Queue q) throws Exception {
                BytesMessage message = s.createBytesMessage();
                TestMessages.writeBytesMessage(message);
                return message; }
            @Override
            void check(Message m, Queue q) throws Exception { TestMessages.readBytesMessage((BytesMessage) m); }
        },
        MAP {
            @Override
            Message gen(QueueSession s, Queue q) throws Exception {
                MapMessage message = s.createMapMessage();
                TestMessages.writeMapMessage(message);
                return message; }
            @Override
            void check(Message m, Queue q) throws Exception { TestMessages.readMapMessage((MapMessage) m); }
        },
        STREAM {
            @Override
            Message gen(QueueSession s, Queue q) throws Exception {
                StreamMessage message = s.createStreamMessage();
                TestMessages.writeStreamMessage(message);
                return message; }
            @Override
            void check(Message m, Queue q) throws Exception { TestMessages.readStreamMessage((StreamMessage) m); }
        },
        OBJECT {
            @Override
            Message gen(QueueSession s, Queue q) throws Exception {
                ObjectMessage message = s.createObjectMessage();
                message.setObjectProperty("objectProp", "This is an object property"); // try setting an object property, too
                message.setObject((Serializable)q);
                return message; }
            @Override
            void check(Message m, Queue q) throws Exception {
                RMQDestination oq = (RMQDestination) q;
                RMQDestination rq = (RMQDestination) ((ObjectMessage) m).getObject();
                assertEquals("Object not read correctly;", oq, rq); }
        };
        abstract Message gen(QueueSession s, Queue q) throws Exception;
        abstract void check(Message m, Queue q) throws Exception;
    }

    private void messageTestBase(MessageTestType mtt) throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            queueSender.send(mtt.gen(queueSession, queue));
        } finally {
            reconnect();
        }

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        mtt.check(queueReceiver.receive(TEST_RECEIVE_TIMEOUT), queue);
    }

    @Test
    public void testSendBrowseAndReceiveLongTextMessage() throws Exception {
        messageTestBase(MessageTestType.LONG_TEXT);
    }

    @Test
    public void testSendBrowseAndReceiveTextMessage() throws Exception {
        messageTestBase(MessageTestType.TEXT);
    }

    @Test
    public void testSendBrowseAndReceiveBytesMessage() throws Exception {
        messageTestBase(MessageTestType.BYTES);
    }

    @Test
    public void testSendBrowseAndReceiveMapMessage() throws Exception {
        messageTestBase(MessageTestType.MAP);
    }

    @Test
    public void testSendBrowseAndReceiveStreamMessage() throws Exception {
        messageTestBase(MessageTestType.STREAM);
    }

    @Test
    public void testSendBrowseAndReceiveObjectMessage() throws Exception {
        messageTestBase(MessageTestType.OBJECT);
    }
}
