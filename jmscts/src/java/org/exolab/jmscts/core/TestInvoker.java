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
 *    please contact tma@netspace.net.au
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
 * $Id: TestInvoker.java,v 1.4 2005/06/16 08:10:52 tanderson Exp $
 */
package org.exolab.jmscts.core;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;


/**
 * Helper class to run a test case, for the given message type,
 * delivery mode, messaging mode, and destination type.
 * <p>
 * Prior to running, it creates a new message of
 * the specified type, and creates any destinations required by the test.
 *
 * @version     $Revision: 1.4 $ $Date: 2005/06/16 08:10:52 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
class TestInvoker implements Protectable {

    /**
     * The test case to run
     */
    private final Test _test;

    /**
     * The instance to collect test results in
     */
    private final TestResult _result;

    /**
     * The test context
     */
    private final TestContext _context;

    /**
     * The test filter. May be <code>null</code>.
     */
    private final TestFilter _filter;

    /**
     * Connection context allocated for this test. May be <code>null</code>
     */
    private TestContext _connection;

    /**
     * Session context allocated for this test. May be <code>null</code>
     */
    private TestContext _session;


    /**
     * Construct a new <code>TestInvoker</code>
     *
     * @param test the test to run. Must be an instance of <code>JmsTest</code>
     * @param result the result of the test
     * @param context the test context
     * @param filter the test filter. May be <code>null</code>
     */
    public TestInvoker(Test test, TestResult result, TestContext context,
                       TestFilter filter) {
        if (test == null) {
            throw new IllegalArgumentException("Argument 'test' is null");
        }
        if (!(test instanceof JMSTest)) {
            throw new IllegalArgumentException(
                "Argument test must implement JMSTest");
        }
        if (result == null) {
            throw new IllegalArgumentException("Argument 'result' is null");
        }
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }

        _test = test;
        _result = result;
        _context = context;
        _filter = filter;
    }

    /**
     * Run the test
     *
     * @throws Exception for any error
     */
    @Override
    public void protect() throws Exception {
        setUp();
        _test.run(_result);
        tearDown();
    }

    /**
     * Returns the test
     *
     * @return the test
     */
    public Test getTest() {
        return _test;
    }

    /**
     * Returns the test context
     *
     * @return the test context
     */
    public TestContext getContext() {
        return _context;
    }

    /**
     * Sets up the test. If required, this allocates a new test context,
     * before invoking {@link #setUp(JMSTest test, TestContext context)}
     *
     * @throws Exception for any error
     */
    protected void setUp() throws Exception {
        JMSTest test = (JMSTest) _test;
        TestContext context = _context;

        if (test instanceof TestRunner) {
            ((TestRunner) test).setFilter(_filter);
        }

        if (!test.share()) {
            // test case cannot share resources, so allocate a new set
            if (_context.getSession() != null) {
                _session = TestContextHelper.createSessionContext(_context);
                _connection = _session.getParent();
                context = _session;
            } else if (context.getConnection() != null) {
                _connection = TestContextHelper.createConnectionContext(
                    _context);
                context = _connection;
            }
        }
        setUp(test, context);
    }

    /**
     * Setup the test
     *
     * @param test the test
     * @param context the test context
     * @throws Exception for any error
     */
    protected void setUp(JMSTest test, TestContext context) throws Exception {
        // default implementation does nothing
    }

    /**
     * Tears down the test. This invokes
     * {@link #tearDown(JMSTest test, TestContext context)}, before
     * destroying any allocated contexts
     *
     * @throws Exception for any error
     */
    protected void tearDown() throws Exception {
        JMSTest test = (JMSTest) _test;
        TestContext context = _context;
        if (_session != null) {
            context = _session;
        } else if (_connection != null) {
            context = _connection;
        }
        try {
            tearDown(test, context);
        } finally {
            // if new contexts were allocated, close them
            if (_session != null) {
                _session.close();
                _session = null;
            }
            if (_connection != null) {
                _connection.close();
                _connection = null;
            }
        }
    }

    /**
     * Tear down the test
     *
     * @param test the test
     * @param context the test context
     * @throws Exception for any error
     */
    protected void tearDown(JMSTest test, TestContext context)
        throws Exception {
        // default implementation does nothing
    }

}
