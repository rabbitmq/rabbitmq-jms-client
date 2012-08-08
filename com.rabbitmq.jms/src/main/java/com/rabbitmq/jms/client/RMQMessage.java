package com.rabbitmq.jms.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import com.rabbitmq.jms.util.IteratorEnum;
import com.rabbitmq.jms.util.Util;

/**
 *
 */
public abstract class RMQMessage implements Message, Cloneable {
    protected static final int DEFAULT_MESSAGE_BODY_SIZE = Integer.getInteger("com.rabbitmq.jms.message.size", 512);

    private static final String PREFIX = "rmq.";
    private static final String JMS_MESSAGE_ID = PREFIX + "jms.message.id";
    private static final String JMS_MESSAGE_TIMESTAMP = PREFIX + "jms.message.timestamp";
    private static final String JMS_MESSAGE_CORR_ID = PREFIX + "jms.message.correlation.id";
    private static final String JMS_MESSAGE_REPLY_TO = PREFIX + "jms.message.reply.to";
    private static final String JMS_MESSAGE_DESTINATION = PREFIX + "jms.message.destination";
    private static final String JMS_MESSAGE_DELIVERY_MODE = PREFIX + "jms.message.delivery.mode";
    private static final String JMS_MESSAGE_REDELIVERED = PREFIX + "jms.message.redelivered";
    private static final String JMS_MESSAGE_TYPE = PREFIX + "jms.message.type";
    private static final String JMS_MESSAGE_EXPIRATION = PREFIX + "jms.message.expiration";
    private static final String JMS_MESSAGE_PRIORITY = PREFIX + "jms.message.priority";

    private static final Charset charset = Charset.forName("UTF-8");

    private Map<String, Serializable> rmqProperties = new HashMap<String, Serializable>();
    private Map<String, Serializable> jmsProperties = new HashMap<String, Serializable>();

    public RMQMessage() {
    }

    @Override
    public String getJMSMessageID() throws JMSException {
        return this.getStringProperty(JMS_MESSAGE_ID);
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {
        this.setStringProperty(JMS_MESSAGE_ID, id);

    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return this.getLongProperty(JMS_MESSAGE_TIMESTAMP);
    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        this.setLongProperty(JMS_MESSAGE_TIMESTAMP, timestamp);
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        String id = this.getStringProperty(JMS_MESSAGE_CORR_ID);
        if (id != null)
            return id.getBytes(charset);
        else
            return null;
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        String id = correlationID != null ? new String(correlationID, charset) : null;
        this.setStringProperty(JMS_MESSAGE_CORR_ID, id);
    }

    @Override
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        this.setStringProperty(JMS_MESSAGE_CORR_ID, correlationID);
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        return this.getStringProperty(JMS_MESSAGE_CORR_ID);
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return (Destination) this.getObjectProperty(JMS_MESSAGE_REPLY_TO);
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        this.setObjectProperty(JMS_MESSAGE_REPLY_TO, replyTo);
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return (Destination) this.getObjectProperty(JMS_MESSAGE_DESTINATION);
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        this.setObjectProperty(JMS_MESSAGE_DESTINATION, destination);
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return this.getIntProperty(JMS_MESSAGE_DELIVERY_MODE);
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        this.setIntProperty(JMS_MESSAGE_DELIVERY_MODE, deliveryMode);

    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return this.getBooleanProperty(JMS_MESSAGE_REDELIVERED);
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        this.setBooleanProperty(JMS_MESSAGE_REDELIVERED, redelivered);
    }

    @Override
    public String getJMSType() throws JMSException {
        return this.getStringProperty(JMS_MESSAGE_TYPE);
    }

    @Override
    public void setJMSType(String type) throws JMSException {
        this.setStringProperty(JMS_MESSAGE_TYPE, type);

    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return this.getLongProperty(JMS_MESSAGE_EXPIRATION);
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        this.setLongProperty(JMS_MESSAGE_EXPIRATION, expiration);

    }

    @Override
    public int getJMSPriority() throws JMSException {
        return this.getIntProperty(JMS_MESSAGE_PRIORITY);
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        this.setIntProperty(JMS_MESSAGE_PRIORITY, priority);

    }

    @Override
    public void clearProperties() throws JMSException {
        this.jmsProperties.clear();
    }

    @Override
    public boolean propertyExists(String name) throws JMSException {
        return this.jmsProperties.containsKey(name) || this.rmqProperties.containsKey(name);
    }

    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            return false;
        else if (o instanceof String)
            return Boolean.parseBoolean((String) o);
        else if (o instanceof Boolean) {
            Boolean b = (Boolean) o;
            return b.booleanValue();
        } else
            throw new JMSException("Unable to convert from class:" + o.getClass().getName());
    }

    @Override
    public byte getByteProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            return 0;
        else if (o instanceof String) {
            try {
                return Byte.parseByte((String) o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Byte) {
            Byte b = (Byte) o;
            return b.byteValue();
        } else
            throw new JMSException("Unable to convert from class:" + o.getClass().getName());
    }

    @Override
    public short getShortProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            return 0;
        else if (o instanceof String) {
            try {
                return Short.parseShort((String) o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Byte) {
            Byte b = (Byte) o;
            return b.byteValue();
        } else if (o instanceof Short) {
            Short b = (Short) o;
            return b.shortValue();
        } else
            throw new JMSException("Unable to convert from class:" + o.getClass().getName());
    }

    @Override
    public int getIntProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            return 0;
        else if (o instanceof String) {
            try {
                return Short.parseShort((String) o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Byte) {
            Byte b = (Byte) o;
            return b.byteValue();
        } else if (o instanceof Short) {
            Short b = (Short) o;
            return b.shortValue();
        } else if (o instanceof Integer) {
            Integer b = (Integer) o;
            return b.intValue();
        } else
            throw new JMSException("Unable to convert from class:" + o.getClass().getName());
    }

    @Override
    public long getLongProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            return 0;
        else if (o instanceof String) {
            try {
                return Short.parseShort((String) o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Byte) {
            Byte b = (Byte) o;
            return b.byteValue();
        } else if (o instanceof Short) {
            Short b = (Short) o;
            return b.shortValue();
        } else if (o instanceof Integer) {
            Integer b = (Integer) o;
            return b.intValue();
        } else if (o instanceof Long) {
            Long b = (Long) o;
            return b.longValue();
        } else
            throw new JMSException("Unable to convert from class:" + o.getClass().getName());
    }

    @Override
    public float getFloatProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            return 0;
        else if (o instanceof String) {
            try {
                return Float.parseFloat((String) o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Float) {
            Float b = (Float) o;
            return b.floatValue();
        } else
            throw new JMSException("Unable to convert from class:" + o.getClass().getName());
    }

    @Override
    public double getDoubleProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            return 0;
        else if (o instanceof String) {
            try {
                return Float.parseFloat((String) o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Float) {
            Float b = (Float) o;
            return b.floatValue();
        } else if (o instanceof Double) {
            Double b = (Double) o;
            return b.doubleValue();
        } else
            throw new JMSException("Unable to convert from class:" + o.getClass().getName());
    }

    @Override
    public String getStringProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            return null;
        else if (o instanceof String)
            return (String) o;
        else
            return o.toString();
    }

    @Override
    public Object getObjectProperty(String name) throws JMSException {
        if (name.startsWith(PREFIX))
            return this.rmqProperties.get(name);
        else
            return this.jmsProperties.get(name);
    }

    @Override
    public Enumeration<?> getPropertyNames() throws JMSException {
        return new IteratorEnum<String>(this.jmsProperties.keySet().iterator());
    }

    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        this.setObjectProperty(name, Boolean.valueOf(value));
    }

    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {
        try {
            if (name.startsWith(PREFIX)) {
                this.rmqProperties.put(name, (Serializable) value);
            } else {
                this.jmsProperties.put(name, (Serializable) value);
            }
        } catch (ClassCastException x) {
            Util.util().handleException(x, "Property value not serializable.");
        }
    }

    @Override
    public void acknowledge() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public abstract void clearBody() throws JMSException;

    public Charset getCharset() {
        return charset;
    }

    public abstract void writeBody(ObjectOutput out) throws IOException;

    public abstract void readBody(ObjectInput in) throws IOException,
                                                 ClassNotFoundException,
                                                 InstantiationException,
                                                 IllegalAccessException;

    public static byte[] toMessage(RMQMessage msg) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(DEFAULT_MESSAGE_BODY_SIZE);
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeUTF(msg.getClass().getName());
        out.writeInt(msg.rmqProperties.size());
        for (Map.Entry<String, Serializable> entry : msg.rmqProperties.entrySet()) {
            out.writeUTF(entry.getKey());
            writePrimitive(entry.getValue(), out);
        }
        out.writeInt(msg.jmsProperties.size());
        for (Map.Entry<String, Serializable> entry : msg.jmsProperties.entrySet()) {
            out.writeUTF(entry.getKey());
            writePrimitive(entry.getValue(), out);
        }
        msg.writeBody(out);
        out.flush();
        return bout.toByteArray();
    }

    public static RMQMessage fromMessage(byte[] b) throws ClassNotFoundException,
                                                  IOException,
                                                  IllegalAccessException,
                                                  InstantiationException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
        String clazz = in.readUTF();
        RMQMessage msg = (RMQMessage) Class.forName(clazz, true, Thread.currentThread().getContextClassLoader()).newInstance();
        int propsize = in.readInt();
        for (int i = 0; i < propsize; i++) {
            String name = in.readUTF();
            Object value = readPrimitive(in);
            msg.rmqProperties.put(name, (Serializable) value);
        }
        propsize = in.readInt();
        for (int i = 0; i < propsize; i++) {
            String name = in.readUTF();
            Object value = readPrimitive(in);
            msg.jmsProperties.put(name, (Serializable) value);
        }
        msg.readBody(in);
        return msg;
    }

    public static void writePrimitive(Object s, ObjectOutput out) throws IOException {
        if (s == null) {
            out.write(-1);
        } else if (s instanceof Boolean) {
            out.writeByte(1);
            out.writeBoolean(((Boolean) s).booleanValue());
        } else if (s instanceof Byte) {
            out.writeByte(2);
            out.writeByte(((Byte) s).byteValue());
        } else if (s instanceof Short) {
            out.writeByte(3);
            out.writeShort((((Short) s).shortValue()));
        } else if (s instanceof Integer) {
            out.writeByte(4);
            out.writeInt(((Integer) s).intValue());
        } else if (s instanceof Long) {
            out.writeByte(5);
            out.writeLong(((Long) s).longValue());
        } else if (s instanceof Float) {
            out.writeByte(6);
            out.writeFloat(((Float) s).floatValue());
        } else if (s instanceof Double) {
            out.writeByte(7);
            out.writeDouble(((Double) s).doubleValue());
        } else if (s instanceof String) {
            out.writeByte(8);
            out.writeUTF((String) s);
        } else if (s instanceof Character) {
            out.writeByte(9);
            out.writeChar(((Character) s).charValue());
        } else if (s instanceof byte[]) {
            out.writeByte(10);
            out.writeInt(((byte[]) s).length);
            out.write(((byte[]) s));
        } else {
            out.writeByte(Byte.MAX_VALUE);
            out.writeObject(s);
        }
    }

    public static Object readPrimitive(ObjectInput in) throws IOException, ClassNotFoundException {
        byte b = in.readByte();
        switch (b) {
        case -1:
            return null;
        case 1:
            return in.readBoolean();
        case 2:
            return in.readByte();
        case 3:
            return in.readShort();
        case 4:
            return in.readInt();
        case 5:
            return in.readLong();
        case 6:
            return in.readFloat();
        case 7:
            return in.readDouble();
        case 8:
            return in.readUTF();
        case 9:
            return in.readChar();
        case 10: {
            int length = in.readInt();
            byte[] buf = new byte[length];
            in.read(buf);
            return buf;
        }
        default:
            return in.readObject();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
