package com.rabbitmq.jms.client.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.Util;

/**
 *
 */
public class RMQBytesMessage extends RMQMessage implements BytesMessage {
    private static final String NOT_READABLE = "Message not readable";
    private static final String NOT_WRITEABLE = "Message not writeable";
    private volatile boolean reading;

    private transient ObjectInputStream in;
    private transient ByteArrayInputStream bin;
    private transient ObjectOutputStream out;
    private transient ByteArrayOutputStream bout;

    public RMQBytesMessage() {
        this(false);
    }

    public RMQBytesMessage(boolean reading) {
        this.reading = reading;
        if (!reading) {
            this.bout = new ByteArrayOutputStream(RMQMessage.DEFAULT_MESSAGE_BODY_SIZE);
            try {
                this.out = new ObjectOutputStream(this.bout);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }

    }

    @Override
    public boolean readBoolean() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readBoolean();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public byte readByte() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readByte();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public int readUnsignedByte() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readByte() & 0xFF;
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public short readShort() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readShort();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public int readUnsignedShort() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            int val = this.in.readShort();
            return val & 0xFFFF;
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public char readChar() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readChar();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public int readInt() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readInt();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public long readLong() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readLong();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public float readFloat() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readFloat();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public double readDouble() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readDouble();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public String readUTF() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readUTF();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public int readBytes(byte[] value) throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.read(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public int readBytes(byte[] value, int length) throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.read(value, 0, length);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    public Object readObject() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readObject();
        } catch (ClassNotFoundException x) {
            throw Util.util().handleException(x);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public void writeBoolean(boolean value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeBoolean(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public void writeByte(byte value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeByte(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeShort(short value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeShort(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeChar(char value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeChar(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeInt(int value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeInt(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public void writeLong(long value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeLong(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeFloat(float value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeFloat(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeDouble(double value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeDouble(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeUTF(String value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.writeUTF(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeBytes(byte[] value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.write(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.out.write(value, offset, length);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeObject(Object value) throws JMSException {
        if (this.reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            this.writePrimitiveData(value, this.out);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public void reset() throws JMSException {
        if (this.reading)
            return;
        try {
            byte[] buf = null;
            if (this.out != null) {
                this.out.flush();
                buf = this.bout.toByteArray();
            } else {
                buf = new byte[0];
            }
            this.bin = new ByteArrayInputStream(buf);
            this.in = new ObjectInputStream(this.bin);
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        this.reading = true;
        this.out = null;
        this.bout = null;
    }

    @Override
    public long getBodyLength() throws JMSException {
        return this.reading ? this.bin.available() : this.bout.size();
    }

    @Override
    public void clearBody() throws JMSException {
        this.bout = new ByteArrayOutputStream(RMQMessage.DEFAULT_MESSAGE_BODY_SIZE);
        try {
            this.out = new ObjectOutputStream(this.bout);
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        this.bin = null;
        this.in = null;
        this.reading = false;

    }

    @Override
    public void writeBody(ObjectOutput out) throws IOException {
        this.out.flush();
        byte[] buf = this.bout.toByteArray();
        out.writeInt(buf.length);
        out.write(buf);
    }

    @Override
    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int len = in.readInt();
        byte[] buf = new byte[len];
        in.read(buf);
        this.reading = true;
        this.bin = new ByteArrayInputStream(buf);
        this.in = new ObjectInputStream(this.bin);
    }

    public void writePrimitiveData(Object s, ObjectOutput out) throws IOException {
        if (s instanceof Boolean) {
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
        } else if (RMQBytesMessage.class.equals(this.getClass()))
            // bytes message can not contain objects
            throw new IOException(s + " is not a recognized primitive type.");
        else if (s instanceof Serializable) {
            out.writeObject(s);
        } else
            throw new IOException(s + " is not a serializable object.");
    }

}
