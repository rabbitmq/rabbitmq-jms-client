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
 *    please contact jima@intalio.com.
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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: BytesMessageTest.java,v 1.7 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.message.bytes;

import java.util.Arrays;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.ClassHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.MessageValues;


/**
 * This class tests the BytesMessage message type
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>message.bytes.eof</li>
 *   <li>message.bytes.read</li>
 *   <li>message.bytes.method.readBytes(1)</li>
 *   <li>message.bytes.method.readBytes(2)</li>
 *   <li>message.bytes.method.writeObject</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.7 $
 * @see javax.jms.BytesMessage
 * @see AbstractMessageTestCase
 * @see org.exolab.jmscts.core.MessageTestRunner
 */
public class BytesMessageTest extends AbstractMessageTestCase 
    implements MessageValues {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testReadFailure", "message.bytes.read"},
        {"testReadBytesForEmptyStream1", "message.bytes.method.readBytes(1)"},
        {"testReadBytesForEmptyStream2", "message.bytes.method.readBytes(2)"},
        {"testReadBytes", "message.bytes.method.readBytes(1)"},
        {"testPartialReadBytes1", "message.bytes.method.readBytes(2)"},
        {"testPartialReadBytes2", "message.bytes.method.readBytes(2)"},
        {"testReadBytesIndexException", "message.bytes.method.readBytes(2)"},
        {"testWriteObject", "message.bytes.method.writeObject"},
        {"testInvalidObject", "message.bytes.method.writeObject"}};

    /**
     * Create an instance of this class for a specific test case
     * 
     * @param name the name of test case
     */
    public BytesMessageTest(String name) {
        super(name, MessageTypes.BYTES, REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner test = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(test);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by 
     * {@link JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(BytesMessageTest.class);
    }

    /**
     * Get the message populator. This implementation always returns null
     *
     * @return null
     */
    public MessagePopulator getMessagePopulator() {
        return null;
    }

    /**
     * Test that if a read method throws MessageFormatException or 
     * NumberFormatException, the current position of the read pointer is not
     * be incremented, and that a subsequent read is capable of recovering 
     * from the exception by re-reading the data as a different type.<br/>
     * NOTE: With the exception of the readUTF() method, it is difficult to 
     * conceive test cases for read methods.
     * <ul>
     *   <li>
     *     A provider that implements BytesMessage using a DataInputStream 
     *     or equivalent, is likely to only throw MessageFormatException for 
     *     the readUTF() method.<br/>
     *     The other likely exceptions are MessageEOFException for stream 
     *     overruns, and JMSException for any other error.
     *    </li>
     *    <li>
     *      As BytesMessage does not support conversion, NumberFormatException 
     *      is unlikely to be thrown. 
     *    </li>
     *  </ul>
     *
     * @throws Exception for any error
     */
    public void testReadFailure() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();
        byte[] invalidUTF = {0x0, 0x1, (byte) 0xFF};
        // first 2 bytes of a UTF are the length.

        message.writeBytes(invalidUTF);
        message.reset();
        try {
            message.readUTF();
            fail("Expected MessageFormatException to be thrown for invalid " +
                 "UTF string");
        } catch (MessageFormatException ignore) {
        } catch (Exception exception) {
            fail("Expected MessageFormatException to be thrown for invalid " +
                 "UTF string, but got exception=" + 
                 exception.getClass().getName() + ", message=" + 
                 exception.getMessage());
        }

        // now try and read the data as an array of bytes
        byte[] buffer = new byte[invalidUTF.length];
        message.readBytes(buffer);
        if (!Arrays.equals(invalidUTF, buffer)) {
            fail("Byte array does not match that written");
        }
        int count = message.readBytes(buffer);
        if (count != -1) {
            fail("Expected readBytes(byte[]) to return count=-1 to indicate " +
                 "end-of-stream, but returned count=" + count);
        }
    }

    /**
     * Test the readBytes(byte[]) method for an empty BytesMessage.
     * This covers requirements:
     * <ul>
     *   <li>message.bytes.method.readBytes(1)</li>
     * </ul>
     *
     * @throws Exception for any error 
     */
    public void testReadBytesForEmptyStream1() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();
        message.reset();
        checkEndOfStream1(message);
    }

    /**
     * Test the readBytes(byte[], int) method for an empty BytesMessage.
     * This covers requirements:
     * <ul>
     *   <li>message.bytes.method.readBytes(2)</li>
     * </ul>
     *
     * @throws Exception for any error 
     */
    public void testReadBytesForEmptyStream2() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();
        message.reset();
        checkEndOfStream2(message);
    }

    /**
     * Test the readBytes method.
     * This covers requirements:
     * <ul>
     *   <li>message.bytes.method.readBytes(1)</li>
     * </ul>
     *
     * @throws Exception for any error 
     */
    public void testReadBytes() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();

        byte[] bytes = populateByteArray(256, 0);
        message.writeBytes(bytes);
        message.reset();
        byte[] buffer1 = new byte[bytes.length];
        checkReadBytes1(message, buffer1, bytes);
        checkEndOfStream1(message);
        
        message.reset();
        byte[] buffer2 = new byte[bytes.length / 2];
        checkReadBytes1(message, buffer2, 
                        populateByteArray(buffer2.length, 0));
        checkReadBytes1(message, buffer2, 
                        populateByteArray(buffer2.length, buffer2.length));
        checkEndOfStream1(message);

        message.reset();
        byte[] buffer3 = new byte[bytes.length * 2];
        checkReadBytes1(message, buffer3, bytes);
        checkEndOfStream1(message);
    } 

    /**
     * Test that the readBytes(byte[], int) method can be called multiple
     * times passing a byte array less than the length of the byte stream,
     * and that the result matches that expected.
     * This covers requirements:
     * <ul>
     *   <li>message.bytes.method.readBytes(2)</li>
     * </ul>
     *
     * @throws Exception for any error 
     */
    public void testPartialReadBytes1() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();

        byte[] bytes = populateByteArray(256, 0);
        final int chunkSize = 8;
        final int chunks = bytes.length / chunkSize;

        message.writeBytes(bytes);
        message.reset();
        byte[] readBytes = new byte[bytes.length];
        byte[] buffer = new byte[chunkSize];
        for (int i = 0; i < chunks; ++i) {
            checkReadBytes2(message, buffer, buffer.length);
            System.arraycopy(buffer, 0, readBytes, i * chunkSize, chunkSize);
        }
        if (!Arrays.equals(bytes, readBytes)) {
            fail("Byte array does not match that written");
        }
        checkEndOfStream2(message);
    } 

    /**
     * Test that the readBytes(byte[], int) method can be called multiple
     * times passing a length parameter less than the length of the array,
     * and that the result matches that expected.
     * This covers requirements:
     * <ul>
     *   <li>message.bytes.method.readBytes(2)</li>
     * </ul>
     *
     * @throws Exception for any error 
     */
    public void testPartialReadBytes2() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();
        byte[] bytes = populateByteArray(256, 0);
        final int chunkSize = 8;
        final int chunks = bytes.length / chunkSize;

        message.writeBytes(bytes);
        message.reset();
        byte[] buffer = new byte[bytes.length];
        byte[] readBytes = new byte[bytes.length];
        for (int i = 0; i < chunks; ++i) {
            checkReadBytes2(message, buffer, chunkSize);
            System.arraycopy(buffer, 0, readBytes, i * chunkSize, chunkSize);
        }
        if (!Arrays.equals(bytes, readBytes)) {
            fail("Byte array does not match that written");
        }
        checkEndOfStream2(message);
    } 

    /**
     * Test that the readBytes(byte[], int) method throws 
     * IndexOutOfBoundsException for invalid length parameters
     * This covers requirements:
     * <ul>
     *   <li>message.bytes.method.readBytes(2)</li>
     * </ul>
     *
     * @throws Exception for any error 
     */
    public void testReadBytesIndexException() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();

        byte[] buffer = new byte[10];
        message.reset();

        try {
            message.readBytes(buffer, -1);
            fail("BytesMessage.readBytes(byte[], int) should throw " +
                 "IndexOutOfBoundsException for negative length parameter");
        } catch (IndexOutOfBoundsException ignore) {
        } catch (Exception exception) {
            fail("BytesMessage.readBytes(byte[], int) should throw " +
                 "IndexOutOfBoundsException for negative length parameter" +
                 " but got exception=" + exception.getClass().getName() +
                 ", message=" + exception.getMessage());
        }

        try {
            message.readBytes(buffer, buffer.length + 1);
            fail("BytesMessage.readBytes(byte[], int) should throw " +
                 "IndexOutOfBoundsException for invalid length parameter");
        } catch (IndexOutOfBoundsException ignore) {
        } catch (Exception exception) {
            fail("BytesMessage.readBytes(byte[], int) should throw " +
                 "IndexOutOfBoundsException for invalid length parameter" +
                 " but got exception=" + exception.getClass().getName() +
                 ", message=" + exception.getMessage());
        }
    }

    /**
     * Test that all objectified primitives can be written using the 
     * writeObject() method
     * This covers requirements:
     * <ul>
     *   <li>message.bytes.method.writeObject</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testWriteObject() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();

        message.writeObject(Boolean.TRUE);
        message.writeObject(new Byte(Byte.MAX_VALUE));
        message.writeObject(new Character(Character.MAX_VALUE));
        message.writeObject(new Short(Short.MAX_VALUE));
        message.writeObject(new Integer(Integer.MAX_VALUE));
        message.writeObject(new Float(Float.MAX_VALUE));
        message.writeObject(new Double(Double.MAX_VALUE));
        String utf = "ABCD";
        message.writeObject(utf);
        byte[] bytes = populateByteArray(256, 0);
        message.writeObject(bytes);

        message.reset();

        assertTrue(message.readBoolean() == true);
        assertTrue(message.readByte() == Byte.MAX_VALUE);
        assertTrue(message.readChar() == Character.MAX_VALUE);
        assertTrue(message.readShort() == Short.MAX_VALUE);
        assertTrue(message.readInt() == Integer.MAX_VALUE);
        assertTrue(message.readFloat() == Float.MAX_VALUE);
        assertTrue(message.readDouble() == Double.MAX_VALUE);
        assertTrue(message.readUTF().equals(utf));
        byte[] buffer = new byte[bytes.length];
        message.readBytes(buffer);
        assertTrue(Arrays.equals(bytes, buffer));
        checkEndOfStream1(message);
        checkEndOfStream2(message);
    }

    /**
     * Test an invalid object being written using the writeObject() method
     * This covers requirements:
     * <ul>
     *   <li>message.bytes.method.writeObject</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testInvalidObject() throws Exception {
        TestContext context = getContext();
        BytesMessage message = (BytesMessage) context.getMessage();

        try {
            message.writeObject(new java.math.BigDecimal(0.0));
            fail("BytesMessage.writeObject() should only support " + 
                 "objectified primitives");
        } catch (MessageFormatException ignore) {
        }
    }

    /**
     * Helper to return a byte array of the specified length, populated with
     * an incrementing sequence of values
     *
     * @param length the length of the array
     * @param start the number to start the sequence at
     * @return a new byte array
     */
    private byte[] populateByteArray(int length, int start) {
        byte[] result = new byte[length];
        byte j = (byte) start;
        for (int i = 0; i < length; ++i, ++j) {
            result[i] = j;
        }
        return result;
    }

    /**
     * Helper to invoke readBytes(byte[]) and verify that the result equals
     * that expected
     *
     * @param message the message to read
     * @param buffer the buffer to read to
     * @param expected the expected result
     * @throws Exception for any error
     */
    private void checkReadBytes1(BytesMessage message, byte[] buffer, 
                                 byte[] expected) throws Exception {
        int count = message.readBytes(buffer);
        if (count != expected.length) {
            fail("Expected readBytes(byte[]) to return count=" + 
                 expected.length + " but returned count=" + count);
        }

        if (buffer.length > expected.length) {
            // only compare the relevant portion of the array
            byte[] tmp = new byte[expected.length];
            System.arraycopy(buffer, 0, tmp, 0, tmp.length);
            buffer = tmp;
        }

        if (!Arrays.equals(expected, buffer)) {
            fail("Byte array read by readBytes(byte[]) is different to that " +
                 "written");
        }
    }

    /**
     * Helper to test that readBytes(byte[]) returns -1 to indicate 
     * end-of-stream
     * 
     * @param message the message to check
     * @throws Exception for any error
     */
    private void checkEndOfStream1(BytesMessage message) throws Exception {
        // invoke readBytes twice on an empty stream to ensure that -1 is
        // returned each time. 
        byte[] buffer = new byte[10];
        try {
            for (int i = 0; i < 2; ++i) {
                int count = message.readBytes(buffer);
                if (count != -1) {
                    fail("Expected readBytes(byte[]) to return count=-1 to " +
                         "indicate end-of-stream, but returned count=" + 
                         count);
                }
            }
        } catch (Exception exception) {
            fail("Expected readBytes(byte[]) to return count=-1 to indicate " +
                 "end-of-stream, but threw exception=" + 
                 exception.getClass().getName() + ", message=" + 
                 exception.getMessage());
        }
    }

    /**
     * Helper to invoke readBytes(byte[], int)
     *
     * @param message the message to read
     * @param buffer the buffer to read to
     * @param length the number of bytes to read
     * @throws Exception for any error
     */
    private void checkReadBytes2(BytesMessage message, byte[] buffer, 
                                 int length) 
        throws Exception {
        int count = message.readBytes(buffer, length);
        if (count != length) {
            fail("Expected readBytes(byte[], int) to return count=" + 
                 length + " but returned count=" + count);
        }
    }

    /**
     * Helper to test that readBytes(byte[], int) returns -1 to indicate 
     * end-of-stream
     * 
     * @param message the message to check
     * @throws Exception for any error
     */
    private void checkEndOfStream2(BytesMessage message) throws Exception {
        // invoke readBytes twice on an empty stream to ensure that -1 is
        // returned each time. 
        byte[] buffer = new byte[10];
        try {
            for (int i = 0; i < 2; ++i) {
                int count = message.readBytes(buffer, buffer.length);
                if (count != -1) {
                    fail("Expected readBytes(byte[], int) to return count=-1" +
                         "to indicate end-of-stream, but returned count=" + 
                         count);
                }
            }
        } catch (Exception exception) {
            fail("Expected readBytes(byte[], int) to return count=-1 to " +
                 "indicate end-of-stream, but threw exception=" + 
                 exception.getClass().getName() + ", message=" + 
                 exception.getMessage());
        }
    }

} //-- BytesMessageTest
