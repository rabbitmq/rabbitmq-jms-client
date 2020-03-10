/* Copyright (c) 2014-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.QueueSession;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import com.rabbitmq.jms.client.message.TestMessages;

/**
 * This enumerated type encapsulates various types of message, how to generate one, and how to check (using asserts) if we have one.
 * This is useful for tests: the test can concentrate on the queues/topics/receive mechanisms used, and not worry about the messages themselves.
 *
 */
enum MessageTestType {
    LONG_TEXT {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception { return s.createTextMessage(LONG_TEXT_BODY); }
        @Override
        void check(Message m, Serializable obj) throws Exception { assertEquals(LONG_TEXT_BODY, goodText(m).getText()); }
    },
    TEXT {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception { return s.createTextMessage(MESSAGE); }
        @Override
        void check(Message m, Serializable obj) throws Exception { assertEquals(MESSAGE, goodText(m).getText()); }
    },
    BYTES {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            BytesMessage message = s.createBytesMessage();
            TestMessages.writeBytesMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception { TestMessages.readBytesMessage(goodBytes(m)); }
    },
    LONG_BYTES {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            BytesMessage message = s.createBytesMessage();
            TestMessages.writeLongBytesMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception { TestMessages.readLongBytesMessage(goodBytes(m)); }
    },
    MAP {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            MapMessage message = s.createMapMessage();
            TestMessages.writeMapMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception { TestMessages.readMapMessage(goodMap(m)); }
    },
    STREAM {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            StreamMessage message = s.createStreamMessage();
            TestMessages.writeStreamMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception { TestMessages.readStreamMessage(goodStream(m)); }
    },
    OBJECT {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            ObjectMessage message = s.createObjectMessage();
            message.setObjectProperty("objectProp", "This is an object property"); // try setting an object property, too
            message.setObject(obj);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception {
            Serializable robj = (Serializable) goodObject(m).getObject();
            assertEquals(obj, robj, "Object not read correctly;"); }
    };
    abstract Message gen(QueueSession s, Serializable obj) throws Exception;
    abstract void check(Message m, Serializable obj) throws Exception;
    public void checkAttrs(Message m, int mode, int priority) throws Exception {
        assertEquals(mode, m.getJMSDeliveryMode(), "DeliveryMode incorrect");
        assertEquals(priority, m.getJMSPriority(), "Priority incorrect");
    }

    private static final Message nonNull(Message m) throws Exception { assertNotNull(m, "Message received is null"); return m; }

    private static final BytesMessage goodBytes(Message m) throws Exception { nonNull(m); assertTrue(m instanceof BytesMessage, "Message is not BytesMessage"); return (BytesMessage)m; }
    private static final TextMessage goodText(Message m) throws Exception { nonNull(m); assertTrue(m instanceof TextMessage, "Message is not TextMessage"); return (TextMessage)m; }
    private static final MapMessage goodMap(Message m) throws Exception { nonNull(m); assertTrue(m instanceof MapMessage, "Message is not MapMessage"); return (MapMessage)m; }
    private static final StreamMessage goodStream(Message m) throws Exception { nonNull(m); assertTrue(m instanceof StreamMessage, "Message is not StreamMessage"); return (StreamMessage)m; }
    private static final ObjectMessage goodObject(Message m) throws Exception { nonNull(m); assertTrue(m instanceof ObjectMessage, "Message is not ObjectMessage"); return (ObjectMessage)m; }

    private static final String MESSAGE = "Hello " + MessageTestType.class.getName();
    private static final char[] CHAR = new char[]{' ', ';', '…', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int CHARLEN = CHAR.length;
    private static final String LONG_TEXT_BODY = generateLongString(655350);
    private static final String generateLongString(int len) {
        StringBuilder sb = new StringBuilder("Long String length>="+len);
        for (int i=0; i<len; ++i) {
            sb.append(CHAR[i % CHARLEN]);
        }
        return sb.toString();
    }
}