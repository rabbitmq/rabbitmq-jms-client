// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageFormatException;

import com.rabbitmq.jms.client.message.RMQBytesMessage;
import org.junit.jupiter.api.Test;

public class ForeignBytesMessageTest {

    @Test
    public void testBytesMessage() throws Exception {
        BytesMessage testMsg = new ForeignBytesMessage();
        populateBytesMessage(testMsg);
        BytesMessage rmqMsg = (BytesMessage) RMQMessage.normalise(testMsg);
        rmqMsg.reset();
        inspectBytesMessage(rmqMsg);
    }

    @Test
    public void testEmptyBytesMessage() throws Exception {
        BytesMessage testMsg = new RMQBytesMessage();
        assertEquals(0, testMsg.getBodyLength());
        BytesMessage rmqMsg = (BytesMessage) RMQBytesMessage.recreate(testMsg);
        assertEquals(0, rmqMsg.getBodyLength());
    }

    private static final byte[] BYTE_ARRAY = new byte[]{ 0,1,2, (byte) -1 };
    private static final float DELTA = 1.0e-10f;
    private void populateBytesMessage(BytesMessage message) throws Exception {
        message.writeBoolean(Boolean.TRUE);
        message.writeByte(Byte.MIN_VALUE);
        message.writeByte((byte) 0xFF);
        message.writeBytes(BYTE_ARRAY);

        message.writeShort(Short.MIN_VALUE);
        message.writeShort((short) 0xFFFF);
        message.writeChar(Character.MIN_VALUE);
        message.writeInt(Integer.MIN_VALUE);
        message.writeLong(Long.MIN_VALUE);
        message.writeFloat(Float.MIN_VALUE);
        message.writeDouble(Double.MIN_VALUE);
        message.writeUTF("ABC");

        message.writeObject(Boolean.TRUE);
        message.writeObject(Byte.MAX_VALUE);
        message.writeObject(Short.MAX_VALUE);
        message.writeObject(Character.MAX_VALUE);
        message.writeObject(Integer.MAX_VALUE);
        message.writeObject(Long.MAX_VALUE);
        message.writeObject(Float.MAX_VALUE);
        message.writeObject(Double.MAX_VALUE);
        message.writeObject("ABC");
    }

    private void inspectBytesMessage(BytesMessage message) throws Exception {
        assertEquals(message.readBoolean(), Boolean.TRUE);
        assertEquals(message.readByte(), Byte.MIN_VALUE);
        assertEquals(message.readByte(), (byte) 0xFF);

        byte[] byteArray = new byte[BYTE_ARRAY.length];
        assertEquals(message.readBytes(byteArray), BYTE_ARRAY.length);
        assertArrayEquals(BYTE_ARRAY, byteArray);

        assertEquals(message.readShort(), Short.MIN_VALUE);
        assertEquals(message.readShort(), (short) 0xFFFF);
        assertEquals(message.readChar(), Character.MIN_VALUE);
        assertEquals(message.readInt(), Integer.MIN_VALUE);
        assertEquals(message.readLong(), Long.MIN_VALUE);
        assertEquals(message.readFloat(), Float.MIN_VALUE, DELTA);
        assertEquals(message.readDouble(), Double.MIN_VALUE, DELTA);
        assertEquals(message.readUTF(), "ABC");

        assertEquals(message.readBoolean(), Boolean.TRUE);
        assertEquals(message.readByte(), Byte.MAX_VALUE);
        assertEquals(message.readShort(), Short.MAX_VALUE);
        assertEquals(message.readChar(), Character.MAX_VALUE);
        assertEquals(message.readInt(), Integer.MAX_VALUE);
        assertEquals(message.readLong(), Long.MAX_VALUE);
        assertEquals(message.readFloat(), Float.MAX_VALUE, DELTA);
        assertEquals(message.readDouble(), Double.MAX_VALUE, DELTA);
        assertEquals(message.readUTF(), "ABC");
    }


    private static class ForeignBytesMessage implements BytesMessage {

        @Override public String                 getJMSMessageID() throws JMSException { return null; }
        @Override public void                   setJMSMessageID(String id) throws JMSException {}
        @Override public long                   getJMSTimestamp() throws JMSException { return 0; }
        @Override public void                   setJMSTimestamp(long timestamp) throws JMSException { }
        @Override public byte[]                 getJMSCorrelationIDAsBytes() throws JMSException { return null; }
        @Override public void                   setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException { }
        @Override public void                   setJMSCorrelationID(String correlationID) throws JMSException { }
        @Override public String                 getJMSCorrelationID() throws JMSException { return null; }
        @Override public Destination            getJMSReplyTo() throws JMSException { return null; }
        @Override public void                   setJMSReplyTo(Destination replyTo) throws JMSException { }
        @Override public Destination            getJMSDestination() throws JMSException { return null; }
        @Override public void                   setJMSDestination(Destination destination) throws JMSException { }
        @Override public int                    getJMSDeliveryMode() throws JMSException { return 0; }
        @Override public void                   setJMSDeliveryMode(int deliveryMode) throws JMSException { }
        @Override public boolean                getJMSRedelivered() throws JMSException { return false; }
        @Override public void                   setJMSRedelivered(boolean redelivered) throws JMSException { }
        @Override public String                 getJMSType() throws JMSException { return null; }
        @Override public void                   setJMSType(String type) throws JMSException { }
        @Override public long                   getJMSExpiration() throws JMSException { return 0; }
        @Override public void                   setJMSExpiration(long expiration) throws JMSException { }
        @Override public int                    getJMSPriority() throws JMSException { return 0; }
        @Override public void                   setJMSPriority(int priority) throws JMSException { }
        @Override public void                   clearProperties() throws JMSException { }
        @Override public boolean                propertyExists(String name) throws JMSException { return false; }
        @Override public boolean                getBooleanProperty(String name) throws JMSException { return false; }
        @Override public byte                   getByteProperty(String name) throws JMSException { return 0; }
        @Override public short                  getShortProperty(String name) throws JMSException { return 0; }
        @Override public int                    getIntProperty(String name) throws JMSException { return 0; }
        @Override public long                   getLongProperty(String name) throws JMSException { return 0; }
        @Override public float                  getFloatProperty(String name) throws JMSException { return 0; }
        @Override public double                 getDoubleProperty(String name) throws JMSException { return 0; }
        @Override public String                 getStringProperty(String name) throws JMSException { return null; }
        @Override public Object                 getObjectProperty(String name) throws JMSException { return null; }
        @Override public Enumeration<String>    getPropertyNames() throws JMSException { return Collections.enumeration(Collections.<String>emptyList()); }
        @Override public void                   setBooleanProperty(String name, boolean value) throws JMSException { }
        @Override public void                   setByteProperty(String name, byte value) throws JMSException { }
        @Override public void                   setShortProperty(String name, short value) throws JMSException { }
        @Override public void                   setIntProperty(String name, int value) throws JMSException { }
        @Override public void                   setLongProperty(String name, long value) throws JMSException { }
        @Override public void                   setFloatProperty(String name, float value) throws JMSException { }
        @Override public void                   setDoubleProperty(String name, double value) throws JMSException { }
        @Override public void                   setStringProperty(String name, String value) throws JMSException { }
        @Override public void                   setObjectProperty(String name, Object value) throws JMSException { }
        @Override public void                   acknowledge() throws JMSException { }

        private static final int BODY_MAX_LENGTH = 2048;
        private final byte[] body = new byte[BODY_MAX_LENGTH];
        private int pos = 0; // read position
        private int len = 0; // number bytes written to body

        @Override public void                   clearBody() throws JMSException { pos = len = 0; }
        @Override public long                   getBodyLength() throws JMSException { return len; }
        @Override public void                   reset() throws JMSException { pos = 0; }

        private static int ub(byte b) { return (int)b | 0xff; }
        private byte nb() { assertTrue(pos<len); return body[pos++]; }
        private int pb() { return ub(nb()); }
        private long lb() { return pb(); }

        @Override public boolean                readBoolean() throws JMSException { return nb()!=0; }
        @Override public byte                   readByte() throws JMSException { return nb(); }
        @Override public int                    readUnsignedByte() throws JMSException { return pb(); }
        @Override public short                  readShort() throws JMSException { return (short) (pb()*256 + pb()); }
        @Override public int                    readUnsignedShort() throws JMSException { return pb()*256 + pb(); }
        @Override public char                   readChar() throws JMSException { return (char)((pb()<<8) + pb()); }
        @Override public int                    readInt() throws JMSException { return (pb()<<24) + (pb()<<16) + (pb()<<8) + pb(); }
        @Override public long                   readLong() throws JMSException { return (lb()<<56) + (lb()<<48) + (lb()<<40) + (lb()<<32) + (lb()<<24) + (lb()<<16) + (lb()<<8) + lb(); }
        @Override public float                  readFloat() throws JMSException { return Float.intBitsToFloat(readInt()); }
        @Override public double                 readDouble() throws JMSException { return Double.longBitsToDouble(readLong()); }
        @Override public String                 readUTF() throws JMSException { return readUTF(this); }
        @Override public int                    readBytes(byte[] value) throws JMSException { return readBytes(value, value.length); }
        @Override public int                    readBytes(byte[] value, int length) throws JMSException {
            if (length < 0 || length > value.length) throw new IndexOutOfBoundsException("TestForeignBytesMessage.readBytes(byte[],int)");
            length = Math.min(length, len-pos);
            System.arraycopy(body, pos, value, 0, length);
            pos+=length;
            return length;
        }

        private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

        @Override public void                   writeBoolean(boolean value) throws JMSException { body[len++] = (byte)(value?1:0); }
        @Override public void                   writeByte(byte value) throws JMSException { body[len++] = value; }
        @Override public void                   writeShort(short value) throws JMSException { writeByte((byte)(value>>>8)); writeByte((byte)value); }
        @Override public void                   writeChar(char value) throws JMSException { writeShort((short)value); }
        @Override public void                   writeInt(int value) throws JMSException { writeShort((short)(value>>16)); writeShort((short)value); }
        @Override public void                   writeLong(long value) throws JMSException { writeInt((int)(value>>32)); writeInt((int)value); }
        @Override public void                   writeFloat(float value) throws JMSException { writeInt(Float.floatToIntBits(value)); }
        @Override public void                   writeDouble(double value) throws JMSException { writeLong(Double.doubleToLongBits(value)); }
        @Override public void                   writeUTF(String value) throws JMSException { writeUTF(this, value); }
        @Override public void                   writeBytes(byte[] value) throws JMSException { writeBytes(value, 0, value.length) ;}
        @Override public void                   writeBytes(byte[] value, int offset, int length) throws JMSException {
            if (length < 0 || offset+length > value.length) throw new IndexOutOfBoundsException("TestForeignBytesMessage.writeBytes(byte[],int,int)");
            System.arraycopy(value, offset, body, len, length);
            len+=length;
        }

        @Override public void                   writeObject(Object value) throws JMSException {
                 if (value instanceof Boolean   ) writeBoolean((Boolean)value);
            else if (value instanceof Byte      ) writeByte((Byte)value);
            else if (value instanceof Short     ) writeShort((Short)value);
            else if (value instanceof Character ) writeChar((Character)value);
            else if (value instanceof Integer   ) writeInt((Integer)value);
            else if (value instanceof Long      ) writeLong((Long)value);
            else if (value instanceof Float     ) writeFloat((Float)value);
            else if (value instanceof Double    ) writeDouble((Double)value);
            else if (value instanceof String    ) writeUTF((String)value);
            else if (value instanceof byte[]    ) writeBytes((byte[])value);
            else throw new MessageFormatException("Invalid type on writeObject() for TestForeignBytesMessage");
        }

        private static void writeUTF(ForeignBytesMessage fbmsg, String value) throws JMSException {
            byte[] btArr = value.getBytes(UTF8_CHARSET);
            fbmsg.writeShort((short)btArr.length);
            fbmsg.writeBytes(btArr);
        }

        private static String readUTF(ForeignBytesMessage fbmsg) throws JMSException {
            int utflen = fbmsg.readUnsignedShort();

            byte[] byteArr = new byte[utflen];
            int bytesRead = fbmsg.readBytes(byteArr);
            assertEquals(utflen, bytesRead, "Not enough bytes for readUTF()");

            return new String(byteArr, UTF8_CHARSET);
        }

        @Override
        public long getJMSDeliveryTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setJMSDeliveryTime(long deliveryTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getBody(Class<T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isBodyAssignableTo(Class c) {
            throw new UnsupportedOperationException();
        }
    }

}
