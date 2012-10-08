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
 * $Id: SendReceiveStopTest.java,v 1.4 2004/02/03 07:32:07 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import javax.jms.Connection;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of <code>Connection.stop</code> and
 * <code>Connection.start</code>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @see AbstractSendReceiveTestCase
 * @jmscts.message TextMessage
 * @jmscts.delivery consumer
 */
public class SendReceiveStopTest extends AbstractSendReceiveTestCase {

    /**
     * The destination to create prior to running the test
     */
    private static final String DESTINATION = "SendReceiveStopTest";

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log =
        Logger.getLogger(SendReceiveStopTest.class);


    /**
     * Construct a new <code>SendReceiveStopTest</code>
     *
     * @param name the name of test case
     */
    public SendReceiveStopTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(SendReceiveStopTest.class);
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
     * Verifies that a connection does not receive any messages on creation.
     *
     * @jmscts.requirement connection.creation
     * @jmscts.requirement connection.stopped
     * @jmscts.requirement connection.stopped.send
     * @throws Exception for any error
     */
    public void testStoppedOnCreation() throws Exception {
        final int count = 10; // the number of messages to send

        MessageReceiver receiver = createReceiver(DESTINATION);

        send(DESTINATION, count);

        try {
            // verify that the receiver doesn't receive any messages
            receive(receiver, 0);
        } finally {
            receiver.remove();
        }
    }

    static int testStopRun = 1;
    /**
     * Verifies that a connection doesn't receive any messages after starting a
     * connection, sending messages, and then stopping it.
     *
     * @jmscts.requirement connection.stopped
     * @throws Exception for any error
     */
    public void testStop() throws Exception {
        //System.out.println("Running testStop:"+(testStopRun++));
        final int count = 10; // the number of messages to send
        TestContext context = getContext();
        Connection connection = context.getConnection();
        connection.start();

        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            // send count messages and stop the connection.
            send(DESTINATION, count);
            connection.stop();

            // verify that the receiver doesn't receive any messages
            receive(receiver, 0);

            // restart the connection and verify that the receiver
            // receives messages
            connection.start();
            receive(receiver, count);
        } finally {
            receiver.remove();
        }
    }

}
