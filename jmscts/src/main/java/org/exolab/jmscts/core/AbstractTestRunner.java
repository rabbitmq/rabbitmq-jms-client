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
 * $Id: AbstractTestRunner.java,v 1.7 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Iterator;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.exolab.core.service.ServiceException;
import org.exolab.core.service.ServiceGroup;
import org.exolab.core.util.RmiRegistryService;
import org.exolab.jmscts.core.filter.Filter;
import org.exolab.jmscts.core.service.SnapshotService;
import org.exolab.jmscts.core.service.TestTerminatorService;
import org.exolab.jmscts.provider.Configuration;
import org.exolab.jmscts.provider.ProviderLoader;


/**
 * This class enables test cases to be run against different JMS providers
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
 *     <td valign="top">the report output directory</td>
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
 * @version     $Revision: 1.7 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public abstract class AbstractTestRunner extends TestSetup {

    /**
     * The default registry port
     */
    public static final String DEFAULT_PORT = "4000";

    /**
     * The configuration file path argument
     */
    private static final String CONFIG = "config";

    /**
     * The test case filter path argument
     */
    private static final String FILTER = "filter";

    /**
     * The coverage report output directory
     */
    private static final String OUTPUT = "output";

    /**
     * The registry port argument
     */
    private static final String PORT = "port";

    /**
     * The log4j configuration filename
     */
    private static final String LOG4J = "log4j.xml";

    /**
     * The configuration for each provider
     */
    private Configuration _config = null;

    /**
     * The report output directory
     */
    private String _output = null;

    /**
     * The test case filter
     */
    private Filter _filter = null;

    /**
     * The command line arguments
     */
    private final String[] _args;

    /**
     * The test result
     */
    private TestResult _result = null;

    /**
     * The current provider being tested against
     */
    private ProviderLoader _provider = null;

    /**
     * The registry port
     */
    private int _port;

    /**
     * The services
     */
    private ServiceGroup _services = new ServiceGroup();

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(AbstractTestRunner.class.getName());


    /**
     * Construct an instance using the class of the test case, and the list
     * of arguments to configure the test suite
     *
     * @param test a class implementing the {@link ConnectionFactoryTestCase}
     * interface
     * @param args command line arguments
     */
    public AbstractTestRunner(Class<?> test, String[] args) {
        super(new TestSuite(test));
        if (args == null) {
            throw new IllegalArgumentException("Argument 'args' is null");
        }
        _args = args;

        // configure the logger
        DOMConfigurator.configure(getHome() + "/config/" + LOG4J);
    }

    /**
     * Construct an instance with the test to run, and the list of arguments
     * to configure the test suite
     *
     * @param test the test to run
     * @param args command line arguments
     */
    public AbstractTestRunner(Test test, String[] args) {
        super(test);
        if (args == null) {
            throw new IllegalArgumentException("Argument args is null");
        }
        _args = args;

        // configure the logger
        DOMConfigurator.configure(getHome() + "/config/" + LOG4J);
    }

    /**
     * Generate a report
     *
     * @param path the path to write the report to
     * @throws Exception for any error
     */
    public abstract void report(String path) throws Exception;

    /**
     * Snapshot the current state of the test
     *
     * @param path the path to write the report to
     * @throws Exception for any error
     */
    public void snapshot(String path) throws Exception {
        report(path);
    }

    /**
     * Generate a report and stop the test
     */
    public void stop() {
        if (_result != null) {
            _result.stop();
        }
        try {
            report(_output);
        } catch (Exception exception) {
            log.error("Failed to produce report", exception);
        }
    }

    /**
     * Generate a report, and abort the test
     */
    public void abort() {
        try {
            report(_output);
        } catch (Exception exception) {
            log.error("Failed to produce report", exception);
        }
        System.exit(1);
    }

    /**
     * Creates a test context
     *
     * @return a new test context
     */
    public abstract TestContext createContext();

    /**
     * Runs the test case against each provider
     *
     * @param result the instance to collect results in
     */
    @Override
    public void basicRun(TestResult result) {
        TestFilter filter = (_filter != null) ? new TestFilter(_filter) : null;
        _result = result;

        Iterator<?> iterator = _config.getProviders().iterator();
        while (iterator.hasNext() && !result.shouldStop()) {
            _provider = (ProviderLoader) iterator.next();

            TestSuite suite = new TestSuite();
            mergeSuites(suite, getTest());

            TestContext context = createContext();

            // run the tests against the provider configuration
            ProviderTestRunner runner = new ProviderTestRunner(suite);
            runner.setContext(context);
            runner.setFilter(filter);
            runner.setProvider(_provider);
            runner.run(result);

            // generate a results report
            try {
                report(_output);
            } catch (Exception exception) {
                log.error(exception, exception);
            }
        }
    }

    /**
     * Returns the registry port
     *
     * @return the registry port
     */
    public int getPort() {
        return _port;
    }

    /**
     * Reads the provider configuration
     *
     * @throws Exception if the configuration cannot be initialised
     */
    @Override
    protected void setUp() throws Exception {
        // parse the command line
        Options options = new Options();
        options.addOption(CONFIG, true, "the provider configuration path");
        options.addOption(OUTPUT, true, "the output path");
        options.addOption(FILTER, true, "the test case filter path");
        options.addOption(PORT, true, "registry port");

        CommandLineParser parser = new GnuParser();
        CommandLine commands = parser.parse(options, _args);

        String config = commands.getOptionValue(
            CONFIG, getHome() + "/config/providers.xml");

        _output = commands.getOptionValue(
            OUTPUT, getHome() + "/target/jmscts-report");
        checkAndMakeDir(new File(_output));

        String filter = commands.getOptionValue(FILTER);
        _port = Integer.parseInt(commands.getOptionValue(PORT, DEFAULT_PORT));

        // load the provider configuration
        _config = Configuration.read(config);

        // load the filter (if supplied)
        loadFilter(filter);

        // initialise and start the services
        startServices();
    }

    private void checkAndMakeDir(File dir) throws Exception {
        if (!dir.isDirectory()) {
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    throw new Exception("Invalid output directory: " + _output);
                }
            } else {
                throw new Exception("Invalid output directory: " + _output);
            }
        }
        if (!dir.canWrite()) {
            throw new Exception("Cannot write to output directory: "
                                + _output);
        }
    }

    /**
     * Returns the current provider being tested against
     *
     * @return the current provider being tested against, or <code>null</code>
     * if no test is being run
     */
    protected ProviderLoader getProvider() {
        return _provider;
    }

    /**
     * Clean up any resources allocated
     *
     * @throws Exception for any error
     */
    @Override
    protected void tearDown() throws Exception {
        _config = null;
        if (_services != null) {
            _services.stopAll();
        }
    }

    /**
     * Start the services
     *
     * @throws Exception for any error
     */
    protected void startServices() throws Exception {
        registerServices(_services);
        _services.startAll();
    }

    /**
     * Register services
     *
     * @param services the service group to register the services with
     * @throws ServiceException for any error
     */
    protected void registerServices(ServiceGroup services)
        throws ServiceException {

        RmiRegistryService registry = new RmiRegistryService(_port);
        services.add(registry.getName(), registry);

        SnapshotService snapshot = new SnapshotService(this, _port);
        services.add(snapshot.getName(), snapshot);

        TestTerminatorService terminator = new TestTerminatorService(
            this, _port);
        services.add(terminator.getName(), terminator);

        services.startAll();
    }

    /**
     * Returns the value of the <code>jmscts.home</code> system property,
     * defaulting to the value of <code>user.dir</code> if its not set
     *
     * @return the value of the <code>jmscts.home</code> system property
     */
    protected String getHome() {
        return System.getProperty("jmscts.home",
                                  System.getProperty("user.dir"));
    }

    /**
     * Removes nested TestSuite instances from <code>test</code>, adding the
     * contained Test instances to <code>suite</code>, in order for the
     * ProviderTestRunner to be invoked successfully.
     *
     * @param suite the resulting test suite
     * @param test if an instance of TestSuite, this method will be called
     * recursively with each contained Test; else the test will be added
     * directly to <code>suite</code>
     */
    protected void mergeSuites(TestSuite suite, Test test) {
        if (test instanceof TestSuite) {
            Enumeration<?> iter = ((TestSuite) test).tests();
            while (iter.hasMoreElements()) {
                mergeSuites(suite, (Test) iter.nextElement());
            }
        } else {
            suite.addTest(test);
        }
    }

    /**
     * Loads the filter configuration file
     *
     * @param filter the filter configuration file path
     * @throws Exception for any error
     */
    private void loadFilter(String filter) throws Exception {
        if (filter != null) {
            FileReader filterReader = null;
            try {
                filterReader = new FileReader(filter);
                _filter = Filter.unmarshal(filterReader);
            } finally {
                if (filterReader != null) {
                    filterReader.close();
                }
            }
        }
    }

}
