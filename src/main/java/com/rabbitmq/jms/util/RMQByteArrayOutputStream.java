/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;

public class RMQByteArrayOutputStream extends ByteArrayOutputStream {

    public RMQByteArrayOutputStream(int size) {
        super(size);
    }

    public void writeBoolean(boolean value) {
        this.write((byte) (value ? 1 : 0));
    }

    public void writeByte(byte value) {
        this.write(value);
    }

    public void writeShort(short value) {
        this.write((byte) (value >>> 8));
        this.write((byte) (value >>> 0));
    }

    public void writeChar(char value) {
        this.write((byte) (value >>> 8));
        this.write((byte) (value >>> 0));
    }

    public void writeInt(int value) {
        this.write((byte) (value >>> 24));
        this.write((byte) (value >>> 16));
        this.write((byte) (value >>> 8));
        this.write((byte) (value >>> 0));
    }

    public void writeLong(long value) {
        this.write((byte) (value >>> 56));
        this.write((byte) (value >>> 48));
        this.write((byte) (value >>> 40));
        this.write((byte) (value >>> 32));
        this.write((byte) (value >>> 24));
        this.write((byte) (value >>> 16));
        this.write((byte) (value >>> 8));
        this.write((byte) (value >>> 0));
    }

    public void writeFloat(float value) {
        this.writeInt(Float.floatToIntBits(value));
    }

    public void writeDouble(double value) {
        this.writeLong(Double.doubleToLongBits(value));
    }

    public void writeUTF(String value) throws JMSException {
        try {
            byte[] ba = value.getBytes("UTF-8");
            if (ba.length > 0xFFFF)
                throw new MessageFormatException("UTF String too long");
            this.writeShort((short)ba.length);
            this.write(ba);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    public void writeBytes(byte[] value)  {
        if (value == null)
            throw new IllegalArgumentException("Null byte array");
        this.writeBytes(value, 0, value.length);
    }

    public void writeBytes(byte[] value, int offset, int length) {
        if (value == null) {
            throw new IllegalArgumentException("Null byte array");
        } else if (offset>=value.length || length<0) {
            throw new IndexOutOfBoundsException();
        }
        this.write(value, offset, length);
    }

    public void writeObject(Object value) throws JMSException {
        writePrimitiveData(value, this);
    }

    /**
     * Utility method to write an object as a primitive or as an object
     * @param s the object to write
     * @param out the output (normally a stream) to write it to
     * @throws IOException from write primitives
     * @throws MessageFormatException if s is not a recognised type for writing
     * @throws NullPointerException if s is null
     */
    private static final void writePrimitiveData(Object s, RMQByteArrayOutputStream out) throws JMSException {
        if(s==null) {
            throw new NullPointerException();
        } else if (s instanceof Boolean) {
            out.writeBoolean(((Boolean) s).booleanValue());
        } else if (s instanceof Byte) {
            out.writeByte(((Byte) s).byteValue());
        } else if (s instanceof Short) {
            out.writeShort((((Short) s).shortValue()));
        } else if (s instanceof Integer) {
            out.writeInt(((Integer) s).intValue());
        } else if (s instanceof Long) {
            out.writeLong(((Long) s).longValue());
        } else if (s instanceof Float) {
            out.writeFloat(((Float) s).floatValue());
        } else if (s instanceof Double) {
            out.writeDouble(((Double) s).doubleValue());
        } else if (s instanceof String) {
            out.writeUTF((String) s);
        } else if (s instanceof Character) {
            out.writeChar(((Character) s).charValue());
        } else if (s instanceof Character) {
            out.writeChar(((Character) s).charValue());
        } else if (s instanceof byte[]) {
            try {
                out.write((byte[])s);
            } catch (IOException e) {
                throw new RMQJMSException(e);
            }
        } else
            throw new MessageFormatException(s + " is not a recognized writable type.");
    }
}
