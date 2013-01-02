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
 * $Id: TestCoverage.java,v 1.6 2005/06/16 06:23:29 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.apache.log4j.Logger;
import org.exolab.jmscts.report.Context;
import org.exolab.jmscts.report.Coverage;
import org.exolab.jmscts.report.CurrentTest;
import org.exolab.jmscts.report.Failure;
import org.exolab.jmscts.report.ReportHelper;
import org.exolab.jmscts.report.RequirementCoverage;
import org.exolab.jmscts.report.TestRun;
import org.exolab.jmscts.report.TestRuns;
import org.exolab.jmscts.requirements.Requirement;
import org.exolab.jmscts.requirements.Requirements;


/**
 * This class maintains the state of the test suite's coverage of requirements
 *
 * @version     $Revision: 1.6 $ $Date: 2005/06/16 06:23:29 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         Coverage
 * @see         Requirements
 */
public class TestCoverage {

    /**
     * A map of requirement id's to {@link Coverage} instances
     */
    private Map<String, Serializable> _coverage = new HashMap<String, Serializable>();

    /**
     * A map of test names to their corresponding TestRuns instances
     */
    private Map<String, Serializable> _testRuns = new HashMap<String, Serializable>();

    /**
     * A list of failures not associated with any test case
     */
    private List<Serializable> _failures = new ArrayList<Serializable>();

    /**
     * The requirements
     */
    private Requirements _requirements;

    /**
     * The current executing test case
     */
    private JMSTestCase _current;

    /**
     * The current test run
     */
    private TestRun _currentRun;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(TestCoverage.class);


    /**
     * Construct an instance with the set of requirements
     *
     * @param requirements the requirements
     */
    public TestCoverage(Requirements requirements) {
        if (requirements == null) {
            throw new IllegalArgumentException(
                "Argument 'requirements' is null");
        }
        _requirements = requirements;
    }

    /**
     * Log the start of a test.</br>
     * NOTE: {@link #begin} may not be invoked if the test setup fails. In
     * this instance, only {@link #failed} will be invoked
     *
     * @param test the test case
     */
    public synchronized void begin(JMSTestCase test) {
        String[] requirements = test.getRequirements(test.getName());
        if (requirements == null || requirements.length == 0) {
            log.error("Test=" + ReportHelper.getName(test) + " started but "
                      + "is not associated with any requirements");
        } else {
            _current = test;
            _currentRun = getTest(test, test.getContext());
            for (int i = 0; i < requirements.length; ++i) {
                addCoverage(requirements[i], test);
            }
        }
    }

    /**
     * Log the end of a test
     *
     * @param test the test that has completed
     */
    public synchronized void end(JMSTestCase test) {
        _current = null;
        _currentRun = null;
    }

    /**
     * Log a test case failure
     *
     * @param test the test case
     * @param cause the reason for the failure
     * @param rootCause the root cause of the failure. May be null
     */
    public synchronized void failed(Test test, Throwable cause,
                                    Throwable rootCause) {
        if (test instanceof JMSTestCase) {
            addFailure((JMSTestCase) test, cause, rootCause);
        } else if (test instanceof TestRunner) {
            addFailure((TestRunner) test, cause, rootCause);
        } else {
            addFailure(cause, rootCause);
        }
    }

    /**
     * Set a requirement as being unsupported
     *
     * @param requirementId the requirement identifier
     */
    public synchronized void setUnsupported(String requirementId) {
        Coverage coverage = (Coverage) _coverage.get(requirementId);
        if (coverage == null) {
            log.error("Invalid requirement identifier=" + requirementId);
        } else {
            coverage.setSupported(false);
        }
    }

    /**
     * Returns the coverage of requirements by the test suite
     *
     * @return the coverage of requirements by the test suite
     */
    public synchronized RequirementCoverage getCoverage() {
        RequirementCoverage result = new RequirementCoverage();

        if (_current != null) {
            // if a test case is currently executing, return its state
            CurrentTest current = new CurrentTest();
            current.setTest(ReportHelper.getName(_current));
            current.setTestRun(_currentRun);
            String[] requirements = _current.getRequirements(
                _current.getName());
            for (int i = 0; i < requirements.length; ++i) {
                Requirement requirement = _requirements.getRequirement(
                    requirements[i]);
                if (requirement != null) {
                    current.addRequirementId(requirements[i]);
                }
            }
            result.setCurrentTest(current);
        }

        HashSet<String> covered = new HashSet<String>();
        HashSet<Serializable> requirementIds = new HashSet<Serializable>(
            _requirements.getRequirements().keySet());

        // add the requirements covered by the test suite
        Iterator<Serializable> iter = _coverage.values().iterator();
        while (iter.hasNext()) {
            Coverage coverage = (Coverage) iter.next();
            result.addCoverage(coverage);
            covered.add(coverage.getRequirementId());
        }

        // add the list of all requirements not covered by the test suite
        requirementIds.removeAll(covered);
        iter = requirementIds.iterator();
        while (iter.hasNext()) {
            String requirementId = (String) iter.next();
            Coverage coverage = createCoverage(requirementId);
            result.addCoverage(coverage);
        }

        // add the test details
        iter = _testRuns.values().iterator();
        while (iter.hasNext()) {
            TestRuns runs = (TestRuns) iter.next();
            result.addTestRuns(runs);
        }

        // add failures not associated with any test case
        iter = _failures.iterator();
        while (iter.hasNext()) {
            result.addFailure((Failure) iter.next());
        }
        return result;
    }

    /**
     * Returns the test state for a particular test, creating it if it
     * doesn't exist
     *
     * @param test the test case
     * @param context the test context. May be null.
     * @return the state of the test
     */
    private TestRun getTest(JMSTestCase test, TestContext context) {
        TestRun result = null;
        String name = ReportHelper.getName(test);
        TestRuns testRuns = (TestRuns) _testRuns.get(name);
        if (testRuns == null) {
            testRuns = ReportHelper.getTestRuns(test);
            _testRuns.put(name, testRuns);
        } else if (context != null) {
            // see if it exists already
            Context mapped = ReportHelper.getContext(context);
            TestRun[] runs = testRuns.getTestRun();
            for (int i = 0; i < runs.length; ++i) {
                TestRun run = runs[i];
                if (run.getContext() != null
                        && run.getContext().equals(mapped)) {
                    result = runs[i];
                    break;
                }
            }
        }
        if (result == null) {
            result = ReportHelper.getTestRun(test, context);
            testRuns.addTestRun(result);
        }
        return result;
    }

    /**
     * Add requirement coverage
     *
     * @param requirementId the requirement identifier
     * @param test the test case
     */
    private void addCoverage(String requirementId, JMSTestCase test) {
        Coverage coverage = getCoverage(requirementId, test);

        if (coverage != null) {
            String name = ReportHelper.getName(test);
            String[] tests = coverage.getTest();
            boolean found = false;
            for (int i = 0; i < tests.length; ++i) {
                if (tests[i].equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                coverage.addTest(name);
            }
            int runs = coverage.getRuns();
            coverage.setRuns(runs + 1);
        }
    }

    /**
     * Add a test case failure
     *
     * @param test the test case
     * @param cause the reason for the failure
     * @param rootCause the root cause of the failure. May be null
     */
    private void addFailure(JMSTestCase test, Throwable cause,
                            Throwable rootCause) {
        addFailure(test, test.getContext(), cause, rootCause);
    }

    /**
     * Add a test case failure
     *
     * @param test the test case
     * @param context the test context. May be null.
     * @param cause the reason for the failure
     * @param rootCause the root cause of the failure. May be null
     */
    private void addFailure(JMSTestCase test, TestContext context,
                            Throwable cause, Throwable rootCause) {
        TestRun run = getTest(test, context);
        if (run.getFailure() == null) {
            // only log if one already hasn't been recorded,
            // to avoid hiding the original cause of the failure
            run.setFailure(ReportHelper.getFailure(cause, rootCause));

            // get the requirements associated with the test
            String[] requirements = test.getRequirements(test.getName());
            if (requirements == null || requirements.length == 0) {
                log.error("Test=" + ReportHelper.getName(test)
                          + " is not associated with any requirements");
            } else {
                // increment the failure count against each requirement
                // associated with the test
                for (int i = 0; i < requirements.length; ++i) {
                    Coverage coverage = getCoverage(requirements[i], test);
                    if (coverage != null) {
                        int failures = coverage.getFailures();
                        coverage.setFailures(failures + 1);
                    }
                }
            }
        }
    }

    /**
     * Add a failure occuring during setUp/tearDown of a test
     *
     * @param runner the test runner
     * @param cause the reason for the failure
     * @param rootCause the root cause of the failure. May be null
     */
    private void addFailure(TestRunner runner, Throwable cause,
                            Throwable rootCause) {
        // locate the test case
        Test test = runner.getTest();
        while (test instanceof TestRunner) {
            test = ((TestRunner) test).getTest();
        }
        if (test instanceof JMSTestCase) {
            // log the failure, using the context associated with the runner
            addFailure((JMSTestCase) test, runner.getContext(), cause,
                       rootCause);
        } else {
            // shouldn't happen
            addFailure(cause, rootCause);
        }
    }

    /**
     * Add a failure for a test not associated with any requirements
     *
     * @param cause the reason for the failure
     * @param rootCause the root cause of the failure. May be null
     */
    private void addFailure(Throwable cause, Throwable rootCause) {
        _failures.add(ReportHelper.getFailure(cause, rootCause));
    }

    /**
     * Returns a coverage instance for the specified requirement identifier.
     * If none exists, it will be created.
     *
     * @param requirementId the requirement identifier
     * @param test the test case
     * @return a coverage instance for the specified requirement identifier,
     * or <code>null</code> if <code>requirementId</code> is invalid
     */
    private Coverage getCoverage(String requirementId, JMSTestCase test) {
        Coverage coverage = (Coverage) _coverage.get(requirementId);
        if (coverage == null) {
            Requirement requirement = _requirements.getRequirement(
                requirementId);
            if (requirement != null) {
                coverage = createCoverage(requirementId);
                _coverage.put(requirementId, coverage);
            } else {
                log.error("Invalid requirement identifier="
                          + requirementId + " used by test="
                          + ReportHelper.getName(test));
            }
        }
        return coverage;
    }

    /**
     * Create a new coverage instance
     *
     * @param requirementId the requirement identifier
     * @return a new coverage instance
     */
    private Coverage createCoverage(String requirementId) {
        Coverage coverage = new Coverage();
        coverage.setRequirementId(requirementId);
        coverage.setRuns(0);
        coverage.setFailures(0);
        return coverage;
    }

}
