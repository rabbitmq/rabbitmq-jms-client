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
 * $Id: AbstractMessageTestRunner.java,v 1.5 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import junit.framework.Test;
import junit.framework.TestResult;


/**
 * This class enables generic message test cases to be run for each JMS
 * message type.
 *
 * @version     $Revision: 1.5 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessageTestCase
 */
public abstract class AbstractMessageTestRunner extends TestRunner
    implements SessionTestCase {

    /**
     * Construct an instance with the test case to run.
     *
     * @param test the test case to run.
     */
    public AbstractMessageTestRunner(MessageTestCase test) {
        super(test);
    }

    /**
     * Get the connection factory types to test against
     *
     * @return the connection factory types to test against
     */
    @Override
    public ConnectionFactoryTypes getConnectionFactoryTypes() {
        return ((MessageTestCase) getTest()).getConnectionFactoryTypes();
    }

    /**
     * Returns true if the connection should be started prior to running the
     * test.
     *
     * @return true if the connection should be started, false if it should be
     * stopped
     */
    @Override
    public boolean startConnection() {
        return ((MessageTestCase) getTest()).startConnection();
    }

    /**
     * Returns true if the client identifier should be set on the connection.
     * This is only applicable for TopicConnection instances, and will be
     * ignored for QueueConnection instances.
     *
     * @return true if an identifier should be allocated to the connection
     */
    @Override
    public boolean setClientID() {
        return ((MessageTestCase) getTest()).setClientID();
    }

    /**
     * Returns the message acknowledgement types of the sessions to test
     * against. A session will be constructed for each type
     *
     * @return the acknowledgement type of the sessions to test against
     */
    @Override
    public AckTypes getAckTypes() {
        return ((MessageTestCase) getTest()).getAckTypes();
    }

    /**
     * Counts the number of test cases that will be run by this test
     *
     * @return the number of test cases that will be run
     */
    @Override
    public int countTestCases() {
        MessageTestCase test = (MessageTestCase) getTest();
        return test.getMessageTypes().count();
    }

    /**
     * Run a test against each of the message types it supports
     *
     * @param test the test case
     * @param result the instance to collect test results in
     */
    @Override
    protected void runTest(Test test, TestResult result) {
        MessageTestCase messageTest = (MessageTestCase) test;
        @SuppressWarnings("rawtypes")
        Class[] types = messageTest.getMessageTypes().getTypes();
        TestContext context = getContext();
        TestFilter filter = getFilter();

        for (int i = 0; i < types.length && !result.shouldStop(); ++i) {
            if (filter == null || filter.includes(context, types[i], test)) {
                runTest(test, result, types[i]);
            }
        }
    }

    /**
     * Runs a test case for the given message type.
     *
     * @param test the test case
     * @param result the instance to collect test results in
     * @param messageType the message type
     */
    protected abstract void runTest(Test test, TestResult result,
                                    Class<?> messageType);

}
