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
 * Copyright 2001-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: StreamMessageTest.java,v 1.6 2004/02/03 07:31:02 tanderson Exp $
 */
package org.exolab.jmscts.test.message.copy;

import java.util.Arrays;

import javax.jms.StreamMessage;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.MessageValues;


/**
 * This class verifies that <code>StreamMessage</code> copies content
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see javax.jms.StreamMessage
 * @see AbstractMessageTestCase
 * @see org.exolab.jmscts.core.MessageTestRunner
 * @jmscts.message StreamMessage
 */
public class StreamMessageTest extends AbstractMessageTestCase
    implements MessageValues {

    /**
     * Construct a new <code>StreamMessageTest</code>
     *
     * @param name the name of test case
     */
    public StreamMessageTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(StreamMessageTest.class);
    }

    /**
     * Get the message populator. This implementation always returns null
     *
     * @return null
     */
    @Override
    public MessagePopulator getMessagePopulator() {
        return null;
    }

    /**
     * Verifies that <code>StreamMessage.writeBytes(byte[])</code> takes a copy
     * of the byte array.
     *
     * @jmscts.requirement message.copy
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
     * Verifies that <code>StreamMessage.writeBytes(byte[], int, int)</code>
     * takes a copy of the byte array.
     *
     * @jmscts.requirement message.copy
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
        assertTrue("StreamMessage.writeBytes(byte[], int, int) did not copy "
                   + "the array", !Arrays.equals(buffer, bytes));
    }

    /**
     * Verifies that <code>StreamMessage.setObject()</code> copies byte arrays
     *
     * @jmscts.requirement message.copy
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

}
