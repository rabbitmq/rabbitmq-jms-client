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
 * $Id: ListenerCloseTest.java,v 1.4 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TopicSession;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.DelayedAction;
import org.exolab.jmscts.core.DeliveryType;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.EchoListener;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.MessagingHelper;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.WaitingListener;


/**
 * This class tests the behaviour of stopping and closing a connection
 * while a listener is active.
 * It covers the following requirements:
 * <ul>
 *   <li>connection.stop.listeners</li>
 *   <li>connection.close.listeners</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @see AbstractSendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 */
public class ListenerCloseTest extends AbstractSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testSessionClose", "session.close.listeners"}};

    /**
     * The destination that the message listener receives messages on
     */ 
    private static final String REQUEST = "requestClose";

    /**
     * The destination that the message listeners send messages to
     */
    private static final String REPLY = "replyClose";

    /**
     * The destinations to create prior to running the test
     */
    private static final String DESTINATIONS[] = {REQUEST, REPLY};

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(ListenerCloseTest.class.getName());


    /**
     * The delivery types to use for this test. Temporary destinations
     * cannot be used as a separate connection is required to verify that
     * the test is successful.
     */
    private static final DeliveryTypes DELIVERY_TYPES = 
        new DeliveryTypes(new DeliveryType[]{
            DeliveryTypes.NON_PERSISTENT_ADMINISTERED_ASYNC,
            DeliveryTypes.PERSISTENT_ADMINISTERED_ASYNC});

    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against all session types, 
     * administered asynchronous delivery types, and using TextMessage messages
     *
     * @param name the name of test case
     */
    public ListenerCloseTest(String name) {
        super(name, MessageTypes.TEXT, DELIVERY_TYPES, REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner runner = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(runner);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by 
     * {@link JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(ListenerCloseTest.class, 
                                                 AckTypes.ALL);
    }

    /**
     * Returns if this test can share resources with other test cases.
     * This implementation always returns <code>false</code>, to 
     * ensure that a new connection is created for each test.
     *
     * @return <code>false</code>
     */
    public boolean share() {
        return false;
    }

    /**
     * Returns if the connection should be started prior to running the test. 
     * This implementation always returns <code>false</code> to avoid
     * conflicts with test cases
     * 
     * @return <code>false</code>
     */
    public boolean startConnection() {
        return false;
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Verify that running MessageListeners have full access to session if
     * the session is closed. This covers requirements:
     * <ul>
     *   <li>session.close.listeners</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testSessionClose() throws Exception {
        final int count = 10; // the number of messages to send
        TestContext context = getContext();
        Session session = context.getSession();
        MessagingBehaviour behaviour = context.getMessagingBehaviour();
        boolean durable = behaviour.getDurable();
        Destination request = getDestination(REQUEST);
        Destination reply = getDestination(REPLY);

        // set up a duplicate context, to enable a receiver to be created
        // for the destination that the listener replies to 
        TestContext local = TestContextHelper.createSendReceiveContext(
            context);
        MessageReceiver receiver = SessionHelper.createReceiver(local, reply);
        MessageConsumer consumer = null;

        try {
            // set up the listener
            String name = (durable) ? SessionHelper.getSubscriberName() : null;
            consumer = SessionHelper.createConsumer(context, request, name);
            MessageSender sender = SessionHelper.createSender(context, reply);
            EchoListener replier = new EchoListener(context.getSession(), 
                                                    sender, count, true);
            final WaitingListener listener = new WaitingListener(replier, 1);
            consumer.setMessageListener(listener);
        
            // send a message and start the connection
            MessagingHelper.send(context, request, 1);
            context.getConnection().start();

            // wait for the listener to start processing before closing
            // the session
            _log.debug("waiting for listener");
            listener.waitForReceipt();

            // notify the listener in a separate thread that it may proceed 
            // with processing. The notification has to occur in a separate 
            // thread else close would block indefinitely waiting for the 
            // listener to complete.
            // The half second delay is to help ensure that close is invoked 
            // before the listener begins processing the message
            _log.debug("notifying listener");
            DelayedAction action = new DelayedAction(500) {
                public void runProtected() {
                    listener.notifyContinue();
                }
            };
            action.start();
            session.close();
        } finally {
            try {
                consumer.close();
            } catch (Exception exception) {
                fail("Attempting to invoke close() for a consumer on a " +
                     "closed session threw exception=" + 
                     exception.getClass().getName() + ", message=" + 
                     exception.getMessage());
            }
        }

        try {
            // start the duplicate connection
            local.getConnection().start();

            // verify that the listener sent a message
            MessagingHelper.receive(receiver, count, behaviour.getTimeout());
        } finally {
            receiver.remove();
            local.getConnection().close();
        }
    }
    
} //-- ListenerCloseTest

