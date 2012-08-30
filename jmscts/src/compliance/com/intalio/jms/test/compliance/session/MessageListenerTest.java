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
 * $Id: MessageListenerTest.java,v 1.8 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests that MessageListeners are invoked serially.
 * This 
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>session.listener.serialization</li>
 * </ul>
 *
 * @author <a href="tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.8 $
 * @see AbstractSendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 */
public class MessageListenerTest extends AbstractSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testSerialInvocation", "session.listener.serialization"}};

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {"dest1", "dest2", "dest3"};

    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against all session transaction types, all
     * aynchronous messaging types, using TextMessage messages
     *
     * @param name the name of test case
     */
    public MessageListenerTest(String name) {
        super(name, MessageTypes.TEXT, REQUIREMENTS);
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
        // test against all session transactional behaviours
        return TestCreator.createSendReceiveTest(MessageListenerTest.class,
                                                 AckTypes.ALL);
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
     * Return the delivery types to run this test case against
     *
     * @return all asynchronous delivery types
     */
    public DeliveryTypes getDeliveryTypes() {
        return DeliveryTypes.ASYNCHRONOUS;
    }

    /**
     * Tests that message listeners are invoked serially.
     * This covers requirements:
     * <ul>
     *   <li>session.listener.serialization</li>
     * </ul>
     * 
     * @throws Exception for any error
     */
    public void testSerialInvocation() throws Exception {
        final int send = 50;
        final int expected = send * DESTINATIONS.length;
        final long sleep = 100;

        MessageConsumer[] consumers = new MessageConsumer[DESTINATIONS.length];

        TestListener listener = new TestListener(expected, sleep);

        TestContext context = getContext();
        MessagingBehaviour behaviour = context.getMessagingBehaviour();
        for (int i = 0; i < DESTINATIONS.length; ++i) {
            Destination destination = getDestination(DESTINATIONS[i]);
            String name = null;
            if (behaviour.getDurable()) {
                name = SessionHelper.getSubscriberName();
            }
            MessageConsumer consumer = SessionHelper.createConsumer(
                context, destination, name);
            consumer.setMessageListener(listener);
            consumers[i] = consumer;
        }
        Message message = context.getMessage();
        for (int i = 0; i < DESTINATIONS.length; ++i) {
            Destination destination = getDestination(DESTINATIONS[i]);
            MessageSender sender = SessionHelper.createSender(
                context, destination);
            sender.send(message, send);
            sender.close();
        }
        Session session = context.getSession();
        if (session.getTransacted()) {
            session.commit();
        }
        
        synchronized (listener) {
            if (listener.getReceived() != expected) {
                listener.wait(expected * sleep * 2);
            }
        }
        for (int i = 0; i < consumers.length; ++i) {
            consumers[i].close();
        }
        if (listener.getFailures() != 0) {
            fail("The listener was not invoked serially");
        }
        if (listener.getReceived() != expected) {
            fail("Received " + listener.getReceived() + 
                 " messages, but expected " + expected);
        }
    }

    private class TestListener implements MessageListener {
        private final int _expected;
        private final long _sleep;
        private boolean _invoked = false;
        private int _failures = 0;
        private int _received = 0;
            
        public TestListener(int expected, long sleep) {
            _expected = expected;
            _sleep = sleep;
        }

        public void onMessage(Message message) {
            synchronized (this) {
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
                Thread.currentThread().sleep(_sleep);
            } catch (InterruptedException ignore) {
            }
            synchronized (this) {
                _invoked = false;
            }
        }
        
        public int getFailures() {
            return _failures;
        }
        
        public int getReceived() {
            return _received;
        }
    } //-- TestListener
            
} //-- MessageListenerTest
