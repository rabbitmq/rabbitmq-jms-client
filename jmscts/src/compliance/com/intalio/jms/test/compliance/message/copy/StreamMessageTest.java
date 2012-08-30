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
 * $Id: StreamMessageTest.java,v 1.4 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.message.copy;

import java.util.Arrays;

import javax.jms.StreamMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

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
 * This class tests that {@link StreamMessage} copies content
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>message.copy</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @see javax.jms.StreamMessage
 * @see AbstractMessageTestCase
 * @see org.exolab.jmscts.core.MessageTestRunner
 */
public class StreamMessageTest extends AbstractMessageTestCase 
    implements MessageValues {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testWriteBytesCopy", "message.copy"},
        {"testPartialWriteBytesCopy", "message.copy"},
        {"testByteArrayCopy", "message.copy"}};

    /**
     * Create an instance of this class for a specific test case
     * 
     * @param name the name of test case
     */
    public StreamMessageTest(String name) {
        super(name, MessageTypes.STREAM, REQUIREMENTS);
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
        return TestCreator.createMessageTest(StreamMessageTest.class);
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
     * Tests that {@link StreamMessage#writeBytes(byte[])} takes a copy of
     * the byte array.
     * This covers requirements:
     * <ul>
     *   <li>message.copy</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testWriteBytesCopy() throws Exception {
        final int size = 10;
        TestContext context = getContext();
        StreamMessage message = (StreamMessage) context.getMessage();

        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 0);
        message.writeBytes(bytes);
        message.reset();

        // verify that the input was copied, by modifying it
        Arrays.fill(bytes, (byte) 1);
        byte[] buffer = new byte[size];
        message.readBytes(buffer);
        assertTrue("StreamMessage.writeBytes(byte[]) did not copy the array",
                   !Arrays.equals(buffer, bytes));
    }

    /**
     * Tests that {@link StreamMessage#writeBytes(byte[], int, int)} takes a 
     * copy of the byte array.
     * This covers requirements:
     * <ul>
     *   <li>message.copy</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testPartialWriteBytesCopy() throws Exception {
        final int size = 10;
        TestContext context = getContext();
        StreamMessage message = (StreamMessage) context.getMessage();

        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 0);
        message.writeBytes(bytes, 1, size - 1);
        message.reset();

        // verify that the input was copied, by modifying it
        Arrays.fill(bytes, (byte) 1);
        byte[] buffer = new byte[size];
        message.readBytes(buffer);
        assertTrue("StreamMessage.writeBytes(byte[], int, int) did not copy " +
                   "the array", !Arrays.equals(buffer, bytes));
    }

    /**
     * Tests that {@link StreamMessage#setObject} copies byte arrays
     * This covers requirements:
     * <ul>
     *   <li>message.copy</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testByteArrayCopy() throws Exception {
        final int size = 10;
        TestContext context = getContext();
        StreamMessage message = (StreamMessage) context.getMessage();

        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 0);
        message.writeObject(bytes);
        message.reset();

        // verify that the input was copied, by modifying it
        Arrays.fill(bytes, (byte) 1);
        byte[] buffer = (byte[]) message.readObject();
        assertTrue("StreamMessage.writeObject() did not copy the array",
                   !Arrays.equals(buffer, bytes));
    }

} //-- StreamMessageTest
