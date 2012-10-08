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
 * $Id: MessageTestRunner.java,v 1.5 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Message;

import org.apache.log4j.Logger;

import junit.framework.Test;
import junit.framework.TestResult;


/**
 * This class enables generic message test cases to be run for each JMS
 * message type.
 * <p>
 * Test cases must implement the {@link MessageTestCase} interface.
 *
 * @version     $Revision: 1.5 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessageTestCase
 */
public class MessageTestRunner extends AbstractMessageTestRunner {

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(MessageTestRunner.class.getName());


    /**
     * Construct an instance with the test case to run.
     *
     * @param test the test case to run.
     */
    public MessageTestRunner(MessageTestCase test) {
        super(test);
    }

    /**
     * Runs a test case for the given message type.
     *
     * @param test the test case
     * @param result the instance to collect test results in
     * @param messageType the message type
     */
    @Override
    protected void runTest(Test test, TestResult result, Class<?> messageType) {
        Tester tester = new Tester(test, result, getContext(), getFilter(),
                                   messageType);
        result.runProtected(test, tester);
    }

    /**
     * Helper class to run a test case, for the given message type
     */
    private class Tester extends MessageTestInvoker {

        /**
         * Construct a new <code>Tester</code>
         *
         * @param test the test to run. Must be an instance of
         * <code>MessageTestCase</code>
         * @param result the result of the test
         * @param context the test context
         * @param filter the test filter. May be <code>null</code>
         * @param type the message type to run the test against
         */
        public Tester(Test test, TestResult result, TestContext context,
                      TestFilter filter, Class<?> type) {
            super(test, result, context, filter, type);
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
            super.setUp(test, context);

            if (log.isDebugEnabled()) {
                String sessionType = context.getSessionType().getName();
                String messageType =
                    getChildContext().getMessageType().getName();
                String msg = "test=" + test + " using session type="
                    + sessionType + ", message type=" + messageType;

                log.debug("running " + msg);
            }

            MessageTestCase messageTest = (MessageTestCase) test;
            TestContext child;
            if (messageTest.shouldCreateMessage()) {
                // set up the message for the test
                Message message = create();
                child = new TestContext(context, message);
            } else {
                child = new TestContext(context, getMessageType());
            }

            setChildContext(child);
            test.setContext(child);
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
                String sessionType = context.getSessionType().getName();
                String messageType =
                    getChildContext().getMessageType().getName();
                String msg = "test=" + test + " using session type="
                    + sessionType + ", message type=" + messageType;

                log.debug("completed " + msg);
            }
        }

    }

}
