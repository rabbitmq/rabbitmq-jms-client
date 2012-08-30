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
 * $Id: CloseTest.java,v 1.9 2005/06/16 07:59:07 tanderson Exp $
 */
package org.exolab.jmscts.test.session.clientack;

import javax.jms.Session;
import javax.jms.Message;

import junit.framework.Test;

import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of closing sessions created with the
 * <code>Session.CLIENT_ACKNOWLEDGE</code> message acknowledgment mode.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.9 $
 * @jmscts.session CLIENT_ACKNOWLEDGE
 * @jmscts.message TextMessage
 * @jmscts.delivery administered-consumer
 */
public class CloseTest extends ClientAcknowledgeTestCase {

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {"clientack1", "clientack2",
                                                  "clientack3"};

    /**
     * Construct a new <code>CloseTest</code>
     *
     * @param name the name of test case
     */
    public CloseTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite.
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(CloseTest.class);
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
     * Verifies that closing a connection does not force an acknowledgement of
     * client-acknowledged sessions.
     *
     * @jmscts.requirement connection.close.session.CLIENT_ACKNOWLEDGE
     * @throws Exception for any error
     */
    public void testConnectionClose() throws Exception {
        final int count = 10; // send count messages to each destination
        TestContext context = getContext();

        // create the receivers prior to sending any messages
        MessageReceiver[] receivers = createReceivers(context);

        // create a new connection, and corresponding receivers
        TestContext context2 = TestContextHelper.createSendReceiveContext(
            context);
        MessageReceiver[] receivers2 = createReceivers(context2);

        try {
            // send the messages on the new connection, and receive them
            context.getConnection().start();
            context2.getConnection().start();
            send(context2, count);
            receive(context2,  receivers2, count, 1, false);
            close(receivers2);

            // close the new connection, and re-receive the messages on the
            // existing connection
            // Note: the JMSRedelivered property shouldn't be set for topics
            // as they are being received by different consumers.
            boolean redelivered = context.isQueueConnectionFactory();
            context2.getConnection().close();
            Message last = receive(receivers, count, 1, redelivered);
            last.acknowledge();
        } finally {
            close(receivers);
        }
    }

    /**
     * Verifies that closing a session does not force an acknowledgement of
     * client-acknowledged sessions.
     *
     * @jmscts.requirement session.close.CLIENT_ACKNOWLEDGE
     * @throws Exception for any error
     */
    public void testSessionClose() throws Exception {
        final int count = 10; // send count messages to each destination
        TestContext context = getContext();

        // create the receivers prior to sending any messages
        MessageReceiver[] receivers = createReceivers(context);

        // create a new session context, and corresponding receivers
        Session session2 = ConnectionHelper.createSession(context);
        TestContext parent = new TestContext(
            context, session2, context.getAckType());
        TestContext context2 = new TestContext(
            parent, context.getMessage(), context.getMessagingBehaviour());
        MessageReceiver[] receivers2 = createReceivers(context2);

        try {
            // send the messages using the new session, and receive them
            send(context2, count);
            receive(context2,  receivers2, count, 1, false);
            close(receivers2);

            // close the new session, and re-receive the messages on the
            // existing connection.
            // Note: the JMSRedelivered property shouldn't be set for topics
            // as they are being received by different consumers.
            boolean redelivered = context.isQueueConnectionFactory();
            session2.close();
            Message last = receive(receivers, count, 1, redelivered);
            last.acknowledge();
        } finally {
            close(receivers);
        }
    }

}
