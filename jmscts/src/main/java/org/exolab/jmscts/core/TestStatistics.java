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
 * $Id: TestStatistics.java,v 1.4 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.HashMap;

import org.apache.log4j.Logger;

import org.exolab.castor.types.Time;

import org.exolab.jmscts.report.Failure;
import org.exolab.jmscts.report.ReportHelper;
import org.exolab.jmscts.report.Statistic;
import org.exolab.jmscts.report.Statistics;
import org.exolab.jmscts.report.TestRun;
import org.exolab.jmscts.report.TestRuns;
import org.exolab.jmscts.report.types.StatisticType;


/**
 * This class maintains statistics for the test suite
 *
 * @version     $Revision: 1.4 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class TestStatistics {

    /**
     * A map of test names to their corresponding TestRuns instances
     */
    private HashMap<String, TestRuns> _testRuns = new HashMap<String, TestRuns>();

    /**
     * The current test
     */
    private TestRun _current;

    /**
     * The statistics for all executed tests
     */
    private Statistics _statistics = new Statistics();


    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(TestStatistics.class);


    /**
     * Log a statistic for the current test
     *
     * @param statistic the statistic to log
     */
    public synchronized void log(Statistic statistic) {
        _current.addStatistic(statistic);
    }

    /**
     * Log a statistic for the current test
     *
     * @param type the type of the statistic
     * @param count the number of iterations
     * @param start the start time, in milliseconds
     * @param end the end time, in milliseconds
     */
    public void log(StatisticType type, int count, long start, long end) {
        long duration = end - start;
        log(type, count, duration);
    }

    /**
     *
     * Log a statistic for the current test
     *
     * @param type the type of the statistic
     * @param count the number of iterations
     * @param duration the elapsed time, in milliseconds
     */
    public void log(StatisticType type, int count, long duration) {
        final int oneSec = 1000;
        Statistic statistic = new Statistic();
        statistic.setType(type);
        statistic.setCount(count);
        statistic.setTime(new Time(duration));
        double seconds = 0.0;
        double rate = 0.0;
        if (duration != 0) {
            seconds = (double) duration / oneSec;
            rate = count / seconds;
        }
        statistic.setRate(rate);
        log(statistic);
    }

    /**
     * Returns the test suite statistics
     *
     * @return the test suite statistics
     */
    public synchronized Statistics getStatistics() {
        return _statistics;
    }

    /**
     * Log the start of a test
     *
     * @param test the test case
     */
    public synchronized void begin(JMSTestCase test) {
        String name = ReportHelper.getName(test);
        TestRuns runs = _testRuns.get(name);
        if (runs == null) {
            runs = ReportHelper.getTestRuns(test);
            _testRuns.put(name, runs);
            _statistics.addTestRuns(runs);
        }
        TestRun run = ReportHelper.getTestRun(test);
        runs.addTestRun(run);
        _current = run;
    }

    /**
     * Log the end of a test
     *
     * @param test the test that has completed
     */
    public synchronized void end(JMSTestCase test) {
        _current = null;
    }

    /**
     * Log a test case failure
     *
     * @param test the test case
     * @param cause the reason for the failure
     * @param rootCause the root cause of the failure. May be null
     */
    public synchronized void failed(JMSTestCase test, Throwable cause,
                                    Throwable rootCause) {

        if (_current != null) {
            Failure failure = ReportHelper.getFailure(cause, rootCause);
            _current.setFailure(failure);
        } else {
            log.error("Current test=" + ReportHelper.getName(test)
                      + " is invalid. Cannot record failure: "
                      + cause.getMessage());
        }
    }

}
