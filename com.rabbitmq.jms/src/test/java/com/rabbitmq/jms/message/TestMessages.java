package com.rabbitmq.jms.message;

import java.util.Arrays;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.junit.Test;

import junit.framework.Assert;

import com.rabbitmq.jms.client.message.RMQBytesMessage;

public class TestMessages {

    @Test
    public void testBytesMessage() throws Exception {
        byte[] buf = { (byte) -2, (byte) -3 };
        RMQBytesMessage message = new RMQBytesMessage();
        writeBytesMessage(message,buf);
        message.reset();
        readBytesMessage(buf, message);
    }

    public static void readBytesMessage(byte[] buf, BytesMessage message) throws JMSException {
        Assert.assertTrue(message.readBoolean());
        Assert.assertEquals(-1, message.readByte());
        Assert.assertEquals(255, message.readUnsignedByte());
        byte[] b1 = new byte[1];
        byte[] b2 = new byte[2];
        message.readBytes(b2);
        Assert.assertTrue(Arrays.equals(buf, b2));
        message.readBytes(b1, 1);
        Assert.assertEquals(buf[1], b1[0]);
        Assert.assertEquals('X', message.readChar());
        Assert.assertEquals(2.35d, message.readDouble());
        Assert.assertEquals(1.54f, message.readFloat());
        Assert.assertEquals(Integer.MAX_VALUE, message.readInt());
        Assert.assertEquals(Long.MAX_VALUE, message.readLong());
        Assert.assertEquals(Short.MAX_VALUE, message.readShort());
        Assert.assertEquals((int) 0xFFFF, message.readUnsignedShort());
        Assert.assertEquals("TEST", message.readUTF());
    }
    
    public static void writeBytesMessage(BytesMessage message, byte[] buf) throws JMSException {
        message.writeBoolean(true);
        message.writeByte((byte) -1); // signed
        message.writeByte((byte) (255 & 0xFF)); // unsigned
        message.writeBytes(buf);
        message.writeBytes(buf, 1, 1);
        message.writeChar('X');
        message.writeDouble(2.35d);
        message.writeFloat(1.54f);
        message.writeInt(Integer.MAX_VALUE);
        message.writeLong(Long.MAX_VALUE);
        message.writeShort(Short.MAX_VALUE);
        message.writeShort((short) 0xFFFF);
        message.writeUTF("TEST");
    }
}
