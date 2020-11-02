/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.client.message;

import com.rabbitmq.jms.util.WhiteListObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UTFDataFormatException;

import java.util.List;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;
import javax.jms.StreamMessage;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.RMQMessageFormatException;

/* TODO For now we don't handle direct TCP streaming
 * this should write to disk, and when we send the message
 * we have disassemble into multiple messages and reassemble multiple messages
 * on the other side
 */
/**
 * Implements {@link StreamMessage} interface.
 */
public class RMQStreamMessage extends RMQMessage implements StreamMessage {

    private static final byte[] EOF_ARRAY = new byte[0];

    private volatile boolean reading;

    private transient ObjectInputStream in;
    private transient ByteArrayInputStream bin;
    private transient ObjectOutputStream out;
    private transient ByteArrayOutputStream bout;
    private volatile transient byte[] buf;
    private volatile transient byte[] readbuf = null;

    private final List<String> trustedPackages;

    public RMQStreamMessage(List<String> trustedPackages) {
        this(false, trustedPackages);
    }

    public RMQStreamMessage() {
        this(false, WhiteListObjectInputStream.DEFAULT_TRUSTED_PACKAGES);
    }

    private RMQStreamMessage(boolean reading, List<String> trustedPackages) {
        this.reading = reading;
        this.trustedPackages = trustedPackages;
        if (!reading) {
            this.bout = new ByteArrayOutputStream(RMQMessage.DEFAULT_MESSAGE_BODY_SIZE);
            try {
                this.out = new ObjectOutputStream(this.bout);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    private void writePrimitive(Object value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            RMQMessage.writePrimitive(value, this.out);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    private Object readPrimitiveType(Class<?> type) throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.readbuf!=null) {
            throw new MessageFormatException("You must call 'int readBytes(byte[])' since the buffer is not empty");
        }
        boolean success = true;
        try {
            this.bin.mark(0);
            Object o = RMQMessage.readPrimitive(in);
            if (o instanceof byte[]) {
                if (type == ByteArray.class || type == Object.class) {
                    return o;
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "byte[]"));
                }
            } else if (type == ByteArray.class) {
                if (o==null) {
                    return null;
                }
                throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "byte[]"));
            } else if (type == Boolean.class) {
                if (o == null) {
                    return Boolean.FALSE;
                } else if (o instanceof Boolean) {
                    return o;
                } else if (o instanceof String) {
                    return Boolean.parseBoolean((String)o);
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "boolean"));
                }
            } else if (type == Byte.class) {
                if (o instanceof Byte) {
                    return o;
                } else if (o instanceof String) {
                    return Byte.parseByte((String)o);
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "byte"));
                }
            } else if (type == Short.class) {
                if (o instanceof Byte) {
                    return (short) (Byte) o;
                } else if (o instanceof Short) {
                    return o;
                } else if (o instanceof String) {
                    return Short.parseShort((String)o);
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "byte"));
                }
            } else if (type == Integer.class) {
                if (o instanceof Byte) {
                    return (int) (Byte) o;
                } else if (o instanceof Short) {
                    return (int) (Short) o;
                } else if (o instanceof Integer){
                    return o;
                } else if (o instanceof String) {
                    return Integer.parseInt((String)o);
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "int"));
                }
            } else if (type == Character.class) {
                if (o instanceof Character) {
                    return o;
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "char"));
                }
            } else if (type == Long.class) {
                if (o instanceof Byte) {
                    return (long) (Byte) o;
                } else if (o instanceof Short) {
                    return (long) (Short) o;
                } else if (o instanceof Integer){
                    return (long) (Integer) o;
                } else if (o instanceof Long){
                    return o;
                } else if (o instanceof String) {
                    return Long.parseLong((String)o);
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "long"));
                }
            } else if (type == Float.class) {
                if (o instanceof Float) {
                    return (Float) o;
                } else if (o instanceof String) {
                    return Float.parseFloat((String)o);
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "float"));
                }
            } else if (type == Double.class) {
                if (o instanceof Float) {
                    return (double) (Float) o;
                } else if (o instanceof Double) {
                    return (Double) o;
                } else if (o instanceof String) {
                    return Double.parseDouble((String)o);
                } else {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "double"));
                }
            } else if (type == String.class) {
                if (o == null) {
                    return null;
                } else if (o instanceof byte[]) {
                    throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "String"));
                } else {
                    return o.toString();
                }
            } else if (type == Object.class) {
                return o;
            } else {
                throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, type.toString()));
            }
        } catch (NumberFormatException x) {
            success = false;
            throw x;
        } catch (ClassNotFoundException x) {
            success = false;
            throw new RMQJMSException(x);
        } catch (EOFException x) {
            success = false;
            throw new MessageEOFException(MSG_EOF);
        } catch (UTFDataFormatException x) {
            success = false;
            throw new RMQMessageFormatException(x);
        } catch (IOException x) {
            success = false;
            throw new RMQJMSException(x);
        } catch (Exception x) {
            success = false;
            if (x instanceof JMSException) {
                throw (JMSException)x;
            } else {
                throw new RMQJMSException(x);
            }
        } finally {
            if (!success) {
                this.bin.reset();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean readBoolean() throws JMSException {
        return (Boolean)this.readPrimitiveType(Boolean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() throws JMSException {
        return (Byte)this.readPrimitiveType(Byte.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws JMSException {
        return (Short)this.readPrimitiveType(Short.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char readChar() throws JMSException {
        return (Character)this.readPrimitiveType(Character.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws JMSException {
        return (Integer)this.readPrimitiveType(Integer.class);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong() throws JMSException {
        return (Long)this.readPrimitiveType(Long.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float readFloat() throws JMSException {
        return (Float)this.readPrimitiveType(Float.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double readDouble() throws JMSException {
        return (Double)this.readPrimitiveType(Double.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readString() throws JMSException {
        return (String)this.readPrimitiveType(String.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int readBytes(byte[] value) throws JMSException {
        if (readbuf==null) {
            readbuf = (byte[])this.readPrimitiveType(ByteArray.class);
            if (readbuf==null) return -1;
        }
        if (readbuf!=null) {
            if (readbuf==EOF_ARRAY) {
                readbuf = null;
                return -1;
            } else if (readbuf.length > value.length) {
                int result = value.length;
                int diff = readbuf.length - result;
                System.arraycopy(readbuf, 0, value, 0, result);
                byte[] tmp = new byte[diff];
                System.arraycopy(readbuf, result, tmp, 0, diff);
                readbuf = tmp;
                return result;
            } else {
                int result = Math.min(readbuf.length, value.length);
                System.arraycopy(readbuf, 0, value, 0, result);
                readbuf = (result==value.length) ? EOF_ARRAY : null;
                return result;
            }
        } else {
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, null,"byte[]"));
        }
    }

    /**
     * reads an object from the stream that was used to serialize this message
     * @return the object read
     * @throws JMSException if a deserialization exception happens
     */
    @Override
    public Object readObject() throws JMSException {
        return this.readPrimitiveType(Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBoolean(boolean value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeByte(byte value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeShort(short value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeChar(char value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeInt(int value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLong(long value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFloat(float value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeDouble(double value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeString(String value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBytes(byte[] value) throws JMSException {
        writePrimitive(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        byte[] buf = new byte[length];
        System.arraycopy(value, offset, buf, 0, length);
        writePrimitive(buf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeObject(Object value) throws JMSException {
        writeObject(value,false);
    }

    private void writeObject(Object value, boolean allowSerializable) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            RMQMessage.writePrimitive(value, this.out, allowSerializable);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws JMSException {
        this.readbuf = null;

        if (this.reading) {
            //if we already are reading, all we want to do is reset to the
            //beginning of the stream
            try {
                this.bin = new ByteArrayInputStream(buf);
                this.in = new ObjectInputStream(this.bin);
            } catch (IOException x) {
                throw new RMQJMSException(x);
            }
        } else {
            try {
                buf = null;
                if (this.out != null) {
                    this.out.flush();
                    buf = this.bout.toByteArray();
                } else {
                    buf = new byte[0];
                }
                this.bin = new ByteArrayInputStream(buf);
                this.in = new ObjectInputStream(this.bin);
            } catch (IOException x) {
                throw new RMQJMSException(x);
            }
            this.reading = true;
            this.out = null;
            this.bout = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearBodyInternal() throws JMSException {
        this.bout = new ByteArrayOutputStream(RMQMessage.DEFAULT_MESSAGE_BODY_SIZE);
        try {
            this.out = new ObjectOutputStream(this.bout);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
        this.bin = null;
        this.in = null;
        this.buf = null;
        this.readbuf = null;
        this.reading = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeBody(ObjectOutput out, ByteArrayOutputStream bout) throws IOException {
        this.out.flush();
        byte[] buf = this.bout.toByteArray();
        out.writeInt(buf.length);
        out.write(buf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readBody(ObjectInput inputStream, ByteArrayInputStream bin) throws IOException, ClassNotFoundException {
        int len = inputStream.readInt();
        buf = new byte[len];
        inputStream.read(buf);
        this.reading = true;
        this.bin = new ByteArrayInputStream(buf);
        this.in = new WhiteListObjectInputStream(this.bin, this.trustedPackages);
    }

    @Override
    protected void readAmqpBody(byte[] barr) {
        throw new UnsupportedOperationException();
    }

    private static class ByteArray {

    }

    @Override
    protected void writeAmqpBody(ByteArrayOutputStream out) throws IOException {
        throw new UnsupportedOperationException();
    }

    public static final RMQMessage recreate(StreamMessage msg) throws JMSException {
        RMQStreamMessage rmqSMsg = new RMQStreamMessage();
        RMQMessage.copyAttributes(rmqSMsg, msg);

        msg.reset(); // reset to beginning
        boolean endOfStream = false;
        while(!endOfStream) {
            try {
                rmqSMsg.writeObject(msg.readObject());
            } catch (MessageEOFException e) {
                endOfStream = true;
            }
        }
        return rmqSMsg;
    }
}
