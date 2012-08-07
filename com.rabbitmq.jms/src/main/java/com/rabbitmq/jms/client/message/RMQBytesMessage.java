package com.rabbitmq.jms.client.message;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import com.rabbitmq.jms.client.RMQMessage;

/**
 *
 */
public class RMQBytesMessage extends RMQMessage implements BytesMessage {

    @Override
    public long getBodyLength() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean readBoolean() throws JMSException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public byte readByte() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int readUnsignedByte() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public short readShort() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int readUnsignedShort() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public char readChar() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int readInt() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long readLong() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float readFloat() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double readDouble() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String readUTF() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int readBytes(byte[] value) throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int readBytes(byte[] value, int length) throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeBoolean(boolean value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeByte(byte value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeShort(short value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeChar(char value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeInt(int value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeLong(long value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeFloat(float value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeDouble(double value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeUTF(String value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeBytes(byte[] value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeBytes(byte[] value, int offset, int length) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeObject(Object value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset() throws JMSException {
        // TODO Auto-generated method stub

    }

}
