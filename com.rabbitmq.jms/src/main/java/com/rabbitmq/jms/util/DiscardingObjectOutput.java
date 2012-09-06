package com.rabbitmq.jms.util;

import java.io.IOException;
import java.io.ObjectOutput;
/**
 * Simple implementation of an {@link ObjectOutput} interface that does nothing
 * all bytes are lost
 */
public class DiscardingObjectOutput implements ObjectOutput {

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBoolean(boolean paramBoolean) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeByte(int paramInt) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeShort(int paramInt) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeChar(int paramInt) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeInt(int paramInt) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLong(long paramLong) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFloat(float paramFloat) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeDouble(double paramDouble) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBytes(String paramString) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeChars(String paramString) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeUTF(String paramString) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeObject(Object paramObject) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int paramInt) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] paramArrayOfByte) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
    }

}
