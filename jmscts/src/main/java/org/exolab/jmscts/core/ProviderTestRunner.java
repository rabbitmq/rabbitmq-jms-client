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
 * $Id: ProviderTestRunner.java,v 1.6 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnectionFactory;

import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.log4j.Logger;

import org.exolab.jmscts.provider.Administrator;
import org.exolab.jmscts.provider.Provider;
import org.exolab.jmscts.provider.ProviderLoader;


/**
 * This class enables generic connection factory test cases to be run for
 * each JMS provider.
 * <p>
 * Test cases must implement the {@link ConnectionFactoryTestCase} interface.
 *
 * @version     $Revision: 1.6 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         ConnectionTestCase
 * @see         JMSTestRunner
 * @see         Provider
 */
public class ProviderTestRunner extends TestRunner {

    /**
     * The provider
     */
    private ProviderLoader _provider = null;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ProviderTestRunner.class);


    /**
     * Construct an instance with the test case to run.
     *
     * @param test the test case to run. The test must implement the
     * {@link ConnectionFactoryTestCase} interface, unless it is a TestSuite,
     * where each contained test must implement the
     * {@link ConnectionFactoryTestCase} interface.
     */
    public ProviderTestRunner(Test test) {
        super(test);
    }

    /**
     * Set the provider loader
     * This must be invoked prior to running the test case.
     *
     * @param provider the provider loader
     */
    public void setProvider(ProviderLoader provider) {
        _provider = provider;
    }

    /**
     * Initialises the provider
     *
     * @throws Exception if the provider proxy cannot be initialised.
     */
    @Override
    protected void setUp() throws Exception {
        Provider provider = _provider.getProvider();
        provider.initialise(_provider.getStart());

        TestContext context = new TestContext(getContext(),
                                              provider.getAdministrator());
        setContext(context);
    }

    /**
     * Cleans up after the test case has been run
     *
     * @throws Exception if the provider can't be cleaned up
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Provider provider = _provider.getProvider();
        provider.cleanup(_provider.getStop());
        _provider = null;
    }

    /**
     * Runs a test case against each supported connection type
     *
     * @param test the test case
     * @param result the instance to collect test results in
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void runTest(Test test, TestResult result) {
        ConnectionFactoryTestCase factoryTest =
            (ConnectionFactoryTestCase) test;
        Class[] types = factoryTest.getConnectionFactoryTypes().getTypes();
        TestContext context = getContext();
        TestFilter filter = getFilter();
        for (int i = 0; i < types.length; ++i) {
            if (filter == null || filter.includes(types[i], test)) {
                Tester tester = new Tester(test, result, context, filter,
                                           types[i]);
                result.runProtected(test, tester);
            }
        }
    }

    /**
     * Helper class to run a test case against a connection factory
     */
    private class Tester extends TestInvoker {

        /**
         * The connection factory type
         */
        private final Class<?> _type;

        /**
         * Construct an instance to run a test case against a connection
         * factory.
         *
         * @param test the test case
         * @param result the instance to collect test results in
         * @param context the test context
         * @param filter the test filter
         * @param type the type of the connection factory
         */
        public Tester(Test test, TestResult result, TestContext context,
                      TestFilter filter, Class<?> type) {
            super(test, result, context, filter);
            _type = type;
        }

        /**
         * Setup the test
         *
         * @param test the test
         * @param context the test context
         * @throws Exception for any error
         */
        @Override
        protected void setUp(JMSTest test, TestContext context)
            throws Exception {

            final ConnectionFactoryTestCase factoryTest =
                (ConnectionFactoryTestCase) test;

            Provider provider = _provider.getProvider();
            Administrator admin = provider.getAdministrator();
            String factoryName = null;
            ConnectionFactory factory = null;

            try {
                if (_type.equals(QueueConnectionFactory.class)) {
                    factoryName = admin.getQueueConnectionFactory();
                } else if (_type.equals(TopicConnectionFactory.class)) {
                    factoryName = admin.getTopicConnectionFactory();
                } else if (_type.equals(XAQueueConnectionFactory.class)) {
                    factoryName = admin.getXAQueueConnectionFactory();
                } else if (_type.equals(XATopicConnectionFactory.class)) {
                    factoryName = admin.getXATopicConnectionFactory();
                } else {
                    throw new Exception("Unsupported connection factory type="
                                        + _type.getName());
                }

                if (factoryName == null) {
                    throw new Exception(
                        "Provider returned null for factory of type="
                        + _type.getName());
                }

                factory = (ConnectionFactory) admin.lookup(factoryName);

            } catch (Exception exception) {
                log.debug(exception, exception);
                throw exception;
            }


            if (log.isDebugEnabled()) {
                log.debug("running test=" + test
                          + " using connection factory type="
                          + _type.getName());
            }
            TestContext child = new TestContext(getContext(), factory, _type);
            factoryTest.setContext(child);
        }

        /**
         * Tear down the test
         *
         * @param test the test
         * @param context the test context
         * @throws Exception for any error
         */
        @Override
        protected void tearDown(JMSTest test, TestContext context)
            throws Exception {

            if (log.isDebugEnabled()) {
                log.debug("completed running test="
                          + test + " using connection factory type="
                          + _type.getName());
            }
        }

    }

}

