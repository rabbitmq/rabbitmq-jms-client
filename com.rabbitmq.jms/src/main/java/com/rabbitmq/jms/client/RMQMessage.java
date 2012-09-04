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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;

import com.rabbitmq.jms.util.IteratorEnum;
import com.rabbitmq.jms.util.Util;

/**
 *
 */
public abstract class RMQMessage implements Message, Cloneable {
    /**
     * Error message used when throwing {@link javax.jms.MessageFormatException}
     */
    public static final String UNABLE_TO_CAST = "Unable to cast the object, %s, into the specified type %s";
    
    /**
     * Properties inside of a JMS message can NOT be named any of these reserved words
     */
    public static final String[] RESERVED_NAMES = {"NULL", "TRUE", "FALSE", "NOT", "AND", "OR", "BETWEEN", "LIKE", "IN",
                                                   "IS", "ESCAPE"};

    /**
     * Properties inside of a JMS message can NOT start with any of the following characters in the name
     */
    public static final char[] INVALID_STARTS_WITH = {'0','1','2','3','4','5','6','7','8','9','-','+', '\'','"'};
    
    /**
     * Properties inside of a JMS may not contain these characters in the name
     */
    public static final char[] MAY_NOT_CONTAIN = {'.','\'','"'};
    
    /**
     * When we create a message that has a byte[] as the underlying 
     * structure, BytesMessage and StreamMessage, this is the default size
     * Can be changed with a system property
     */
    protected static final int DEFAULT_MESSAGE_BODY_SIZE = Integer.getInteger("com.rabbitmq.jms.message.size", 512);

    /**
     * We store all the JMS hard coded values, such as {@link #setJMSMessageID(String)}, as properties
     * instead of hard coded fields. This way we can create a structure later on that
     * the rabbit MQ broker can read by just changing the 
     * {@link #toMessage(RMQMessage)} and {@link #fromMessage(byte[])}  
     */
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

    /**
     * For turning Strings into byte[] and back we use this charset
     * This is used for {@link RMQMessage#getJMSCorrelationIDAsBytes()}
     */
    private static final Charset charset = Charset.forName("UTF-8");

    /**
     * Here we store the JMS_ properties that would have been fields 
     */
    private final Map<String, Serializable> rmqProperties = new HashMap<String, Serializable>();
    /**
     * Here we store the users custom JMS properties
     */
    private final Map<String, Serializable> jmsProperties = new HashMap<String, Serializable>();
    /**
     * Has this message been acked?
     */
    private final AtomicBoolean isAcked = new AtomicBoolean(false);
    /**
     * We generate a unique message ID each time we send a message
     * It is stored here. This is also used for 
     * {@link #hashCode()} and {@link #equals(Object)}
     */
    private volatile String internalMessageID=null;
    /**
     * A message is read only if it has been received.
     * We set this flag when we receive a message in the following method
     * {@link RMQMessageConsumer#processMessage(com.rabbitmq.client.GetResponse, boolean)}
     */
    private volatile boolean readonlyProperties=false;
    private volatile boolean readonlyBody=false;
    
    
    /**
     * Returns true if this message body is read only
     * This means that message has been received and can not 
     * be modified
     * @return true if the message is read only
     */
    public boolean isReadonlyBody() {
        return readonlyBody;
    }
    
    public boolean isReadOnlyProperties() {
        return this.readonlyProperties;
    }
    
    /**
     * Sets the read only flag on this message
     * @see {@link RMQMessageConsumer#processMessage(com.rabbitmq.client.GetResponse, boolean)}
     * @param readonly
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
     * @see {@link RMQSession#acknowledge(RMQMessage)}
     * We set this value in 
      * @see {@link RMQMessageConsumer#processMessage(com.rabbitmq.client.GetResponse, boolean)}
     */
    private long rabbitDeliveryTag = -1;
    /**
     * returns the delivery tag for this message 
     * @return 
     */
    public long getRabbitDeliveryTag() {
        return rabbitDeliveryTag;
    }
    /**
     * Sets the delivery tag for this message, this field is 
      * @see {@link RMQMessageConsumer#processMessage(com.rabbitmq.client.GetResponse, boolean)}

     * @param rabbitDeliveryTag
     */
    protected void setRabbitDeliveryTag(long rabbitDeliveryTag) {
        this.rabbitDeliveryTag = rabbitDeliveryTag;
    }

    /**
     * The Message must hold a reference to the session itself
     * So that it can ack itself. Ack belongs to the session
     * @see {@link RMQSession#acknowledge(RMQMessage)}
     */
    private volatile transient RMQSession session = null;
    /**
     * Returns the session this object belongs to
     * @return
     */
    public RMQSession getSession() {
        return session;
    }
    /**
     * Sets the session this object was received by
     * @see {@link RMQMessageConsumer#processMessage(com.rabbitmq.client.GetResponse, boolean)}
     * @see {@link RMQSession#acknowledge(RMQMessage)}
     * @param session
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
        return this.getLongProperty(JMS_MESSAGE_TIMESTAMP);
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
            return id.getBytes(charset);
        else
            return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        String id = correlationID != null ? new String(correlationID, charset) : null;
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
        this.jmsProperties.clear();
        this.setReadOnlyProperties(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean propertyExists(String name) throws JMSException {
        return this.jmsProperties.containsKey(name) || this.rmqProperties.containsKey(name);
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
            Boolean b = (Boolean) o;
            return b.booleanValue();
        } else {
            throw new MessageFormatException("Unable to convert from class:" + o.getClass().getName());
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
            Byte b = (Byte) o;
            return b.byteValue();
        } else
            throw new MessageFormatException("Unable to convert from class:" + o.getClass().getName());
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
            Byte b = (Byte) o;
            return b.byteValue();
        } else if (o instanceof Short) {
            Short b = (Short) o;
            return b.shortValue();
        } else
            throw new MessageFormatException("Unable to convert from class:" + o.getClass().getName());
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
            Byte b = (Byte) o;
            return b.byteValue();
        } else if (o instanceof Short) {
            Short b = (Short) o;
            return b.shortValue();
        } else if (o instanceof Integer) {
            Integer b = (Integer) o;
            return b.intValue();
        } else
            throw new MessageFormatException("Unable to convert from class:" + o.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLongProperty(String name) throws JMSException {
        Object o = this.getObjectProperty(name);
        if (o == null)
            throw new NumberFormatException("Null is not a valid long");
        else if (o instanceof String) {
            return Long.parseLong((String) o);
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
            throw new MessageFormatException("Unable to convert from class:" + o.getClass().getName());
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
            Float b = (Float) o;
            return b.floatValue();
        } else
            throw new MessageFormatException("Unable to convert from class:" + o.getClass().getName());
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
            Float b = (Float) o;
            return b.floatValue();
        } else if (o instanceof Double) {
            Double b = (Double) o;
            return b.doubleValue();
        } else
            throw new MessageFormatException("Unable to convert from class:" + o.getClass().getName());
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
            return this.jmsProperties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<?> getPropertyNames() throws JMSException {
        return new IteratorEnum<String>(this.jmsProperties.keySet().iterator());
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
     * @throws {@link JMSException} if the name contains invalid characters
     * @throws {@link IllegalArgumentException} if the name parameter is null 
     */
    protected void checkName(String name) throws JMSException {
        if (name==null || name.trim().length()==0) {
            throw new IllegalArgumentException("Invalid identifier:null");
        } else {
            //check start letters
            char c = name.charAt(0);
            for (int i=0; i<INVALID_STARTS_WITH.length; i++) {
                if (c == INVALID_STARTS_WITH[i]) {
                    throw new JMSException("Identifier may not start with:"+c);
                }
            }
            //check funky chars inside the string
            for (int i=0; i<MAY_NOT_CONTAIN.length; i++) {
                if (name.indexOf(MAY_NOT_CONTAIN[i])>=0) {
                    throw new JMSException("Identifier may not contain:"+MAY_NOT_CONTAIN[i]);
                }
            }
            //check reserverd names
            for (int i=0; i<RESERVED_NAMES.length; i++) {
                if (name.equalsIgnoreCase(RESERVED_NAMES[i])) {
                    throw new JMSException("Invalid identifier:"+RESERVED_NAMES[i]);
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
                    throw new MessageFormatException(RMQConnectionMetaData.JMSX_GROUP_SEQ_LABEL+" can only be of type int");
                } else {
                    int val = ((Integer)value).intValue();
                    if (val<=0) throw new JMSException(RMQConnectionMetaData.JMSX_GROUP_SEQ_LABEL+" must be >0");
                }
            } else if (RMQConnectionMetaData.JMSX_GROUP_ID_LABEL.equals(name)) {
                /**
                 * Special case property must be a string
                 */
                if (value!=null && (!(value instanceof String))) {
                    throw new MessageFormatException(RMQConnectionMetaData.JMSX_GROUP_ID_LABEL+" can only be of type String");
                }
            }
            
            if (name!=null && name.startsWith(PREFIX)) {
                if (value==null) {
                    this.rmqProperties.remove(name);
                } else {
                    this.rmqProperties.put(name, (Serializable) value);
                }
            } else {
                Util.util().checkTrue(isReadOnlyProperties(), new MessageNotWriteableException("Message has been received and is read only."));
                checkName(name);

                if (value==null) {
                    this.jmsProperties.remove(name);
                } else {
                    this.jmsProperties.put(name, (Serializable) value);
                }
            }
        } catch (ClassCastException x) {
            Util.util().handleException(x, "Property value not serializable.");
        }
    }

    /**
     * {@inheritDoc}
     * @see {@link RMQSession#acknowledge(RMQMessage)}
     */
    @Override
    public void acknowledge() throws JMSException {
        if (isAcked.compareAndSet(false, true)) {
            getSession().acknowledge(this);
        }
    }

    /**
     * {@inheritDoc}
     * This is an abstract method, and is implementation specific
     */
    @Override
    public final void clearBody() throws JMSException {
        setReadOnlyBody(false);
        clearBodyInternal();
    }
    
    protected abstract void clearBodyInternal() throws JMSException;

    /**
     *@return the charset used to convert a TextMessage to byte[]
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Invoked when {@link #toMessage(RMQMessage)} is called to create 
     * a byte[] from a message. Each subclass must implement this, but ONLY
     * write it's specific body. All the properties defined in {@link Message}
     * will be written by the parent class.
     * @param out - the output which to write the message body to.
     * @throws IOException you may throw an IOException if the body can not be written
     */
    public abstract void writeBody(ObjectOutput out) throws IOException;

    /**
     * Invoked when a message is being deserialized. The implementing class 
     * should ONLY read it's body from this stream
     * @param in the stream to read it's body from
     * @throws IOException - if an IOException occurs - this will prevent message delivery
     * @throws ClassNotFoundException - if an ClassNotFoundException occurs - this will prevent message delivery
     * @throws InstantiationException - if an InstantiationException occurs - this will prevent message delivery
     * @throws IllegalAccessException - if an IllegalAccessException occurs - this will prevent message delivery
     */
    public abstract void readBody(ObjectInput in) throws IOException,
        ClassNotFoundException, InstantiationException, IllegalAccessException;

    /**
     * Serializes a {@link RMQMessage} to a byte array. 
     * This method invokes the {@link #writeBody(ObjectOutput)} method
     * on the class that is being serialized
     * @param msg - the message to serialize  
     * @return the body in a byte array
     * @throws IOException if serialization fails
     */
    public static byte[] toMessage(RMQMessage msg) throws IOException, JMSException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(DEFAULT_MESSAGE_BODY_SIZE);
        ObjectOutputStream out = new ObjectOutputStream(bout);
        //write the class of the message so we can instantiate on the other end
        out.writeUTF(msg.getClass().getName());
        //write out message id
        out.writeUTF(msg.internalMessageID);
        //write our JMS properties
        out.writeInt(msg.rmqProperties.size());
        for (Map.Entry<String, Serializable> entry : msg.rmqProperties.entrySet()) {
            out.writeUTF(entry.getKey());
            writePrimitive(entry.getValue(), out, true);
        }
        //write custom properties
        out.writeInt(msg.jmsProperties.size());
        for (Map.Entry<String, Serializable> entry : msg.jmsProperties.entrySet()) {
            out.writeUTF(entry.getKey());
            writePrimitive(entry.getValue(), out, true);
        }
        //invoke write body
        msg.writeBody(out);
        //flush and return
        out.flush();
        return bout.toByteArray();
    }

    /**
     * Deserializes a {@link RMQMessage} from a byte array
     * This method invokes the {@link #readBody(ObjectInput)} method
     * on the deserialized class
     * @param b - the message bytes
     * @return a subclass to a RMQmessage 
     * @throws ClassNotFoundException - if the class to be deserialized wasn't found
     * @throws IOException if an exception occurs during deserialization
     * @throws IllegalAccessException if an exception occurs during class instantiation
     * @throws InstantiationException if an exception occurs during class instantiation
     */
    public static RMQMessage fromMessage(byte[] b) throws ClassNotFoundException,
    IOException,
    IllegalAccessException,
    InstantiationException {
        // TODO If we don't recognize the message format then we need to
        // create a generic BytesMessage
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
        //read the classname from the stream
        String clazz = in.readUTF();
        //instantiate the message object with the thread context classloader
        RMQMessage msg = (RMQMessage) Class.forName(clazz, true, Thread.currentThread().getContextClassLoader()).newInstance();
        //read the message id
        msg.internalMessageID = in.readUTF();
        //read JMS properties
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
            msg.jmsProperties.put(name, (Serializable) value);
        }
        //invoke read body on the 
        msg.readBody(in);
        return msg;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((internalMessageID == null) ? 0 : internalMessageID.hashCode());
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
        if (internalMessageID == null) {
            if (other.internalMessageID != null)
                return false;
        } else if (!internalMessageID.equals(other.internalMessageID))
            return false;
        return true;
    }
    
    /**
     * Returns the unique ID for this message.
     * This will return null unless the message has been sent on the wire
     * @return
     */
    public String getInternalID() {
        return internalMessageID;
    }
    
    /**
     * Called when a message is sent so that each message is unique
     */
    public void generateInternalID() throws JMSException {
        internalMessageID = Util.util().generateUUIDTag();
        this.rmqProperties.put(JMS_MESSAGE_ID,"ID:"+this.internalMessageID);
    }
    
    /**
     * Utility method used to be able to write
     * primitives and objects to a data stream without keeping track of order and type
     * this also allows any Object to be written.
     * The purpose of this method is to optimize the writing of a primitive that is represented as an object
     * by only writing the type and the primitive value to the stream
     * @param s the primitive to be written
     * @param out the stream to write the primitive to.
     * @throws IOException if an IOException occurs.
     */
    public static void writePrimitive(Object s, ObjectOutput out) throws IOException, MessageFormatException {
        writePrimitive(s, out,false);
    }
    
    public static void writePrimitive(Object s, ObjectOutput out, boolean allowSerializable) throws IOException, MessageFormatException {
        if (s == null) {
            out.writeByte(-1);
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
     * @throws IOException 
     * @throws ClassNotFoundException
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
