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
 * $Id: QueueSenderTest.java,v 1.3 2004/02/03 07:33:32 tanderson Exp $
 */
package org.exolab.jmscts.test.producer.ttl;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;

import junit.framework.Test;

import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of the time-to-live methods and their
 * affect on the JMSExpiration, for <code>QueueSender</code>s
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @jmscts.factory QueueConnectionFactory
 * @jmscts.factory XAQueueConnectionFactory
 */
public class QueueSenderTest extends TimeToLiveTestCase {

    /**
     * The default destination
     */
    private static final String DESTINATION = "QueueSenderTest";

    /**
     * Time to live, in milliseconds (ie. 50 secs)
     */
    private static final long TTL = 50000;


    /**
     * Construct a new <code>QueueSenderTest</code>
     *
     * @param name the name of test case
     */
    public QueueSenderTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(QueueSenderTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[] {DESTINATION};
    }

    /**
     * Verifies that the default time-to-live for a sender equals
     * <code>0</code>, and that the JMSExpiration property is set to
     * <code>0</code> on send and receive when the default time-to-live is
     * used.
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.requirement message.expiration.zero
     * @jmscts.requirement producer.ttl.default
     * @jmscts.message Message
     * @throws Exception for any error
     */
    public void testDefaultTTL() throws Exception {
        final long timeToLive = 0;
        TestContext context = getContext();

        // construct a sender
        QueueSender sender = (QueueSender)
            SessionHelper.createProducer(context.getSession(),
                                         getDestination(DESTINATION));

        assertEquals("Default time-to-live incorrect for QueueSender",
                     timeToLive, sender.getTimeToLive());

        verifySend(sender, context.getMessage(), timeToLive);
        sender.close();
    }

    /**
     * Verifies that the default time-to-live for a sender constructed
     * with no destination equals <code>0</code>, and that the JMSExpiration
     * property is set to <code>0</code> on send and receive when the default
     * time-to-live is used.
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.requirement message.expiration.zero
     * @jmscts.requirement producer.ttl.default
     * @jmscts.message BytesMessage
     * @throws Exception for any error
     */
    public void testDefaultTTLForAnonQueue() throws Exception {
        final long timeToLive = 0;
        TestContext context = getContext();
        Queue queue = (Queue) getDestination(DESTINATION);
        QueueSender sender = (QueueSender)
            SessionHelper.createProducer(context.getSession(), null);

        assertEquals("Default time-to-live incorrect for QueueSender "
                     + "created with an anonymous queue",
                     timeToLive, sender.getTimeToLive());

        verifySendWithQueue(sender, queue, context.getMessage(),
                               timeToLive);
        sender.close();
    }

    /**
     * Verifies that the time-to-live can be set on a sender, that the value is
     * used when no time-to-live is provided when a message is sent,
     * and that the JMSExpiration message property is set correctly on send and
     * receive
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.requirement producer.ttl.set
     * @jmscts.message MapMessage
     * @throws Exception for any error
     */
    public void testSetTTL() throws Exception {
        final long timeToLive = TTL;
        TestContext context = getContext();

        // construct a sender, and set its ttl
        QueueSender sender = (QueueSender)
            SessionHelper.createProducer(context.getSession(),
                                         getDestination(DESTINATION));
        sender.setTimeToLive(timeToLive);
        assertEquals("Failed to set the time-to-live on QueueSender",
                     timeToLive, sender.getTimeToLive());

        verifySend(sender, context.getMessage(), timeToLive);
        sender.close();
    }

    /**
     * Verifies that the time-to-live can be set on a sender created
     * with no default queue, that the value is used when no time-to-live
     * is provided when a message is sent, and that the JMSExpiration property
     * on the received message equals that sent
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.requirement producer.ttl.set
     * @jmscts.message ObjectMessage
     * @throws Exception for any error
     */
    public void testSetTTLWithAnonQueue() throws Exception {
        final long timeToLive = TTL;
        TestContext context = getContext();
        Queue queue = (Queue) getDestination(DESTINATION);

        // construct a sender, and set its ttl
        QueueSender sender = (QueueSender)
            SessionHelper.createProducer(context.getSession(), null);
        sender.setTimeToLive(timeToLive);

        assertEquals("Failed to set the time-to-live on QueueSender "
                     + "with an anonymous destination",
                     timeToLive, sender.getTimeToLive());

        verifySendWithQueue(sender, queue, context.getMessage(), timeToLive);
        sender.close();
    }

    /**
     * Verifies that the time-to-live set at send is used when a
     * message is sent, and that the JMSExpiration property on the received
     * message equals that sent
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.message StreamMessage
     * @throws Exception for any error
     */
    public void testSendWithTTL() throws Exception {
        final long timeToLive = TTL;
        TestContext context = getContext();
        Message message = context.getMessage();
        int deliveryMode = context.getMessagingBehaviour().getDeliveryMode();

        // construct a sender
        QueueSender sender = (QueueSender)
            SessionHelper.createProducer(context.getSession(),
                                         getDestination(DESTINATION));

        // construct a receiver to consume the message
        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            // now verify that a message sent via the sender has its
            // JMSExpiration set correctly
            long start = System.currentTimeMillis();
            sender.send(message, deliveryMode, 1, timeToLive);
            long end = System.currentTimeMillis();
            checkExpiration(message, start, end, timeToLive);

            // now receive the message and verify it has the same JMSExpiration
            checkSameExpiration(message, receiver);
        } finally {
            close(receiver);
            sender.close();
        }
    }

    /**
     * Verifies that the time-to-live set at send is used when a message is
     * sent, using a QueueSender created with no default queue, and that the
     * JMSExpiration property on the received message equals that sent
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.message TextMessage
     * @throws Exception for any error
     */
    public void testSendWithTTLAndAnonQueue() throws Exception {
        final long timeToLive = TTL;
        TestContext context = getContext();
        Message message = context.getMessage();
        int deliveryMode = context.getMessagingBehaviour().getDeliveryMode();
        Queue queue = (Queue) getDestination(DESTINATION);

        // construct a sender
        QueueSender sender = (QueueSender)
            SessionHelper.createProducer(context.getSession(), null);

        // construct a receiver to consume the message
        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            // now verify that a message sent via the sender has its
            // JMSExpiration set correctly
            long start = System.currentTimeMillis();
            sender.send(queue, message, deliveryMode, 1, timeToLive);
            long end = System.currentTimeMillis();
            checkExpiration(message, start, end, timeToLive);

            // now receive the message and verify it has the same JMSExpiration
            checkSameExpiration(message, receiver);
        } finally {
            close(receiver);
            sender.close();
        }
    }

    /**
     * Sendes a message using the QueueSender.send(Message) method
     * and verifies that the JMSExpiration property is set correctly on
     * send, and is the same on receipt
     *
     * @param sender the sender
     * @param message the message to send
     * @param timeToLive the expected time-to-live
     * @throws Exception for any error
     */
    protected void verifySend(QueueSender sender, Message message,
                              long timeToLive)
        throws Exception {

        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            long start = System.currentTimeMillis();
            sender.send(message);
            long end = System.currentTimeMillis();
            checkExpiration(message, start, end, timeToLive);

            // now receive the message and verify it has the same JMSExpiration
            checkSameExpiration(message, receiver);
        } finally {
            close(receiver);
        }
    }

    /**
     * Sendes a message using the QueueSender.send(Queue, Message)
     * method and verifies that the JMSExpiration property is set correctly
     *
     * @param sender the sender
     * @param queue the queue to send to
     * @param message the message to send
     * @param timeToLive the expected time-to-live
     * @throws Exception for any error
     */
    protected void verifySendWithQueue(QueueSender sender, Queue queue,
                                       Message message, long timeToLive)
        throws Exception {

        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            long start = System.currentTimeMillis();
            sender.send(queue, message);
            long end = System.currentTimeMillis();
            checkExpiration(message, start, end, timeToLive);

            // now receive the message and verify it has the same JMSExpiration
            checkSameExpiration(message, receiver);
        } finally {
            close(receiver);
        }
    }

}
