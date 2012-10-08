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
 * $Id: ConnectionStopStartTest.java,v 1.9 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessagingHelper;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the {@link Connection#stop} and {@link Connection#start} 
 * methods
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>connection.creation</li>
 *   <li>connection.start.started</li>
 *   <li>connection.stopped</li>
 *   <li>connection.stopped.send</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.9 $
 * @see AbstractSendReceiveTestCase
 */
public class ConnectionStopStartTest extends AbstractSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testStoppedOnCreation", "connection.creation", "connection.stopped",
         "connection.stopped.send"},
        {"testStop", "connection.stopped"},
        {"testStopForStoppedConnection", "connection.stop.stopped"},
        {"testStartForStartedConnection", "connection.start.started"}};

    /**
     * The destination to create prior to running the test
     */
    private static final String DESTINATION = "ConnectionStopStartTest";

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(ConnectionStopStartTest.class.getName());


    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against all session types, all
     * message delivery types, and using TextMessage messages
     *
     * @param name the name of test case
     */
    public ConnectionStopStartTest(String name) {
        super(name, MessageTypes.TEXT, REQUIREMENTS);
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
        return TestCreator.createSendReceiveTest(
            ConnectionStopStartTest.class);
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
     * Test that a connection does not receive any messages on creation.
     * This covers requirements:
     * <ul>
     *   <li>connection.creation</li>
     *   <li>connection.stopped</li>
     *   <li>connection.stopped.send</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testStoppedOnCreation() throws Exception {
        final int count = 10; // the number of messages to send
        TestContext context = getContext();

        Destination destination = getDestination(DESTINATION);
        MessagingHelper.send(context, destination, count);

        // verify that the receiver doesn't receive any messages
        MessageReceiver receiver = 
            SessionHelper.createReceiver(context, destination);
        try {
            MessagingHelper.receive(context, receiver, 0);
        } finally {
            receiver.remove();
        }
    }

    /**
     * Test that a connection doesn't receive any messages after starting a
     * connection, sending messages, and then stopping it.
     * This covers requirements:
     * <ul>
     *   <li>connection.stopped</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testStop() throws Exception {
        final int count = 10; // the number of messages to send
        TestContext context = getContext();
        Connection connection = context.getConnection();
        connection.start();

        Destination destination = getDestination(DESTINATION);
        MessageReceiver receiver = 
            SessionHelper.createReceiver(context, destination);

        try {
            // send count messages and stop the connection. 
            MessagingHelper.send(context, destination, count);
            connection.stop();

            // verify that the receiver doesn't receive any messages
            MessagingHelper.receive(context, receiver, 0);

            // restart the connection and verify that the receiver
            // receives messages
            connection.start();                
            MessagingHelper.receive(context, receiver, count);
        } finally {
            receiver.remove();
        }
    }

    /**
     * Verify that invoking {@link Connection#stop} for a stopped connection
     * has no effect. This covers requirements:
     * <ul>
     *   <li>connection.stop.stopped</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testStopForStoppedConnection() throws Exception {
        TestContext context = getContext();
        Connection connection = context.getConnection();
        try {
            // the connection should already be stopped
            connection.stop();
        } catch (Exception exception) { 
            String msg = "Invoking Connection.stop() on a stopped connection "
                + "should be ignored. Test context=" + context;
            _log.debug(msg, exception);
            fail(msg);
        } 
    }

    /**
     * Verify that invoking {@link Connection#start} for a started connection
     * has no effect. This covers requirements:
     * <ul>
     *   <li>connection.start.started</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testStartForStartedConnection() throws Exception {
        TestContext context = getContext();
        Connection connection = context.getConnection();
        connection.start();
        try {
            connection.start();
        } catch (Exception exception) { 
            String msg = "Invoking Connection.start() on a started connection "
                + "should be ignored";
            _log.debug(msg, exception);
            fail(msg);
        }
    }
    
} //-- ConnectionStopStartTest
