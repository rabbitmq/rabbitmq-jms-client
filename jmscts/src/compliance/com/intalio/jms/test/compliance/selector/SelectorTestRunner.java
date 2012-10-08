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
 *    please contact jima@intalio.com.
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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: SelectorTestRunner.java,v 1.4 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.log4j.Logger;

import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.JMSTest;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.SendReceiveTestInvoker;
import org.exolab.jmscts.core.SendReceiveTestRunner;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestFilter;


/**
 * This class enables generic message test cases to be run for each JMS
 * message type using different delivery modes, synchronous/asynchronous 
 * messaging, and durable and non-durable destinations.
 * <p>
 * Test cases must extend the {@link AbstractSelectorTestCase} class.
 *
 * @version     $Revision: 1.4 $ $Date: 2003/05/04 14:12:38 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         AbstractSelectorTestCase
 * @see         SendReceiveTestRunner
 */
public class SelectorTestRunner extends SendReceiveTestRunner {

    /**
     * The cache of selector test cases. This is a map of paths to Selectors 
     * instances
     */
    private static HashMap _cache = new HashMap();

    /**
     * The selectors to run the test case against
     */
    private Selectors _selectors = null;

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(SelectorTestRunner.class.getName());


    /**
     * Construct an instance with the test case to run.
     *
     * @param test the test case to run. The test must extend the 
     * {@link AbstractSelectorTestCase} class, unless it is a TestSuite, where 
     * each contained test must extend the {@link AbstractSelectorTestCase} 
     * class.
     */
    public SelectorTestRunner(Test test) {
        super(test);
    }

    /**
     * Construct an instance using the class of the test case
     *
     * @param test a class extending the {@link AbstractSelectorTestCase} class
     * and implementing {@link junit.framework.Test}
     */
    public SelectorTestRunner(Class test) {
        super(test);
    }

    /**
     * Construct an instance using the class of the test case, and the 
     * message acknowledgement types of the sessions to run the test against
     *
     * @param test a class extending the {@link AbstractSelectorTestCase} class
     * and implementing {@link junit.framework.Test}
     * @param types the message acknowledgement types of the sessions
     */
    public SelectorTestRunner(Class test, AckTypes types) {
        super(test, types);
    }

    /**
     * Verifies that the test(s) implement the SendReceiveTestCase interface
     *
     * @throws Exception if the test(s) do not implement the 
     * SendReceiveTestCase interface
     */
    protected void setUp() throws Exception {
        super.setUp();
        checkImplements(SelectorTestCase.class);

        // load the Selectors documents used by the tests
        Test[] tests = getTests();
        for (int i = 0; i < tests.length; ++i) {
            SelectorTestCase test = (SelectorTestCase) tests[i];
            String[] paths = test.getPaths();
            for (int j = 0; j < paths.length; ++j) {
                String path = paths[j];
                Selectors selectors = (Selectors) _cache.get(path);
                if (selectors == null) {
                    InputStream stream = getClass().getResourceAsStream(path);
                    if (stream == null) {
                        throw new Exception("Failed to locate resource: " + 
                                            path);
                    }
                    InputStreamReader reader = new InputStreamReader(stream);
                    try {
                        selectors = Selectors.unmarshal(reader);
                    } catch (Exception exception) {
                        _log.error("Failed to unmarshal resource: " + 
                                   path, exception);
                        throw exception;
                    } finally {
                        reader.close();
                    }
                    _cache.put(path, selectors);
                }
            }
        }
    }

    /**
     * Runs a test case for a particular message type, against each of the
     * supported delivery types.
     *
     * @param test the test case
     * @param result the instance to collect test results in
     * @param messageType the message type
     */
    protected void runTest(Test test, TestResult result, Class messageType,
                           MessagingBehaviour behaviour) {
        String[] paths = ((SelectorTestCase) test).getPaths();
        for (int i = 0; i < paths.length && !result.shouldStop(); ++i) {
            Selectors selectors = (Selectors) _cache.get(paths[i]);
            Selector[] list = selectors.getSelector();
            for (int j = 0; j < list.length && !result.shouldStop(); ++j) {
                Tester tester = new Tester(
                    test, result, getContext(), getFilter(), messageType, 
                    behaviour, list[j]);
                result.runProtected(test, tester);
            }
        }
    }

    /**
     * Counts the test cases for a particular test
     *
     * @param test the test case
     * @return the count of test cases
     */
    protected int countTests(Test test) {
        int cases = 0;
        String[] paths = ((SelectorTestCase) test).getPaths();
        for (int i = 0; i < paths.length; ++i) {
            Selectors selectors = (Selectors) _cache.get(paths[i]);
            cases += selectors.getSelectorCount();
        }
        return super.countTests(test) * cases;
    }

    private class Tester extends SendReceiveTestInvoker {
        
        private final Selector _selector;

        public Tester(Test test, TestResult result, TestContext context, 
                      TestFilter filter, Class messageType, 
                      MessagingBehaviour behaviour, Selector selector) {
            super(test, result, context, filter, messageType, behaviour);
            _selector = selector;
        }

        protected void setUp(JMSTest test, TestContext context) 
            throws Exception {
            super.setUp(test, context);

            SelectorTestCase selectorTest = (SelectorTestCase) test;
            selectorTest.setSelector(_selector);
            _log.debug("Running test for selector=" + 
                       _selector.getExpression() + ", context=" + context);
        }

    } //-- Tester

} //-- SelectorTestRunner
