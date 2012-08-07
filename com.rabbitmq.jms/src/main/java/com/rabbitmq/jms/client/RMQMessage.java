package com.rabbitmq.jms.client;

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
public class RMQMessage implements Message {

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
    
    private volatile byte[] body;

    private Map<String,Serializable> rmqProperties = new HashMap<String,Serializable>();
    private Map<String,Serializable> jmsProperties = new HashMap<String,Serializable>();
    
    public RMQMessage() {
    }
    
    
    @Override
    public String getJMSMessageID() throws JMSException {
        return getStringProperty(JMS_MESSAGE_ID);
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {
        setStringProperty(JMS_MESSAGE_ID, id);

    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return getLongProperty(JMS_MESSAGE_TIMESTAMP);
    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        setLongProperty(JMS_MESSAGE_TIMESTAMP, timestamp);
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        String id = getStringProperty(JMS_MESSAGE_CORR_ID);
        if (id!=null) return id.getBytes(charset);
        else return null;
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        String id = correlationID!=null ? new String(correlationID,charset) : null;
        setStringProperty(JMS_MESSAGE_CORR_ID, id);
    }

    @Override
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        setStringProperty(JMS_MESSAGE_CORR_ID, correlationID);
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        return getStringProperty(JMS_MESSAGE_CORR_ID);
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return (Destination)getObjectProperty(JMS_MESSAGE_REPLY_TO);
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        setObjectProperty(JMS_MESSAGE_REPLY_TO,replyTo);
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return (Destination)getObjectProperty(JMS_MESSAGE_DESTINATION);
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        setObjectProperty(JMS_MESSAGE_DESTINATION, destination);
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return getIntProperty(JMS_MESSAGE_DELIVERY_MODE);
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        setIntProperty(JMS_MESSAGE_DELIVERY_MODE, deliveryMode);

    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return getBooleanProperty(JMS_MESSAGE_REDELIVERED);
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        setBooleanProperty(JMS_MESSAGE_REDELIVERED, redelivered);
    }

    @Override
    public String getJMSType() throws JMSException {
        return getStringProperty(JMS_MESSAGE_TYPE);
    }

    @Override
    public void setJMSType(String type) throws JMSException {
        setStringProperty(JMS_MESSAGE_TYPE, type);

    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return getLongProperty(JMS_MESSAGE_EXPIRATION);
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        setLongProperty(JMS_MESSAGE_EXPIRATION, expiration);

    }

    @Override
    public int getJMSPriority() throws JMSException {
        return getIntProperty(JMS_MESSAGE_PRIORITY);
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        setIntProperty(JMS_MESSAGE_PRIORITY, priority);

    }

    @Override
    public void clearProperties() throws JMSException {
        jmsProperties.clear();
    }

    @Override
    public boolean propertyExists(String name) throws JMSException {
        return jmsProperties.containsKey(name) || rmqProperties.containsKey(name);
    }

    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        Object o = getObjectProperty(name);
        if (o == null) {
            return false;
        } else if (o instanceof String) {
            return Boolean.parseBoolean((String)o);
        } else if (o instanceof Boolean) {
            Boolean b = (Boolean)o;
            return b.booleanValue();
        } else {
            throw new JMSException("Unable to convert from class:"+o.getClass().getName());
        }
    }

    @Override
    public byte getByteProperty(String name) throws JMSException {
        Object o = getObjectProperty(name);
        if (o == null) {
            return 0;
        } else if (o instanceof String) {
            try {
                return Byte.parseByte((String)o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Byte) {
            Byte b = (Byte)o;
            return b.byteValue();
        } else {
            throw new JMSException("Unable to convert from class:"+o.getClass().getName());
        }
    }

    @Override
    public short getShortProperty(String name) throws JMSException {
        Object o = getObjectProperty(name);
        if (o == null) {
            return 0;
        } else if (o instanceof String) {
            try {
                return Short.parseShort((String)o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Byte) {
            Byte b = (Byte)o;
            return b.byteValue();
        } else if (o instanceof Short) {
            Short b = (Short)o;
            return b.shortValue();
        } else {
            throw new JMSException("Unable to convert from class:"+o.getClass().getName());
        }
    }

    @Override
    public int getIntProperty(String name) throws JMSException {
        Object o = getObjectProperty(name);
        if (o == null) {
            return 0;
        } else if (o instanceof String) {
            try {
                return Short.parseShort((String)o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Byte) {
            Byte b = (Byte)o;
            return b.byteValue();
        } else if (o instanceof Short) {
            Short b = (Short)o;
            return b.shortValue();
        } else if (o instanceof Integer) {
            Integer b = (Integer)o;
            return b.intValue();
        } else {
            throw new JMSException("Unable to convert from class:"+o.getClass().getName());
        }
    }

    @Override
    public long getLongProperty(String name) throws JMSException {
        Object o = getObjectProperty(name);
        if (o == null) {
            return 0;
        } else if (o instanceof String) {
            try {
                return Short.parseShort((String)o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Byte) {
            Byte b = (Byte)o;
            return b.byteValue();
        } else if (o instanceof Short) {
            Short b = (Short)o;
            return b.shortValue();
        } else if (o instanceof Integer) {
            Integer b = (Integer)o;
            return b.intValue();
        } else if (o instanceof Long) {
            Long b = (Long)o;
            return b.longValue();
        } else {
            throw new JMSException("Unable to convert from class:"+o.getClass().getName());
        }
    }

    @Override
    public float getFloatProperty(String name) throws JMSException {
        Object o = getObjectProperty(name);
        if (o == null) {
            return 0;
        } else if (o instanceof String) {
            try {
                return Float.parseFloat((String)o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Float) {
            Float b = (Float)o;
            return b.floatValue();
        } else {
            throw new JMSException("Unable to convert from class:"+o.getClass().getName());
        }
    }

    @Override
    public double getDoubleProperty(String name) throws JMSException {
        Object o = getObjectProperty(name);
        if (o == null) {
            return 0;
        } else if (o instanceof String) {
            try {
                return Float.parseFloat((String)o);
            } catch (NumberFormatException x) {
                throw Util.util().handleException(x);
            }
        } else if (o instanceof Float) {
            Float b = (Float)o;
            return b.floatValue();
        } else if (o instanceof Double) {
            Double b = (Double)o;
            return b.doubleValue();
        } else {
            throw new JMSException("Unable to convert from class:"+o.getClass().getName());
        }
    }

    @Override
    public String getStringProperty(String name) throws JMSException {
        Object o = getObjectProperty(name);
        if (o == null) {
            return null;
        } else if (o instanceof String) {
            return (String)o;
        } else {
            return o.toString();
        }
    }

    @Override
    public Object getObjectProperty(String name) throws JMSException {
        if (name.startsWith(PREFIX)) {
            return rmqProperties.get(name);
        } else {
            return jmsProperties.get(name);
        }
    }

    @Override
    public Enumeration<?> getPropertyNames() throws JMSException {
        return new IteratorEnum<String>(jmsProperties.keySet().iterator());
    }

    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        setObjectProperty(name,Boolean.valueOf(value));
    }

    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        setObjectProperty(name,value);
    }

    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        setObjectProperty(name,value);
    }

    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        setObjectProperty(name,value);
    }

    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        setObjectProperty(name,value);
    }

    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        setObjectProperty(name,value);
    }

    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        setObjectProperty(name,value);
    }

    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        setObjectProperty(name,value);
    }

    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {
        try {
            if (name.startsWith(PREFIX)) {
                rmqProperties.put(name,(Serializable)value);
            } else {
                jmsProperties.put(name,(Serializable)value);
            }
        }catch (ClassCastException x) {
            Util.util().handleException(x, "Property value not serializable.");
        }
    }

    @Override
    public void acknowledge() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearBody() throws JMSException {
        body = null;

    }
    
    public byte[] getBody() throws JMSException {
        return body;
    }

    public void setBody(byte[] b) {
        body = b;
    }

    public Charset getCharset() {
        return charset;
    }
}
