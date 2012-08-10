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

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.IteratorEnum;

public class RMQMapMessage extends RMQMessage implements MapMessage {

    private static final String UNABLE_TO_CAST = "Unable to cast the object, %s, into the specified type %s";
    private Map<String, Serializable> data = new HashMap<String, Serializable>();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBoolean(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return false;
        else if (o instanceof Boolean)
            return ((Boolean) o).booleanValue();
        else if (o instanceof String)
            return Boolean.parseBoolean((String) o);
        else
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "boolean"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return 0;
        else if (o instanceof Byte)
            return ((Byte) o).byteValue();
        else if (o instanceof String)
            return Byte.parseByte((String) o);
        else
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "byte"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return 0;
        else if (o instanceof Byte)
            return ((Byte) o).byteValue();
        else if (o instanceof Short)
            return ((Short) o).shortValue();
        else if (o instanceof String)
            return Short.parseShort((String) o);
        else
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "short"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char getChar(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return 0;
        else if (o instanceof Character)
            return ((Character) o).charValue();
        else
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "char"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return 0;
        else if (o instanceof Byte)
            return ((Byte) o).byteValue();
        else if (o instanceof Short)
            return ((Short) o).shortValue();
        else if (o instanceof Integer)
            return ((Integer) o).intValue();
        else if (o instanceof String)
            return Integer.parseInt((String) o);
        else
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "int"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLong(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return 0;
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
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "long"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloat(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return 0f;
        else if (o instanceof Float)
            return ((Float) o).floatValue();
        else if (o instanceof String)
            return Float.parseFloat((String) o);
        else
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "float"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDouble(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return 0f;
        else if (o instanceof Float)
            return ((Float) o).floatValue();
        else if (o instanceof Double)
            return ((Double) o).doubleValue();
        else if (o instanceof String)
            return Double.parseDouble((String) o);
        else
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "double"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getString(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return null;
        else if (o instanceof String)
            return ((String) o);
        else
            return o.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBytes(String name) throws JMSException {
        Object o = this.data.get(name);
        if (o == null)
            return null;
        else if (o instanceof byte[])
            return ((byte[]) o);
        else
            throw new JMSException(String.format(UNABLE_TO_CAST, o, "byte[]"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject(String name) throws JMSException {
        return this.data.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getMapNames() throws JMSException {
        return new IteratorEnum<String>(this.data.keySet().iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoolean(String name, boolean value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setByte(String name, byte value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShort(String name, short value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChar(String name, char value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInt(String name, int value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLong(String name, long value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFloat(String name, float value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDouble(String name, double value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setString(String name, String value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBytes(String name, byte[] value) throws JMSException {
        this.data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBytes(String name, byte[] value, int offset, int length) throws JMSException {
        if (value == null) {
            this.data.remove(name);
            return;
        }
        if (offset == 0 && length == value.length) {
            this.data.put(name, value);
        } else {
            byte[] buf = new byte[length];
            System.arraycopy(value, offset, buf, 0, length);
            this.data.put(name, buf);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObject(String name, Object value) throws JMSException {
        if (!(value instanceof Serializable))
            throw new JMSException(String.format(UNABLE_TO_CAST, value, Serializable.class.getName()));
        this.data.put(name, (Serializable) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean itemExists(String name) throws JMSException {
        return this.data.containsKey(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearBody() throws JMSException {
        this.data.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBody(ObjectOutput out) throws IOException {
        int size = this.data.size();
        out.writeInt(size);
        for (Map.Entry<String, Serializable> entry : this.data.entrySet()) {
            out.writeUTF(entry.getKey());
            RMQMessage.writePrimitive(entry.getValue(), out);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String name = in.readUTF();
            Object value = RMQMessage.readPrimitive(in);
            this.data.put(name, (Serializable) value);
        }
    }

}
