// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;

import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageFormatException;
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
        void check(Message m, Serializable obj) throws Exception {
            assertThat(goodText(m).getText()).isEqualTo(LONG_TEXT_BODY);
            assertThat(m.isBodyAssignableTo(String.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Object.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Serializable.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Connection.class)).isFalse();
            assertThat(m.getBody(String.class)).isEqualTo(LONG_TEXT_BODY);
            assertThatThrownBy(() -> m.getBody(Connection.class)).isInstanceOf(
                MessageFormatException.class);
        }
    },
    TEXT {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception { return s.createTextMessage(MESSAGE); }
        @Override
        void check(Message m, Serializable obj) throws Exception {
            assertThat(goodText(m).getText()).isEqualTo(MESSAGE);
            assertThat(m.isBodyAssignableTo(String.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Object.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Serializable.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Connection.class)).isFalse();
            assertThat(m.getBody(String.class)).isEqualTo(MESSAGE);
            assertThatThrownBy(() -> m.getBody(Connection.class)).isInstanceOf(
                MessageFormatException.class);
        }
    },
    BYTES {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            BytesMessage message = s.createBytesMessage();
            TestMessages.writeBytesMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception {
            TestMessages.readBytesMessage(goodBytes(m));
            assertThat(m.isBodyAssignableTo(byte[].class)).isTrue();
            assertThat(m.isBodyAssignableTo(Object.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Serializable.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Connection.class)).isFalse();
            BytesMessage bytesMessage = (BytesMessage) m;
            bytesMessage.reset();
            byte [] body = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(body);
            assertThat(m.getBody(byte[].class)).isEqualTo(body);
            assertThatThrownBy(() -> m.getBody(Connection.class)).isInstanceOf(
                MessageFormatException.class);
        }
    },
    LONG_BYTES {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            BytesMessage message = s.createBytesMessage();
            TestMessages.writeLongBytesMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception {
            TestMessages.readLongBytesMessage(goodBytes(m));
            assertThat(m.isBodyAssignableTo(byte[].class)).isTrue();
            assertThat(m.isBodyAssignableTo(Object.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Serializable.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Connection.class)).isFalse();
            BytesMessage bytesMessage = (BytesMessage) m;
            bytesMessage.reset();
            byte [] body = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(body);
            assertThat(m.getBody(byte[].class)).isEqualTo(body);
            assertThatThrownBy(() -> m.getBody(Connection.class)).isInstanceOf(
                MessageFormatException.class);
        }
    },
    MAP {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            MapMessage message = s.createMapMessage();
            TestMessages.writeMapMessage(message);
            return message; }
        @SuppressWarnings("unchecked")
        @Override
        void check(Message m, Serializable obj) throws Exception {
            TestMessages.readMapMessage(goodMap(m));
            assertThat(m.isBodyAssignableTo(Map.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Object.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Serializable.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Connection.class)).isFalse();
            assertThatThrownBy(() -> m.getBody(Connection.class)).isInstanceOf(
                MessageFormatException.class);
            MapMessage mapMessage = (MapMessage) m;
            Map<String, Serializable> body = mapMessage.getBody(Map.class);
            Enumeration mapNames = mapMessage.getMapNames();
            int keyCount = 0;
            while (mapNames.hasMoreElements()) {
                String key = mapNames.nextElement().toString();
                assertThat(body).containsEntry(key, (Serializable) mapMessage.getObject(key));
                keyCount++;
            }
            assertThat(body).hasSize(keyCount);
        }
    },
    STREAM {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            StreamMessage message = s.createStreamMessage();
            TestMessages.writeStreamMessage(message);
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception {
            TestMessages.readStreamMessage(goodStream(m));
            assertThat(m.isBodyAssignableTo(Object.class)).isFalse();
            assertThatThrownBy(() -> m.getBody(Object.class))
                .isInstanceOf(MessageFormatException.class);
        }
    },
    OBJECT {
        @Override
        Message gen(QueueSession s, Serializable obj) throws Exception {
            ObjectMessage message = s.createObjectMessage();
            message.setObjectProperty("objectProp", "This is an object property"); // try setting an object property, too
            if (obj instanceof Destination) {
                // we ignore destinations
                message.setObject(MESSAGE_OBJECT);
            } else {
                message.setObject(obj);
            }
            return message; }
        @Override
        void check(Message m, Serializable obj) throws Exception {
            assertThat(m.isBodyAssignableTo(Object.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Serializable.class)).isTrue();
            assertThat(m.isBodyAssignableTo(Connection.class)).isFalse();
            assertThatThrownBy(() -> m.getBody(Connection.class)).isInstanceOf(
                MessageFormatException.class);
            Serializable robj = goodObject(m).getObject();
            if (obj instanceof Destination) {
                assertThat(robj).as("Object not read correctly").isEqualTo(MESSAGE_OBJECT);
                assertThat(m.isBodyAssignableTo(StockObject.class)).isTrue();
                StockObject body = m.getBody(StockObject.class);
                assertThat(body).isEqualTo(MESSAGE_OBJECT);
            } else {
                assertThat(robj).as("Object not read correctly").isEqualTo(obj);
                assertThat(m.isBodyAssignableTo(obj.getClass())).isTrue();
                Serializable body = m.getBody(Serializable.class);
                assertThat(body).as("Object not read correctly").isEqualTo(obj);
            }
        }

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

    private static StockObject MESSAGE_OBJECT = new StockObject(
       "NAME", 256, 1664186274, 5, "INFO"
    );

    public static class StockObject implements Serializable {

        private static final long serialVersionUID = 9167825719999094717L;

        String stockName;
        double stockValue;
        long stockTime;
        double stockDiff;
        String stockInfo;

        public StockObject(String stockName, double stockValue, long stockTime, double stockDiff,
            String stockInfo) {
            this.stockName = stockName;
            this.stockValue = stockValue;
            this.stockTime = stockTime;
            this.stockDiff = stockDiff;
            this.stockInfo = stockInfo;
        }

        public StockObject() {

        }

        public String getStockName() {
            return stockName;
        }

        public void setStockName(String stockName) {
            this.stockName = stockName;
        }

        public double getStockValue() {
            return stockValue;
        }

        public void setStockValue(double stockValue) {
            this.stockValue = stockValue;
        }

        public long getStockTime() {
            return stockTime;
        }

        public void setStockTime(long stockTime) {
            this.stockTime = stockTime;
        }

        public double getStockDiff() {
            return stockDiff;
        }

        public void setStockDiff(double stockDiff) {
            this.stockDiff = stockDiff;
        }

        public String getStockInfo() {
            return stockInfo;
        }

        public void setStockInfo(String stockInfo) {
            this.stockInfo = stockInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StockObject that = (StockObject) o;
            return Double.compare(that.stockValue, stockValue) == 0 && stockTime == that.stockTime
                && Double.compare(that.stockDiff, stockDiff) == 0 && stockName.equals(
                that.stockName) && stockInfo.equals(that.stockInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stockName, stockValue, stockTime, stockDiff, stockInfo);
        }
    }
}
