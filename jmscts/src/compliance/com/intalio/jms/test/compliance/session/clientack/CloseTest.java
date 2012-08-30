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
 * $Id: CloseTest.java,v 1.4 2003/05/04 14:12:39 tanderson Exp $
 */
package org.exolab.jmscts.test.session.clientack;

import javax.jms.Connection;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of closing sessions created with the 
 * Session.CLIENT_ACKNOWLEDGE message acknowledgment mode. It covers
 * requirements:
 * <ul>
 *  <li>connection.close.session.CLIENT_ACKNOWLEDGE</li>
 *  <li>session.close.CLIENT_ACKNOWLEDGE</li>
 * </ul>
 *
 * @author <a href="tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @see ClientAcknowledgeTestCase
 */
public class CloseTest extends ClientAcknowledgeTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testConnectionClose", "connection.close.session.CLIENT_ACKNOWLEDGE"},
        {"testSessionClose", "session.close.CLIENT_ACKNOWLEDGE"}};
        

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {"clientack1", "clientack2",
                                                  "clientack3"};

    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against the CLIENT_ACKNOWLEGE acknowledgement type,
     * all administered destination consumer messaging behaviours, and using 
     * TextMessage messages
     *
     * @param name the name of test case
     */
    public CloseTest(String name) {
        super(name, MessageTypes.TEXT, DeliveryTypes.ADMINISTERED_CONSUMER, 
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
        AckTypes types = new AckTypes(AckTypes.CLIENT_ACKNOWLEDGE);
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
     * Tests that closing a connection does not force an acknowledgement of 
     * client-acknowledged sessions. This covers requirements:
     * <ul>
     *   <li>connection.close.session.CLIENT_ACKNOWLEDGE
     * </ul>
     *
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
            closeReceivers(receivers2);

            // close the new connection, and re-receive the messages on the
            // existing connection
            // Note: the JMSRedelivered property shouldn't be set for topics
            // as they are being received by different consumers.
            boolean redelivered = context.isQueueConnectionFactory();
            context2.getConnection().close(); 
            receive(receivers, count, 1, redelivered);
        } finally {
            closeReceivers(receivers);
        }
    }

    /**
     * Tests that closing a session does not force an acknowledgement of 
     * client-acknowledged sessions. This covers requirements:
     * <ul>
     *   <li>session.close.CLIENT_ACKNOWLEDGE
     * </ul>
     *
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
            closeReceivers(receivers2);

            // close the new session, and re-receive the messages on the
            // existing connection.
            // Note: the JMSRedelivered property shouldn't be set for topics
            // as they are being received by different consumers.
            boolean redelivered = context.isQueueConnectionFactory();
            session2.close(); 
            receive(receivers, count, 1, redelivered);
        } finally {
            closeReceivers(receivers);
        }
    }
            
} //-- CloseTest
