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
 * Copyright 2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: SubscriberRedeliveredTest.java,v 1.2 2005/06/16 07:59:07 tanderson Exp $
 */
package org.exolab.jmscts.test.session.clientack;

import java.util.Iterator;
import java.util.List;

import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of the JMSRedelivered flag when
 * multiple topic subscribers subscribe to the same topic, and
 * one of the CLIENT_ACKNOWLEDGE sessions is recovered.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 * @jmscts.factory TopicConnectionFactory
 * @jmscts.factory XATopicConnectionFactory
 * @jmscts.message ObjectMessage
 */
public class SubscriberRedeliveredTest extends AbstractSendReceiveTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "SubscriberRedeliveredTest";

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log =
        Logger.getLogger(SubscriberRedeliveredTest.class);


    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against the CLIENT_ACKNOWLEGE acknowledgement type,
     * all consumer messaging behaviours, and using TextMessage messages
     *
     * @param name the name of test case
     */
    public SubscriberRedeliveredTest(String name) {
        super(name);
    }

    /**
     * The main line used to execute this test
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JMSTestRunner test = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(test);
    }

    /**
     * Sets up the test suite.
     *
     * @return the test suite
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(
            SubscriberRedeliveredTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[]{DESTINATION};
    }

    /**
     * Verifies that messages received after
     * <code>TopicSession.recover()</code> have their JMSRedelivered flag set
     * to <code>true</code>, and that the same messages received via
     * another TopicSession have their JMSRedelivered flag set to
     * <code>false</code>.
     *
     * @jmscts.requirement message.redelivered.receive
     * @throws Exception for any error
     */
    public void testJMSRedelivered() throws Exception {
        final int count = 10; // send count messages

        TestContext context = getContext();
        Session session = context.getSession();

        // set up a client ack session
        TestContext clientAckContext = TestContextHelper.createSessionContext(
            context, AckTypes.CLIENT_ACKNOWLEDGE);
        Session clientAckSession = clientAckContext.getSession();

        MessageReceiver subscriber1 = null;
        MessageReceiver subscriber2 = null;

        try {
            // create the subscribers prior to sending any messages
            subscriber1 = createReceiver(context, DESTINATION);
            subscriber2 = createReceiver(clientAckContext, DESTINATION);

            send(context, DESTINATION, count);
            if (session.getTransacted()) {
                session.commit();
            }

            // receive messages via the CLIENT_ACKNOWLEDGE session
            List<?> messages2 = receive(clientAckContext, subscriber2, count);
            checkJMSRedelivered(messages2, false);
            clientAckSession.recover();
            messages2 = receive(clientAckContext, subscriber2, count);
            checkJMSRedelivered(messages2, true);
            acknowledge(messages2);

            // messages received by subscriber1 shouldn't have their
            // JMSRedelivered set
            List<?> messages1 = receive(context, subscriber1, count);
            checkJMSRedelivered(messages1, false);
            acknowledge(messages1);
        } finally {
            close(subscriber1);
            close(subscriber2);
            clientAckSession.close();
        }
    }

    /**
     * Verifies that the JMSRedelivered property matches that expected
     *
     * @param messages the list of messages to verify
     * @param redelivered the expected value of JMSRedelivered
     * @throws Exception for any error
     */
    protected void checkJMSRedelivered(List<?> messages, boolean redelivered)
        throws Exception {
        Iterator<?> iter = messages.iterator();
        while (iter.hasNext()) {
            Message message = (Message) iter.next();
            if (message.getJMSRedelivered() != redelivered) {
                fail("Expected message to have JMSRedelivered="
                     + redelivered + ", but got JMSRedelivered="
                     + message.getJMSRedelivered());
            }
        }
    }

}