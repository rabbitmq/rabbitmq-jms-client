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
 * $Id: BasicStreamMessage.java,v 1.2 2004/02/02 03:49:55 tanderson Exp $
 */

package org.exolab.jmscts.jms.message;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;
import javax.jms.StreamMessage;


/**
 * This class provides a basic implementation of the javax.jms.ObjectMessage
 * interface.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:55 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         javax.jms.StreamMessage
 */
public class BasicStreamMessage extends BasicMessage implements StreamMessage {

    /**
     * The message body
     */
    private List<Object> _body = new ArrayList<Object>();

    /**
     * Index into the body array
     */
    private int _index = 0;


    /**
     * Current byte array field being processed
     */
    private byte[] _bytes;

    /**
     * Non-zero if incrementally reading a byte array using
     * {@link #readBytes(byte[])}
     */
    private int _bytesRead = 0;


    /**
     * Construct a new <code>BasicStreamMessage</code>.
     * When first created, the message is in write-only mode.
     */
    public BasicStreamMessage() {
    }

    /**
     * Read a <code>boolean</code> from the bytes message stream
     *
     * @return the <code>boolean</code> value read
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     */
    @Override
    public boolean readBoolean() throws JMSException {
        boolean result = FormatConverter.getBoolean(readNext());
        _index++;
        return result;
    }

    /**
     * Read a byte value from the stream message
     *
     * @return the next byte from the stream message as an 8-bit
     * <code>byte</code>
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     * @throws NumberFormatException if numeric conversion is invalid
     */
    @Override
    public byte readByte() throws JMSException {
        byte result = FormatConverter.getByte(readNext());
        _index++;
        return result;
    }

    /**
     * Read a 16-bit number from the stream message.
     *
     * @return a 16-bit number from the stream message
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     * @throws NumberFormatException if numeric conversion is invalid
     */
    @Override
    public short readShort() throws JMSException {
        short result = FormatConverter.getShort(readNext());
        _index++;
        return result;
    }

    /**
     * Read a Unicode character value from the stream message
     *
     * @return a Unicode character from the stream message
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     */
    @Override
    public char readChar() throws JMSException {
        char result = FormatConverter.getChar(readNext());
        _index++;
        return result;
    }

    /**
     * Read a 32-bit integer from the stream message
     *
     * @return a 32-bit integer value from the stream message, interpreted
     * as an <code>int</code>
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     * @throws NumberFormatException if numeric conversion is invalid
     */
    @Override
    public int readInt() throws JMSException {
        int result = FormatConverter.getInt(readNext());
        _index++;
        return result;
    }

    /**
     * Read a 64-bit integer from the stream message
     *
     * @return a 64-bit integer value from the stream message, interpreted as
     * a <code>long</code>
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     * @throws NumberFormatException if numeric conversion is invalid
     */
    @Override
    public long readLong() throws JMSException {
        long result = FormatConverter.getLong(readNext());
        _index++;
        return result;
    }

    /**
     * Read a <code>float</code> from the stream message
     *
     * @return a <code>float</code> value from the stream message
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     * @throws NullPointerException if the value is null
     * @throws NumberFormatException if numeric conversion is invalid
     */
    @Override
    public float readFloat() throws JMSException {
        float result = FormatConverter.getFloat(readNext());
        _index++;
        return result;
    }

    /**
     * Read a <code>double</code> from the stream message
     *
     * @return a <code>double</code> value from the stream message
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     * @throws NullPointerException if the value is null
     * @throws NumberFormatException if numeric conversion is invalid
     */
    @Override
    public double readDouble() throws JMSException {
        double result = FormatConverter.getDouble(readNext());
        _index++;
        return result;
    }

    /**
     * Read in a string from the stream message
     *
     * @return a Unicode string from the stream message
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     */
    @Override
    public String readString() throws JMSException {
        String result = FormatConverter.getString(readNext());
        _index++;
        return result;
    }

    /**
     * Read a byte array field from the stream message into the
     * specified byte[] object (the read buffer).
     * <p>
     * To read the field value, readBytes should be successively called
     * until it returns a value less than the length of the read buffer.
     * The value of the bytes in the buffer following the last byte
     * read are undefined.
     * <p>
     * If readBytes returns a value equal to the length of the buffer, a
     * subsequent readBytes call must be made. If there are no more bytes
     * to be read this call will return -1.
     * <p>
     * If the bytes array field value is null, readBytes returns -1.
     * <p>
     * If the bytes array field value is empty, readBytes returns 0.
     * <p>
     * Once the first readBytes call on a byte[] field value has been done,
     * the full value of the field must be read before it is valid to read
     * the next field. An attempt to read the next field before that has
     * been done will throw a MessageFormatException.
     * <p>
     * To read the byte field value into a new byte[] object, use the
     * {@link #readObject} method.
     *
     * @param value the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 if
     * there is no more data because the end of the byte field has been
     * reached.
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if an end of message stream
     * @throws MessageFormatException if this type conversion is invalid
     */
    @Override
    public int readBytes(byte[] value) throws JMSException {
        int read = 0;            // the number of bytes read
        if (_bytes == null) {
            Object next = readNext();
            if (!(next instanceof byte[])) {
                throw new MessageFormatException("Field is not a byte array");
            }
            _bytes = (byte[]) next;
            _bytesRead = 0;
            _index++;
        }
        int available = _bytes.length - _bytesRead;

        if (available == 0) {
            if (_bytesRead != 0) { // not the first invocation
                read = -1;
            }
        } else {
            if (value.length <= available) {
                read = value.length;
            } else {
                read = available;
            }
            System.arraycopy(_bytes, _bytesRead, value, 0, value.length);
            _bytesRead += read;
        }
        return read;
    }

    /**
     * Read a Java object from the stream message
     * <p>
     * Note that this method can be used to return in objectified format,
     * an object that had been written to the stream with the equivalent
     * <code>writeObject</code> method call, or it's equivalent primitive
     * write<type> method.
     * <p>
     * Note that byte values are returned as byte[], not Byte[].
     *
     * @return a Java object from the stream message, in objectified
     * format (eg. if it set as an int, then a Integer is returned).
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     */
    @Override
    public Object readObject() throws JMSException {
        Object result = readNext();
        _index++;
        return result;
    }

    /**
     * Write a <code>boolean</code> to the stream message.
     * The value <code>true</code> is written out as the value
     * <code>(byte)1</code>; the value <code>false</code> is written out as
     * the value <code>(byte)0</code>.
     *
     * @param value the <code>boolean</code> value to be written.
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeBoolean(boolean value)
        throws MessageNotWriteableException {
        checkWrite();
        _body.add(new Boolean(value));
    }

    /**
     * Write out a <code>byte</code> to the stream message
     *
     * @param value the <code>byte</code> value to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeByte(byte value) throws MessageNotWriteableException {
        checkWrite();
        _body.add(new Byte(value));
    }

    /**
     * Write a <code>short</code> to the stream message
     *
     * @param value the <code>short</code> to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeShort(short value) throws MessageNotWriteableException {
        checkWrite();
        _body.add(new Short(value));
    }

    /**
     * Write a <code>char</code> to the stream message
     *
     * @param value the <code>char</code> value to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeChar(char value) throws MessageNotWriteableException {
        checkWrite();
        _body.add(new Character(value));
    }

    /**
     * Write an <code>int</code> to the stream message
     *
     * @param value the <code>int</code> to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeInt(int value) throws MessageNotWriteableException {
        checkWrite();
        _body.add(new Integer(value));
    }

    /**
     * Write a <code>long</code> to the stream message
     *
     * @param value the <code>long</code> to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeLong(long value) throws MessageNotWriteableException {
        checkWrite();
        _body.add(new Long(value));
    }

    /**
     * Write a <code>float</code> to the stream message
     *
     * @param value the <code>float</code> value to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeFloat(float value) throws MessageNotWriteableException {
        checkWrite();
        _body.add(new Float(value));
    }

    /**
     * Write a <code>double</code> to the stream message
     *
     * @param value the <code>double</code> value to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeDouble(double value) throws MessageNotWriteableException {
        checkWrite();
        _body.add(new Double(value));
    }

    /**
     * Write a string to the stream message
     *
     * @param value the <code>String</code> value to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeString(String value) throws MessageNotWriteableException {
        checkWrite();
        _body.add(value);
    }

    /**
     * Write a byte array field to the stream message
     * <p>
     * The byte array <code>value</code> is written as a byte array field
     * into the StreamMessage. Consecutively written byte array fields are
     * treated as two distinct fields when reading byte array fields.
     *
     * @param value the byte array to be written
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeBytes(byte[] value) throws MessageNotWriteableException {
        writeBytes(value, 0, value.length);
    }

    /**
     * Write a portion of a byte array as a byte array field to the stream
     * message
     * <p>
     * The a portion of the byte array <code>value</code> is written as a
     * byte array field into the StreamMessage. Consecutively written byte
     * array fields are treated as two distinct fields when reading byte
     * array fields.
     *
     * @param value the byte array value to be written
     * @param offset the initial offset within the byte array
     * @param length the number of bytes to write
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeBytes(byte[] value, int offset, int length)
        throws  MessageNotWriteableException {
        checkWrite();
        byte[] buffer = new byte[length];
        System.arraycopy(value, offset, buffer, 0, length);
        _body.add(buffer);
    }

    /**
     * Write a Java object to the stream message
     * <p>
     * Note that this method only works for the objectified primitive
     * object types (Integer, Double, Long ...), String's and byte arrays.
     *
     * @param value the Java object to be written
     * @throws JMSException if JMS fails to write message due to
     * some internal JMS error
     * @throws MessageFormatException if the object is invalid
     * @throws MessageNotWriteableException if message in read-only mode
     */
    @Override
    public void writeObject(Object value) throws JMSException {
        if (value == null) {
            checkWrite();
            _body.add(null);
        } else if (value instanceof Boolean) {
            writeBoolean(((Boolean) value).booleanValue());
        } else if (value instanceof Byte) {
            writeByte(((Byte) value).byteValue());
        } else if (value instanceof byte[]) {
            writeBytes((byte[]) value);
        } else if (value instanceof Short) {
            writeShort(((Short) value).shortValue());
        } else if (value instanceof Character) {
            writeChar(((Character) value).charValue());
        } else if (value instanceof Integer) {
            writeInt(((Integer) value).intValue());
        } else if (value instanceof Long) {
            writeLong(((Long) value).longValue());
        } else if (value instanceof Float) {
            writeFloat(((Float) value).floatValue());
        } else if (value instanceof Double) {
            writeDouble(((Double) value).doubleValue());
        } else if (value instanceof String) {
            writeString((String) value);
        } else {
            throw new MessageFormatException(
                "Objects of type " + value.getClass().getName()
                + " are not supported by StreamMessage");
        }
    }

    /**
     * Put the message body in read-only mode, and reposition the stream
     * to the beginning
     */
    @Override
    public void reset() {
        setBodyReadOnly(true);
        _index = 0;
        _bytes = null;
        _bytesRead = 0;
    }

    /**
     * Clear the message body
     *
     * @throws JMSException if JMS fails to reset the message due to
     * some internal JMS error.
     */
    @Override
    public void clearBody() throws JMSException {
        super.clearBody();
        _body.clear();
        _index = 0;
        _bytes = null;
        _bytesRead = 0;
    }

    /**
     * Set the read-only mode of the message.
     *
     * @param readOnly if true, make the message body and properties read-only,
     * and invoke {@link #reset}
     * @throws JMSException if the read-only mode cannot be changed
     */
    @Override
    public void setReadOnly(boolean readOnly) throws JMSException {
        super.setReadOnly(readOnly);
        if (readOnly) {
            reset();
        }
    }

    /**
     * Read the next object from the stream message
     *
     * @return a Java object from the stream message, in objectified
     * format (eg. if it set as an int, then a Integer is returned).
     * @throws JMSException if JMS fails to read message due to some internal
     * JMS error
     * @throws MessageEOFException if end of message stream
     * @throws MessageFormatException if a byte array has not been fully read
     * by {@link #readBytes(byte[])}
     */
    private Object readNext() throws JMSException {
        checkRead();

        if (_bytesRead != 0) {
            throw new MessageFormatException(
                "Cannot read the next field until the byte array is read");
        }

        if (_index == _body.size()) {
            throw new MessageEOFException("End of stream");
        }
        return _body.get(_index);
    }

}
