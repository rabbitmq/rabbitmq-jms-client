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
 * $Id: ConnectionTestRunner.java,v 1.5 2005/06/16 04:08:11 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Connection;

import org.apache.log4j.Logger;

import junit.framework.Test;
import junit.framework.TestResult;


/**
 * This class enables generic connection test cases to be run for each JMS
 * connection type.
 * <p>
 * Test cases must implement the {@link ConnectionTestCase} interface.
 *
 * @version     $Revision: 1.5 $ $Date: 2005/06/16 04:08:11 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         ConnectionTestCase
 * @see         ConnectionFactoryTestCase
 * @see         ProviderTestRunner
 * @see         TestRunner
 */
public class ConnectionTestRunner extends TestRunner
    implements ConnectionFactoryTestCase {

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ConnectionTestRunner.class.getName());


    /**
     * Construct an instance with the test case to run
     *
     * @param test the test case to run.
     */
    public ConnectionTestRunner(ConnectionTestCase test) {
        super(test);
    }

    /**
     * Return the connection factory types to run the test cases against
     *
     * @return the connection factory types to run the test cases against
     */
    @Override
    public ConnectionFactoryTypes getConnectionFactoryTypes() {
        return ((ConnectionTestCase) getTest()).getConnectionFactoryTypes();
    }

    /**
     * Runs a test case for the given connection.
     *
     * @param test the test case
     * @param result the instance to collect test results in
     */
    @Override
    protected void runTest(Test test, TestResult result) {
        Tester tester = new Tester(test, result, getContext(), getFilter());
        result.runProtected(test, tester);
    }

    /**
     * Helper class to run a test case
     */
    private class Tester extends TestInvoker {

        /**
         * Construct a new <code>Tester</code>
         *
         * @param test the test to invoke
         * @param result the instance to collect test results in
         * @param context the test context
         * @param filter the test filter
         */
        public Tester(Test test, TestResult result, TestContext context,
                      TestFilter filter) {
            super(test, result, context, filter);
        }

        /**
         * Set up the test case
         *
         * @param test the test case
         * @param context the test context
         * @throws Exception for any error
         */
        @Override
        protected void setUp(JMSTest test, TestContext context)
            throws Exception {

            ConnectionTestCase connectionTest = (ConnectionTestCase) test;
            Connection connection = null;

            String clientID = null; // "ConnectionTestRunner";
            connection = ConnectionFactoryHelper.createConnection(
                context, clientID);
            if (log.isDebugEnabled()) {
                log.debug("ConnectionTestRunner: running test=" + test
                          + " using connection type="
                          + connection.getClass().getName());
            }

            TestContext child = new TestContext(context, connection);
            setChildContext(child);
            connectionTest.setContext(child);

            if (connectionTest.startConnection()) {
                try {
                    connection.start();
                } catch (Exception exception) {
                    log.error(exception, exception);
                    throw exception;
                }
            }
        }

        /**
         * Cleans up after the test case has been run
         *
         * @param test the test case
         * @param context the test context
         * @throws Exception for any error
         */
        @Override
        protected void tearDown(JMSTest test, TestContext context)
            throws Exception {

            Connection connection = getChildContext().getConnection();

            if (log.isDebugEnabled()) {
                log.debug("ConnectionTestRunner: completed test=" + test
                          + " using connection type="
                          + connection.getClass().getName());
            }

            connection.close();
        }

    }

}
