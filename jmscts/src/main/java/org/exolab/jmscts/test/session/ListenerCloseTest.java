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
 * $Id: ListenerCloseTest.java,v 1.9 2005/06/16 06:40:15 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.DelayedAction;
import org.exolab.jmscts.core.EchoListener;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.WaitingListener;


/**
 * This class tests the behaviour of stopping and closing a connection
 * while a listener is active.
 *
 * NOTE: Temporary destinations cannot be used as a separate connection is
 * required to verify that the test is successful.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.9 $
 * @jmscts.session all
 * @jmscts.message TextMessage
 * @jmscts.delivery NON_PERSISTENT, administered, asynchronous
 * @jmscts.delivery PERSISTENT, administered, asynchronous
 */
public class ListenerCloseTest extends AbstractSendReceiveTestCase {

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
    private static final String[] DESTINATIONS = {REQUEST, REPLY};

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ListenerCloseTest.class);


    /**
     * Construct a new <code>ListenerCloseTest</code>
     *
     * @param name the name of test case
     */
    public ListenerCloseTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(ListenerCloseTest.class);
    }

    /**
     * Returns if this test can share resources with other test cases.
     * This implementation always returns <code>false</code>, to
     * ensure that a new connection is created for each test.
     *
     * @return <code>false</code>
     */
    @Override
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
    @Override
    public boolean startConnection() {
        return false;
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Verify that running MessageListeners have full access to session if
     * the session is closed.
     *
     * @jmscts.requirement session.close.listeners
     * @throws Exception for any error
     */
    public void testSessionClose() throws Exception {
        final int count = 10; // the number of messages to send
        final long delayTime = 500; // 500 ms
        final int maxWaitTime = 5000; // 5000 ms
        TestContext context = getContext();
        Session session = context.getSession();

        // set up a duplicate context, to enable a receiver to be created
        // for the destination that the listener replies to
        TestContext local = TestContextHelper.createSendReceiveContext(
            context);
        MessageReceiver receiver = createReceiver(local, REPLY);
        MessageConsumer consumer = null;

        try {
            // set up the listener
            consumer = createConsumer(context, REQUEST);
            MessageSender sender = createSender(context, REPLY);
            EchoListener replier = new EchoListener(context.getSession(),
                                                    sender, count, true);
            final WaitingListener listener = new WaitingListener(replier, 1);
            consumer.setMessageListener(listener);

            // send a message and start the connection
            send(REQUEST, 1);
            context.getConnection().start();

            // wait for the listener to start processing before closing
            // the session
            log.debug("waiting for listener");
            if (!listener.waitForReceipt(maxWaitTime)) {
                fail("No message was received within " + maxWaitTime
                     + " milliseconds");
            }

            // notify the listener in a separate thread that it may proceed
            // with processing. The notification has to occur in a separate
            // thread else close would block indefinitely waiting for the
            // listener to complete.
            // The half second delay is to help ensure that close is invoked
            // before the listener begins processing the message
            log.debug("notifying listener");
            DelayedAction action = new DelayedAction(delayTime) {
                @Override
                public void runProtected() {
                    listener.notifyContinue();
                }
            };
            action.start();
            session.close();
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw exception;
        } finally {
            try {
                consumer.close();
            } catch (Exception exception) {
                fail("Attempting to invoke close() for a consumer on a "
                     + "closed session threw exception="
                     + exception.getClass().getName() + ", message="
                     + exception.getMessage());
            }
        }

        try {
            // start the duplicate connection
            local.getConnection().start();

            // verify that the listener sent a message
            receive(receiver, count);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            throw exception;
        } finally {
            receiver.remove();
            local.getConnection().close();
        }
    }

}
