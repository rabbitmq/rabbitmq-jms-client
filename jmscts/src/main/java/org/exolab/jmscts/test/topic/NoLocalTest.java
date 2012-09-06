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
 * $Id: NoLocalTest.java,v 1.7 2004/02/03 21:52:12 tanderson Exp $
 */
package org.exolab.jmscts.test.topic;

import javax.jms.Destination;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessagingHelper;
import org.exolab.jmscts.core.ReceiptType;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.TestContext;


/**
 * This class tests the behaviour of the noLocal argument of
 * <code>TopicSubscriber</code>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.7 $
 * @see AbstractSendReceiveTestCase
 * @jmscts.factory TopicConnectionFactory
 * @jmscts.factory XATopicConnectionFactory
 * @jmscts.message ObjectMessage
 */
public class NoLocalTest extends AbstractSendReceiveTestCase {

    /**
     * The destination to create prior to running the test
     */
    private static final String DESTINATION = "NoLocalTest";


    /**
     * Construct a new <code>NoLocalTest</code>
     *
     * @param name the name of test case
     */
    public NoLocalTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(NoLocalTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[]{DESTINATION};
    }

    /**
     * Verifies the behaviour of the subscriber noLocal attribute
     *
     * @jmscts.requirement subscriber.nolocal
     * @throws Exception for any error
     */
    public void testNoLocal() throws Exception {
        final int count = 10; // the number of messages to send
        TestContext context = getContext();
        Destination destination = getDestination(DESTINATION);

        MessageReceiver noLocal = null;
        MessageReceiver receiver = null;
        Session session = null;

        try {
            
            //FILIP HANIK -TODO - we are excluding SYNC RECEIVERS
//            if (!ReceiptType.ASYNCHRONOUS.equals(context.getMessagingBehaviour().getReceiptType())) {
//                return;
//            }
            
            
            // create a receiver that doesn't receive messages sent on the
            // current connection
            noLocal = SessionHelper.createReceiver(
                context, destination, null, true);

            // Create another receiver to verify that messages can be received.
            // Use another session, in case the provider doesn't support
            // multiple receivers to the same topic per session
            session = ConnectionHelper.createSession(context);
            receiver = SessionHelper.createReceiver(
                session, destination, context.getMessagingBehaviour());

            send(destination, count);

            // don't expect any messages from noLocal
            MessagingHelper.receive(context, noLocal, 0);

            // expect all messages from receiver
            receive(receiver, count);
        } finally {
            close(receiver);
            if (noLocal != null) {
                noLocal.close();
            }
            if (session != null) {
                session.close();
            }
        }
    }

}
