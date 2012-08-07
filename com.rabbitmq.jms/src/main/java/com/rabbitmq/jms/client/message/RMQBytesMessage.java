package com.rabbitmq.jms.client.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

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
            bout = new ByteArrayOutputStream(RMQMessage.DEFAULT_MESSAGE_BODY_SIZE);
            try {
                out = new ObjectOutputStream(bout);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }

    }

    @Override
    public boolean readBoolean() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readBoolean();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public byte readByte() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readByte();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public int readUnsignedByte() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readByte() & 0xFF;
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public short readShort() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readShort();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public int readUnsignedShort() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            int val = in.readShort();
            return val & 0xFFFF;
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public char readChar() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readChar();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public int readInt() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readInt();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public long readLong() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readLong();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public float readFloat() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readFloat();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public double readDouble() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readDouble();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public String readUTF() throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.readUTF();
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public int readBytes(byte[] value) throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.read(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public int readBytes(byte[] value, int length) throws JMSException {
        if (!reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return in.read(value, 0, length);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeBoolean(boolean value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeBoolean(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public void writeByte(byte value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeByte(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeShort(short value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeShort(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeChar(char value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeChar(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeInt(int value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeInt(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    @Override
    public void writeLong(long value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeLong(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeFloat(float value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeFloat(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeDouble(double value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeDouble(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeUTF(String value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.writeUTF(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeBytes(byte[] value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.write(value);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            out.write(value, offset, length);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void writeObject(Object value) throws JMSException {
        if (reading)
            throw new MessageNotWriteableException(NOT_WRITEABLE);
        try {
            RMQBytesMessage.writePrimitive(value, out);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    @Override
    public void reset() throws JMSException {
        if (reading)
            return;
        try {
            byte[] buf = null;
            if (out != null) {
                out.flush();
                buf = bout.toByteArray();
            } else {
                buf = new byte[0];
            }
            bin = new ByteArrayInputStream(buf);
            in = new ObjectInputStream(bin);
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        reading = true;
        out = null;
        bout = null;
    }

    @Override
    public long getBodyLength() throws JMSException {
        return reading ? bin.available() : bout.size();
    }

    @Override
    public void clearBody() throws JMSException {
        bout = new ByteArrayOutputStream(RMQMessage.DEFAULT_MESSAGE_BODY_SIZE);
        try {
            out = new ObjectOutputStream(bout);
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        bin = null;
        in = null;
        reading = false;

    }

    @Override
    public void writeBody(ObjectOutput out) throws IOException {
        this.out.flush();
        byte[] buf = bout.toByteArray();
        out.writeInt(buf.length);
        out.write(buf);
    }

    @Override
    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int len = in.readInt();
        byte[] buf = new byte[len];
        in.read(buf);
        reading = true;
        bin = new ByteArrayInputStream(buf);
        this.in = new ObjectInputStream(bin);
    }

    public static void writePrimitive(Object s, ObjectOutput out) throws IOException {
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
        } else {
            throw new IOException(s + " is not a recognized primitive type.");
        }
    }

}
