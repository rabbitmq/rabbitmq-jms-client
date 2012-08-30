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
 * $Id: ReceiverTest.java,v 1.5 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.DelayedAction;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessagingHelper;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of stopping and closing a connection
 * while a listener is active. This covers requirements:
 * <ul>
 *   <li>connection.stop.receivers</li>
 *   <li>connection.close.receivers</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractSendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 */
public class ReceiverTest extends AbstractSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testConnectionStop", "connection.stop.receivers"},
        {"testConnectionClose", "connection.close.receivers"},
        {"testConnectionRestart", "connection.close.receivers"}};

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "ReceiverTest";

    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against all session types, synchronous delivery
     * types, and using TextMessage messages
     *
     * @param name the name of test case
     */
    public ReceiverTest(String name) {
        super(name, MessageTypes.TEXT, DeliveryTypes.SYNCHRONOUS, 
              REQUIREMENTS);
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
        return TestCreator.createSendReceiveTest(ReceiverTest.class, 
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
        return new String[]{DESTINATION};
    }
    
    /**
     * Verify that the receive timers for a stopped connection continue to 
     * advance, so receives may time out and return a null message while the 
     * connection is stopped. This covers requirements:
     * <ul>
     *   <li>connection.stop.receivers</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testConnectionStop() throws Exception {
        TestContext context = getContext();
        final Connection connection = context.getConnection();
        Destination destination = getDestination(DESTINATION);
        MessageReceiver receiver = SessionHelper.createReceiver(
            context, destination);

        DelayedAction action = new DelayedAction(1000) {
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
                // wait for a non-existent message
                List result = receiver.receive(1, 5000);
            } catch (Exception exception) {
                fail("Expected synchronous consumer to time out and return " +
                     "null when connection stopped, but threw exception=" +
                     exception.getClass().getName() + ", message=" +
                     exception.getMessage());
            }

            // receiver returned null. Verify that the connection was stopped
            Exception exception = action.getException();
            if (exception != null) {
                fail("Failed to stop connection, exception=" +
                     exception.getClass().getName() + ", message=" +
                     exception.getMessage());
            }
        } finally {
            try {
                receiver.remove();
            } catch (Exception exception) {
                fail("Attempting to invoke close() for a consumer on a " +
                     "stopped connection threw exception=" + 
                     exception.getClass().getName() + ", message=" + 
                     exception.getMessage());
            }
        }
    }

    /**
     * Verify that the receive timers for a closed connection continue to 
     * advance, so receives may time out and return a null message while the 
     * connection is closed. This covers requirements:
     * <ul>
     *   <li>connection.close.receivers</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testConnectionClose() throws Exception {
        TestContext context = getContext();
        final Connection connection = context.getConnection();
        Destination destination = getDestination(DESTINATION);
        final MessageReceiver receiver = SessionHelper.createReceiver(
            context, destination);
        final CompletionListener listener = new CompletionListener(2);

        DelayedAction receive = new DelayedAction(0) {
            public void runProtected() throws Exception {
                try {
                    receiver.receive(1, 0);
                } finally {
                    listener.notifyOnCompletion();
                }
            }
        };

        DelayedAction close = new DelayedAction(1000) {
            public void runProtected() throws Exception {
                try {
                    connection.close();
                } finally {
                    listener.notifyOnCompletion();
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
        listener.waitForCompletion(30000);

        if (listener.getCompleted() != 2) {
            // one or both of the threads didn't complete. Invalidate the
            // connection so that the test runner doesn't attempt to close it,
            // which could result in the suite to hang.
            context.invalidate();
            fail("Connection.close() didn't force consumer's receive() " +
                 "to be terminated and return null. Appears to have " +
                 "blocked");
        }

        if (receive.getException() != null) {
            context.invalidate();
            Exception exception = receive.getException();
            fail("Expected synchronous consumer's receive() to be " +
                 "terminated and return null when connection closed, " +
                 "but threw exception=" + exception.getClass().getName() +
                 ", message=" + exception.getMessage());
        }
        if (close.getException() != null) {
            context.invalidate();
            Exception exception = close.getException();
            fail("Closing connection while a synchronous receive() in " +
                 "progress threw exception=" + 
                 exception.getClass().getName() + 
                 ", message=" + exception.getMessage());
        }

        try {
            // Closing a consumer for a closed connection should be ignored
            receiver.remove();
        } catch (Exception exception) {
            fail("Attempting to invoke close() for a consumer on a " +
                 "closed connection threw exception=" + 
                 exception.getClass().getName() + ", message=" + 
                 exception.getMessage());
        }
    }

    /**
     * Verify that the receive timers for a stopped connection continue to 
     * advance, so that when the connection is stopped, a waiting receiver can
     * subsequently receive a message when the connection is started again
     * This covers requirements:
     * <ul>
     *   <li>connection.stop.receivers</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testConnectionRestart() throws Exception {
        final TestContext context = getContext();
        final Connection connection = context.getConnection();
        final Destination destination = getDestination(DESTINATION);

        MessageReceiver receiver = SessionHelper.createReceiver(
            context, destination);

        DelayedAction action = new DelayedAction(1000) {
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
                MessagingHelper.send(local, destination, 1);
            }
        };

        try {
            // start the connection
            connection.start();

            // restart the connection in a separate thread to allow the
            // receiver to invoke receive()
            action.start();

            try {
                // wait indefinitely for the message
                List result = receiver.receive(1, 5000);
                if (result == null) {
                    // No message was received. Verify that the connection was
                    // restarted successfully
                    Exception exception = action.getException();
                    if (exception != null) {
                        fail("Failure in restarting connection, exception=" +
                             exception.getClass().getName() + ", message=" +
                             exception.getMessage());
                    } else {
                        fail("Failed to receive any messages from the " +
                             "destination after the connection was restarted");
                    }
                }
            } catch (Exception exception) {
                fail("Expected synchronous consumer to return a message " +
                     "after connection was restarted, but threw exception=" + 
                     exception.getClass().getName() + ", message=" +
                     exception.getMessage());
            }
        } finally {
            receiver.remove();
        }
    }

    private static class CompletionListener {

        private final Object _lock = new Object();
        private int _count = 0;
        private int _expected;
        
        public CompletionListener(int expected) {
            _expected = expected;
        }

        public synchronized void notifyOnCompletion() {
            ++_count;
            synchronized (_lock) {
                _lock.notifyAll();
            }
        }

        public synchronized void waitForCompletion(long timeout) {
            if (_count != _expected) {
                synchronized (_lock) {
                    try {
                        _lock.wait(timeout);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }

        public synchronized int getCompleted() {
            return _count;
        }

    } //-- CompletionListener
    
} //-- ReceiverTest
