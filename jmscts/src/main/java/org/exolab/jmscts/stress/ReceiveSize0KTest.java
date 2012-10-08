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
 *    please contact tma@netspace.net.
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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: ReceiveSize0KTest.java,v 1.4 2004/02/03 21:52:08 tanderson Exp $
 */
package org.exolab.jmscts.stress;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AckType;
import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.CountingListener;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.MessageCreator;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.TestProperties;
import org.exolab.jmscts.core.TestStatistics;
import org.exolab.jmscts.core.ThreadedReceiver;
import org.exolab.jmscts.core.ThreadedSender;
import org.exolab.jmscts.report.types.StatisticType;


/**
 * Performs a stress test using one producer, one consumer and empty
 * messages.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @jmscts.message all
 * @jmscts.session AUTO_ACKNOWLEDGE
 * @jmscts.session CLIENT_ACKNOWLEDGE
 * @jmscts.session DUPS_OK_ACKNOWLEDGE
 * @jmscts.delivery consumer
 */
public class ReceiveSize0KTest extends AbstractSendReceiveTestCase {

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(
        ReceiveSize0KTest.class);

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "ReceiveSize0KTest";


    /**
     * Construct a <code>ReceiveSize0KTest</code> for a specific
     * test case
     *
     * @param name the name of test case
     */
    public ReceiveSize0KTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.StressTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(ReceiveSize0KTest.class);
    }

    /**
     * Determines if messages should be pre-created and populated for the test.
     *
     * @return <code>false</code>
     */
    @Override
    public boolean shouldCreateMessage() {
        return false;
    }

    /**
     * Get the message populator. This implementation returns <code>null</code>
     *
     * @return <code>null</code>
     */
    @Override
    public MessagePopulator getMessagePopulator() {
        return null;
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
     * Performs a stress test using:
     * <ul>
     *   <li>a single consumer</li>
     *   <li>empty messages</li>
     * </ul>
     * For <em>CLIENT_ACKNOWLEDGE</em> sessions, each message is acknowledged.
     *
     * @throws Exception for any error
     */
    public void test() throws Exception {
        final int defaultCount = 1000;
        final int count = TestProperties.getInt(getClass(), "count",
                                                defaultCount);

        TestContext context = getContext();

        Session session = context.getSession();
        MessageCreator creator = new MessageCreator(session, null);
        Class<?> messageType = context.getMessageType();
        Destination destination = getDestination(DESTINATION);
        MessageSender sender = SessionHelper.createSender(
            context, destination);
        MessageReceiver receiver = SessionHelper.createReceiver(
            context, destination);

        CountingListener listener;
        AckType ack = context.getAckType();
        if (ack.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE) {
            listener = new AckingListener(count);
        } else {
            listener = new CountingListener(count);
        }

        TestStatistics stats = context.getStatistics();

        Message message = creator.create(messageType);

        ThreadedSender send = new ThreadedSender(sender, message, count);
        ThreadedReceiver receive = new ThreadedReceiver(
            receiver, 0, listener);

        send.run();      // run the send in the current thread

        if (send.getException() != null) {
            throw send.getException();
        }

        receive.run();   // run the receive after the send has completed

        if (receive.getException() != null) {
            throw receive.getException();
        }

        int received = listener.getReceived();
        assertEquals("Expected " + count + " messages to be received, but got "
                     + received, count, received);

        stats.log(StatisticType.RECEIVE, count, receive.getElapsedTime());

        receiver.remove();
    }

}
