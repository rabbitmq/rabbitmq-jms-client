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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: JUnitTestRunner.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Enumeration;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.runner.BaseTestRunner;

import org.apache.log4j.Logger;


/**
 * JUnit test runner which logs a trace as the tests are executed,
 * followed by a summary at the end.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class JUnitTestRunner extends BaseTestRunner {

    /**
     * The summary logger
     */
    private static final Logger SUMMARY_LOG = Logger.getLogger(
        JUnitTestRunner.class.getName() + ".Summary");

    /**
     * The trace logger
     */
    private static final Logger TRACE_LOG = Logger.getLogger(
        JUnitTestRunner.class.getName() + ".Trace");

    /**
     * The detail logger
     */
    private static final Logger DETAIL_LOG = Logger.getLogger(
        JUnitTestRunner.class.getName() + ".Detail");


    /**
     * Constructs a <code>JUnitTestRunner</code>
     */
    public JUnitTestRunner() {
    }

    /**
     * Notifies the start of a test
     *
     * @param testName the name of the test
     */
    @Override
    public void testStarted(String testName) {
        // no-op
    }

    /**
     * Notifies the end of a test
     *
     * @param testName the name of the test
     */
    @Override
    public void testEnded(String testName) {
        // no-op
    }

    /**
     * Notifies the failure of a test
     *
     * @param status the status
     * @param test the test
     * @param error the error
     */
    @Override
    public void testFailed(int status, Test test, Throwable error) {
        // no-op
    }

    /**
     * Run a test suite
     *
     * @param suite the suite to run
     * @return the result of the run
     */
    public TestResult doRun(Test suite) {
        TestResult result = createTestResult();
        result.addListener(this);
        long startTime = System.currentTimeMillis();
        suite.run(result);
        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        SUMMARY_LOG.info("Time: " + elapsedTimeAsString(runTime));
        log(result);
        return result;
    }

    /**
     * Logs the start of a test
     *
     * @param test the test
     */
    @Override
    public void startTest(Test test) {
        TRACE_LOG.info("Running: " + test);
    }

    /**
     * Logs a test error
     *
     * @param test the test
     * @param error the error
     */
    @Override
    public void addError(Test test, Throwable error) {
        TRACE_LOG.info("Error: " + test);
    }

    /**
     * Logs a test failure
     *
     * @param test the test
     * @param error the error
     */
    @Override
    public void addFailure(Test test, AssertionFailedError error) {
        TRACE_LOG.info("Failed: " + test);
    }

    /**
     * Logs the result of the test
     *
     * @param result the test result
     */
    public synchronized void log(TestResult result) {
        logErrors(result);
        logFailures(result);
        logHeader(result);
    }

    /**
     * Logs any errors
     *
     * @param result the test result
     */
    public void logErrors(TestResult result) {
        if (result.errorCount() != 0) {
            if (result.errorCount() == 1) {
                DETAIL_LOG.error("There was " + result.errorCount()
                                 + " error:");
            } else {
                DETAIL_LOG.error("There were " + result.errorCount()
                                 + " errors:");
            }
            int i = 1;
            for (Enumeration<?> e = result.errors(); e.hasMoreElements(); i++) {
                TestFailure failure = (TestFailure) e.nextElement();
                DETAIL_LOG.error(i + ") " + failure.failedTest(),
                                 failure.thrownException());
            }
        }
    }

    /**
     * Log any failures
     *
     * @param result the test result
     */
    public void logFailures(TestResult result) {
        if (result.failureCount() != 0) {
            if (result.failureCount() == 1) {
                DETAIL_LOG.error("There was " + result.failureCount()
                                 + " failure:");
            } else {
                DETAIL_LOG.error("There were " + result.failureCount()
                                 + " failures:");
                int i = 1;
                for (Enumeration<?> e = result.failures(); e.hasMoreElements();
                     ++i) {
                    TestFailure failure = (TestFailure) e.nextElement();
                    DETAIL_LOG.error(i + ") " + failure.failedTest(),
                                     failure.thrownException());
                }
            }
        }
    }

    /**
     * Logs the header of the report
     *
     * @param result the test result
     */
    public void logHeader(TestResult result) {
        if (result.wasSuccessful()) {
            SUMMARY_LOG.info("OK (" + result.runCount() + " tests)");
        } else {
            SUMMARY_LOG.info("FAILURES!!!");
            SUMMARY_LOG.info("Tests run: " + result.runCount()
                             + ",  Failures: " + result.failureCount()
                             + ",  Errors: " + result.errorCount());
        }
    }

    /**
     * Runs a suite extracted from a TestCase subclass.
     *
     * @param testClass the class of the test case
     */
    public static void run(Class<?> testClass) {
        run(new TestSuite(testClass));
    }

    /**
     * Runs a suite
     *
     * @param suite the suite to run
     * @return result of test suite
     */
    public static TestResult run(Test suite) {
        JUnitTestRunner runner = new JUnitTestRunner();
        return runner.doRun(suite);
    }

    /**
     * Creates the TestResult to be used for the test run.
     *
     * @return a new <code>TestResult</code>
     */
    protected TestResult createTestResult() {
        return new TestResult();
    }

    /**
     * Notifies of overall test failure, and exits
     *
     * @param message the failure message
     */
    @Override
    protected void runFailed(String message) {
        DETAIL_LOG.error(message);
        System.exit(-1);
    }

}
