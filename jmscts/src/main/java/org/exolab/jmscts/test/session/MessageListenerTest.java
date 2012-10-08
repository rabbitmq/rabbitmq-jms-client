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
 * $Id: MessageListenerTest.java,v 1.9 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.test.session;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests that MessageListeners are invoked serially.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.9 $
 * @see AbstractSendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 * @jmscts.message TextMessage
 * @jmscts.delivery asynchronous
 */
public class MessageListenerTest extends AbstractSendReceiveTestCase {

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {"dest1", "dest2", "dest3"};

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(MessageListenerTest.class);


    /**
     * Contruct a new <code>MessageListenerTest</code>
     *
     * @param name the name of test case
     */
    public MessageListenerTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite.
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(MessageListenerTest.class);
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
     * Verifies that message listeners are invoked serially.
     *
     * @jmscts.requirement session.listener.serialization
     * @throws Exception for any error
     */
    public void testSerialInvocation() throws Exception {
        final int send = 50;
        final int expected = send * DESTINATIONS.length;
        final long sleep = 100;

        TestContext context = getContext();

        TestListener listener = new TestListener(expected, sleep);
        MessageConsumer[] consumers = createConsumers();

        for (int i = 0; i < consumers.length; ++i) {
            consumers[i].setMessageListener(listener);
        }
        Message message = context.getMessage();
        MessageSender[] senders = createSenders();
        for (int i = 0; i < senders.length; ++i) {
            senders[i].send(message, send);
        }
        close(senders);

        Session session = context.getSession();
        if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
            session.commit();
        }

        // wait for the messages to be received
        synchronized (listener) {
            if (listener.getReceived() != expected) {
                listener.wait(expected * sleep * 2);
            }
        }
        close(consumers);

        // acknowledge the messages, so that the destinations can be destroyed
        // cleanly on test completion
        Message last = listener.getMessage();
        if (last != null) {
            last.acknowledge();
        }

        if (listener.getFailures() != 0) {
            fail("The listener was not invoked serially");
        }
        if (listener.getReceived() != expected) {
            fail("Received " + listener.getReceived()
                 + " messages, but expected " + expected);
        }
    }

    /**
     * MessageListener implementation which checks if
     * onMessage() is invoked by multiple threads
     */
    private class TestListener implements MessageListener {
        /**
         * The no. of expected messages
         */
        private final int _expected;

        /**
         * The time to wait for in onMessage(), before returning
         */
        private final long _sleep;

        /**
         * Determines if onMessage() has been invoked by another thread
         */
        private boolean _invoked = false;

        /**
         * The no. of times onMessage() has been invoked by multiple threads
         */
        private int _failures = 0;

        /**
         * The no. of received messages
         */
        private int _received = 0;

        /**
         * The last received message
         */
        private Message _message;


        /**
         * Construct a new <code>TestListener</code>
         *
         * @param expected the no.of messages to receive
         * @param sleep the time to wait in onMessage()
         */
        public TestListener(int expected, long sleep) {
            _expected = expected;
            _sleep = sleep;
        }

        /**
         * Handle a message
         *
         * @param message the message
         */
        @Override
        public void onMessage(Message message) {
            synchronized (this) {
                _message = message;
                if (_invoked) {
                    _failures++;
                }
                _invoked = true;
                ++_received;
                if (_received == _expected) {
                    notify();
                }
            }
            try {
                Thread.sleep(_sleep);
            } catch (InterruptedException ignore) {
                log.debug("TestListener interrupted while sleeping "
                          + "- continuing");
            }
            synchronized (this) {
                _invoked = false;
            }
        }

        /**
         * Returns the no. of times {@link #onMessage} was invoked by
         * more than one thread
         *
         * @return the no. of failures
         */
        public int getFailures() {
            return _failures;
        }

        /**
         * Returns the no. of received messages
         *
         * @return the no. of received messages
         */
        public int getReceived() {
            return _received;
        }

        /**
         * Returns the last messsage received
         *
         * @return the last message received
         */
        public Message getMessage() {
            return _message;
        }
    }

}
