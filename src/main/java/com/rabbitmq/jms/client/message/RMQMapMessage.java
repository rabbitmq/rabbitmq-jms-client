// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.util.Map.Entry;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.DiscardingObjectOutput;
import com.rabbitmq.jms.util.IteratorEnum;
import com.rabbitmq.jms.util.RMQMessageFormatException;

/**
 * Implementation of {@link MapMessage} interface.
 */
public class RMQMapMessage extends RMQMessage implements MapMessage {

    private Map<String, Serializable> data = new HashMap<>();

    @Override
    public boolean getBoolean(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return false;
        else if (o instanceof Boolean)
            return (Boolean) o;
        else if (o instanceof String)
            return Boolean.parseBoolean((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "boolean"));
    }

    @Override
    public byte getByte(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "byte"));
        else if (o instanceof Byte)
            return (Byte) o;
        else if (o instanceof String)
            return Byte.parseByte((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "byte"));
    }

    @Override
    public short getShort(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "short"));
        else if (o instanceof Byte)
            return (Byte) o;
        else if (o instanceof Short)
            return (Short) o;
        else if (o instanceof String)
            return Short.parseShort((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "short"));
    }

    @Override
    public char getChar(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "char"));
        else if (o instanceof Character)
            return (Character) o;
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "char"));
    }

    @Override
    public int getInt(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "int"));
        else if (o instanceof Byte)
            return (Byte) o;
        else if (o instanceof Short)
            return (Short) o;
        else if (o instanceof Integer)
            return (Integer) o;
        else if (o instanceof String)
            return Integer.parseInt((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "int"));
    }

    @Override
    public long getLong(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "long"));
        else if (o instanceof Byte)
            return (Byte) o;
        else if (o instanceof Short)
            return (Short) o;
        else if (o instanceof Integer)
            return (Integer) o;
        else if (o instanceof Long)
            return (Long) o;
        else if (o instanceof String)
            return Long.parseLong((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "long"));
    }

    @Override
    public float getFloat(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "float"));
        else if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof String)
            return Float.parseFloat((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "float"));
    }

    @Override
    public double getDouble(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "double"));
        else if (o instanceof Float)
            return (Float) o;
        else if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof String)
            return Double.parseDouble((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "double"));
    }

    @Override
    public String getString(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return null;
        else if (o instanceof String)
            return ((String) o);
        else if (o instanceof byte[])
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "String"));
        else
            return o.toString();
    }

    @Override
    public byte[] getBytes(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null) {
            return null;
        } else if (o instanceof byte[]) {
            byte[] b1 = ((byte[]) o);
            byte[] b2 = new byte[b1.length];
            System.arraycopy(b1, 0, b2, 0, b1.length);
            return b2;
        } else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "byte[]"));
    }

    @Override
    public Object getObject(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null) {
            return null;
        } else if (o instanceof byte[]) {
            return getBytes(name);
        } else {
            return o;
        }
    }

    @Override
    public Enumeration<String> getMapNames() throws JMSException {
        return new IteratorEnum<>(this.data.keySet().iterator());
    }

    @Override
    public void setBoolean(String name, boolean value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setByte(String name, byte value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setShort(String name, short value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setChar(String name, char value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setInt(String name, int value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setLong(String name, long value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setFloat(String name, float value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setDouble(String name, double value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setString(String name, String value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    @Override
    public void setBytes(String name, byte[] value) throws JMSException {
        checkNotReadonlyBody();
        setBytes(name, value, 0, value.length);
    }

    @Override
    public void setBytes(String name, byte[] value, int offset, int length) throws JMSException {
        checkNotReadonlyBody();
        if (value == null) {
            this.data.remove(name);
            return;
        }
        byte[] buf = new byte[length];
        System.arraycopy(value, offset, buf, 0, length);
        this.data.put(name, buf);
    }

    @Override
    public void setObject(String name, Object value) throws JMSException {
        checkNotReadonlyBody();
        if (name == null && value == null) {
        } else if (value == null) {
            this.data.remove(name);
        } else if (!(value instanceof Serializable)) {
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, value, Serializable.class.getName()));
        } else {
            try {
                /* Make sure we can write this type of object */
                writePrimitiveData(value, new DiscardingObjectOutput(), false);
            } catch (IOException x) {
                throw new RMQMessageFormatException(x);
            }
        }

        if (value instanceof byte[]) {
            setBytes(name, (byte[])value);
        } else {
            this.data.put(name, (Serializable) value);
        }
    }

    @Override
    public boolean itemExists(String name) throws JMSException {
        return this.data.containsKey(name);
    }

    @Override
    public void clearBodyInternal() throws JMSException {
        this.data.clear();
    }

    @Override
    protected void writeBody(ObjectOutput out, ByteArrayOutputStream bout) throws IOException {
        int size = this.data.size();
        out.writeInt(size);
        for (Map.Entry<String, Serializable> entry : this.data.entrySet()) {
            out.writeUTF(entry.getKey());
            try {
                RMQMessage.writePrimitive(entry.getValue(), out);
            } catch (MessageFormatException x) {
                throw new IOException(x);
            }
        }
    }

    @Override
    protected void readBody(ObjectInput inputStream, ByteArrayInputStream bin) throws IOException, ClassNotFoundException {
        int size = inputStream.readInt();
        for (int i = 0; i < size; i++) {
            String name = inputStream.readUTF();
            Object value = RMQMessage.readPrimitive(inputStream);
            this.data.put(name, (Serializable) value);
        }
    }

    @Override
    protected void readAmqpBody(byte[] barr) {
        throw new UnsupportedOperationException();
    }

    /**
     * Utility method to write an object as a primitive or as an object
     * @param s the object to write
     * @param out the stream to write it to
     * @param allowSerializable true if we allow objects other than serializable
     * @throws IOException
     * @throws NullPointerException if s is null
     */
    private void writePrimitiveData(Object s, ObjectOutput out, boolean allowSerializable) throws IOException, MessageFormatException {
        if(s==null) {
            throw new NullPointerException();
        } else if (s instanceof Boolean) {
            out.writeBoolean((Boolean) s);
        } else if (s instanceof Byte) {
            out.writeByte((Byte) s);
        } else if (s instanceof Short) {
            out.writeShort(((Short) s));
        } else if (s instanceof Integer) {
            out.writeInt((Integer) s);
        } else if (s instanceof Long) {
            out.writeLong((Long) s);
        } else if (s instanceof Float) {
            out.writeFloat((Float) s);
        } else if (s instanceof Double) {
            out.writeDouble((Double) s);
        } else if (s instanceof String) {
            out.writeUTF((String) s);
        } else if (s instanceof Character) {
            out.writeChar((Character) s);
        } else if (allowSerializable && s instanceof Serializable) {
            out.writeObject(s);
        } else if (s instanceof byte[]) {
            out.write((byte[])s);
        } else
            throw new MessageFormatException(s + " is not a recognized primitive type.");
    }

    private void checkNotReadonlyBody() throws JMSException {
        if (isReadonlyBody())
            throw new MessageNotWriteableException("Message not writeable");
    }

    @Override
    protected void writeAmqpBody(ByteArrayOutputStream out) throws IOException {
        throw new UnsupportedOperationException();
    }

    public static RMQMessage recreate(MapMessage msg) throws JMSException {
        RMQMapMessage rmqMMsg = new RMQMapMessage();
        RMQMessage.copyAttributes(rmqMMsg, msg);

        copyMapObjects(rmqMMsg, msg);
        return rmqMMsg;
    }

    private static void copyMapObjects(RMQMapMessage rmqMsg, MapMessage msg) throws JMSException {
        @SuppressWarnings("unchecked")
        Enumeration<String> mapNames = (Enumeration<String>) msg.getMapNames();
        while (mapNames.hasMoreElements()) {
            String name = mapNames.nextElement();
            rmqMsg.setObject(name, msg.getObject(name));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isBodyAssignableTo(Class c) {
        return this.data == null ? true : c.isAssignableFrom(Map.class)
            || Serializable.class == c;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T doGetBody(Class<T> c) {
        if (this.data == null) {
            return null;
        } else if (this.data.isEmpty()) {
            return (T) Collections.emptyMap();
        } else {
            Map<String, Serializable> copy = new HashMap<>(this.data.size());
            for (Entry<String, Serializable> entry : this.data.entrySet()) {
                copy.put(entry.getKey(), entry.getValue());
            }
            return (T) copy;
        }
    }

    @Override
    public boolean isAmqpWritable() {
        return false;
    }
}
