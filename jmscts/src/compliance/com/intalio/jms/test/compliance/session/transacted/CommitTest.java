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
 * $Id: CommitTest.java,v 1.6 2003/05/04 14:12:39 tanderson Exp $
 */
package org.exolab.jmscts.test.session.transacted;

import java.util.Iterator;
import java.util.List;

import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.Message;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.session.SessionSendReceiveTestCase;


/**
 * This class tests session commit functionality
 * It covers the following requirements:
 * <ul>
 *   <li>session.transactions</li>
 *   <li>session.ack.closedconsumer</li>
 * </ul>
 *
 * @author <a href="tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see SessionSendReceiveTestCase
 */
public class CommitTest extends SessionSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testCommit", "session.transactions"},
        {"testCommitForClosedEndpoint", "session.ack.closedconsumer"}};

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {"commit1", "commit2", 
                                                  "commit3"};

    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against a transacted session type, all
     * consumer delivery types, and using TextMessage messages
     *
     * @param name the name of test case
     */
    public CommitTest(String name) {
        super(name, MessageTypes.TEXT, DeliveryTypes.CONSUMER, REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner test = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(test);
    }

    /**
     * Sets up the test suite.
     */
    public static Test suite() {
        AckTypes types = AckTypes.TRANSACTIONAL;
        return TestCreator.createSendReceiveTest(CommitTest.class, types);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Tests session commit
     * This covers requirements:
     * <ul>
     *   <li>session.transactions</li>
     * </ul>
     *
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
        } catch (Exception exception) {
            throw exception;
        } finally {
            closeSenders(senders);
            closeReceivers(receivers);
        }
    }

    /**
     * Tests that messages are sent on commit even if their producer has been 
     * closed, and that consumed messages are acknowledged even if the consumer
     * that received them has been closed prior to commit
     * This covers requirements:
     * <ul>
     *  <li>session.ack.closedconsumer</li>
     * </li>
     *
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
            closeSenders(senders);

            // commit the session
            session.commit();

            // receive the messages, close the consumers, and then commit.
            receive(receivers, count);
            closeReceivers(receivers);

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
        } catch (Exception exception) {
            throw exception;
        } finally {
            closeSenders(senders);
            closeReceivers(receivers);
        }
    }

} //-- CommitTest
