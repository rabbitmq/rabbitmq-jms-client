/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.client.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.Bits;
import com.rabbitmq.jms.util.RMQJMSException;

/**
 * Implementation of {@link BytesMessage} interface.
 */
public class RMQBytesMessage extends RMQMessage implements BytesMessage {
    /**
     * This variable is set true if we are reading, but not writing the message
     * and false if we are writing but can not read the message
     */
    private volatile boolean reading;

    /** The object input is wrapping a {@link ByteArrayInputStream} */
    private transient ByteArrayInputStream bin;
    /** The byte array input stream is reading from our body buffer */
    private volatile transient byte[] buf;
    /** The position of our read in the byte array buf */
    private volatile transient int pos;

    /** The {@link ObjectOutput} we use to write structured data */
    private transient ObjectOutputStream out;
    /** The object output is wrapping a {@link ByteArrayOutputStream} */
    private transient ByteArrayOutputStream bout;

    public RMQBytesMessage() {
        this(false);
    }

    /**
     * Instantiates a new RMQBytesMessage
     * @param reading if this message is in a read state
     */
    public RMQBytesMessage(boolean reading) {
        this.reading = reading;
        if (!reading) {
            /* If we are in Write state, then create the objects to support that state */
            this.bout = new ByteArrayOutputStream(RMQMessage.DEFAULT_MESSAGE_BODY_SIZE);
            try {
                this.out = new ObjectOutputStream(this.bout);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean readBoolean() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + Bits.NUM_BYTES_IN_BOOLEAN > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        return Bits.getBoolean(this.buf, this.pos++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + 1 > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        return this.buf[this.pos++];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedByte() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + 1 > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        return ((int) (this.buf[this.pos++])) & 0xFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + Bits.NUM_BYTES_IN_SHORT > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        short s = Bits.getShort(this.buf, this.pos);
        this.pos += Bits.NUM_BYTES_IN_SHORT;
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + Bits.NUM_BYTES_IN_SHORT > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        short s = Bits.getShort(this.buf, this.pos);
        this.pos += Bits.NUM_BYTES_IN_SHORT;
        return ((int) s) & 0xFFFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char readChar() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + Bits.NUM_BYTES_IN_CHAR > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        char ch = Bits.getChar(this.buf, this.pos);
        this.pos += Bits.NUM_BYTES_IN_CHAR;
        return ch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + Bits.NUM_BYTES_IN_INT > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        int i = Bits.getInt(this.buf, this.pos);
        this.pos += Bits.NUM_BYTES_IN_INT;
        return i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + Bits.NUM_BYTES_IN_LONG > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        long l = Bits.getChar(this.buf, this.pos);
        this.pos += Bits.NUM_BYTES_IN_LONG;
        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float readFloat() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + Bits.NUM_BYTES_IN_FLOAT > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        float flt = Bits.getFloat(this.buf, this.pos);
        this.pos += Bits.NUM_BYTES_IN_FLOAT;
        return flt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double readDouble() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (this.pos + Bits.NUM_BYTES_IN_DOUBLE > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        double dbl = Bits.getDouble(this.buf, this.pos);
        this.pos += Bits.NUM_BYTES_IN_DOUBLE;
        return dbl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readUTF() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        int lenUTF = readUnsignedShort(); // modifies pos
        if (this.pos + lenUTF > this.buf.length)
            throw new MessageEOFException(MSG_EOF);
        try {
            String str = new String(this.buf, this.pos, lenUTF, "UTF-8");
            this.pos += lenUTF;
            return str;
        } catch (UnsupportedEncodingException uee) {
            throw new RMQJMSException("Format of UTF not valid", uee);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readBytes(byte[] value) throws JMSException {
        return this.readBytes(value, value.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readBytes(byte[] value, int length) throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        if (length<0 || length>value.length) {
            throw new IndexOutOfBoundsException();
        }
        if (this.pos < this.buf.length) {
            int readLen = Math.min(length, this.buf.length - this.pos);
            System.arraycopy(this.buf, this.pos, value, 0, readLen);
            return readLen;
        }
        return -1; // means EOF already
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBoolean(boolean value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeBoolean(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeByte(byte value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeByte(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeShort(short value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeShort(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeChar(char value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeChar(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeInt(int value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeInt(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLong(long value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeLong(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFloat(float value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeFloat(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeDouble(double value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeDouble(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeUTF(String value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeUTF(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBytes(byte[] value) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.write(value);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            if (value == null ) {
                throw new MessageFormatException("Null byte array");
            } else if (offset>=value.length || length<0) {
                throw new IndexOutOfBoundsException();
            }
            this.out.write(value, offset, length);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeObject(Object value) throws JMSException {
        writeObject(value,false);
    }

    protected void writeObject(Object value, boolean allowSerializable) throws JMSException {
        if (this.reading || isReadonlyBody())
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            writePrimitiveData(value, this.out, allowSerializable);
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws JMSException {
        if (this.reading) {
            //if we already are reading, all we want to do is reset to the
            //beginning of the stream
            this.pos = 0;
        } else {
            if (this.bout != null) {
                this.buf = this.bout.toByteArray();
            } else {
                this.buf = new byte[0];
            }
            this.pos = 0;
            this.reading = true;
            this.bout = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getBodyLength() throws JMSException {
        return this.reading ? this.buf.length : this.bout.size();
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
        this.pos = 0;
        this.buf = null;
        this.reading = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeBody(ObjectOutput oOut) throws IOException {
        this.out.flush();
        byte[] buf = this.bout.toByteArray();
        oOut.writeInt(buf.length);
        oOut.write(buf);
    }

    /**
     * {@inheritDoc}
     * Structured data (if any) is already read by the time this is called, in which case, for {@link RMQBytesMessage},
     * only a byte array remains.
     */
    @Override
    protected void readBody(ObjectInput inputStream) throws IOException, ClassNotFoundException {
        int len = inputStream.readInt();
        this.buf = new byte[len];
        inputStream.read(this.buf);
        this.reading = true;
        this.pos = 0;
    }

    /**
     * Utility method to write an object as a primitive or as an object
     * @param s the object to write
     * @param out the output (normally a stream) to write it to
     * @param allowSerializable true if we allow {@link Serializable} objects
     * @throws IOException from write primitives
     * @throws MessageFormatException if s is not a recognised type for writing
     * @throws NullPointerException if s is null
     */
    private final static void writePrimitiveData(Object s, ObjectOutput out, boolean allowSerializable) throws IOException, MessageFormatException {
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
        } else if (allowSerializable && s instanceof Serializable) {
            out.writeObject(s);
        } else if (s instanceof byte[]) {
            out.write((byte[])s);
        } else
            throw new MessageFormatException(s + " is not a recognized writable type.");
    }

    @Override
    protected void writeAmqpBody(ByteArrayOutputStream out) throws IOException {
        this.out.flush();
        byte[] buf = this.bout.toByteArray();
        out.write(buf);
    }
}
