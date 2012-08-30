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
 * $Id: TestRunner.java,v 1.4 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.ArrayList;
import java.util.Enumeration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;


/**
 * This is the base class for test case runners.
 *
 * @version     $Revision: 1.4 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public abstract class TestRunner extends TestSetup implements JMSTest {

    /**
     * The context to test against
     */
    private TestContext _context = null;

    /**
     * The child context
     */
    private TestContext _child = null;

    /**
     * The test case filter, if any
     */
    private TestFilter _filter = null;

    /**
     * Construct an instance with the test case to run.
     *
     * @param test the test case to run.
     */
    public TestRunner(Test test) {
        super(test);
    }

    /**
     * Set the context to test against
     *
     * @param context the test context
     */
    @Override
    public void setContext(TestContext context) {
        _context = context;
    }

    /**
     * Return the context to test against
     *
     * @return the test context
     */
    @Override
    public TestContext getContext() {
        return _context;
    }

    /**
     * Set the test case filter
     *
     * @param filter the test case filter
     */
    public void setFilter(TestFilter filter) {
        _filter = filter;
    }

    /**
     * Return the test case filter
     *
     * @return the test case filter
     */
    public TestFilter getFilter() {
        return _filter;
    }

    /**
     * Returns if this test can share resources with other test cases.
     * This implementation always returns <code>true</code>
     *
     * @return <code>true</code>
     */
    @Override
    public boolean share() {
        return true;
    }

    /**
     * Runs the test case, by invoking {@link #runTest}. If {@link #getTest}
     * returns a TestSuite, then {@link #runTest} will be invoked for each
     * contained test.
     *
     * @param result the instance to collect results in
     */
    @Override
    public void basicRun(TestResult result) {
        Test[] tests = getTests();
        for (int i = 0; i < tests.length && !result.shouldStop(); ++i) {
            runTest(tests[i], result);
        }
    }

    /**
     * Run a test case
     *
     * @param test the test case to run
     * @param result the instance to collect results in
     */
    protected abstract void runTest(Test test, TestResult result);

    /**
     * Sets the test context of child test cases
     *
     * @param context the child test case context
     */
    protected void setChildContext(TestContext context) {
        _child = context;
    }

    /**
     * Returns the test context of child test cases
     *
     * @return the child test case context
     */
    protected TestContext getChildContext() {
        return _child;
    }

    /**
     * Cleans up after the test case has been run
     *
     * @throws Exception for any error
     */
    @Override
    protected void tearDown() throws Exception {
        _context = null;
    }

    /**
     * Helper method to check that a test case or suite implements the
     * appropriate interface.
     *
     * @param type the interface that the test must implement
     * @throws Exception if the test doesn't implement the required interface
     */
    protected void checkImplements(Class<?> type) throws Exception {
        Test[] tests = getTests();
        for (int i = 0; i < tests.length; ++i) {
            Test test = tests[i];
            if (!type.isAssignableFrom(test.getClass())) {
                throw new Exception("Test " + test + " doesn't implement "
                                    + type.getName());
            }
        }
    }

    /**
     * Returns the test(s) as an array. If the test is a {@link TestSuite},
     * the contained tests will be extracted.
     *
     * @return an array of the contained tests
     */
    @SuppressWarnings("unchecked")
    protected Test[] getTests() {
        Test test = getTest();
        ArrayList<Test> result = new ArrayList<Test>();

        if (test instanceof TestSuite) {
            Enumeration<Test> iter = ((TestSuite) test).tests();
            while (iter.hasMoreElements()) {
                result.add(iter.nextElement());
            }
        } else {
            result.add(test);
        }
        return result.toArray(new Test[]{});
    }

}
