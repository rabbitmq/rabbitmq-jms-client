//
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License
// at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
// the License for the specific language governing rights and
// limitations under the License.
//
// The Original Code is RabbitMQ.
//
// The Initial Developer of the Original Code is VMware, Inc.
// Copyright (c) 2012 VMware, Inc. All rights reserved.
//
package com.rabbitmq.jms.client;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

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
