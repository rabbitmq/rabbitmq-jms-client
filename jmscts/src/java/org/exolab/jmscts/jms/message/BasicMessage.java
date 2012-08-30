/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact tma@netspace.net.au.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: BasicMessage.java,v 1.2 2004/02/02 03:49:55 tanderson Exp $
 */

package org.exolab.jmscts.jms.message;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;


/**
 * This class provides a basic implementation of the javax.jms.Message
 * interface.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:55 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         javax.jms.Message
 */
public class BasicMessage implements Message {

    /**
     * If <code>true</code>, message properties are read-only
     */
    private boolean _propertiesReadOnly = false;

    /**
     * If <code>true</code>, the message body is read-only
     */
    private boolean _bodyReadOnly = false;

    /**
     * The message ID
     */
    private String _messageID;

    /**
     * The message type
     */
    private String _type;

    /**
     * The message timestamp
     */
    private long _timestamp;

    /**
     * The correlation ID
     */
    private String _correlationID;

    /**
     * The correlation ID bytes
     */
    private byte[] _correlationIDBytes;

    /**
     * The message destination
     */
    private Destination _destination;

    /**
     * The reply-to destination
     */
    private Destination _replyTo;

    /**
     * The delivery mode
     */
    private int _deliveryMode;

    /**
     * The message's expiration value
     */
    private long _expiration;

    /**
     * The message priority
     */
    private int _priority;

    /**
     * Determines if the message is being redelivered
     */
    private boolean _redelivered;

    /**
     * The map of message header properties
     */
    private HashMap<String, Object> _properties = new HashMap<String, Object>();


    /**
     * Construct a new <code>BasicMessage</code>
     */
    public BasicMessage() {
    }

    /**
     * No-op.
     */
    @Override
    public void acknowledge() {
    }

    /**
     * Clear the message body
     *
     * @throws JMSException for any error
     */
    @Override
    public void clearBody() throws JMSException {
        _bodyReadOnly = false;
    }

    /**
     * Clear the message properties
     */
    @Override
    public void clearProperties() {
        _properties.clear();
        _propertiesReadOnly = false;
    }

    /**
     * Get the message ID
     *
     * @return the message ID
     */
    @Override
    public String getJMSMessageID() {
        return _messageID;
    }

    /**
     * Set the message ID
     *
     * @param messageID the message ID
     */
    @Override
    public void setJMSMessageID(String messageID) {
        _messageID = messageID;
    }

    /**
     * Get the message type
     *
     * @return the message type
     */
    @Override
    public String getJMSType() {
        return _type;
    }

    /**
     * Set the message type
     *
     * @param type the message type
     */
    @Override
    public void setJMSType(String type) {
        _type = type;
    }

    /**
     * Get the message timestamp
     *
     * @return the message timestamp
     */
    @Override
    public long getJMSTimestamp() {
        return _timestamp;
    }

    /**
     * Set the message timetamp
     *
     * @param timestamp the message timestamp
     */
    @Override
    public void setJMSTimestamp(long timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Get the correlation ID for the message
     *
     * @return the correlation ID for the message
     */
    @Override
    public String getJMSCorrelationID() {
        return _correlationID;
    }

    /**
     * Set the correlation ID
     *
     * @param correlationID the correlation ID
     */
    @Override
    public void setJMSCorrelationID(String correlationID) {
        _correlationID = correlationID;
    }

    /**
     * Get the correlation ID as an array of bytes
     *
     * @return the correlation ID as an array of bytes
     */
    @Override
    public byte[] getJMSCorrelationIDAsBytes() {
        return _correlationIDBytes;
    }

    /**
     * Set the correlation ID as an array of bytes
     *
     * @param correlationID the correlation ID as an array of bytes
     */
    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) {
        _correlationIDBytes = correlationID;
    }

    /**
     * Get the message destination
     *
     * @return the message destination
     */
    @Override
    public Destination getJMSDestination() {
        return _destination;
    }

    /**
     * Set the message destination
     *
     * @param destination the message destination
     */
    @Override
    public void setJMSDestination(Destination destination) {
        _destination = destination;
    }

    /**
     * Get the reply-to destination
     *
     * @return the reply-to destination
     */
    @Override
    public Destination getJMSReplyTo() {
        return _replyTo;
    }

    /**
     * Set the reply-to destination
     *
     * @param replyTo the reply-to destination
     */
    @Override
    public void setJMSReplyTo(Destination replyTo) {
        _replyTo = replyTo;
    }

    /**
     * Get the message delivery mode
     *
     * @return the message delivery mode
     */
    @Override
    public int getJMSDeliveryMode() {
        return _deliveryMode;
    }

    /**
     * Set the message delivery mode
     *
     * @param deliveryMode the message delivery mode
     */
    @Override
    public void setJMSDeliveryMode(int deliveryMode) {
        _deliveryMode = deliveryMode;
    }

    /**
     * Get the message's expiration value
     *
     * @return the message's expiration value
     */
    @Override
    public long getJMSExpiration() {
        return _expiration;
    }

    /**
     * Set the message's expiration value
     *
     * @param expiration the message's expiration value
     */
    @Override
    public void setJMSExpiration(long expiration) {
        _expiration = expiration;
    }

    /**
     * Get the message priority
     *
     * @return the message priority
     */
    @Override
    public int getJMSPriority() {
        return _priority;
    }

    /**
     * Set the message priority
     *
     * @param priority the message priority
     */
    @Override
    public void setJMSPriority(int priority) {
        _priority = priority;
    }

    /**
     * Determine if the message is being delivered
     *
     * @return <code>true</code> if the message is being delivered
     */
    @Override
    public boolean getJMSRedelivered() {
        return _redelivered;
    }

    /**
     * Set to indicate if the message is being redelivered
     *
     * @param redelivered if <code>true</code> indicates the message is being
     * delivered
     */
    @Override
    public void setJMSRedelivered(boolean redelivered) {
        _redelivered = redelivered;
    }

    /**
     * Determines if a property exists
     *
     * @param name the property name
     * @return <code>true</code> if the property exists
     */
    @Override
    public boolean propertyExists(String name) {
        return _properties.containsKey(name);
    }

    /**
     * Returns the boolean property value with the give name
     *
     * @param name the name of the boolean property
     * @return the boolean property value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public boolean getBooleanProperty(String name)
        throws MessageFormatException  {
        return FormatConverter.getBoolean(_properties.get(name));
    }

    /**
     * Set a boolean property value with the given name
     *
     * @param name the name of the boolean property
     * @param value the boolean property value
     */
    @Override
    public void setBooleanProperty(String name, boolean value) {
        _properties.put(name, new Boolean(value));
    }

    /**
     * Returns the byte property value with the give name
     *
     * @param name the name of the byte property
     * @return the byte property value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public byte getByteProperty(String name) throws MessageFormatException {
        return FormatConverter.getByte(_properties.get(name));
    }

    /**
     * Set a byte property value with the given name
     *
     * @param name the name of the byte property
     * @param value the byte property value
     */
    @Override
    public void setByteProperty(String name, byte value) {
        _properties.put(name, new Byte(value));
    }

    /**
     * Returns the short property value with the give name
     *
     * @param name the name of the short property
     * @return the short property value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public short getShortProperty(String name) throws MessageFormatException {
        return FormatConverter.getShort(_properties.get(name));
    }

    /**
     * Set a short property value with the given name
     *
     * @param name the name of the short property
     * @param value the short property value
     */
    @Override
    public void setShortProperty(String name, short value)  {
        _properties.put(name, new Short(value));
    }

    /**
     * Returns the int property value with the give name
     *
     * @param name the name of the int property
     * @return the int property value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public int getIntProperty(String name)  throws MessageFormatException {
        return FormatConverter.getInt(_properties.get(name));
    }

    /**
     * Set an int property value with the given name
     *
     * @param name the name of the int property
     * @param value the int property value
     */
    @Override
    public void setIntProperty(String name, int value) {
        _properties.put(name, new Integer(value));
    }

    /**
     * Returns the long property value with the give name
     *
     * @param name the name of the long property
     * @return the long property value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public long getLongProperty(String name) throws MessageFormatException {
        return FormatConverter.getLong(_properties.get(name));
    }

    /**
     * Set a long property value with the given name
     *
     * @param name the name of the long property
     * @param value the long property value
     */
    @Override
    public void setLongProperty(String name, long value) {
        _properties.put(name, new Long(value));
    }

    /**
     * Returns the float property value with the give name
     *
     * @param name the name of the float property
     * @return the float property value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public float getFloatProperty(String name)  throws MessageFormatException {
        return FormatConverter.getFloat(_properties.get(name));
    }

    /**
     * Set a float property value with the given name
     *
     * @param name the name of the float property
     * @param value the float property value
     */
    @Override
    public void setFloatProperty(String name, float value) {
        _properties.put(name, new Float(value));
    }

    /**
     * Returns the double property value with the give name
     *
     * @param name the name of the double property
     * @return the double property value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public double getDoubleProperty(String name)
        throws MessageFormatException {
        return FormatConverter.getDouble(_properties.get(name));
    }

    /**
     * Set a double property value with the given name
     *
     * @param name the name of the double property
     * @param value the double property value
     */
    @Override
    public void setDoubleProperty(String name, double value) {
        _properties.put(name, new Double(value));
    }

    /**
     * Returns the String property value with the give name
     *
     * @param name the name of the String property
     * @return the String property value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public String getStringProperty(String name)
        throws MessageFormatException {
        return FormatConverter.getString(_properties.get(name));
    }

    /**
     * Set a String property value with the given name
     *
     * @param name the name of the String property
     * @param value the String property value
     */
    @Override
    public void setStringProperty(String name, String value) {
        _properties.put(name, value);
    }

    /**
     * Returns the Object property value with the give name
     *
     * @param name the name of the Object property
     * @return the Object property value with the given name
     */
    @Override
    public Object getObjectProperty(String name) {
        return _properties.get(name);
    }

    /**
     * Set an Object property value with the given name
     *
     * @param name the name of the Object property
     * @param value the Object property value
     */
    @Override
    public void setObjectProperty(String name, Object value) {
        _properties.put(name, value);
    }

    /**
     * Return an enumeration of all the property names
     *
     * @return an enumeration of all the property names
     */
    @Override
    public Enumeration<String> getPropertyNames() {
        return Collections.enumeration(_properties.keySet());
    }

    /**
     * Sets the read-only state of the message
     *
     * @param readOnly if true, make the message body and properties read-only
     * @throws JMSException if the read-only state cannot be changed
     */
    public void setReadOnly(boolean readOnly) throws JMSException {
        _propertiesReadOnly = readOnly;
        _bodyReadOnly = readOnly;
    }

    /**
     * Determines if the message properties are read only
     *
     * @return <code>true</code> if the message properties are read only
     */
    protected boolean getPropertiesReadOnly() {
        return _propertiesReadOnly;
    }

    /**
     * Sets the read-only state of the message properties
     *
     * @param readOnly if <code>true</code>, make the message properties
     * read-only
     */
    protected void setPropertiesReadOnly(boolean readOnly) {
        _propertiesReadOnly = readOnly;
    }

    /**
     * Determines if the message body is read only
     *
     * @return <code>true</code> if the message body is read only
     */
    protected boolean getBodyReadOnly() {
        return _bodyReadOnly;
    }

    /**
     * Sets the read-only state of the message body
     *
     * @param readOnly if <code>true</code>, make the message body
     */
    protected void setBodyReadOnly(boolean readOnly) {
        _bodyReadOnly = readOnly;
    }

    /**
     * Determines if properties may be set
     *
     * @throws MessageNotWriteableException if message is read-only
     */
    protected void checkPropertyWrite() throws MessageNotWriteableException {
        if (_propertiesReadOnly) {
            throw new MessageNotWriteableException(
                "Message in read-only mode");
        }
    }

    /**
     * Determines if the message body may be written
     *
     * @throws MessageNotWriteableException if the message is read-only
     */
     protected void checkWrite() throws MessageNotWriteableException {
        if (_bodyReadOnly) {
            throw new MessageNotWriteableException(
                "Message in read-only mode");
        }
    }

    /**
     * Determines if the message body may be read
     *
     * @throws MessageNotReadableException if the message is write-only
     */
    protected void checkRead() throws MessageNotReadableException {
        if (!_bodyReadOnly) {
            throw new MessageNotReadableException(
                "Message in write-only mode");
        }
    }

}
