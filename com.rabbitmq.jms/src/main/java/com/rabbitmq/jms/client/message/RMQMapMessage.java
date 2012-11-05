package com.rabbitmq.jms.client.message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.DiscardingObjectOutput;
import com.rabbitmq.jms.util.IteratorEnum;
import com.rabbitmq.jms.util.RMQMessageFormatException;

/**
 * Implementation of {@link MapMessage} interface.
 */
public class RMQMapMessage extends RMQMessage implements MapMessage {

    private Map<String, Serializable> data = new HashMap<String, Serializable>();

    public boolean getBoolean(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return false;
        else if (o instanceof Boolean)
            return ((Boolean) o).booleanValue();
        else if (o instanceof String)
            return Boolean.parseBoolean((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "boolean"));
    }

    public byte getByte(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "byte"));
        else if (o instanceof Byte)
            return ((Byte) o).byteValue();
        else if (o instanceof String)
            return Byte.parseByte((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "byte"));
    }

    public short getShort(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "short"));
        else if (o instanceof Byte)
            return ((Byte) o).byteValue();
        else if (o instanceof Short)
            return ((Short) o).shortValue();
        else if (o instanceof String)
            return Short.parseShort((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "short"));
    }

    public char getChar(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "char"));
        else if (o instanceof Character)
            return ((Character) o).charValue();
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "char"));
    }

    public int getInt(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "int"));
        else if (o instanceof Byte)
            return ((Byte) o).byteValue();
        else if (o instanceof Short)
            return ((Short) o).shortValue();
        else if (o instanceof Integer)
            return ((Integer) o).intValue();
        else if (o instanceof String)
            return Integer.parseInt((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "int"));
    }

    public long getLong(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "long"));
        else if (o instanceof Byte)
            return ((Byte) o).byteValue();
        else if (o instanceof Short)
            return ((Short) o).shortValue();
        else if (o instanceof Integer)
            return ((Integer) o).intValue();
        else if (o instanceof Long)
            return ((Long) o).longValue();
        else if (o instanceof String)
            return Long.parseLong((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "long"));
    }

    public float getFloat(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "float"));
        else if (o instanceof Float) {
            return ((Float) o).floatValue();
        } else if (o instanceof String)
            return Float.parseFloat((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "float"));
    }

    public double getDouble(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            throw new NumberFormatException(String.format(UNABLE_TO_CAST, o, "double"));
        else if (o instanceof Float)
            return ((Float) o).floatValue();
        else if (o instanceof Double) {
            return ((Double) o).doubleValue();
        } else if (o instanceof String)
            return Double.parseDouble((String) o);
        else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "double"));
    }

    public String getString(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return null;
        else if (o instanceof String)
            return ((String) o);
        else if (o instanceof byte[])
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "String"));
        else
            return o.toString();
    }

    public byte[] getBytes(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null) {
            return null;
        } else if (o instanceof byte[]) {
            byte[] b1 = ((byte[]) o);
            byte[] b2 = new byte[b1.length];
            System.arraycopy(b1, 0, b2, 0, b1.length);
            return b2;
        } else
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, o, "byte[]"));
    }

    public Object getObject(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null) {
            return null;
        } else if (o instanceof byte[]) {
            return getBytes(name);
        } else {
            return o;
        }
    }

    public Enumeration<String> getMapNames() throws JMSException {
        return new IteratorEnum<String>(this.data.keySet().iterator());
    }

    public void setBoolean(String name, boolean value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setByte(String name, byte value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setShort(String name, short value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setChar(String name, char value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setInt(String name, int value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setLong(String name, long value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setFloat(String name, float value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setDouble(String name, double value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setString(String name, String value) throws JMSException {
        checkNotReadonlyBody();
        this.data.put(name, value);
    }

    public void setBytes(String name, byte[] value) throws JMSException {
        checkNotReadonlyBody();
        setBytes(name, value, 0, value.length);
    }

    public void setBytes(String name, byte[] value, int offset, int length) throws JMSException {
        checkNotReadonlyBody();
        if (value == null) {
            this.data.remove(name);
            return;
        }
        byte[] buf = new byte[length];
        System.arraycopy(value, offset, buf, 0, length);
        this.data.put(name, buf);
    }

    public void setObject(String name, Object value) throws JMSException {
        checkNotReadonlyBody();
        if (name==null && value==null) {
        } else if (value==null) {
            this.data.remove(name);
        } else if (!(value instanceof Serializable)) {
            throw new MessageFormatException(String.format(UNABLE_TO_CAST, value, Serializable.class.getName()));
        } else {
            try {
                /* Make sure we can write this type of object */
                writePrimitiveData(value, new DiscardingObjectOutput(), false);
            } catch (IOException x) {
                throw new RMQMessageFormatException(x);
            }
        }

        if (value instanceof byte[]) {
            setBytes(name, (byte[])value);
        } else {
            this.data.put(name, (Serializable) value);
        }
    }

    public boolean itemExists(String name) throws JMSException {
        return this.data.containsKey(name);
    }

    public void clearBodyInternal() throws JMSException {
        this.data.clear();
    }

    public void writeBody(ObjectOutput out) throws IOException {
        int size = this.data.size();
        out.writeInt(size);
        for (Map.Entry<String, Serializable> entry : this.data.entrySet()) {
            out.writeUTF(entry.getKey());
            try {
                RMQMessage.writePrimitive(entry.getValue(), out);
            } catch (MessageFormatException x) {
                throw new IOException(x);
            }
        }
    }

    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String name = in.readUTF();
            Object value = RMQMessage.readPrimitive(in);
            this.data.put(name, (Serializable) value);
        }
    }

    /**
     * Utility method to write an object as a primitive or as an object
     * @param s the object to write
     * @param out the stream to write it to
     * @param allowSerializable true if we allow objects other than serializable
     * @throws IOException
     * @throws NullPointerException if s is null
     */
    private void writePrimitiveData(Object s, ObjectOutput out, boolean allowSerializable) throws IOException, MessageFormatException {
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
            throw new MessageFormatException(s + " is not a recognized primitive type.");
    }

    private void checkNotReadonlyBody() throws JMSException {
        if (isReadonlyBody())
            throw new MessageNotWriteableException("Message not writeable");
    }
}
