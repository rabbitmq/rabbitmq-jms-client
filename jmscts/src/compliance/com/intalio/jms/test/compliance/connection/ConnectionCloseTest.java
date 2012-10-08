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
 * $Id: ConnectionCloseTest.java,v 1.6 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractConnectionTestCase;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MethodInvoker;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of {@link Connection#close}
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>connection.close</li>
 *   <li>connection.close.closed</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractConnectionTestCase
 */
public class ConnectionCloseTest extends AbstractConnectionTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testExceptionOnClose", "connection.close"},
        {"testCloseForClosedConnection", "connection.close.closed"}};

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(ConnectionCloseTest.class.getName());


    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against all connection types.
     *
     * @param name the name of test case
     */
    public ConnectionCloseTest(String name) {
        super(name, REQUIREMENTS);
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
        return TestCreator.createConnectionTest(ConnectionCloseTest.class);
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
     * Test that a IllegalStateException is thrown for a closed
     * connection. This covers requirements:
     * <ul>
     *   <li>connection.method.close</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testExceptionOnClose() throws Exception {
        TestContext context = getContext();
        Connection connection = context.getConnection();
        connection.close();        

        ExceptionListener dummyListener = new ExceptionListener() {
            public void onException(JMSException exception) {
            }
        };

        MethodInvoker invoker = new MethodInvoker(IllegalStateException.class);
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
     * Test that closing a closed connection has no effect. 
     * This covers requirements:
     * <ul>
     *   <li>connection.close.closed</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testCloseForClosedConnection() throws Exception {
        TestContext context = getContext();
        Connection connection = context.getConnection();
        connection.close();

        try {
            connection.close();        
        } catch (Exception exception) {
            String msg = "Closing a closed connection shouldn't generate an " +
                "exception";
            _log.debug(msg, exception);
            fail(msg);
        }
    }
    
} //-- ConnectionCloseTest
