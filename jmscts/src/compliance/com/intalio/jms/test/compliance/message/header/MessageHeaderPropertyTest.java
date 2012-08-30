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
 * $Id: MessageHeaderPropertyTest.java,v 1.5 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.message.header;

import java.util.Arrays;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests message header properties.
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>message.correlation</li>
 *   <li>message.correlation.bytes</li>
 *   <li>message.replyTo</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractSendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 */
public class MessageHeaderPropertyTest extends AbstractSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testReplyTo", "message.replyTo"},
        {"testCorrelationID", "message.correlation"},
        {"testNullCorrelationID", "message.correlation"},
        {"testCorrelationIDAsBytes", "message.correlation.bytes"}};

    /**
     * The default destination
     */
    private static final String DESTINATION = "MessageHeaderPropertyTest";

    /**
     * The destination used for reply-to testing
     */
    private static final String REPLY_TO = "ReplyTo";

    /**
     * Create an instance of this class for a specific test case, testing 
     * against all message and delivery types
     * 
     * @param name the name of test case
     */
    public MessageHeaderPropertyTest(String name) {
        super(name, REQUIREMENTS);
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
        return TestCreator.createSendReceiveTest(
            MessageHeaderPropertyTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return new String[] {DESTINATION, REPLY_TO};
    }

    /**
     * Test that the reply-to address on a received message can be used to 
     * send and subsequently receive a message.
     * This covers requirements:
     * <ul>
     *   <li>message.replyTo</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testReplyTo() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        assertTrue(message.getJMSReplyTo() == null);

        Destination replyTo = getDestination(REPLY_TO);
        message.setJMSReplyTo(replyTo);

        // send the message, and receive it from the same destination
        Message received = sendReceive(message, DESTINATION);

        // make sure the replyTo properties are the same
        assertTrue(DestinationHelper.equal(replyTo, received.getJMSReplyTo()));

        // send the original message to the reply-to address, and ensure
        // that it can be received
        sendReceive(message, received.getJMSReplyTo());
    }

    /**
     * Test the JMSCorrelationID header property.
     * This covers requirements:
     * <ul>
     *   <li>message.correlation</li> 
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testCorrelationID() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        String badID = "ID:bad correlation ID";
        try {
            message.setJMSCorrelationID(badID);
            String msg = "Managed to set an invalid correlation ID, " +
                "prefixed with 'ID:'";
            fail(msg);
        } catch (JMSException ignore) {
        }

        String correlationID = "Good correlation ID";
        try {
            message.setJMSCorrelationID(correlationID);
        } catch (JMSException exception) {
            String msg = "Failed to set a valid application specific " +
                "correlation ID";
            fail(msg);
        }

        // send the message, and receive it from the same destination
        Message received = sendReceive(message, DESTINATION);

        // make sure the correlation ID properties are the same
        if (!correlationID.equals(message.getJMSCorrelationID())) {
            String msg = "JMSCorrelationID on received message doesn't " +
                "match that expected";
            fail(msg);
        }
    }

    /**
     * Test the JMSCorrelationID for null values
     * This covers requirements:
     * <ul>
     *   <li>message.correlation</li> 
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testNullCorrelationID() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        if (message.getJMSCorrelationID() != null) {
            String msg = "getJMSCorrelationID() should return null, as " +
                "no value is set";
            fail(msg);
        }

        // send the message, and receive it from the same destination
        Message received = sendReceive(message, DESTINATION);
        String correlationID = received.getJMSCorrelationID();
        if (correlationID != null) {
            String msg = "JMSCorrelationID on received message should " +
                "be null, but got '" + correlationID + "'";
            fail(msg);
        }
    }

    /**
     * Test the JMSCorrelationIDAsBytes method.
     * This covers requirements:
     * <ul>
     *   <li>message.correlation.bytes</li> 
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testCorrelationIDAsBytes() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        byte[] id = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        try {
            if (message.getJMSCorrelationIDAsBytes() != null) {
                fail("getJMSCorrelationIDAsBytes() should return null, " +
                     "as no value is set");
            }
            message.setJMSCorrelationIDAsBytes(id);

            // the method must take a copy of the byte array
            assertTrue(message.getJMSCorrelationIDAsBytes() != id);

            // send the message, and receive it from the same destination
            Message received = sendReceive(message, DESTINATION);
        
            // verify that the correlation ids are identical
            assertTrue(Arrays.equals(
                id, received.getJMSCorrelationIDAsBytes()));
        } catch (UnsupportedOperationException ignore) {
            context.getCoverage().setUnsupported("message.correlation.bytes");
        }
    }
      
} //-- MessageHeaderPropertyTest
