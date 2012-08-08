package com.rabbitmq.jms.message;

import java.util.Arrays;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.junit.Test;

import static junit.framework.Assert.*;

import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQMapMessage;

public class TestMessages {

    @Test
    public void testBytesMessage() throws Exception {
        RMQBytesMessage message = new RMQBytesMessage();
        writeBytesMessage(message);
        message.reset();
        readBytesMessage(message);
    }
    
    @Test
    public void testMapMessage() throws Exception {
        RMQMapMessage message = new RMQMapMessage();
        writeMapMessage(message);
        readMapMessage(message);
    }
    
    public static void writeMapMessage(MapMessage message) throws JMSException {
        byte[] buf = { (byte) -2, (byte) -3 };
        message.setBoolean("boolean", true);
        message.setObject("string.boolean", "true");

        message.setByte("byte", Byte.MAX_VALUE);
        message.setObject("string.byte", String.valueOf(Byte.MAX_VALUE));
        
        message.setShort("short", Short.MAX_VALUE);
        message.setObject("string.short", String.valueOf(Short.MAX_VALUE));

        message.setInt("int", Integer.MAX_VALUE);
        message.setObject("string.int", String.valueOf(Integer.MAX_VALUE));

        message.setLong("long", Long.MAX_VALUE);
        message.setObject("string.long", String.valueOf(Long.MAX_VALUE));

        message.setFloat("float", Float.MAX_VALUE);
        message.setObject("string.float", String.valueOf(Float.MAX_VALUE));
        
        message.setDouble("double", Double.MAX_VALUE);
        message.setObject("string.double", String.valueOf(Double.MAX_VALUE));
        
        message.setString("string", "string");
        message.setObject("string.string", "string");
        
        message.setBytes("bytes", buf);
        message.setObject("string.bytes", buf);
    
    }
    
    public static void readMapMessage(MapMessage message) throws JMSException {
        byte[] buf = { (byte) -2, (byte) -3 };
        
        assertTrue(message.getBoolean("boolean"));
        assertTrue(message.getBoolean("string.boolean"));
        
        assertEquals(Byte.MAX_VALUE, message.getByte("byte"));
        assertEquals(Byte.MAX_VALUE, message.getShort("byte"));
        assertEquals(Byte.MAX_VALUE, message.getInt("byte"));
        assertEquals(Byte.MAX_VALUE, message.getLong("byte"));
        assertEquals(Byte.MAX_VALUE, message.getByte("string.byte"));
        
        assertEquals(Short.MAX_VALUE, message.getShort("short"));
        assertEquals(Short.MAX_VALUE, message.getInt("short"));
        assertEquals(Short.MAX_VALUE, message.getLong("short"));
        assertEquals(Short.MAX_VALUE, message.getShort("string.short"));
        
        assertEquals(Integer.MAX_VALUE, message.getInt("int"));
        assertEquals(Integer.MAX_VALUE, message.getLong("int"));
        assertEquals(Integer.MAX_VALUE, message.getInt("string.int"));

        assertEquals(Long.MAX_VALUE, message.getLong("long"));
        assertEquals(Long.MAX_VALUE, message.getLong("string.long"));
        
        assertEquals(Float.MAX_VALUE, message.getFloat("float"));
        
        //we must cast to get the same precision error - http://en.wikipedia.org/wiki/Floating_point
        assertEquals((double)Float.MAX_VALUE, message.getDouble("float"));
        assertEquals(Float.MAX_VALUE, message.getFloat("string.float"));

        assertEquals(Double.MAX_VALUE, message.getDouble("double"));
        assertEquals(Double.MAX_VALUE, message.getDouble("string.double"));

        assertTrue(Arrays.equals(buf, message.getBytes("bytes")));
        assertTrue(Arrays.equals(buf, message.getBytes("string.bytes")));
        
    }
    
    
    public static void readBytesMessage(BytesMessage message) throws JMSException {
        byte[] buf = { (byte) -2, (byte) -3 };
        assertTrue(message.readBoolean());
        assertEquals(-1, message.readByte());
        assertEquals(255, message.readUnsignedByte());
        byte[] b1 = new byte[1];
        byte[] b2 = new byte[2];
        message.readBytes(b2);
        assertTrue(Arrays.equals(buf, b2));
        message.readBytes(b1, 1);
        assertEquals(buf[1], b1[0]);
        assertEquals('X', message.readChar());
        assertEquals(2.35d, message.readDouble());
        assertEquals(1.54f, message.readFloat());
        assertEquals(Integer.MAX_VALUE, message.readInt());
        assertEquals(Long.MAX_VALUE, message.readLong());
        assertEquals(Short.MAX_VALUE, message.readShort());
        assertEquals((int) 0xFFFF, message.readUnsignedShort());
        assertEquals("TEST", message.readUTF());
    }
    
    public static void writeBytesMessage(BytesMessage message) throws JMSException {
        byte[] buf = { (byte) -2, (byte) -3 };
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
