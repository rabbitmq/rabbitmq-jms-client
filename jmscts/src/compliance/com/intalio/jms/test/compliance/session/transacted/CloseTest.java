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
 * $Id: CloseTest.java,v 1.3 2003/05/04 14:12:39 tanderson Exp $
 */
package org.exolab.jmscts.test.session.transacted;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.session.SessionSendReceiveTestCase;


/**
 * This class tests session rollback functionality.
 *
 * It covers requirements:
 * <ul>
 *   <li>session.close.transacted</li>
 *   <li>connection.close.session.transacted</li>
 * </ul>
 *
 * @author <a href="tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see SessionSendReceiveTestCase
 */
public class CloseTest extends SessionSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testRollbackForClosedSession", "session.close.transacted"},
        {"testRollbackForClosedConnection", 
         "connection.close.session.transacted"}};

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {"rollback1", "rollback2", 
                                                  "rollback3"};

    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against a transacted session type, all administered
     * destination message delivery types, and using MapMessage messages
     *
     * @param name the name of test case
     */
    public CloseTest(String name) {
        super(name, MessageTypes.MAP, DeliveryTypes.ADMINISTERED_CONSUMER,
              REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner test = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(test);
    }

    /**
     * Sets up the test suite.
     */
    public static Test suite() {
        AckTypes types = AckTypes.TRANSACTIONAL;
        return TestCreator.createSendReceiveTest(CloseTest.class, types);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Test that closing a session rolls back the transaction in progress 
     * <ul>
     *   <li>session.close.transacted</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testRollbackForClosedSession() throws Exception {
        final int count = 10;  // send count messages to each destination
        TestContext context = getContext();
        Message message = context.getMessage();

        // create a new session context
        Session session = ConnectionHelper.createSession(context);
        TestContext tmp = new TestContext(context, session, 
                                          context.getAckType());
        TestContext local = new TestContext(tmp, context.getMessage(), 
                                            context.getMessagingBehaviour());

        // create the senders and receivers
        MessageSender[] senders = createSenders(local);
        MessageReceiver[] receivers = createReceivers(context);

        try {
            // send the messages, and close the session
            send(senders, message, count);
            session.close();

            // verify that the session rolled back, by ensuring that the
            // receivers cannot receive any messages
            receive(receivers, 0);
        } finally {
            closeSenders(senders);
            closeReceivers(receivers);
            // close of receivers should be ignored as the connection is closed
        }
    }

    /**
     * Test that closing a connection rolls back the transactions in progress 
     * on its transacted sessions. This covers requirements:
     * <ul>
     *   <li>connection.close.session.transacted</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testRollbackForClosedConnection() throws Exception {
        final int count = 10;  // send count messages to each destination
        TestContext context = getContext();
        Message message = context.getMessage();

        // create a new connection and session
        TestContext local = TestContextHelper.createSendReceiveContext(
            context);

        // create the senders and receivers
        MessageSender[] senders = createSenders(local);
        MessageReceiver[] receivers = createReceivers(context);

        try {
            // send the messages, and close the connection
            send(senders, message, count);
            local.getConnection().close();

            // verify that the session rolled back, by ensuring that the
            // receivers cannot receive any messages
            receive(receivers, 0);
        } finally {
            closeSenders(senders);
            closeReceivers(receivers);
            // close of receivers should be ignored as the connection is closed
        }
    }
            
} //-- CloseTest
