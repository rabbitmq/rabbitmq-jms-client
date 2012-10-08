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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: SendReceiveStressTestCase.java,v 1.3 2004/02/03 07:34:54 tanderson Exp $
 */
package org.exolab.jmscts.stress;

import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import org.exolab.jmscts.core.AckType;
import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.CountingListener;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.ReceiptType;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.core.TestProperties;
import org.exolab.jmscts.core.TestStatistics;
import org.exolab.jmscts.core.ThreadedAction;
import org.exolab.jmscts.core.ThreadedActions;
import org.exolab.jmscts.core.ThreadedReceiver;
import org.exolab.jmscts.core.ThreadedSender;
import org.exolab.jmscts.report.types.StatisticType;


/**
 * This class provides functionality for stress tests involving a single
 * producer, and multiple concurrent consumers, for a single destination
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 */
public class SendReceiveStressTestCase extends AbstractSendReceiveTestCase {

    /**
     * The number of messages to send
     */
    private int _count;

    /**
     * The destination to use
     */
    private final String _destination;

    /**
     * The message sender
     */
    private MessageSender _sender;

    /**
     * The number of receivers to user
     */
    private final int _receiverCount;

    /**
     * The message listener
     */
    private CountingListener _listener;

    /**
     * The message receivers
     */
    private MessageReceiver[] _receivers;

    /**
     * The contexts used by the receivers
     */
    private TestContext[] _contexts;

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(
        SendReceiveStressTestCase.class);


    /**
     * Construct a <code>SendReceiveStressTestCase</code> for a specific
     * test case
     *
     * @param name the name of test case
     * @param destination the name of the destination to use
     * @param receivers the number of receivers
     */
    public SendReceiveStressTestCase(String name, String destination,
                                     int receivers) {
        super(name);
        _destination = destination;
        _receiverCount = receivers;
    }

    /**
     * Runs the stress test
     *
     * @throws Exception for any error
     */
    protected void runStress() throws Exception {
        // the timeout defaults to 2 seconds, but can be overriden by defining
        // a property
        final int defaultTimeout = 2000;
        long timeout = TestProperties.getInt(getClass(), "timeout",
                                             defaultTimeout);

        TestContext context = getContext();

        // starts and monitors the completion of the receivers.
        ThreadedActions receive = new ThreadedActions();
        for (int i = 0; i < _receiverCount; ++i) {
            ThreadedAction action = new ThreadedReceiver(
                _receivers[i], timeout, _listener);
            receive.addAction(action);
        }

        TestStatistics stats = context.getStatistics();

        Message message = context.getMessage();
        ThreadedSender send = new ThreadedSender(_sender, message, _count);

        receive.start(); // run the receivers in a separate thread
        send.run();      // run the send in the current thread

        receive.waitForCompletion();

        if (receive.getException() != null) {
            throw receive.getException();
        }
        if (send.getException() != null) {
            throw send.getException();
        }

        int expected = _listener.getExpected();
        int received = _listener.getReceived();
        assertEquals("Expected " + expected + " messages to be received, "
                     + "but got " + received, expected, received);

        stats.log(StatisticType.SEND, _count, send.getElapsedTime());
        stats.log(StatisticType.RECEIVE, _count, receive.getElapsedTime());
    }

    /**
     * Sets up the test
     *
     * @throws Exception for any error
     */
    @Override
    protected void setUp() throws Exception {
        // the number of messages to send/receive defaults to 1000, but
        // can be overriden by defining a property
        final int defaultCount = 1000;
        _count = TestProperties.getInt(getClass(), "count", defaultCount);

        TestContext context = getContext();
        _sender = createSender(_destination);
        _receivers = new MessageReceiver[_receiverCount];
        _contexts = new TestContext[_receiverCount];
        AckType ack = context.getAckType();
        ReceiptType receipt = context.getMessagingBehaviour().getReceiptType();

        // all message handling is handled via a single listener.
        // Queue messages will be shared across all receivers, whereas
        // each topic message will be received by all subscribers.
        int expected;
        if (context.isQueue() && !receipt.equals(ReceiptType.BROWSER)) {
            expected = _count;
        } else {
            expected = _count * _receiverCount;
        }
        if (ack.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE
            && !receipt.equals(ReceiptType.BROWSER)) {
            // for client ack sessions with consumers, ack each message
            _listener = new AckingListener(expected);
        } else {
            _listener = new CountingListener(expected);
        }

        // set up the receivers. These will use the same connection as
        // the sender, but different sessions
        for (int i = 0; i < _receiverCount; ++i) {
            TestContext receiveContext =
                TestContextHelper.createSendReceiveContext(context, false);
            _contexts[i] = receiveContext;

            MessageReceiver receiver =
                createReceiver(receiveContext, _destination);
            _receivers[i] = receiver;
        }
    }

    /**
     * Cleans up after the test
     *
     * @throws Exception for any error
     */
    @Override
    protected void tearDown() throws Exception {
        close(_sender);
        close(_receivers);
        for (int i = 0; i < _contexts.length; ++i) {
            TestContext context = _contexts[i];
            if (context != null) {
                context.getSession().close();
            }
        }
    }

}
