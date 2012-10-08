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
 * $Id: ConnectionCloseTest.java,v 1.5 2004/02/03 07:32:07 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import junit.framework.Test;

import org.apache.log4j.Logger;
import org.exolab.jmscts.core.AbstractConnectionTestCase;
import org.exolab.jmscts.core.MethodInvoker;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of <code>Connection.close</code>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractConnectionTestCase
 */
public class ConnectionCloseTest extends AbstractConnectionTestCase {

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ConnectionCloseTest.class);


    /**
     * Create a new <code>ConnectionCloseTest</code>.
     * The test will be run against all connection types.
     *
     * @param name the name of test case
     */
    public ConnectionCloseTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createConnectionTest(ConnectionCloseTest.class);
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
     * Verifies that a JMSException is thrown for any Connection
     * method (except Connection.close()), if the connection has been closed
     *
     * @jmscts.requirement connection.close
     * @throws Exception for any error
     */
    public void testExceptionOnClose() throws Exception { 
        TestContext context = getContext();
        Connection connection = context.getConnection();
        MethodInvoker invoker = new MethodInvoker(
            context.getConnectionType(), javax.jms.IllegalStateException.class);

        ExceptionListener dummyListener = new ExceptionListener() {
            @Override
            public void onException(JMSException exception) {
            }
        };

        connection.close();

        invoker.invoke(connection, "getClientID");
        invoker.invoke(connection, "getExceptionListener");
        invoker.invoke(connection, "getMetaData");
        invoker.invoke(connection, "setClientID", "dummyID");
        invoker.invoke(connection, "setExceptionListener", dummyListener);
        invoker.invoke(connection, "start");
        invoker.invoke(connection, "stop");

        Object[] args = new Object[]{Boolean.TRUE, new Integer(0)};
        if (context.isQueueConnectionFactory()) {
            invoker.invoke(connection, "createQueueSession", args);
        } else if (context.isTopicConnectionFactory()) {
            invoker.invoke(connection, "createTopicSession", args);
        } else if (context.isXAQueueConnectionFactory()) {
            invoker.invoke(connection, "createQueueSession", args);
            invoker.invoke(connection, "createXAQueueSession");
        } else {
            invoker.invoke(connection, "createTopicSession", args);
            invoker.invoke(connection, "createXATopicSession");
        }
    }

    /**
     * Verifies that closing a closed connection does not produce an
     * exception.
     *
     * @jmscts.requirement connection.close.closed
     * @throws Exception for any error
     */
    public void testCloseForClosedConnection() throws Exception {
        TestContext context = getContext();
        Connection connection = context.getConnection();
        connection.close();

        try {
            connection.close();
        } catch (Exception exception) {
            String msg = "Closing a closed connection shouldn't generate an "
                + "exception";
            log.debug(msg, exception);
            fail(msg);
        }
    }

}
