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
 * $Id: StressTestRunner.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.log4j.Logger;

import org.exolab.jmscts.provider.ProviderLoader;
import org.exolab.jmscts.report.StatisticsReport;


/**
 * This class enables stress test cases to be run against different JMS
 * providers
 * <p>
 * The provider configuration file path is passed at construction. This
 * contains a list of providers to run the tests against.
 * <p>
 * <h3>Parameters</h3>
 * <table border="1" cellpadding="2" cellspacing="0">
 *   <tr>
 *     <td valign="top"><b>Argument</b></td>
 *     <td valign="top"><b>Description</b></td>
 *     <td align="center" valign="top"><b>Required</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top">config</td>
 *     <td valign="top">the provider configuration file path</td>
 *     <td valign="top" align="center">No</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">output</td>
 *     <td valign="top">the coverage report output directory</td>
 *     <td valign="top" align="center">No</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">filter</td>
 *     <td valign="top">the test filter configuration file path</td>
 *     <td valign="top" align="center">No</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">port</td>
 *     <td valign="top">the registry port</td>
 *     <td valign="top" align="center">No</td>
 *   </tr>
 * </table>
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class StressTestRunner extends AbstractTestRunner {

    /**
     * Stress test statistics
     */
    private final TestStatistics _statistics = new TestStatistics();

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log =
        Logger.getLogger(StressTestRunner.class);


    /**
     * Construct an instance using the class of the test case, and the list
     * of arguments to configure the provider
     *
     * @param test a class implementing the {@link ConnectionFactoryTestCase}
     * and {@link junit.framework.Test} interfaces
     * @param args command line arguments
     */
    public StressTestRunner(Class<?> test, String[] args) {
        super(test, args);
    }

    /**
     * Construct an instance with the test to run, and the list of arguments
     * to configure test suite.
     *
     * @param test the test to run
     * @param args command line arguments
     */
    public StressTestRunner(Test test, String[] args) {
        super(test, args);
    }

    /**
     * Generate a stress test report, writing it out to the specified
     * path
     *
     * @param path the path to write the report
     * @throws Exception if the report can't be written, or the test
     * isn't running
     */
    @Override
    public void report(String path) throws Exception {
        ProviderLoader provider = getProvider();
        if (provider != null) {
            StatisticsReport report = new StatisticsReport(
                getHome(), _statistics, provider);
            report.report(path);
        } else {
            throw new Exception("The test is not running");
        }
    }

    /**
     * Runs the test cases against each provider
     *
     * @param result the instance to collect results in
     */
    @Override
    public void basicRun(TestResult result) {
        // set up the handler to record statistics
        TestStatisticsListener listener =
            new TestStatisticsListener(_statistics);
        result.addListener(listener);
        super.basicRun(result);
    }

    /**
     * Creates a test context
     *
     * @return a new test context
     */
    @Override
    public TestContext createContext() {
        return new TestContext(_statistics);
    }

}
