/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import java.io.IOException;
import java.io.ObjectOutput;

/**
 * Simple implementation of an {@link ObjectOutput} interface that accepts and discards all output.
 */
public class DiscardingObjectOutput implements ObjectOutput {

    public void writeBoolean(boolean paramBoolean) throws IOException {
    }

    public void writeByte(int paramInt) throws IOException {
    }

    public void writeShort(int paramInt) throws IOException {
    }

    public void writeChar(int paramInt) throws IOException {
    }

    public void writeInt(int paramInt) throws IOException {
    }

    public void writeLong(long paramLong) throws IOException {
    }

    public void writeFloat(float paramFloat) throws IOException {
    }

    public void writeDouble(double paramDouble) throws IOException {
    }

    public void writeBytes(String paramString) throws IOException {
    }

    public void writeChars(String paramString) throws IOException {
    }

    public void writeUTF(String paramString) throws IOException {
    }

    public void writeObject(Object paramObject) throws IOException {
    }

    public void write(int paramInt) throws IOException {
    }

    public void write(byte[] paramArrayOfByte) throws IOException {
    }

    public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2) throws IOException {
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
    }

}
