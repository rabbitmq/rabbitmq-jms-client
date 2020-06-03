/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.client;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQMapMessage;
import com.rabbitmq.jms.client.message.RMQObjectMessage;
import com.rabbitmq.jms.client.message.RMQStreamMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import com.rabbitmq.jms.util.HexDisplay;
import com.rabbitmq.jms.util.IteratorEnum;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.Util;
import com.rabbitmq.jms.util.WhiteListObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class for RMQ*Message classes. This is abstract and cannot be instantiated independently.
 */
public abstract class RMQMessage implements Message, Cloneable {
    /** Logger shared with derived classes */
    protected final Logger logger = LoggerFactory.getLogger(RMQMessage.class);

    private static final String DIRECT_REPLY_TO = "amq.rabbitmq.reply-to";

    protected void loggerDebugByteArray(String format, byte[] buffer, Object arg) {
        if (logger.isDebugEnabled()) {
            StringBuilder bufferOutput = new StringBuilder("Byte array, length ").append(buffer.length).append(" :\n");
            HexDisplay.decodeByteArrayIntoStringBuilder(buffer, bufferOutput);
            logger.debug(format, bufferOutput.append("end of byte array, length ").append(buffer.length).append('.'), arg);
        }
    }

    /** Error message when the message is not readable */
    protected static final String NOT_READABLE = "Message not readable";
    /** Error message when the message is not writeable */
    protected static final String NOT_WRITEABLE = "Message not writeable";
    /** Error message used when throwing {@link javax.jms.MessageFormatException} */
    protected static final String UNABLE_TO_CAST = "Unable to cast the object, %s, into the specified type %s";
    /** Error message when we get an EOF exception */
    protected static final String MSG_EOF = "Message EOF";

    /** Properties in a JMS message can NOT be named any of these reserved words */
    private static final String[] RESERVED_NAMES = {"NULL", "TRUE", "FALSE", "NOT", "AND", "OR", "BETWEEN", "LIKE", "IN",
                                                    "IS", "ESCAPE"};

    /** A property in a JMS message must NOT have a name starting with any of the following characters (added period 2013-06-13) */
    private static final char[] INVALID_STARTS_WITH = {'0','1','2','3','4','5','6','7','8','9','-','+','\'','"','.'};

    /** A property in a JMS message must NOT contain these characters in its name (removed period 2013-06-13) */
    private static final char[] MAY_NOT_CONTAIN = {'\'','"'};

    /**
     * When we create a message that has a byte[] as the underlying
     * structure, BytesMessage and StreamMessage, this is the default size.
     * Can be changed with a system property.
     */
    protected static final int DEFAULT_MESSAGE_BODY_SIZE = Integer.getInteger("com.rabbitmq.jms.client.message.size", 512);

    /**
     * We store all the JMS hard coded values, such as {@link #setJMSMessageID(String)}, as properties instead of hard
     * coded fields. This way we can create a structure later on that the rabbit MQ broker can read by just changing the
     * {@link #toByteArray()}} and {@link #fromMessage(byte[], List)}.
     */
    private static final String PREFIX = "rmq.";
    private static final String JMS_MESSAGE_ID = PREFIX + "jms.message.id";
    private static final String JMS_MESSAGE_TIMESTAMP = PREFIX + "jms.message.timestamp";
    private static final String JMS_MESSAGE_CORR_ID = PREFIX + "jms.message.correlation.id";
    private static final String JMS_MESSAGE_REPLY_TO = PREFIX + "jms.message.reply.to";
    private static final String JMS_MESSAGE_DESTINATION = PREFIX + "jms.message.destination";
    private static final String JMS_MESSAGE_REDELIVERED = PREFIX + "jms.message.redelivered";
    private static final String JMS_MESSAGE_TYPE = PREFIX + "jms.message.type";

    /**
     * Those needs to be checked in the producer
     */
    static final String JMS_MESSAGE_DELIVERY_MODE = PREFIX + "jms.message.delivery.mode";
    static final String JMS_MESSAGE_EXPIRATION = PREFIX + "jms.message.expiration";
    static final String JMS_MESSAGE_PRIORITY = PREFIX + "jms.message.priority";

    /**
     * For turning {@link String}s into <code>byte[]</code> and back we use this {@link Charset} instance.
     * This is used for {@link RMQMessage#getJMSCorrelationIDAsBytes()}.
     */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /** Here we store the JMS_ properties that would have been fields */
    private final Map<String, Serializable> rmqProperties = new HashMap<String, Serializable>();
    /** Here we store the user’s custom JMS properties */
    private final Map<String, Serializable> userJmsProperties = new HashMap<String, Serializable>();
    /**
     * We generate a unique message ID each time we send a message
     * It is stored here. This is also used for
     * {@link #hashCode()} and {@link #equals(Object)}
     */
    private volatile String internalMessageID=null;
    /**
     * A message is read only if it has been received.
     * We set this flag when we receive a message in the following method
     * {@link RMQMessage#convertMessage(RMQSession, RMQDestination, com.rabbitmq.client.GetResponse, ReceivingContextConsumer)}
     */
    private volatile boolean readonlyProperties=false;
    private volatile boolean readonlyBody=false;

    /**
     * Returns true if this message body is read only
     * This means that message has been received and can not
     * be modified
     * @return true if the message is read only
     */
    protected boolean isReadonlyBody() {
        return this.readonlyBody;
    }

    protected boolean isReadOnlyProperties() {
        return this.readonlyProperties;
    }

    /**
     * Sets the read only flag on this message
     * @see RMQMessage#convertMessage(RMQSession, RMQDestination, com.rabbitmq.client.GetResponse, ReceivingContextConsumer)
     * @param readonly read only flag value
     */
    protected void setReadonly(boolean readonly) {
        this.readonlyBody = readonly;
        this.readonlyProperties = readonly;
    }

    protected void setReadOnlyBody(boolean readonly) {
        this.readonlyBody = readonly;
    }

    protected void setReadOnlyProperties(boolean readonly) {
        this.readonlyProperties = readonly;
    }

    /**
     * When a message is received, it has a delivery tag
     * We use this delivery tag when we ack
     * a single message
     * @see RMQSession#acknowledgeMessage(RMQMessage)
     * @see RMQMessage#convertMessage(RMQSession, RMQDestination, com.rabbitmq.client.GetResponse, ReceivingContextConsumer)
     */
    private long rabbitDeliveryTag = -1;
    /**
     * returns the delivery tag for this message
     * @return the delivery tag for this message
     */
    public long getRabbitDeliveryTag() {
        return this.rabbitDeliveryTag;
    }
    /**
     * Sets the RabbitMQ delivery tag for this message.
      * @see RMQMessage#convertMessage(RMQSession, RMQDestination, com.rabbitmq.client.GetResponse, ReceivingContextConsumer)

     * @param rabbitDeliveryTag RabbitMQ delivery tag
     */
    protected void setRabbitDeliveryTag(long rabbitDeliveryTag) {
        this.rabbitDeliveryTag = rabbitDeliveryTag;
    }

    /**
     * The Message must hold a reference to the session itself
     * So that it can ack itself. Ack belongs to the session
     * @see RMQSession#acknowledgeMessage(RMQMessage)
     */
    private volatile transient RMQSession session = null;
    /**
     * Returns the session this object belongs to
     * @return the session this object belongs to
     */
    public RMQSession getSession() {
        return this.session;
    }
    /**
     * Sets the session this object was received by
     * @see RMQMessage#convertMessage(RMQSession, RMQDestination, com.rabbitmq.client.GetResponse, ReceivingContextConsumer)
     * @see RMQSession#acknowledgeMessage(RMQMessage)
     * @param session the session this object was received by
     */
    protected void setSession(RMQSession session) {
        this.session = session;
    }

    /**
     * Constructor for auto de-serialization
     */
    public RMQMessage() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJMSMessageID() throws JMSException {
        return this.getStringProperty(JMS_MESSAGE_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSMessageID(String id) throws JMSException {
        this.setStringProperty(JMS_MESSAGE_ID, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getJMSTimestamp() throws JMSException {
        Object timestamp = this.getObjectProperty(JMS_MESSAGE_TIMESTAMP);
        if (timestamp == null) {
            return 0L;
        } else {
            return convertToLong(timestamp);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        this.setLongProperty(JMS_MESSAGE_TIMESTAMP, timestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        String id = this.getStringProperty(JMS_MESSAGE_CORR_ID);
        if (id != null)
            return id.getBytes(getCharset());
        else
            return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        String id = correlationID != null ? new String(correlationID, getCharset()) : null;
        this.setStringProperty(JMS_MESSAGE_CORR_ID, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        this.setStringProperty(JMS_MESSAGE_CORR_ID, correlationID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJMSCorrelationID() throws JMSException {
        return this.getStringProperty(JMS_MESSAGE_CORR_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return (Destination) this.getObjectProperty(JMS_MESSAGE_REPLY_TO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        this.setObjectProperty(JMS_MESSAGE_REPLY_TO, replyTo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Destination getJMSDestination() throws JMSException {
        return (Destination) this.getObjectProperty(JMS_MESSAGE_DESTINATION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        this.setObjectProperty(JMS_MESSAGE_DESTINATION, destination);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return this.getIntProperty(JMS_MESSAGE_DELIVERY_MODE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        this.setIntProperty(JMS_MESSAGE_DELIVERY_MODE, deliveryMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return this.getBooleanProperty(JMS_MESSAGE_REDELIVERED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        this.setBooleanProperty(JMS_MESSAGE_REDELIVERED, redelivered);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJMSType() throws JMSException {
        return this.getStringProperty(JMS_MESSAGE_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSType(String type) throws JMSException {
        this.setStringProperty(JMS_MESSAGE_TYPE, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getJMSExpiration() throws JMSException {
        return this.getLongProperty(JMS_MESSAGE_EXPIRATION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        this.setLongProperty(JMS_MESSAGE_EXPIRATION, expiration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMSPriority() throws JMSException {
        return this.getIntProperty(JMS_MESSAGE_PRIORITY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSPriority(int priority) throws JMSException {
        this.setIntProperty(JMS_MESSAGE_PRIORITY, priority);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clearProperties() throws JMSException {
        this.userJmsProperties.clear();
        this.setReadOnlyProperties(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean propertyExists(String name) throws JMSException {
        return this.userJmsProperties.containsKey(name) || this.rmqProperties.containsKey(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null) {
            //default value for null is false
            return false;
        } else if (o instanceof String) {
            return Boolean.parseBoolean((String) o);
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            throw new MessageFormatException(String.format("Unable to convert from class [%s]", o.getClass().getName()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByteProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            throw new NumberFormatException("Null is not a valid byte");
        else if (o instanceof String) {
            return Byte.parseByte((String) o);
        } else if (o instanceof Byte) {
            return (Byte) o;
        } else
            throw new MessageFormatException(String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShortProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            throw new NumberFormatException("Null is not a valid short");
        else if (o instanceof String) {
            return Short.parseShort((String) o);
        } else if (o instanceof Byte) {
            return (Byte) o;
        } else if (o instanceof Short) {
            return (Short) o;
        } else
            throw new MessageFormatException(String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIntProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            throw new NumberFormatException("Null is not a valid int");
        else if (o instanceof String) {
            return Integer.parseInt((String) o);
        } else if (o instanceof Byte) {
            return (Byte) o;
        } else if (o instanceof Short) {
            return (Short) o;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else
            throw new MessageFormatException(String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLongProperty(String name) throws JMSException {
        return convertToLong(this.getObjectProperty(name));
    }

    private static long convertToLong(Object o) throws JMSException {
        if (o == null)
            throw new NumberFormatException("Null is not a valid long");
        else if (o instanceof String) {
            return Long.parseLong((String) o);
        } else if (o instanceof Byte) {
            return (Byte) o;
        } else if (o instanceof Short) {
            return (Short) o;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Long) {
            return (Long) o;
        } else
            throw new MessageFormatException(String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloatProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            throw new NumberFormatException("Null is not a valid float");
        else if (o instanceof String) {
            return Float.parseFloat((String) o);
        } else if (o instanceof Float) {
            return (Float) o;
        } else
            throw new MessageFormatException(String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDoubleProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            throw new NumberFormatException("Null is not a valid double");
        else if (o instanceof String) {
            return Double.parseDouble((String) o);
        } else if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof Double) {
            return (Double) o;
        } else
            throw new MessageFormatException(String.format("Unable to convert from class [%s]", o.getClass().getName()));
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectProperty(String name) throws JMSException {
        if (name.startsWith(PREFIX))
            return this.rmqProperties.get(name);
        else
            return this.userJmsProperties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<?> getPropertyNames() throws JMSException {
        return new IteratorEnum<String>(this.userJmsProperties.keySet().iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        this.setObjectProperty(name, Boolean.valueOf(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        this.setObjectProperty(name, value);
    }

    /**
     * Validates that a JMS property name is correct
     * @param name the name of the property
     * @throws JMSException if the name contains invalid characters
     * @throws IllegalArgumentException if the name parameter is null
     */
    private void checkName(String name) throws JMSException {
        if (name==null || name.trim().length()==0) {
            throw new IllegalArgumentException("Invalid identifier:null");
        } else {
            //check start letters
            char c = name.charAt(0);
            for (char aINVALID_STARTS_WITH : INVALID_STARTS_WITH) {
                if (c == aINVALID_STARTS_WITH) {
                    throw new JMSException(String.format("Identifier may not start with character [%s]", c));
                }
            }
            //check funky chars inside the string
            for (char aMAY_NOT_CONTAIN : MAY_NOT_CONTAIN) {
                if (name.indexOf(aMAY_NOT_CONTAIN) >= 0) {
                    throw new JMSException(String.format("Identifier may not contain character [%s]", aMAY_NOT_CONTAIN));
                }
            }
            //check reserverd names
            for (String RESERVED_NAME : RESERVED_NAMES) {
                if (name.equalsIgnoreCase(RESERVED_NAME)) {
                    throw new JMSException(String.format("Invalid identifier [%s]", RESERVED_NAME));
                }
            }


        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {
        try {
            if (RMQConnectionMetaData.JMSX_GROUP_SEQ_LABEL.equals(name)) {
                /**
                 * Special case property, this property must be
                 * an integer and it must be larger than 0
                 */
                if (!(value instanceof Integer)) {
                    throw new MessageFormatException(String.format("Property [%s] can only be of type int", RMQConnectionMetaData.JMSX_GROUP_SEQ_LABEL));
                } else {
                    int val = (Integer) value;
                    if (val<=0) throw new JMSException(String.format("Property [%s] must be >0", RMQConnectionMetaData.JMSX_GROUP_SEQ_LABEL));
                }
            } else if (RMQConnectionMetaData.JMSX_GROUP_ID_LABEL.equals(name)) {
                /**
                 * Special case property must be a string
                 */
                if (value!=null && (!(value instanceof String))) {
                    throw new MessageFormatException(String.format("Property [%s] can only be of type String", RMQConnectionMetaData.JMSX_GROUP_ID_LABEL));
                }
            }

            if (name!=null && name.startsWith(PREFIX)) {
                if (value==null) {
                    this.rmqProperties.remove(name);
                } else {
                    this.rmqProperties.put(name, (Serializable) value);
                }
            } else {
                if (isReadOnlyProperties()) throw new MessageNotWriteableException(NOT_WRITEABLE);
                checkName(name);

                if (value==null) {
                    this.userJmsProperties.remove(name);
                } else if (validPropertyValueType(value)) {
                    this.userJmsProperties.put(name, (Serializable) value);
                } else {
                    throw new MessageFormatException(String.format("Property [%s] has incorrect value type.", name));
                }
            }
        } catch (ClassCastException x) {
            throw new RMQJMSException("Property value not serializable.", x);
        }
    }

    /**
     * Only certain types are allowed as property value types (excluding our own types).
     * @param value property value type
     * @return true if the type is valid
     */
    private boolean validPropertyValueType(Object value) {
        return (value instanceof String
             || value instanceof Boolean
             || value instanceof Byte
             || value instanceof Short
             || value instanceof Integer
             || value instanceof Long
             || value instanceof Float
             || value instanceof Double);
    }

    /**
     * Although the JavaDoc implies otherwise, this call acknowledges all unacknowledged messages on the message's
     * session.
     * <p>
     * The JavaDoc says:
     * </p>
     * <blockquote>
     * <p>A client may individually acknowledge each message as it is consumed, or it may choose to
     * acknowledge messages as an application-defined group (which is done by calling acknowledge on the last received
     * message of the group, thereby acknowledging all messages consumed by the session.)
     * </p>
     * </blockquote>
     * <p>
     * ...but there is no way to do either of these things, as is explained by the specification[1].
     * </p>
     * {@inheritDoc}
     * <p>
     * [1] <a href="https://download.oracle.com/otn-pub/jcp/7195-jms-1.1-fr-spec-oth-JSpec/jms-1_1-fr-spec.pdf">JMS 1.1 spec</a> Section 11.3.2.2.
     * </p>
     * @see RMQSession#acknowledgeMessage(RMQMessage)
     */
    @Override
    public void acknowledge() throws JMSException {
        getSession().acknowledgeMessage(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clearBody() throws JMSException {
        setReadOnlyBody(false);
        clearBodyInternal();
    }

    protected abstract void clearBodyInternal() throws JMSException;

    /** @return the {@link Charset} used to convert a {@link TextMessage} to <code>byte[]</code> */
    private static Charset getCharset() {
        return CHARSET;
    }

    /**
     * Invoked when {@link RMQMessage#toByteArray()} is called to create
     * a byte[] from a message. Each subclass must implement this, but ONLY
     * write its specific body. All the properties defined in {@link Message}
     * will be written by the parent class.
     * @param out - the output stream to which the structured part of message body (scalar types) is written
     * @param bout - the output stream to which the un-structured part of message body (explicit bytes) is written
     * @throws IOException if the body can not be written
     */
    protected abstract void writeBody(ObjectOutput out, ByteArrayOutputStream bout) throws IOException;

    /**
     * Invoked when {@link RMQMessage#toAmqpByteArray()} is called to create
     * a byte[] from a message. Each subclass must implement this, but ONLY
     * write its specific body.
     * @param out - the output stream to which the message body is written
     * @throws IOException if the body can not be written
     */
    protected abstract void writeAmqpBody(ByteArrayOutputStream out) throws IOException;

    /**
     * Invoked when a message is being deserialized to read and decode the message body.
     * The implementing class should <i>only</i> read its body from this stream.
     * If any exception is thrown, the message will not have been delivered.
     *
     * @param inputStream - the stream to read its body from
     * @param bin - the underlying byte input stream
     * @throws IOException if a read error occurs on the input stream
     * @throws ClassNotFoundException if the object class cannot be found
     */
    protected abstract void readBody(ObjectInput inputStream, ByteArrayInputStream bin) throws IOException, ClassNotFoundException;

    /**
     * Invoked when an AMQP message is being transformed into a RMQMessage
     * The implementing class should <i>only</i> read its body by this method
     *
     * @param barr - the byte array payload of the AMQP message
     */
    protected abstract void readAmqpBody(byte[] barr);

    /**
     * Generate the headers for this JMS message; these are the properties used in selection.
     * <p>
     * We attach <i>some</i> JMS properties as headers on the message. This is for the
     * purposes of message selection.
     * </p>
     * <p>
     * The headers are:
     * </p>
     * <pre>
     * <b>Header Field</b>        <b>Set By</b>
     * JMSDestination      send or publish method
     * JMSDeliveryMode     send or publish method
     * JMSExpiration       send or publish method
     * JMSPriority         send or publish method
     * JMSMessageID        send or publish method
     * JMSTimestamp        send or publish method
     * JMSCorrelationID    Client
     * JMSReplyTo          Client
     * JMSType             Client
     * JMSRedelivered      JMS provider
     * </pre>
     * <p>
     * <i>From the JMS 1.1 spec:</i><br/>
     * <blockquote> Message header field references are restricted to <code>JMSDeliveryMode</code>,
     * <code>JMSPriority</code>, <code>JMSMessageID</code>, <code>JMSTimestamp</code>, <code>JMSCorrelationID</code>,
     * and <code>JMSType</code>.</blockquote>
     * <p>[<i>Why not <code>JMSExpiration</code>?</i> ]</p>
     * <blockquote>
     * <p>
     * <code>JMSMessageID</code>, <code>JMSCorrelationID</code>, and <code>JMSType</code> values may be
     * <code>null</code> and if so are treated as a NULL value.
     * </p>
     * </blockquote>
     */
    Map<String, Object> toHeaders() throws IOException, JMSException {
        Map<String, Object> hdrs = new HashMap<String, Object>();

        // set non-null user properties
        for (Map.Entry<String, Serializable> e : this.userJmsProperties.entrySet()) {
            putIfNotNull(hdrs, e.getKey(), e.getValue());
        }

        // set (overwrite?) selectable JMS properties
        hdrs.put("JMSDeliveryMode", (this.getJMSDeliveryMode()==DeliveryMode.PERSISTENT ? "PERSISTENT": "NON_PERSISTENT"));
        putIfNotNull(hdrs, "JMSMessageID", this.getJMSMessageID());
        hdrs.put("JMSTimestamp", this.getJMSTimestamp());
        hdrs.put("JMSPriority", this.getJMSPriority());
        putIfNotNull(hdrs, "JMSCorrelationID", this.getJMSCorrelationID());
        putIfNotNull(hdrs, "JMSType", this.getJMSType());

        return hdrs;
    }

    /**
     * Converts a {@link GetResponse} to a {@link RMQMessage}
     *
     * @param response - the message information from RabbitMQ {@link Channel#basicGet} or via a {@link Consumer}.
     * @return the JMS message corresponding to the RabbitMQ message
     * @throws JMSException
     */
    static RMQMessage convertMessage(RMQSession session, RMQDestination dest, GetResponse response, ReceivingContextConsumer receivingContextConsumer) throws JMSException {
        if (response == null) /* return null if the response is null */
            return null;
        if (dest.isAmqp()) {
            return convertAmqpMessage(session, dest, response, receivingContextConsumer);
        } else {
            return convertJmsMessage(session, response, receivingContextConsumer);
        }
    }

    static RMQMessage convertJmsMessage(RMQSession session, GetResponse response, ReceivingContextConsumer receivingContextConsumer) throws JMSException {
        // Deserialize the message payload from the byte[] body
        RMQMessage message = fromMessage(response.getBody(), session.getTrustedPackages());

        message.setSession(session);                                            // Insert session in received message for Message.acknowledge
        message.setJMSRedelivered(response.getEnvelope().isRedeliver());        // Set the redelivered flag
        message.setRabbitDeliveryTag(response.getEnvelope().getDeliveryTag());  // Insert delivery tag in received message for Message.acknowledge
        // message.setJMSDestination(dest);                                     // DO NOT set the destination bug#57214768
        // JMSProperties already set
        message.setReadonly(true);                                              // Set readOnly - mandatory for received messages

        maybeSetupDirectReplyTo(message, response.getProps().getReplyTo());
        receivingContextConsumer.accept(new ReceivingContext(message));

        return message;
    }

    private static RMQMessage convertAmqpMessage(RMQSession session, RMQDestination dest, GetResponse response, ReceivingContextConsumer receivingContextConsumer) throws JMSException {
        try {
            BasicProperties props = response.getProps();

            RMQMessage message = RMQMessage.isAmqpTextMessage(props.getHeaders()) ? new RMQTextMessage() : new RMQBytesMessage();
            message = RMQMessage.fromAmqpMessage(response.getBody(), message);      // Deserialize the message payload from the byte[] body

            message.setSession(session);                                            // Insert session in received message for Message.acknowledge
            message.setJMSRedelivered(response.getEnvelope().isRedeliver());        // Set the redelivered flag
            message.setRabbitDeliveryTag(response.getEnvelope().getDeliveryTag());  // Insert delivery tag in received message for Message.acknowledge
            message.setJMSDestination(dest);                                        // We cannot know the original destination, so set local one
            message.setJMSPropertiesFromAmqpProperties(props);
            message.setReadonly(true);                                              // Set readOnly - mandatory for received messages

            maybeSetupDirectReplyTo(message, response.getProps().getReplyTo());
            receivingContextConsumer.accept(new ReceivingContext(message));

            return message;
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * Properly assign JMSReplyTo header when using direct reply to.
     * <p>
     * On a received request message, the AMQP reply-to property is
     * set to a specific <code>amq.rabbitmq.reply-to.ID</code> value.
     * We must use this value for the JMS reply to destination if
     * we want to send the response back to the destination the sender
     * is waiting.
     *
     * @param message
     * @param replyTo
     * @throws JMSException
     * @since 1.11.0
     */
    private static void maybeSetupDirectReplyTo(RMQMessage message, String replyTo) throws JMSException {
        if (replyTo != null && replyTo.startsWith(DIRECT_REPLY_TO)) {
            RMQDestination replyToDestination = new RMQDestination(DIRECT_REPLY_TO, "", replyTo, replyTo);
            message.setJMSReplyTo(replyToDestination);
        }
    }

    /**
     * Indicates to not declare a reply-to {@link RMQDestination}.
     * <p>
     * This is used for inbound messages, when the reply-to destination
     * is supposed to be already created. This avoids trying to create
     * a second time a temporary queue.
     *
     * @param message
     * @throws JMSException
     * @since 1.11.0
     */
    public static void doNotDeclareReplyToDestination(Message message) throws JMSException {
        if (message instanceof RMQMessage && message.getJMSReplyTo() != null && message.getJMSReplyTo() instanceof RMQDestination) {
            ((RMQDestination) message.getJMSReplyTo()).setDeclared(true);
        }
    }

    /**
     * Generate the headers for an AMQP message.
     * <p>
     * We attach <i>some</i> JMS properties as headers on the message. This is so the client can see them at the other end.
     * </p>
     * <p>
     * The headers we code are user headers (if they are type convertible) and:
     * </p>
     *
     * <pre>
     * <b>Header Field</b>        <b>Set By</b>
     * JMSDeliveryMode     send or publish method
     * JMSPriority         send or publish method
     * JMSMessageID        send or publish method
     * JMSTimestamp        send or publish method
     * JMSType             send or publish method (not client, as we aren't the client).
     * </pre>
     * <p>
     * But (<i>from the JMS 1.1 spec</i>):<br/>
     * <blockquote> Message header field references are restricted to <code>JMSDeliveryMode</code>,
     * <code>JMSPriority</code>, <code>JMSMessageID</code>, <code>JMSTimestamp</code>, <code>JMSCorrelationID</code>,
     * and <code>JMSType</code>.
     * <p>
     * <code>JMSMessageID</code>, <code>JMSCorrelationID</code>, and <code>JMSType</code> values may be
     * <code>null</code> and if so are treated as a NULL value.
     * </p>
     * </blockquote>
     */
    Map<String, Object> toAmqpHeaders() throws IOException, JMSException {
        Map<String, Object> hdrs = new HashMap<String, Object>();

        // set non-null user properties
        for (Map.Entry<String, Serializable> e : this.userJmsProperties.entrySet()) {
            putIfNotNullAndAmqpType(hdrs, e.getKey(), e.getValue());
        }

        // set (overwrite?) selectable JMS properties
        hdrs.put("JMSDeliveryMode", (this.getJMSDeliveryMode()==DeliveryMode.PERSISTENT ? "PERSISTENT": "NON_PERSISTENT"));
        putIfNotNull(hdrs, "JMSMessageID", this.getJMSMessageID());
        hdrs.put("JMSTimestamp", this.getJMSTimestamp());
        hdrs.put("JMSPriority", this.getJMSPriority());
        putIfNotNull(hdrs, "JMSCorrelationID", this.getJMSCorrelationID());
        putIfNotNull(hdrs, "JMSType", this.getJMSType());

        return hdrs;
    }

    private static void putIfNotNullAndAmqpType(Map<String, Object> hdrs, String key, Object val) {
        if (val!=null)
            if (  val instanceof String
               || val instanceof Integer
               || val instanceof Float
               || val instanceof Double
               || val instanceof Long
               || val instanceof Short
               || val instanceof Byte
               )
                hdrs.put(key, val);
    }

    private static void putIfNotNull(Map<String, Object> hdrs, String key, Object val) {
        if (val!=null) hdrs.put(key, val);
    }

    /**
     * Generates an AMQP byte array body for this message.
     * This method invokes the {@link #writeAmqpBody(ByteArrayOutputStream)}} method
     * on the message subclass.
     * @return the body in a byte array
     * @throws IOException if conversion fails
     */
    byte[] toAmqpByteArray() throws IOException, JMSException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(DEFAULT_MESSAGE_BODY_SIZE);
        //invoke write body
        this.writeAmqpBody(bout);
        //flush and return
        bout.flush();
        return bout.toByteArray();
    }

    /**
     * Generates a JMS byte array body for this message.
     * This method invokes the {@link #writeBody(ObjectOutput, ByteArrayOutputStream)} method
     * on the class that is being serialized
     * @return the body in a byte array
     * @throws IOException if serialization fails
     */
    byte[] toByteArray() throws IOException, JMSException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(DEFAULT_MESSAGE_BODY_SIZE);
        ObjectOutputStream out = new ObjectOutputStream(bout);
        //write the class of the message so we can instantiate on the other end
        out.writeUTF(this.getClass().getName());
        //write out message id
        out.writeUTF(this.internalMessageID);
        //write our JMS properties
        out.writeInt(this.rmqProperties.size());
        for (Map.Entry<String, Serializable> entry : this.rmqProperties.entrySet()) {
            out.writeUTF(entry.getKey());
            writePrimitive(entry.getValue(), out, true);
        }
        //write custom properties
        out.writeInt(this.userJmsProperties.size());
        for (Map.Entry<String, Serializable> entry : this.userJmsProperties.entrySet()) {
            out.writeUTF(entry.getKey());
            writePrimitive(entry.getValue(), out, true);
        }
        out.flush();  // ensure structured part written to byte stream
        this.writeBody(out, bout);
        out.flush();  // force any more structured data to byte stream
        return bout.toByteArray();
    }

    /**
     * Deserializes a {@link RMQMessage} from a JMS generated byte array
     * This method invokes the {@link #readBody(ObjectInput, ByteArrayInputStream)} method
     * on the deserialized class
     * @param b - the message bytes
     * @param trustedPackages prefixes of packages that are trusted to be safe to deserialize
     * @return a RMQMessage object
     * @throws RMQJMSException if RJMS class-related errors occur
     */
    static RMQMessage fromMessage(byte[] b, List<String> trustedPackages) throws RMQJMSException {
        /* If we don't recognise the message format this throws an exception */
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(b);
            WhiteListObjectInputStream in = new WhiteListObjectInputStream(bin, trustedPackages);
            // read the class name from the stream
            String clazz = in.readUTF();
            // instantiate the message object
            RMQMessage msg = instantiateRmqMessage(clazz, trustedPackages);

            // read the message id
            msg.internalMessageID = in.readUTF();
            // read JMS properties
            int propsize = in.readInt();
            for (int i = 0; i < propsize; i++) {
                String name = in.readUTF();
                Object value = readPrimitive(in);
                msg.rmqProperties.put(name, (Serializable) value);
            }
            //read custom properties
            propsize = in.readInt();
            for (int i = 0; i < propsize; i++) {
                String name = in.readUTF();
                Object value = readPrimitive(in);
                msg.userJmsProperties.put(name, (Serializable) value);
            }
            // read the body of the message
            msg.readBody(in, bin);
            return msg;
        } catch (IOException x) {
            throw new RMQJMSException(x);
        } catch (ClassNotFoundException x) {
            throw new RMQJMSException(x);
        }
    }

    private static RMQMessage instantiateRmqMessage(String messageClass, List<String> trustedPackages) throws RMQJMSException {
        if(isRmqObjectMessageClass(messageClass)) {
            return instantiateRmqObjectMessageWithTrustedPackages(trustedPackages);
        } else {
            try {
                // instantiate the message object with the thread context classloader
                return (RMQMessage) Class.forName(messageClass, true, Thread.currentThread().getContextClassLoader()).getDeclaredConstructor().newInstance();
            } catch (InstantiationException e) {
                throw new RMQJMSException(e);
            } catch (IllegalAccessException e) {
                throw new RMQJMSException(e);
            } catch (ClassNotFoundException e) {
                throw new RMQJMSException(e);
            } catch (NoSuchMethodException e) {
                throw new RMQJMSException(e);
            } catch (InvocationTargetException e) {
                throw new RMQJMSException(e);
            }
        }
    }

    private static boolean isRmqObjectMessageClass(String clazz) {
        return RMQObjectMessage.class.getName().equals(clazz);
    }

    private static RMQObjectMessage instantiateRmqObjectMessageWithTrustedPackages(List<String> trustedPackages) throws RMQJMSException {
        try {
            // instantiate the message object with the thread context classloader
            Class<?> messageClass = Class.forName(RMQObjectMessage.class.getName(), true, Thread.currentThread().getContextClassLoader());
            Constructor<?> constructor = messageClass.getConstructor(List.class);
            return (RMQObjectMessage) constructor.newInstance(trustedPackages);
        } catch (NoSuchMethodException e) {
            throw new RMQJMSException(e);
        } catch (InvocationTargetException e) {
            throw new RMQJMSException(e);
        } catch (IllegalAccessException e) {
            throw new RMQJMSException(e);
        } catch (InstantiationException e) {
            throw new RMQJMSException(e);
        } catch (ClassNotFoundException e) {
            throw new RMQJMSException(e);
        }
    }

    /**
     * Deserializes a {@link RMQBytesMessage} from an AMQP generated byte array
     * into a pre-created message object.
     * @param b - the message bytes
     * @param msg - a pre-created skeleton message object
     * @return a RMQMessage object, with the correct body
     * @throws IOException if an exception occurs during deserialization
     */
    private static RMQMessage fromAmqpMessage(byte[] b, RMQMessage msg) throws IOException {
        msg.readAmqpBody(b);
        return msg;
    }

    /**
     * @param deliveryMode JMS delivery mode value
     * @return RabbitMQ delivery mode value
     */
    static int rmqDeliveryMode(int deliveryMode) {
        return (deliveryMode == javax.jms.DeliveryMode.PERSISTENT ? 2 : 1);
    }

    /**
     * @param rmqDeliveryMode rabbitmq delivery mode value
     * @return JMS delivery mode String
     */
    private static int jmsDeliveryMode(Integer rmqDeliveryMode) {
        return ( ( rmqDeliveryMode != null
                && rmqDeliveryMode == 2
                 ) ? javax.jms.DeliveryMode.PERSISTENT
                   : javax.jms.DeliveryMode.NON_PERSISTENT);
    }

    /**
     * @param rmqExpiration rabbitmq expiration value
     * @return JMS expiration long value
     */
    private static long jmsExpiration(String rmqExpiration, Date da) {
        if (null == da) da = new Date(); // assume now -- wrong nearly always
        if (null == rmqExpiration)
            return (0L);
        try {
            return da.getTime() + Long.valueOf(rmqExpiration);
        } catch (NumberFormatException e) { // ignore it if conversion problems */ }
            return (0L);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.internalMessageID == null) ? 0 : this.internalMessageID.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RMQMessage other = (RMQMessage) obj;
        if (this.internalMessageID == null) {
            if (other.internalMessageID != null)
                return false;
        } else if (!this.internalMessageID.equals(other.internalMessageID))
            return false;
        return true;
    }

    /**
     * Returns the unique ID for this message.
     * This will return null unless the message has been sent on the wire
     * @return internal message ID
     */
    public String getInternalID() {
        return this.internalMessageID;
    }

    /**
     * Called when a message is sent so that each message is unique
     */
    void generateInternalID() {
        this.internalMessageID = Util.generateUUID("");
        this.rmqProperties.put(JMS_MESSAGE_ID, "ID:" + this.internalMessageID);
    }

	/**
	 * Utility method used to be able to write primitives and objects to a data
	 * stream without keeping track of order and type.
	 * <p>
	 * This also allows any Object to be written.
	 * </p>
	 * <p>
	 * The purpose of this method is to optimise the writing of a primitive that
	 * is represented as an object by only writing the type and the primitive
	 * value to the stream.
	 * </p>
	 *
	 * @param s the primitive to be written
	 * @param out the stream to write the primitive to.
	 * @throws IOException if an I/O error occurs
     * @throws MessageFormatException if message cannot be parsed
     *
	 */
    protected static void writePrimitive(Object s, ObjectOutput out) throws IOException, MessageFormatException {
        writePrimitive(s, out, false);
    }

    protected static void writePrimitive(Object s, ObjectOutput out, boolean allowSerializable) throws IOException, MessageFormatException {
        if (s == null) {
            out.writeByte(-1);
        } else if (s instanceof Boolean) {
            out.writeByte(1);
            out.writeBoolean((Boolean) s);
        } else if (s instanceof Byte) {
            out.writeByte(2);
            out.writeByte((Byte) s);
        } else if (s instanceof Short) {
            out.writeByte(3);
            out.writeShort(((Short) s));
        } else if (s instanceof Integer) {
            out.writeByte(4);
            out.writeInt((Integer) s);
        } else if (s instanceof Long) {
            out.writeByte(5);
            out.writeLong((Long) s);
        } else if (s instanceof Float) {
            out.writeByte(6);
            out.writeFloat((Float) s);
        } else if (s instanceof Double) {
            out.writeByte(7);
            out.writeDouble((Double) s);
        } else if (s instanceof String) {
            out.writeByte(8);
            out.writeUTF((String) s);
        } else if (s instanceof Character) {
            out.writeByte(9);
            out.writeChar((Character) s);
        } else if (s instanceof byte[]) {
            out.writeByte(10);
            out.writeInt(((byte[]) s).length);
            out.write(((byte[]) s));
        } else if (allowSerializable && s instanceof Serializable){
            out.writeByte(Byte.MAX_VALUE);
            out.writeObject(s);
        } else {
            throw new MessageFormatException(s + " is not a recognized primitive type.");
        }
    }

    /**
     * Utility method to read objects from a stream. These objects must have
     * been written with the method {@link #writePrimitive(Object, ObjectOutput)} otherwise
     * deserialization will fail and an IOException will be thrown
     * @param in the stream to read from
     * @return the Object read
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if a class of serialized object cannot be found
     */
    protected static Object readPrimitive(ObjectInput in) throws IOException, ClassNotFoundException {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private void setJMSPropertiesFromAmqpProperties(BasicProperties props) throws JMSException {
        this.setJMSDeliveryMode(jmsDeliveryMode(props.getDeliveryMode())); // derive delivery mode from Rabbit delivery mode

        Date da = props.getTimestamp();
        if (null!=da)   this.setJMSTimestamp(da.getTime()/1000l);  // JMS timestamps are the number of *seconds* since jan 1 1900

        String ex = props.getExpiration();  // this is a time-to-live period in milliseconds, as a Long.toString(long, 10)
        this.setJMSExpiration(RMQMessage.jmsExpiration(ex, da));

        Integer pr = props.getPriority();
        if (null!=pr)   this.setJMSPriority(pr);

        String mi = props.getMessageId();  // This is AMQP's message ID
        if (null!=mi)   this.setJMSMessageID(mi);

        String ci = props.getCorrelationId();  // This is AMQP's correlation ID
        if (null!=ci)   this.setJMSCorrelationID(ci);

        this.setJMSType(isAmqpTextMessage(props.getHeaders()) ? "TextMessage" : "BytesMessage");

        // now set properties from header: these may overwrite the ones that have already been set above
        Map<String, Object> hdrs = props.getHeaders();
        if (hdrs!=null) {
            for (Entry<String, Object> e : hdrs.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();
                if      (key.equals("JMSExpiration"))   { this.setJMSExpiration(objectToLong(val, 0l));}
                else if (key.equals("JMSPriority"))     { this.setJMSPriority(objectToInt(val, 4));}
                else if (key.equals("JMSTimestamp"))    { this.setJMSTimestamp(objectToLong(val, 0l));}
                else if (key.equals("JMSMessageID"))    { this.setJMSMessageID(val.toString());}
                else if (key.equals("JMSCorrelationID")){ this.setJMSCorrelationID(val.toString());}
                else if (key.equals("JMSType"))         { this.setJMSType(val.toString());}
                else if (key.startsWith(PREFIX))        {} // avoid setting this internal field
                else if (key.startsWith("JMS"))         {} // avoid setting this field
                else                                    { this.userJmsProperties.put(key, val.toString());}
            }
        }
    }

    private static boolean isAmqpTextMessage(Map<String, Object> hdrs) {
        boolean isTextMessage = false;
        if(hdrs != null) {
            Object headerJMSType = hdrs.get("JMSType");
            isTextMessage = (headerJMSType != null && "TextMessage".equals(headerJMSType.toString()));
        }
        return isTextMessage;
    }

    private static long objectToLong(Object val, long dft) {
        if (val==null) return dft;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            if (val instanceof CharSequence) return Long.valueOf(val.toString());
        } catch (NumberFormatException e) { /*ignore*/ }
        return dft;
    }

    private static int objectToInt(Object val, int dft) {
        if (val==null) return dft;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            if (val instanceof CharSequence) return Integer.valueOf(val.toString());
        } catch (NumberFormatException e) { /*ignore*/ }
        return dft;
    }

    static RMQMessage normalise(Message msg) throws JMSException {
        if (msg instanceof RMQMessage) return (RMQMessage) msg;

        /* If not one of our own, copy it into an RMQMessage */
             if (msg instanceof BytesMessage )    return RMQBytesMessage.recreate((BytesMessage)msg);
        else if (msg instanceof MapMessage   )    return RMQMapMessage.recreate((MapMessage)msg);
        else if (msg instanceof ObjectMessage)    return RMQObjectMessage.recreate((ObjectMessage) msg);
        else if (msg instanceof StreamMessage)    return RMQStreamMessage.recreate((StreamMessage)msg);
        else if (msg instanceof TextMessage  )    return RMQTextMessage.recreate((TextMessage)msg);
        else                                      return RMQNullMessage.recreate(msg);
    }

    /** Assign generic attributes.
     * <pre>
     * “rmqMessage = (RMQMessage) message;”</pre>
     * <p>With conversion as appropriate.</p>
     * @param rmqMessage message filled in with attributes
     * @param message message from which attributes are gained.
     * @throws JMSException if attributes cannot be copied
     */
    protected static void copyAttributes(RMQMessage rmqMessage, Message message) throws JMSException {
        try {
            rmqMessage.setJMSCorrelationID(message.getJMSCorrelationID());
            rmqMessage.setJMSType(message.getJMSType());
            // NOTE: all other JMS header values are set when the message is sent; except for ReplyTo which is ignored on
            // a foreign message.
            copyProperties(rmqMessage, message);
        } catch (Exception e) {
            throw new RMQJMSException("Error converting Message to RMQMessage.", e);
        }
    }

    private static void copyProperties(RMQMessage rmqMsg, Message msg) throws JMSException {
        @SuppressWarnings("unchecked")
        Enumeration<String> propNames = msg.getPropertyNames();

        while (propNames.hasMoreElements()) {
            String name = propNames.nextElement();
            rmqMsg.setObjectProperty(name, msg.getObjectProperty(name));
        }
    }
}
