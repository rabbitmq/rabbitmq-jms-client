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
 * $Id: ReceiverTest.java,v 1.11 2004/02/03 07:32:07 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import java.util.List;

import javax.jms.Connection;
import javax.jms.Session;

import junit.framework.Test;

import org.apache.log4j.Logger;
import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.CompletionListener;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.DelayedAction;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.ThreadedActions;
import org.exolab.jmscts.core.ThreadedReceiver;


/**
 * This class tests the behaviour of stopping and closing a connection
 * while a receiver is active.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.11 $
 * @see AbstractSendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 * @jmscts.session all
 * @jmscts.message TextMessage
 * @jmscts.delivery synchronous
 */
public class ReceiverTest extends AbstractSendReceiveTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "ReceiverTest";

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ReceiverTest.class);


    /**
     * Construct a new <code>ReceiverTest</code>
     *
     * @param name the name of test case
     */
    public ReceiverTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(ReceiverTest.class);
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
        return new String[]{DESTINATION};
    }

    /**
     * Verifies that the receive timers for a stopped connection continue to
     * advance, so receives may time out and return a null message while the
     * connection is stopped.
     *
     * @jmscts.requirement connection.stop.receivers
     * @throws Exception for any error
     */
    public void testConnectionStop() throws Exception {
        final int delayTime = 500; // 500 ms
        final int receiptTime = 3000; // 3 secs
        TestContext context = getContext();
        final Connection connection = context.getConnection();
        MessageReceiver receiver = createReceiver(DESTINATION);

        DelayedAction action = new DelayedAction(delayTime) {
            @Override
            public void runProtected() throws Exception {
                connection.stop();
            }
        };

        try {
            // start the connection
            connection.start();

            // stop the connection in a separate thread to allow the receiver
            // to invoke receive()
            action.start();

            try {
                receiver.receive(0, receiptTime);
            } catch (Exception exception) {
                fail("Expected synchronous consumer to time out and return "
                     + "null when connection stopped, but threw exception="
                     + exception.getClass().getName() + ", message="
                     + exception.getMessage());
            }

            // receiver returned null. Verify that the connection was stopped
            Exception exception = action.getException();
            if (exception != null) {
                fail("Failed to stop connection, exception="
                     + exception.getClass().getName() + ", message="
                     + exception.getMessage());
            }
        } finally {
            try {
                receiver.remove();
            } catch (Exception exception) {
                fail("Attempting to invoke close() for a consumer on a "
                     + "stopped connection threw exception="
                     + exception.getClass().getName() + ", message="
                     + exception.getMessage());
            }
        }
    }

    /**
     * Verifies that the receive timers for a closed connection continue to
     * advance, so receives may time out and return a null message as the
     * connection is closed.
     *
     * @jmscts.requirement connection.close.receivers
     * @throws Exception for any error
     */
    public void testConnectionClose() throws Exception {
        final int delayTime = 1000; // 1 sec
        final int completionTime = 30000; // 30 secs
        TestContext context = getContext();
        final Connection connection = context.getConnection();
        final MessageReceiver receiver = createReceiver(DESTINATION);
        CompletionListener listener = new CompletionListener(2);

        DelayedAction receive = new DelayedAction(0, listener) {
            @Override
            public void runProtected() throws Exception {
                try {
                    log.debug("Receiver starting");
                    receiver.receive(0, 0);
                } finally {
                    log.debug("Receiver terminating");
                }
            }
        };

        DelayedAction close = new DelayedAction(delayTime, listener) {
            @Override
            public void runProtected() throws Exception {
                try {
                    log.debug("Connection closing");
                    connection.close();
                } finally {
                    log.debug("Connection closed");
                }
            }
        };

        // start the connection
        connection.start();

        // invoke the receive in a separate thread
        receive.start();

        // close the connection in a separate thread to allow the receiver
        // to invoke receive()
        close.start();

        // wait for up to 30 seconds for the two threads to complete
        listener.waitForCompletion(completionTime);
        int completed = listener.getCompleted();
        if (completed != 2) {
            // one or both of the threads didn't complete. Invalidate the
            // connection so that the test runner doesn't attempt to close it,
            // which could result in the suite to hang.
            context.invalidate();
            fail("Connection.close() didn't force consumer's receive() "
                 + "to be terminated and return null. Appears to have "
                 + "blocked");
        }

        if (receive.getException() != null) {
            context.invalidate();
            Exception exception = receive.getException();
            fail("Expected synchronous consumer's receive() to be "
                 + "terminated and return null when connection closed, "
                 + "but threw exception=" + exception.getClass().getName()
                 + ", message=" + exception.getMessage());
        }
        if (close.getException() != null) {
            context.invalidate();
            Exception exception = close.getException();
            fail("Closing connection while a synchronous receive() in "
                 + "progress threw exception=" + exception.getClass().getName()
                 + ", message=" + exception.getMessage());
        }

        try {
            // Closing a consumer for a closed connection should be ignored
            receiver.close();
        } catch (Exception exception) {
            fail("Attempting to invoke close() for a consumer on a "
                 + "closed connection threw exception="
                 + exception.getClass().getName() + ", message="
                 + exception.getMessage());
        }
    }

    /**
     * Verifies that the receive timers for a stopped connection continue to
     * advance, so that when the connection is stopped, a waiting receiver can
     * subsequently receive a message when the connection is started again
     *
     * @jmscts.requirement connection.stop.receivers
     * @throws Exception for any error
     */
    public void testConnectionRestart() throws Exception {
        final int delayTime = 1000; // 1 sec
        final int completionTime = 30000; // 30 secs
        final TestContext context = getContext();
        final Connection connection = context.getConnection();
        final MessageReceiver receiver = createReceiver(DESTINATION);

        // receiver which waits indefinitely to receive 1 message
        ThreadedReceiver receive = new ThreadedReceiver(receiver, 1, 0);

        // action which restarts the connection and sends a message,
        // to be received by the receive thread
        DelayedAction restart = new DelayedAction(delayTime) {
            @Override
            public void runProtected() throws Exception {
                connection.stop();
                connection.start();
                // create a context with a new session to avoid synchronization
                // issues
                Session session = ConnectionHelper.createSession(context);
                TestContext tmp = new TestContext(context, session,
                                                  context.getAckType());
                TestContext local = new TestContext(
                    tmp, context.getMessage(),
                    context.getMessagingBehaviour());
                send(local, DESTINATION, 1);
            }
        };

        ThreadedActions actions = new ThreadedActions();
        actions.addAction(receive);
        actions.addAction(restart);

        // start the connection
        connection.start();

        // start the two actions, and wait for them to complete
        actions.start();
        boolean completed = actions.waitForCompletion(completionTime);

        if (!completed) {
            // one or both of the actions didn't complete. Invalidate the
            // connection so that the test runner doesn't attempt to close it,
            // which could result in the suite to hang.
            context.invalidate();
            fail("Receiver didn't return a message after connection restart. "
                 + "Appears to have blocked");
        }

        // make sure both actions completed successfully
        if (receive.getException() != null) {
            context.invalidate();
            Exception exception = receive.getException();
            fail("Expected synchronous consumer to return a message "
                 + "after connection was restarted, but threw exception="
                 + exception.getClass().getName() + ", message="
                 + exception.getMessage());
        }

        if (restart.getException() != null) {
            context.invalidate();
            Exception exception = receive.getException();
            fail("Failure in restarting connection, exception="
                 + exception.getClass().getName() + ", message="
                 + exception.getMessage());
        }

        List<?> messages = receive.getMessages();
        if (messages == null) {
            // No message was received. Verify that the connection was
            // restarted successfully
            fail("Failed to receive any messages from the "
                 + "destination after the connection was restarted");
        }

        receiver.remove();
    }

}
