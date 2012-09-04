package com.rabbitmq.jms.client.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UTFDataFormatException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.Util;

/**
 * Implementation of {@link javax.jms.StreamMessage}
 */
public class RMQBytesMessage extends RMQMessage implements BytesMessage {
    /**
     * Error message when the message is not readable
     */
    private static final String NOT_READABLE = "Message not readable";
    /**
     * Error message when the message is not writeable
     */
    private static final String NOT_WRITEABLE = "Message not writeable";
    /**
     * Error message when we get an EOF exception
     */
    private static final String MSG_EOF = "Message EOF";
    /**
     * This variable is set true if we are reading, but not writing the message
     * and false if we are writing but can not read the message
     */
    private volatile boolean reading;

    /**
     * {@link ObjectInput} to read data
     */
    private transient ObjectInputStream in;
    /**
     * The object input is wrapping a 
     * {@link ByteArrayInputStream}
     */
    private transient ByteArrayInputStream bin;
    /**
     * The byte array input stream is reading 
     * from our body buffer
     */
    private volatile transient byte[] buf;
    
    /**
     * The {@link ObjectOutput} we use 
     * to write data
     */
    private transient ObjectOutputStream out;
    /**
     * The object output is wrapping a 
     * {@link ByteArrayOutputStream}
     */
    private transient ByteArrayOutputStream bout;

    public RMQBytesMessage() {
        this(false);
    }

    /**
     * Instantiates a new RMQBytesMessage
     * @param reading if this message is in a read state
     */
    public RMQBytesMessage(boolean reading) {
        init(reading);
    }
    
    protected void init(boolean reading) {
        this.reading = reading;
        if (!reading) {
            /*
             * If we are in Write state, then create the 
             * objects to support that state
             */
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
        try {
            return this.in.readBoolean();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readByte();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedByte() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readByte() & 0xFF;
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readShort();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            int val = this.in.readShort();
            return val & 0xFFFF;
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char readChar() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readChar();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readInt();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readLong();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float readFloat() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readFloat();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double readDouble() throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            return this.in.readDouble();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readUTF() throws JMSException {
        
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            this.bin.mark(Integer.MAX_VALUE);
            return this.in.readUTF();
        } catch (EOFException x) {
            throw new MessageEOFException(MSG_EOF);
        } catch (UTFDataFormatException x) {
            this.bin.reset();
            throw Util.util().handleMessageFormatException(x);
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readBytes(byte[] value) throws JMSException {
        return readBytes(value,value.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readBytes(byte[] value, int length) throws JMSException {
        if (!this.reading)
            throw new MessageNotReadableException(NOT_READABLE);
        try {
            /*
             * We can't simply do this.in.readBytes(value,0,length)
             * cause this would read block headers from 
             * the ObjectOutputStream note: the ObjectOutput adds characters to the byte array
             */
            int count = 0;
            for (int i=0; i<length; i++) {
                int v = this.in.read();
                if (v==-1) return (count==0?-1:count);
                value[count++] = (byte)(v & 0xFF);
            }
            return count;
        } catch (IOException x) {
            throw Util.util().handleException(x);
        }
    }

    /**
     * reads an object from the stream that was used to serialize this message
     * @return the object read
     * @throws JMSException if a deserialization exception happens
     */
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            throw Util.util().handleException(x);
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
            this.out.write(value, offset, length);
        } catch (IOException x) {
            throw Util.util().handleException(x);
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
            this.writePrimitiveData(value, this.out, allowSerializable);
        } catch (IOException x) {
            throw Util.util().handleException(x);
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
            try {
                this.bin = new ByteArrayInputStream(buf);
                this.in = new ObjectInputStream(this.bin);
                return;
            } catch (IOException x) {
                Util.util().handleException(x);
            }
        }
            
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
            Util.util().handleException(x);
        }
        this.reading = true;
        this.out = null;
        this.bout = null;
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
            Util.util().handleException(x);
        }
        this.bin = null;
        this.in = null;
        this.buf = null;
        this.reading = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBody(ObjectOutput out) throws IOException {
        this.out.flush();
        byte[] buf = this.bout.toByteArray();
        out.writeInt(buf.length);
        out.write(buf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int len = in.readInt();
        buf = new byte[len];
        in.read(buf);
        this.reading = true;
        this.bin = new ByteArrayInputStream(buf);
        this.in = new ObjectInputStream(this.bin);
    }

    /**
     * Utility method to write an object as a primitive or as an object
     * @param s the object to write
     * @param out the stream to write it to
     * @param allowSerializable true if we allow objects other than serializable
     * @throws IOException
     * @throws NullPointerException if s is null
     */
    public void writePrimitiveData(Object s, ObjectOutput out, boolean allowSerializable) throws IOException, MessageFormatException {
        Util.util().writePrimitiveData(s, out, allowSerializable);
    }

}
