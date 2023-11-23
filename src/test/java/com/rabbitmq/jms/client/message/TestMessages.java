// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import com.rabbitmq.jms.util.RMQJMSException;
import org.junit.jupiter.api.Test;

public class TestMessages {

    private static byte[] largeByteArray(final int len) {
        byte[] bytes = new byte[len];
        for (int i=0; i<len; ++i) {
            bytes[i] = (byte)i;
        }
        return bytes;
    }

    private static final int LARGE_BYTES_LEN = 1<<18;
    private static final byte[] BYTE_ARRAY = { (byte) -2, (byte) -3 };
    private static final byte[] LARGE_BYTE_ARRAY = largeByteArray(LARGE_BYTES_LEN);

    @Test
    public void testBytesMessageDebug() throws Exception {
        RMQBytesMessage message1 = new RMQBytesMessage();
        message1.writeInt(1);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        message1.writeBody(new ObjectOutputStream(bout), bout);
        message1.writeInt(2);
        message1.reset();
        RMQBytesMessage message2 = new RMQBytesMessage();
        message2.writeInt(1);
        message2.writeInt(2);
        message2.reset();
        byte[] b1 = new byte[8];
        byte[] b2 = new byte[8];
        message1.readBytes(b1);
        message2.readBytes(b2);
        assertTrue(Arrays.equals(b1, b2));
    }

    @Test
    public void testBytesMessage() throws Exception {
        RMQBytesMessage message = new RMQBytesMessage();
        writeBytesMessage(message);
        message.reset();
        readBytesMessage(message);
    }

    @Test
    public void testMapMessage() throws Exception {
        RMQMapMessage message = new RMQMapMessage();
        writeMapMessage(message);
        readMapMessage(message);
    }

    @Test
    public void testStreamMessage() throws Exception {
        RMQStreamMessage message = new RMQStreamMessage();
        writeStreamMessage(message);
        message.reset();
        readStreamMessage(message);
    }

    @Test
    public void testObjectMessage() throws Exception {
        RMQObjectMessage message = new RMQObjectMessage();
        writeObjectMessage(message);
        readObjectMessage(message);
    }

    @Test
    public void testObjectMessageWithUntrustedPayload() throws Exception {
        assertThrows(RMQJMSException.class, () -> {
            RMQObjectMessage message = new RMQObjectMessage();
            writeObjectMessage(message, Color.WHITE);
            readObjectMessage(message, Color.WHITE, Arrays.asList("java.lang", "com.rabbitmq"));
        });
    }

    @Test
    public void testTextMessage() throws Exception {
        RMQTextMessage message = new RMQTextMessage();
        writeTextMessage(message);
        readTextMessage(message);
    }

    private static final char[] CHAR = new char[]{' ', ';', '…', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final int CHARLEN = CHAR.length;
    private static final String LONG_STRING_BODY = generateLongString(65535);

    private static String generateLongString(int len) {
        StringBuilder sb = new StringBuilder("Long String length>="+len);
        for (int i=0; i<len; ++i) {
            sb.append(CHAR[i % CHARLEN]);
        }
        return sb.toString();
    }

    private static void writeTextMessage(TextMessage message) throws JMSException {
        message.setText(LONG_STRING_BODY);
    }

    private static void readTextMessage(TextMessage message) throws JMSException {
        assertEquals(LONG_STRING_BODY, message.getText());
    }

    public static void writeObjectMessage(ObjectMessage message) throws JMSException {
        message.setObject(new TestSerializable(8, "Test object"));
    }

    public static void writeObjectMessage(ObjectMessage message, Object obj) throws JMSException {
        message.setObject((Serializable) obj);
    }

    public static void readObjectMessage(ObjectMessage message) throws JMSException {
        assertEquals(new TestSerializable(8, "Test object"), message.getObject());
    }

    public static void readObjectMessage(RMQObjectMessage message, Object obj, List<String> trustedPackages) throws JMSException {
        assertEquals(obj, message.getObject(trustedPackages));
    }

    public static void writeMapMessage(MapMessage message) throws JMSException {
        message.setBoolean("boolean", true);
        message.setObject("string.boolean", "true");

        message.setByte("byte", Byte.MAX_VALUE);
        message.setObject("string.byte", String.valueOf(Byte.MAX_VALUE));

        message.setShort("short", Short.MAX_VALUE);
        message.setObject("string.short", String.valueOf(Short.MAX_VALUE));

        message.setInt("int", Integer.MAX_VALUE);
        message.setObject("string.int", String.valueOf(Integer.MAX_VALUE));

        message.setLong("long", Long.MAX_VALUE);
        message.setObject("string.long", String.valueOf(Long.MAX_VALUE));

        message.setFloat("float", Float.MAX_VALUE);
        message.setObject("string.float", String.valueOf(Float.MAX_VALUE));

        message.setDouble("double", Double.MAX_VALUE);
        message.setObject("string.double", String.valueOf(Double.MAX_VALUE));

        message.setString("string", "string");
        message.setObject("string.string", "string");

        message.setBytes("bytes", BYTE_ARRAY);
        message.setObject("string.bytes", BYTE_ARRAY);

    }

    public static void readMapMessage(MapMessage message) throws JMSException {
        assertTrue(message.getBoolean("boolean"));
        assertTrue(message.getBoolean("string.boolean"));

        assertEquals(Byte.MAX_VALUE, message.getByte("byte"));
        assertEquals(Byte.MAX_VALUE, message.getShort("byte"));
        assertEquals(Byte.MAX_VALUE, message.getInt("byte"));
        assertEquals(Byte.MAX_VALUE, message.getLong("byte"));
        assertEquals(Byte.MAX_VALUE, message.getByte("string.byte"));

        assertEquals(Short.MAX_VALUE, message.getShort("short"));
        assertEquals(Short.MAX_VALUE, message.getInt("short"));
        assertEquals(Short.MAX_VALUE, message.getLong("short"));
        assertEquals(Short.MAX_VALUE, message.getShort("string.short"));

        assertEquals(Integer.MAX_VALUE, message.getInt("int"));
        assertEquals(Integer.MAX_VALUE, message.getLong("int"));
        assertEquals(Integer.MAX_VALUE, message.getInt("string.int"));

        assertEquals(Long.MAX_VALUE, message.getLong("long"));
        assertEquals(Long.MAX_VALUE, message.getLong("string.long"));

        assertEquals(Float.MAX_VALUE, message.getFloat("float"), 1e-20D);

        // we must cast to get the same precision error -
        // https://en.wikipedia.org/wiki/Floating_point
        assertEquals((double) Float.MAX_VALUE, message.getDouble("float"), 1e-20D);
        assertEquals(Float.MAX_VALUE, message.getFloat("string.float"), 1e-20D);

        assertEquals(Double.MAX_VALUE, message.getDouble("double"), 1e-20D);
        assertEquals(Double.MAX_VALUE, message.getDouble("string.double"), 1e-20D);

        assertTrue(Arrays.equals(BYTE_ARRAY, message.getBytes("bytes")));
        assertTrue(Arrays.equals(BYTE_ARRAY, message.getBytes("string.bytes")));

    }

    public static void readBytesMessage(BytesMessage message) throws JMSException {
        assertTrue(message.readBoolean());
        assertEquals(-1, message.readByte());
        assertEquals(255, message.readUnsignedByte());
        byte[] b1 = new byte[1];
        byte[] b2 = new byte[2];
        message.readBytes(b2);
        assertTrue(Arrays.equals(BYTE_ARRAY, b2));
        message.readBytes(b1, 1);
        assertEquals(BYTE_ARRAY[1], b1[0]);
        assertEquals('X', message.readChar());
        assertEquals(2.35d, message.readDouble(), 1e-20D);
        assertEquals(1.54f, message.readFloat(), 1e-20D);
        assertEquals(Integer.MAX_VALUE, message.readInt());
        assertEquals(Long.MAX_VALUE, message.readLong());
        assertEquals(Short.MAX_VALUE, message.readShort());
        assertEquals(0xFFFF, message.readUnsignedShort());
        assertEquals("TEST", message.readUTF());
    }

    public static void writeBytesMessage(BytesMessage message) throws JMSException {
        message.writeBoolean(true);
        message.writeByte((byte) -1); // signed
        message.writeByte((byte) (255 & 0xFF)); // unsigned
        message.writeBytes(BYTE_ARRAY);
        message.writeBytes(BYTE_ARRAY, 1, 1);
        message.writeChar('X');
        message.writeDouble(2.35d);
        message.writeFloat(1.54f);
        message.writeInt(Integer.MAX_VALUE);
        message.writeLong(Long.MAX_VALUE);
        message.writeShort(Short.MAX_VALUE);
        message.writeShort((short) 0xFFFF);
        message.writeUTF("TEST");
    }

    public static void readLongBytesMessage(BytesMessage message) throws JMSException {
        assertTrue(message.readBoolean());
        assertEquals(-1, message.readByte());
        assertEquals(255, message.readUnsignedByte());
        byte[] b1 = new byte[1];
        byte[] b2 = new byte[LARGE_BYTES_LEN];
        message.readBytes(b2);
        assertTrue(Arrays.equals(LARGE_BYTE_ARRAY, b2));
        message.readBytes(b1, 1);
        assertEquals(LARGE_BYTE_ARRAY[1], b1[0]);
        assertEquals('X', message.readChar());
        assertEquals(2.35d, message.readDouble(), 1e-20D);
        assertEquals(1.54f, message.readFloat(), 1e-20D);
        assertEquals(Integer.MAX_VALUE, message.readInt());
        assertEquals(Long.MAX_VALUE, message.readLong());
        assertEquals(Short.MAX_VALUE, message.readShort());
        assertEquals(0xFFFF, message.readUnsignedShort());
        assertEquals("TEST", message.readUTF());
    }

    public static void writeLongBytesMessage(BytesMessage message) throws JMSException {
        message.writeBoolean(true);
        message.writeByte((byte) -1); // signed
        message.writeByte((byte) (255 & 0xFF)); // unsigned
        message.writeBytes(LARGE_BYTE_ARRAY);
        message.writeBytes(LARGE_BYTE_ARRAY, 1, 1);
        message.writeChar('X');
        message.writeDouble(2.35d);
        message.writeFloat(1.54f);
        message.writeInt(Integer.MAX_VALUE);
        message.writeLong(Long.MAX_VALUE);
        message.writeShort(Short.MAX_VALUE);
        message.writeShort((short) 0xFFFF);
        message.writeUTF("TEST");
    }

    public static void writeStreamMessage(StreamMessage message) throws JMSException {
        message.writeBoolean(true);
        message.writeByte((byte) -1); // signed
        message.writeByte((byte) (255 & 0xFF)); // unsigned
        message.writeBytes(BYTE_ARRAY);
        message.writeBytes(BYTE_ARRAY, 1, 1);
        message.writeChar('X');
        message.writeDouble(2.35d);
        message.writeFloat(1.54f);
        message.writeInt(Integer.MAX_VALUE);
        message.writeLong(Long.MAX_VALUE);
        message.writeShort(Short.MAX_VALUE);
        message.writeShort((short) 0xFFFF);
        message.writeString("TEST");
        try {
            message.writeObject(new TestNonSerializable());
            fail("Did not throw exception trying to send non-serializable object");
        } catch (Exception ignored) {
        }
    }

    public static void readStreamMessage(StreamMessage message) throws JMSException {
        assertTrue(message.readBoolean());
        assertEquals(-1, message.readByte());
        assertEquals(255, message.readByte() & 0xFF);
        byte[] b1 = new byte[1];
        byte[] b2 = new byte[2];
        int count = message.readBytes(b2);
        if (count==b2.length) {
            assertEquals(-1, message.readBytes(new byte[1024]));
        }
        assertTrue(Arrays.equals(BYTE_ARRAY, b2));

        count = message.readBytes(b1);
        if (count==b1.length) {
            assertEquals(-1, message.readBytes(new byte[1024]));
        }
        assertEquals(BYTE_ARRAY[1], b1[0]);
        assertEquals('X', message.readChar());
        assertEquals(2.35d, message.readDouble(), 1e-20D);
        assertEquals(1.54f, message.readFloat(), 1e-20D);
        assertEquals(Integer.MAX_VALUE, message.readInt());
        assertEquals(Long.MAX_VALUE, message.readLong());
        assertEquals(Short.MAX_VALUE, message.readShort());
        assertEquals(0xFFFF, message.readShort() & 0xFFFF);
        assertEquals("TEST", message.readString());

    }

    public static class TestSerializable implements Serializable {
        /** TODO */
        private static final long serialVersionUID = 3725702565209476472L;
        private int i;
        private Object object;

        public TestSerializable(int i, Object object) {
            this.i = i;
            this.object = object;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.i;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;
            TestSerializable other = (TestSerializable) obj;
            if (this.i != other.i)
                return false;
            if (this.object == other.object)
                return true;
            if (this.object == null)
                return false;
            if (! this.object.equals(other.object))
                return false;
            return true;
        }

    }

    private static class TestNonSerializable {

    }
}
