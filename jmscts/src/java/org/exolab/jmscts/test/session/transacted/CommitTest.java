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
 * $Id: CommitTest.java,v 1.9 2004/02/03 21:52:12 tanderson Exp $
 */
package org.exolab.jmscts.test.session.transacted;

import javax.jms.Message;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests session commit functionality
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.9 $
 * @jmscts.session TRANSACTED
 * @jmscts.message TextMessage
 * @jmscts.delivery consumer
 */
public class CommitTest extends AbstractSendReceiveTestCase {

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {"commit1", "commit2",
                                                  "commit3"};

    /**
     * Construct a new <code>CommitTest</code>
     *
     * @param name the name of test case
     */
    public CommitTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite.
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(CommitTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Verifies session commit behaviour
     *
     * @jmscts.requirement session.transactions
     * @throws Exception for any error
     */
    public void testCommit() throws Exception {
        final int count = 10;      // send count messages to each destination

        TestContext context = getContext();
        Session session = context.getSession();
        Message message = context.getMessage();

        // create the senders and receivers
        MessageSender[] senders = createSenders();
        MessageReceiver[] receivers = createReceivers();

        try {
            // send the messages
            send(senders, message, count);

            // verify that the receivers cannot receive the messages until the
            // session commits
            receive(receivers, 0);

            // commit the session. The receivers should be able to receive all
            // messages
            session.commit();

            receive(receivers, count);
        } finally {
            close(senders);
            close(receivers);
        }
    }

    /**
     * Verifies that messages are sent on commit even if their producer has
     * been closed, and that consumed messages are acknowledged even if the
     * consumer that received them has been closed prior to commit
     *
     * @jmscts.requirement session.ack.closedconsumer
     * @throws Exception for any error
     */
    public void testCommitForClosedEndpoint() throws Exception {
        final int count = 10;      // send count messages to each destination

        TestContext context = getContext();
        Session session = context.getSession();
        Message message = context.getMessage();

        // create the senders and receivers
        MessageSender[] senders = createSenders();
        MessageReceiver[] receivers = createReceivers();

        try {
            // send the messages, and close the producers prior to commit
            send(senders, message, count);
            close(senders);

            // commit the session
            session.commit();

            // receive the messages, close the consumers, and then commit.
            receive(receivers, count);
            close(receivers);

            session.commit();

            if (context.isQueueConnectionFactory()) {
                // for queues, verify that no messages are subsequently
                // received (i.e, that the session acknowledged messages
                // for the closed receivers)
                // TODO - could also perform this test for durable topic
                // subscribers but there is no infrastructure in place to test
                // this. This check cannot be performed for non-durable
                // subscribers
                receivers = createReceivers();
                receive(receivers, 0);
            }
        } finally {
            close(senders);
            close(receivers);
        }
    }

}
