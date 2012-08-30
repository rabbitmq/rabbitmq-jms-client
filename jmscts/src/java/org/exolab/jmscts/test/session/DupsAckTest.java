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
 * $Id: DupsAckTest.java,v 1.2 2004/02/03 21:52:11 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import java.util.List;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of consumers on sessions created with the
 * <code>Session.DUPS_OK_ACKNOWLEDGE</code> message acknowledgment mode.<br/>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 * @jmscts.session DUPS_OK_ACKNOWLEDGE
 * @jmscts.message MapMessage
 * @jmscts.delivery consumer
 * @jmscts.durableonly true
 */
public class DupsAckTest extends AbstractSendReceiveTestCase {

    /**
     * The destination to create prior to running the test
     */
    private static final String DESTINATION = "DupsAckTest";

    /**
     * Construct a new <code>DupsAckTest</code>
     *
     * @param name the name of test case
     */
    public DupsAckTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite.
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(DupsAckTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[]{DESTINATION};
    }

    /**
     * Verifies dups ok acknowledgement functionality. Creates a consumer,
     * send n messages, receives them, and closes the consumer. Creates
     * another consumer and verifies that 0-n messages are be received.
     *
     * @jmscts.requirement session.DUPS_OK_ACKNOWLEDGE
     * @throws Exception for any error
     */
    public void testDupsOKAcknowledgement() throws Exception {
        final int count = 10; // send count messages
        TestContext context = getContext();
        MessagingBehaviour behaviour = context.getMessagingBehaviour();

        // construct the consumer
        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            // send count messages
            send(DESTINATION, count);

            // ...and receive them
            receive(receiver, count);

            // recreate the receiver, and verify that 0 or more messages are
            // received
            receiver = recreate(receiver);
            List<?> messages = receiver.receive(count, behaviour.getTimeout());
            if (messages != null && messages.size() > count) {
                fail("Expected receiver to return 0 to " + count
                     + " messages, but returned " + messages.size());
            }
        } finally {
            receiver.remove();
        }
    }

}
