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
 * $Id: JMSDestinationTest.java,v 1.2 2005/06/16 06:36:53 tanderson Exp $
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
 * This class tests the JMSDestination message property
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 * @see AbstractSendReceiveTestCase
 */
public class JMSDestinationTest extends AbstractSendReceiveTestCase {

    /**
     * The first destination name
     */
    private static final String DEST1 = "JMSDestinationTest1";

    /**
     * The second destination name
     */
    private static final String DEST2 = "JMSDestinationTest2";

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {DEST1, DEST2};


    /**
     * Construct a new <code>JMSDestinationTest</code>
     *
     * @param name the name of test case
     */
    public JMSDestinationTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(JMSDestinationTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Verifies that the JMSDestination is assigned when a message is sent,
     * and that the received message has the same JMSDestination value
     * as that sent
     *
     * @jmscts.requirement message.destination
     * @throws Exception for any error
     */
    public void testJMSDestination() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();
        Destination dest = getDestination(DEST1);

        assertNull("JMSDestination should initially be null",
                   message.getJMSDestination());

        // send the message, and receive it from the same destination
        Message received = sendReceive(message, dest);
        Destination sentDest = message.getJMSDestination();
        assertTrue("JMSDestination after send not equal to that specified",
                   DestinationHelper.equal(dest, sentDest));

        Destination receivedDest = received.getJMSDestination();
        assertTrue("JMSDestination on receipt not equal to that when sent",
                   DestinationHelper.equal(dest, receivedDest));
        acknowledge(received);
    }

    /**
     * Verifies that the JMSDestination is assigned when a message is resent
     * to a different destination
     *
     * @jmscts.requirement message.destination
     * @throws Exception for any error
     */
    public void testJMSDestinationOnResend() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();
        Destination dest1 = getDestination(DEST1);
        Destination dest2 = getDestination(DEST1);

        // send the message, and receive it from the same destination
        Message received1 = sendReceive(message, dest1);

        // resend the message to a different destination
        Message resent = received1;
        Message received2 = sendReceive(resent, dest2);
        assertTrue("JMSDestination on resend not equal to that specified",
                   DestinationHelper.equal(dest2, resent.getJMSDestination()));

        assertTrue("JMSDestination on receipt not equal to that when resent",
                   DestinationHelper.equal(
                       dest2, received2.getJMSDestination()));
        acknowledge(received2);
    }

}
