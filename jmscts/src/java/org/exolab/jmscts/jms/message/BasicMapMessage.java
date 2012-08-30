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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: BasicMapMessage.java,v 1.2 2004/02/02 03:49:55 tanderson Exp $
 */

package org.exolab.jmscts.jms.message;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;


/**
 * This class provides a basic implementation of the javax.jms.MapMessage
 * interface.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:55 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         javax.jms.MapMessage
 */
public class BasicMapMessage extends BasicMessage implements MapMessage {

    /**
     * The container for all message data
     */
    private HashMap<String, Object> _map = new HashMap<String, Object>();

    /**
     * Construct a new <code>BasicMapMessage</code>
     */
    public BasicMapMessage() {
    }

    /**
     * Return the boolean value with the given name
     *
     * @param name the name of the boolean
     * @return the boolean value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public boolean getBoolean(String name)
        throws MessageFormatException {
        return FormatConverter.getBoolean(_map.get(name));
    }

    /**
     * Return the byte value with the given name
     *
     * @param name the name of the byte
     * @return the byte value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public byte getByte(String name) throws MessageFormatException {
        return FormatConverter.getByte(_map.get(name));
    }

    /**
     * Return the short value with the given name
     *
     * @param name the name of the short
     * @return the short value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public short getShort(String name) throws MessageFormatException {
        return FormatConverter.getShort(_map.get(name));
    }

    /**
     * Return the Unicode character value with the given name
     *
     * @param name the name of the Unicode character
     * @return the Unicode character value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public char getChar(String name) throws MessageFormatException {
        return FormatConverter.getChar(_map.get(name));
    }

    /**
     * Return the integer value with the given name
     *
     * @param name the name of the integer
     * @return the integer value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public int getInt(String name) throws MessageFormatException {
        return FormatConverter.getInt(_map.get(name));
    }

    /**
     * Return the long value with the given name
     *
     * @param name the name of the long
     * @return the long value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public long getLong(String name) throws MessageFormatException {
        return FormatConverter.getLong(_map.get(name));
    }

    /**
     * Return the float value with the given name
     *
     * @param name the name of the float
     * @return the float value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public float getFloat(String name) throws MessageFormatException {
        return FormatConverter.getFloat(_map.get(name));
    }

    /**
     * Return the double value with the given name
     *
     * @param name the name of the double
     * @return the double value with the given name
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public double getDouble(String name) throws MessageFormatException {
        return FormatConverter.getDouble(_map.get(name));
    }

    /**
     * Return the String value with the given name
     *
     * @param name the name of the String
     * @return the String value with the given name. If there is no item
     * by this name, a null value is returned.
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public String getString(String name) throws MessageFormatException {
        return FormatConverter.getString(_map.get(name));
    }

    /**
     * Return the byte array value with the given name
     *
     * @param name the name of the byte array
     * @return a copy of the byte array value with the given name.
     * If there is no item by this name, a null value is returned.
     * @throws MessageFormatException if the type conversion is invalid
     */
    @Override
    public byte[] getBytes(String name) throws MessageFormatException {
        return FormatConverter.getBytes(_map.get(name));
    }

    /**
     * Return the Java object value with the given name
     * <p>
     * Note that this method can be used to return in objectified format,
     * an object that had been stored in the Map with the equivalent
     * <code>setObject</code> method call, or it's equivalent primitive
     * set<type> method.
     *
     * @param name the name of the Java object
     * @return a copy of the Java object value with the given name, in
     * objectified format (eg. if it set as an int, then an Integer is
     * returned).
     * Note that byte values are returned as byte[], not Byte[].
     * If there is no item by this name, a null value is returned.
     * @throws JMSException if JMS fails to read the message due to some
     * internal JMS error
     */
    @Override
    public Object getObject(String name) throws JMSException {
        Object result = _map.get(name);
        if (result instanceof byte[]) {
            result = getBytes(name);
        }
        return result;
    }

    /**
     * Return an Enumeration of all the Map message's names.
     *
     * @return an enumeration of all the names in this Map message.
     */
    @Override
    public Enumeration<String> getMapNames() {
        return Collections.enumeration(_map.keySet());
    }

    /**
     * Set a boolean value with the given name, into the Map
     *
     * @param name the name of the boolean
     * @param value the boolean value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setBoolean(String name, boolean value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, new Boolean(value));
    }

    /**
     * Set a byte value with the given name, into the Map
     *
     * @param name the name of the byte
     * @param value the byte value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setByte(String name, byte value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, new Byte(value));
    }

    /**
     * Set a short value with the given name, into the Map
     *
     * @param name the name of the short
     * @param value the short value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setShort(String name, short value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, new Short(value));
    }

    /**
     * Set a Unicode character value with the given name, into the Map
     *
     * @param name the name of the Unicode character
     * @param value the Unicode character value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setChar(String name, char value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, new Character(value));
    }

    /**
     * Set an integer value with the given name, into the Map
     *
     * @param name the name of the integer
     * @param value the integer value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setInt(String name, int value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, new Integer(value));
    }

    /**
     * Set a long value with the given name, into the Map
     *
     * @param name the name of the long
     * @param value the long value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setLong(String name, long value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, new Long(value));
    }

    /**
     * Set a float value with the given name, into the Map
     *
     * @param name the name of the float
     * @param value the float value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setFloat(String name, float value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, new Float(value));
    }

    /**
     * Set a double value with the given name, into the Map
     *
     * @param name the name of the double
     * @param value the double value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setDouble(String name, double value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, new Double(value));
    }

    /**
     * Set a String value with the given name, into the Map
     *
     * @param name the name of the String
     * @param value the String value to set in the Map
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setString(String name, String value)
        throws MessageNotWriteableException {
        checkWrite();
        _map.put(name, value);
    }

    /**
     * Set a byte array value with the given name, into the Map
     *
     * @param name the name of the byte array
     * @param value the byte array value to set in the Map. The array is
     * copied so the value for name will not be altered by future
     * modifications.
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setBytes(String name, byte[] value)
        throws MessageNotWriteableException {
        checkWrite();
        byte[] bytes = null;
        if (value != null) {
            bytes = new byte[value.length];
            System.arraycopy(value, 0, bytes, 0, bytes.length);
        }
        _map.put(name, bytes);
    }

    /**
     * Set a portion of the byte array value with the given name, into the Map
     *
     * @param name the name of the byte array
     * @param value the byte array value to set in the Map.
     * @param offset the initial offset within the byte array.
     * @param length the number of bytes to use.
     * @throws MessageNotWriteableException if the message is in read-only mode
     */
    @Override
    public void setBytes(String name, byte[] value,
                               int offset, int length)
        throws MessageNotWriteableException {
        checkWrite();
        byte[] bytes = null;
        if (value != null) {
            bytes = new byte[length];
            System.arraycopy(value, offset, bytes, 0, length);
        }
        _map.put(name, bytes);
    }

    /**
     * Set a Java object value with the given name, into the Map
     * <p>
     * Note that this method only works for the objectified primitive
     * object types (Integer, Double, Long ...), String's and byte arrays.
     *
     * @param name the name of the Java object
     * @param value the Java object value to set in the Map
     * @throws MessageFormatException if object is invalid
     * @throws MessageNotWriteableException if message in read-only mode.
     */
    @Override
    public void setObject(String name, Object value)
        throws MessageFormatException, MessageNotWriteableException {
        if (value == null) {
            checkWrite();
            _map.put(name, null);
        } else if (value instanceof Boolean) {
            setBoolean(name, ((Boolean) value).booleanValue());
        } else if (value instanceof Byte) {
            setByte(name, ((Byte) value).byteValue());
        } else if (value instanceof Short) {
            setShort(name, ((Short) value).shortValue());
        } else if (value instanceof Character) {
            setChar(name, ((Character) value).charValue());
        } else if (value instanceof Integer) {
            setInt(name, ((Integer) value).intValue());
        } else if (value instanceof Long) {
            setLong(name, ((Long) value).longValue());
        } else if (value instanceof Float) {
            setFloat(name, ((Float) value).floatValue());
        } else if (value instanceof Double) {
            setDouble(name, ((Double) value).doubleValue());
        } else if (value instanceof String) {
            setString(name, (String) value);
        } else if (value instanceof byte[]) {
            setBytes(name, (byte[]) value);
        } else {
            throw new MessageFormatException(
                "MapMessage does not support objects of type="
                + value.getClass().getName());
        }
    }

    /**
     * Check if an item exists in this MapMessage
     *
     * @param name the name of the item to test
     * @return true if the item exists
     */
    @Override
    public boolean itemExists(String name) {
        return _map.containsKey(name);
    }

    /**
     * Clear out the message body. Clearing a message's body does not clear
     * its header values or property entries.
     * If this message body was read-only, calling this method leaves the
     * message body is in the same state as an empty body in a newly created
     * message
     *
     * @throws JMSException if the body can't be cleared
     */
    @Override
    public void clearBody() throws JMSException {
        super.clearBody();
        _map = new HashMap<String, Object>();
    }

}
