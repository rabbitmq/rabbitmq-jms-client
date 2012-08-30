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
 * $Id: SendReceiveTestRunner.java,v 1.6 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import junit.framework.Test;
import junit.framework.TestResult;


/**
 * This class enables generic message test cases to be run for each JMS
 * message type using different delivery modes, synchronous/asynchronous
 * messaging, and administered and non-administered destinations.
 * <p>
 * Test cases must implement the {@link SendReceiveTestCase} interface.
 *
 * @version     $Revision: 1.6 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         SendReceiveTestCase
 */
public class SendReceiveTestRunner extends AbstractMessageTestRunner {

    /**
     * Construct an instance with the test case to run.
     *
     * @param test the test case to run.
     */
    public SendReceiveTestRunner(SendReceiveTestCase test) {
        super(test);
    }

    /**
     * Counts the number of test cases that will be run by this test
     *
     * @return the number of test cases that will be run
     */
    @Override
    public int countTestCases() {
        int count = super.countTestCases();
        SendReceiveTestCase test = (SendReceiveTestCase) getTest();

        DeliveryType[] types = test.getDeliveryTypes().getTypes();
        int deliveryTypes = 0; // the number of applicable delivery types
        int durable = 0;

        if (getContext().isQueueConnectionFactory()) {
            deliveryTypes = types.length;
        } else {
            for (int i = 0; i < types.length; ++i) {
                DeliveryType type = types[i];
                ReceiptType receipt = type.getReceiptType();
                if (!ReceiptType.BROWSER.equals(receipt)) {
                    // BROWSER receipt is not applicable for topic connections
                    if (types[i].getAdministered() && !test.getDurableOnly()) {
                        ++deliveryTypes;
                    }
                    if (types[i].getAdministered()) {
                        ++durable;
                    }
                }
            }
        }
        return (deliveryTypes + durable) * count;
    }

    /**
     * Runs a test case for a particular message type, against each of the
     * supported delivery types.
     *
     * @param test the test case
     * @param result the instance to collect test results in
     * @param messageType the message type
     */
    @Override
    protected void runTest(Test test, TestResult result, Class<?> messageType) {
        TestContext context = getContext();
        SendReceiveTestCase sendReceive = (SendReceiveTestCase) test;
        DeliveryType[] types = sendReceive.getDeliveryTypes().getTypes();
        boolean isTopic = context.isTopic();
        getFilter();

        for (int i = 0; i < types.length && !result.shouldStop(); ++i) {
            DeliveryType type = types[i];
            ReceiptType receipt = type.getReceiptType();

            if (isTopic && ReceiptType.BROWSER.equals(receipt)) {
                // BROWSER receipt is not applicable for topics, so skip it
            } else {
                MessagingBehaviour behaviour = new MessagingBehaviour(type);

                if (!filtered(behaviour, messageType, test)) {
                    runTest(test, result, messageType, behaviour);
                }

                if (receipt != null && isTopic && type.getAdministered()) {
                    // run the test using a durable topic subscriber
                    behaviour.setDurable(true);
                    if (!filtered(behaviour, messageType, test)) {
                        runTest(test, result, messageType, behaviour);
                    }
                }
            }
        }
    }

    /**
     * Runs a test case for a particular message type, against a particular
     * messaging behaviour
     *
     * @param test the test case
     * @param result the instance to collect test results in
     * @param messageType the message type
     * @param behaviour the messaging behaviour
     */
    protected void runTest(Test test, TestResult result, Class<?> messageType,
                           MessagingBehaviour behaviour) {
        SendReceiveTestInvoker tester = new SendReceiveTestInvoker(
            test, result, getContext(), getFilter(), messageType, behaviour);
        result.runProtected(test, tester);
    }

    /**
     * Determines if a test case is excluded from being run
     *
     * @param behaviour the message behaviour
     * @param messageType the message type
     * @param test the test case
     * @return <code>true</code> if the test case is excluded
     */
    private boolean filtered(MessagingBehaviour behaviour, Class<?> messageType,
                             Test test) {
        boolean result = false;
        SendReceiveTestCase sendReceive = (SendReceiveTestCase) test;
        TestContext context = getContext();

        if (context.isTopic() && behaviour.getAdministered()
            && sendReceive.getDurableOnly() && !behaviour.getDurable()) {
            // if the test doesn't support non-durable topic subscribers, then
            // skip this test
            result = true;
        } else {
            TestFilter filter = getFilter();
            result = (filter != null && !filter.includes(context, behaviour,
                                                         messageType, test));
        }
        return result;
    }

}
