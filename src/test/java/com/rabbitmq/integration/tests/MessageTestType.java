/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;

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
        void check(Message m, Serializable obj) throws Exception { assertEquals(LONG_TEXT_BODY, ((TextMessage) m).getText()); }
    },
    TEXT {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception { return s.createTextMessage(MESSAGE); }
        @Override
        void check(Message m, Serializable obj) throws Exception { assertEquals(MESSAGE, ((TextMessage) m).getText()); }
    },
    BYTES {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            BytesMessage message = s.createBytesMessage();
            TestMessages.writeBytesMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception { TestMessages.readBytesMessage((BytesMessage) m); }
    },
    MAP {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            MapMessage message = s.createMapMessage();
            TestMessages.writeMapMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception { TestMessages.readMapMessage((MapMessage) m); }
    },
    STREAM {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            StreamMessage message = s.createStreamMessage();
            TestMessages.writeStreamMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception { TestMessages.readStreamMessage((StreamMessage) m); }
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
            Serializable robj = (Serializable) ((ObjectMessage) m).getObject();
            assertEquals("Object not read correctly;", obj, robj); }
    };
    abstract Message gen(QueueSession s, Serializable obj) throws Exception;
    abstract void check(Message m, Serializable obj) throws Exception;

    private static final String MESSAGE = "Hello " + MessageTestType.class.getName();
    private static final char[] CHAR = new char[]{' ', ';', '…', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int CHARLEN = CHAR.length;
    private static final String LONG_TEXT_BODY = generateLongString(65535);
    private static final String generateLongString(int len) {
        StringBuilder sb = new StringBuilder("Long String length>="+len);
        for (int i=0; i<len; ++i) {
            sb.append(CHAR[i % CHARLEN]);
        }
        return sb.toString();
    }
}
