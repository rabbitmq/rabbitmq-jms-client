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
 * $Id: TestCreator.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Helper class for creating JUnit test cases runnable by JMSTestRunner
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         JMSTestRunner
 * @see         ProviderTestRunner
 * @see         ConnectionTestRunner
 * @see         SessionTestRunner
 * @see         MessageTestRunner
 * @see         SendReceiveTestRunner
 */
public final class TestCreator {

    /**
     * Prevent construction of utility class
     */
    private TestCreator() {
    }

    /**
     * Create a connection factory test using its class. The class must
     * implement the {@link ConnectionFactoryTestCase} interface.
     *
     * @param test the test class
     * @return the test
     */
    public static Test createConnectionFactoryTest(Class<?> test) {
        return new TestSuite(test);
    }

    /**
     * Create a connection test.
     *
     * @param test the test case.
     * @return the test
     */
    public static Test createConnectionTest(Test test) {
        Test result;
        if (test instanceof TestSuite) {
            TestSuite suite = new TestSuite();
            Enumeration<?> tests = ((TestSuite) test).tests();
            while (tests.hasMoreElements()) {
                ConnectionTestCase child =
                    (ConnectionTestCase) tests.nextElement();
                suite.addTest(new ConnectionTestRunner(child));
            }
            result = suite;
        } else {
            result = new ConnectionTestRunner((ConnectionTestCase) test);
        }
        return result;
    }

    /**
     * Create a connection test using its class. The class must implement the
     * {@link ConnectionTestCase} interface.
     *
     * @param test the test class
     * @return the test
     */
    public static Test createConnectionTest(Class<?> test) {
        return createConnectionTest(new TestSuite(test));
    }

    /**
     * Create a session test.
     *
     * @param test the test case
     * @return the test
     */
    public static Test createSessionTest(Test test) {
        Test result;
        if (test instanceof TestSuite) {
            TestSuite suite = new TestSuite();
            Enumeration<?> tests = ((TestSuite) test).tests();
            while (tests.hasMoreElements()) {
                SessionTestCase child = (SessionTestCase) tests.nextElement();
                suite.addTest(new SessionTestRunner(child));
            }
            result = suite;
        } else {
            result = new SessionTestRunner((SessionTestCase) test);
        }
        return createConnectionTest(result);
    }

    /**
     * Create a session test using its class. The class must implement the
     * {@link SessionTestCase} interface.
     *
     * @param test the test class
     * @return the test
     */
    public static Test createSessionTest(Class<?> test) {
        return createSessionTest(new TestSuite(test));
    }

    /**
     * Create a message test. The test must implement the
     * {@link MessageTestCase} interface.
     *
     * @param test the test case
     * @return the test
     */
    public static Test createMessageTest(Test test) {
        Test result;
        if (test instanceof TestSuite) {
            TestSuite suite = new TestSuite();
            Enumeration<?> tests = ((TestSuite) test).tests();
            while (tests.hasMoreElements()) {
                MessageTestCase child = (MessageTestCase) tests.nextElement();
                suite.addTest(new MessageTestRunner(child));
            }
            result = suite;
        } else {
            result = new MessageTestRunner((MessageTestCase) test);
        }
        return createSessionTest(result);
    }

    /**
     * Create a message test using its class. The class must implement the
     * {@link MessageTestCase} interface.
     *
     * @param test the test class
     * @return the test
     */
    public static Test createMessageTest(Class<?> test) {
        return createMessageTest(new TestSuite(test));
    }

    /**
     * Create a send/receive test. The test must implement
     * the {@link SendReceiveTestCase} interface.
     *
     * @param test the test case
     * @return the test
     */
    public static Test createSendReceiveTest(Test test) {
        Test result;
        if (test instanceof TestSuite) {
            TestSuite suite = new TestSuite();
            Enumeration<?> tests = ((TestSuite) test).tests();
            while (tests.hasMoreElements()) {
                SendReceiveTestCase child =
                    (SendReceiveTestCase) tests.nextElement();
                suite.addTest(new SendReceiveTestRunner(child));
            }
            result = suite;
        } else {
            result = new SendReceiveTestRunner((SendReceiveTestCase) test);
        }
        return createSessionTest(result);
    }

    /**
     * Create a send/receive test using its class. The class must implement
     * the {@link SendReceiveTestCase} interface.
     *
     * @param test the test class
     * @return the test
     */
    public static Test createSendReceiveTest(Class<?> test) {
        return createSendReceiveTest(new TestSuite(test));
    }

}
