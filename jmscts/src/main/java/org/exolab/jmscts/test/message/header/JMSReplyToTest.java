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
 * $Id: JMSReplyToTest.java,v 1.2 2005/06/16 06:36:53 tanderson Exp $
 */
package org.exolab.jmscts.test.message.header;

import javax.jms.Destination;
import javax.jms.Message;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the JMSReplyTo message property.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 * @see AbstractSendReceiveTestCase
 */
public class JMSReplyToTest extends AbstractSendReceiveTestCase {

    /**
     * The default destination
     */
    private static final String DESTINATION = "JMSReplyToTest";

    /**
     * The destination used for reply-to testing
     */
    private static final String REPLY_TO = "ReplyTo";


    /**
     * Construct a new <code>JMSReplyToTest</code>
     *
     * @param name the name of test case
     */
    public JMSReplyToTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(JMSReplyToTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[] {DESTINATION, REPLY_TO};
    }

    /**
     * Verifies that the reply-to address on a received message can be used to
     * send and subsequently receive a message.
     *
     * @jmscts.requirement message.replyTo
     * @throws Exception for any error
     */
    public void testReplyTo() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        assertNull("JMSReplyTo should initially be null",
                   message.getJMSReplyTo());

        Destination replyTo = getDestination(REPLY_TO);
        message.setJMSReplyTo(replyTo);

        // send the message, and receive it from the same destination
        Message received1 = sendReceive(message, DESTINATION);
        // make sure the JMSReplyTo hasn't changed
        assertTrue(DestinationHelper.equal(
                       replyTo, message.getJMSReplyTo()));

        // make sure the JMSReplyTo properties are the same
        assertTrue(DestinationHelper.equal(
                       replyTo, received1.getJMSReplyTo()));

        // send the original message to the reply-to address, and ensure
        // that it can be received
        Message received2 = sendReceive(message, received1.getJMSReplyTo());

        // make sure the JMSReplyTo properties are the same
        assertTrue(DestinationHelper.equal(
                       replyTo, received2.getJMSReplyTo()));

        acknowledge(received2);
    }

}
