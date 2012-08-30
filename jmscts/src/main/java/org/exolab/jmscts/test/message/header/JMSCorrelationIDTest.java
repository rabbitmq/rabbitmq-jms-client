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
 * Copyright 2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: JMSCorrelationIDTest.java,v 1.3 2005/06/16 06:36:53 tanderson Exp $
 */
package org.exolab.jmscts.test.message.header;

import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the JMSCorrelationID message property
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see AbstractSendReceiveTestCase
 */
public class JMSCorrelationIDTest extends AbstractSendReceiveTestCase {

    /**
     * The default destination
     */
    private static final String DESTINATION = "JMSCorrelationIDTest";

    /**
     * Construct a new <code>JMSCorrelationIDTest</code>
     *
     * @param name the name of test case
     */
    public JMSCorrelationIDTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(JMSCorrelationIDTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[] {DESTINATION};
    }

    /**
     * Verifies that the JMSCorrelationID header property can be set,
     * and that the received message has the same JMSCorrelationID value
     * as that sent
     *
     * @jmscts.requirement message.correlation
     * @throws Exception for any error
     */
    public void testCorrelationID() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        assertNull("getJMSCorrelationID() should return null, as no value is "
                   + "set", message.getJMSCorrelationID());

        String correlationID = "my correlation Id";
        try {
            message.setJMSCorrelationID(correlationID);
        } catch (JMSException exception) {
            fail("Failed to set a valid application specific correlation ID");
        }

        // send the message, and receive it from the same destination
        Message received = sendReceive(message, DESTINATION);
        acknowledge(received);

        // make sure the correlation ID properties are the same
        assertEquals(
            "JMSCorrelationID on received message doesn't match that sent",
            correlationID, received.getJMSCorrelationID());
    }

    /**
     * Verifies that a null can be assigned to JMSCorrelationID when:
     * <ul>
     *   <li>a message is intially constructed</li>
     *   <li>a message is received with a non-null JMSCorrelationID</li>
     * </ul>
     *
     * @jmscts.requirement message.correlation
     * @jmscts.delivery consumer
     * @throws Exception for any error
     */
    public void testNullCorrelationID() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        try {
            message.setJMSCorrelationID(null);
        } catch (JMSException exception) {
            fail("Failed to set JMSCorrelationID to null");
        }

        String correlationID = "a correlation Id";
        message.setJMSCorrelationID(correlationID);

        // send the message, and receive it from the same destination
        Message received1 = sendReceive(message, DESTINATION);
        assertEquals(
            "JMSCorrelationID on received message doesn't match that sent",
            correlationID, received1.getJMSCorrelationID());

        // resend the message, after settting its JMSCorrelationID to null,
        // and verify that the received message also has a null
        // JMSCorrelationID
        try {
            received1.setJMSCorrelationID(null);
        } catch (JMSException exception) {
            fail("Failed to set JMSCorrelationID on received message to null");
        }
        Message received2 = sendReceive(received1, DESTINATION);
        assertNull("JMSCorrelationID on received message should be null",
                   received2.getJMSCorrelationID());
        acknowledge(received2);
    }

    /**
     * Verifies the behaviour of the JMSCorrelationIDAsBytes methods.
     *
     * @jmscts.requirement message.correlation.bytes
     * @throws Exception for any error
     */
    public void testCorrelationIDAsBytes() throws Exception {
        final int size = 10;
        TestContext context = getContext();
        Message message = context.getMessage();

        byte[] masterId = new byte[size];
        byte[] id = new byte[size];
        for (int i = 0; i < size; ++i) {
            masterId[i] = (byte) i;
            id[i] = masterId[i];
        }

        try {
            if (message.getJMSCorrelationIDAsBytes() != null) {
                fail("getJMSCorrelationIDAsBytes() should return null, "
                     + "as no value is set");
            }
            message.setJMSCorrelationIDAsBytes(id);

            // the method must take a copy of the byte array
            Arrays.fill(id, (byte) 0);
            assertTrue(message.getJMSCorrelationIDAsBytes() != id);
            assertTrue(!Arrays.equals(
                           id, message.getJMSCorrelationIDAsBytes()));

            // send the message, and receive it from the same destination
            Message received = sendReceive(message, DESTINATION);

            // verify that the correlation ids are identical
            assertTrue(Arrays.equals(
                masterId, received.getJMSCorrelationIDAsBytes()));
            acknowledge(received);
        } catch (UnsupportedOperationException ignore) {
            context.getCoverage().setUnsupported("message.correlation.bytes");
        }
    }

}
