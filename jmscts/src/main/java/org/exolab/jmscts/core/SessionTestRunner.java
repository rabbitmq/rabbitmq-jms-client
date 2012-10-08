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
 * $Id: SessionTestRunner.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.JMSException;
import javax.jms.Session;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.log4j.Logger;


/**
 * This class enables generic session test cases to be run for each JMS
 * session type.
 * <p>
 * Test cases must implement the {@link SessionTestCase} interface.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         SessionTestCase
 * @see         ConnectionTestCase
 * @see         ConnectionTestRunner
 */
public class SessionTestRunner extends TestRunner
    implements ConnectionTestCase {

    /**
     * The logger
     */
    private static final Logger _log =
        Logger.getLogger(SessionTestRunner.class);


    /**
     * Construct an instance with the test case to run.
     *
     * @param test the test case to run
     */
    public SessionTestRunner(SessionTestCase test) {
        super(test);
    }

    /**
     * Get the connection factory types to test against
     *
     * @return the connection factory types to test against
     */
    @Override
    public ConnectionFactoryTypes getConnectionFactoryTypes() {
        return ((SessionTestCase) getTest()).getConnectionFactoryTypes();
    }

    /**
     * Returns true if the connection should be started prior to running the
     * test.
     *
     * @return true if the connection should be started, false if it should be
     * stopped
     */
    @Override
    public boolean startConnection() {
        return ((SessionTestCase) getTest()).startConnection();
    }

    /**
     * Returns true if the client identifier should be set on the connection.
     * This is only applicable for TopicConnection instances, and will be
     * ignored for QueueConnection instances.
     *
     * @return true if an identifier should be allocated to the connection
     */
    @Override
    public boolean setClientID() {
        return ((SessionTestCase) getTest()).setClientID();
    }

    /**
     * Runs the test case against sessions for each of the acknowledgement
     * types specified by the test.
     *
     * @param test the test case to run
     * @param result the instance to collect results in
     */
    @Override
    protected void runTest(Test test, TestResult result) {
        SessionTestCase sessionTest = (SessionTestCase) test;
        AckType[] types = sessionTest.getAckTypes().getTypes();
        TestContext context = getContext();
        TestFilter filter = getFilter();

        if (sessionTest instanceof TestRunner) {
            ((TestRunner) sessionTest).setFilter(filter);
        }

        for (int i = 0; i < types.length && !result.shouldStop(); ++i) {
            if (filter == null || filter.includes(context, types[i], test)) {
                result.runProtected(test, new Tester(test, result, types[i]));
            }
        }
    }

    /**
     * Creates the session, prior to running the test case
     *
     * @throws Exception if the session cannot be created, or the test case
     * does not implement the SessionTestCase interface
     */
    @Override
    protected void setUp() throws Exception {
        checkImplements(SessionTestCase.class);
    }

    /**
     * Helper class to run a test case
     */
    private class Tester implements Protectable {

        /**
         * The test case to run
         */
        private Test _test = null;

        /**
         * The instance to collect test results in
         */
        private TestResult _result = null;

        /**
         * The message acknowledgement type
         */
        private AckType _type = null;

        /**
         * Construct a new <code>Tester</code>
         *
         * @param test the test
         * @param result the test result
         * @param type the session acknowledgement type
         */
        public Tester(Test test, TestResult result, AckType type) {
            _test = test;
            _result = result;
            _type = type;
        }

        /**
         * Run the test
         *
         * @throws Exception for any error
         */
        @Override
        public void protect() throws Exception {
            SessionTestCase test = (SessionTestCase) _test;
            TestContext context = getContext();
            TestContext parent = context;

            if (!test.share()) {
                // test case cannot share resources, so allocate a new
                // connection
                parent = TestContextHelper.createConnectionContext(context);
            }
            Session session = null;

            try {
                session = ConnectionHelper.createSession(parent, _type);
            } catch (Exception exception) {
                _log.error(exception, exception);
                throw exception;
            }

            String msg = null;
            if (_log.isDebugEnabled()) {
                msg = "test=" + test + " using session type="
                    + session.getClass().getName() + ", behaviour=" + _type;
                _log.debug("running " + msg);
            }
            TestContext child = new TestContext(parent, session, _type);
            test.setContext(child);

            if (test.startConnection()) {
                child.getConnection().start();
            } else {
                child.getConnection().stop();
            }
            _test.run(_result);

            if (_log.isDebugEnabled()) {
                _log.debug("completed " + msg);
            }

            try {
                child.close();
            } catch (JMSException exception) {
                _log.error(exception, exception);
                throw exception;
            }

            if (parent != context) {
                // if a new connection was allocated, close it
                try {
                    parent.close();
                } catch (Exception exception) {
                    _log.error(exception, exception);
                    throw exception;
                }
            }
        }

    }

}
