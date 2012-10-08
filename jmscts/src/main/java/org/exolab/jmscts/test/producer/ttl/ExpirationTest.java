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
 * $Id: ExpirationTest.java,v 1.5 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.test.producer.ttl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.TestProperties;


/**
 * This class tests message expiration.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 */
public class ExpirationTest extends AbstractSendReceiveTestCase {

    /**
     * The default destination.
     */
    private static final String DESTINATION = "ExpirationTest";

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(
        ExpirationTest.class);

    /**
     * The time in milliseconds to wait for the JMS provider to collect expired
     * This can be set for providers which collect expired messages
     * periodically, rather than at the moment they expire, and is a
     * workaround for section 3.9 of the specification which states:
     *  "Clients should not receive messages which have expired, however JMS
     *   does not guarantee that this will not happen."
     */
    private static final long EXPIRATION_INTERVAL =
        TestProperties.getLong(ExpirationTest.class, "expirationInterval", 0);


    /**
     * Construct a new <code>ExpirationTest</code>.
     *
     * @param name the name of test case
     */
    public ExpirationTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite.
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(ExpirationTest.class);
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
     * Verifies that messages expire.
     * <p/>
     * It sends two groups of messages - one group which expires after
     * 1 second, and another which don't expire, and verifies that after
     * the first group should have expired, only the non-expiring group
     * can be received.
     * <p/>
     * If the JMS provider doesn't support immediate expiration of messages,
     * the
     * <em>
     *   org.exolab.jmscts.test.producer.ttl.ExpirationTest.expirationInterval
     * </em>
     * property should be configured in <em>jmscts.properties</em> to delay
     * the test until the expired messages are collected.
     *
     * @jmscts.requirement message.expiration.zero
     * @jmscts.requirement message.expiration.expired
     * @jmscts.message TextMessage
     * @throws Exception for any error
     */
    public void testExpiration() throws Exception {
        final long timeToLive = 1000; // i.e, 1 second
        final int messages = 10;      // no. of messages to send
        final int receivers = 1;      // no. of receivers.
        checkExpiration(timeToLive, messages, receivers);
    }

    /**
     * Verifies that a single message expires.
     * <p/>
     * It sends two messages - one which expires after 1 second, and another
     * which doesn't expire, and verifies that after the first should have
     * expired, only the non-expiring message can be received.
     * <p/>
     * If the JMS provider doesn't support immediate expiration of messages,
     * the
     * <em>
     *   org.exolab.jmscts.test.producer.ttl.ExpirationTest.expirationInterval
     * </em>
     * property should be configured in <em>jmscts.properties</em> to delay
     * the test until the expired messages are collected.
     *
     * @jmscts.requirement message.expiration.zero
     * @jmscts.requirement message.expiration.expired
     * @jmscts.message TextMessage
     * @throws Exception for any error
     */
    public void testSingleMessageExpiration() throws Exception {
        final long timeToLive = 1000; // i.e, 1 second
        final int messages = 1;       // no. of messages to send
        final int receivers = 1;      // no. of receivers.
        checkExpiration(timeToLive, messages, receivers);
    }

    /**
     * Verifies that messages to topics expire, when there is more than
     * one subscriber.
     * <p/>
     * It sends two groups of messages - one group which expires after
     * 1 second, and another which don't expire, and verifies that after
     * the first group should have expired, only the non-expiring group
     * can be received.
     * <p/>
     * If the JMS provider doesn't support immediate expiration of messages,
     * the
     * <em>
     *   org.exolab.jmscts.test.producer.ttl.ExpirationTest.expirationInterval
     * </em>
     * property should be configured in <em>jmscts.properties</em> to delay
     * the test until the expired messages are collected.
     *
     * @jmscts.requirement message.expiration.zero
     * @jmscts.requirement message.expiration.expired
     * @jmscts.factory TopicConnectionFactory
     * @jmscts.factory XATopicConnectionFactory
     * @jmscts.message ObjectMessage
     * @throws Exception for any error
     */
    public void testExpiration2Subscribers() throws Exception {
        final long timeToLive = 1000; // i.e, 1 second
        final int messages = 10;      // no. of messages to send
        final int subscribers = 2;    // no. of subscribers.
        checkExpiration(timeToLive, messages, subscribers);
    }

    /**
     * Verifies that messages to topics expire, when one subscriber is
     * durable, and the other non-durable.
     * <p/>
     * It sends two groups of messages - one group which expires after
     * 1 second, and another which don't expire, and verifies that after
     * the first group should have expired, only the non-expiring group
     * can be received.
     * <p/>
     * If the JMS provider doesn't support immediate expiration of messages,
     * the
     * <em>
     *   org.exolab.jmscts.test.producer.ttl.ExpirationTest.expirationInterval
     * </em>
     * property should be configured in <em>jmscts.properties</em> to delay
     * the test until the expired messages are collected.
     *
     * @jmscts.requirement message.expiration.zero
     * @jmscts.requirement message.expiration.expired
     * @jmscts.factory TopicConnectionFactory
     * @jmscts.factory XATopicConnectionFactory
     * @jmscts.delivery administered-consumer
     * @jmscts.durableonly true
     * @jmscts.message MapMessage
     * @throws Exception for any error
     */
    public void testExpiration2DiffSubscribers() throws Exception {
        final long timeToLive = 1000; // i.e, 1 second
        final int messages = 10;      // no. of messages to send

        TestContext context = getContext();
        MessageReceiver[] subscribers = new MessageReceiver[2];

        // create a context for the non-durable subscriber
        MessagingBehaviour behaviour = new MessagingBehaviour(
            context.getMessagingBehaviour());
        behaviour.setDurable(false);
        TestContext nondurable = TestContextHelper.createSendReceiveContext(
            context, false, behaviour);
        try {
            subscribers[0] = createReceiver(DESTINATION); // durable subscriber
            subscribers[1] = createReceiver(nondurable, DESTINATION);
            checkExpiration(timeToLive, messages, subscribers);
        } finally {
            close(subscribers);
            // new session was allocated, so close it.
            nondurable.getParent().close();
        }
    }

    /**
     * Sends <code>messageCount</code> messages with time-to-live
     * <code>timeToLive</code>, and <code>messageCount</code> messages which
     * don't expire, and verifies that after sleeping
     * <code>timeToLive + EXPIRATION_INTERVAL</code> milliseconds, only the
     * non-expiring messages may be received.
     *
     * @param timeToLive the time-to-live for the expiring messages
     * @param messageCount the no. of messages to send
     * @param receiverCount the no. of receivers. For queues, should be 1.
     * @throws Exception for any error
     */
    private void checkExpiration(long timeToLive, int messageCount,
                                 int receiverCount) throws Exception {
        MessageReceiver[] receivers = new MessageReceiver[receiverCount];

        try {
            for (int i = 0; i < receivers.length; ++i) {
                receivers[i] = createReceiver(DESTINATION);
            }
            checkExpiration(timeToLive, messageCount, receivers);
        } finally {
            close(receivers);
        }
    }

    /**
     * Sends <code>messageCount</code> messages with time-to-live
     * <code>timeToLive</code>, and <code>messageCount</code> messages which
     * don't expire, and verifies that after sleeping
     * <code>timeToLive + EXPIRATION_INTERVAL</code> milliseconds, only the
     * non-expiring messages may be received.
     *
     * @param timeToLive the time-to-live for the expiring messages
     * @param messageCount the no. of messages to send
     * @param receivers the receivers to use.
     * @throws Exception for any error
     */
    private void checkExpiration(long timeToLive, int messageCount,
                                 MessageReceiver[] receivers)
        throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();
        Message message = context.getMessage();
        HashSet<String> expiringIds = new HashSet<String>();
        HashSet<String> nonExpiringIds = new HashSet<String>();

        MessageSender sender = null;

        try {
            sender = createSender(DESTINATION);

            // send messages that expire
            for (int i = 0; i < messageCount; ++i) {
                sender.send(message, 1, timeToLive);
                expiringIds.add(message.getJMSMessageID());
            }

            // send messages that don't expire
            for (int i = 0; i < messageCount; ++i) {
                sender.send(message, 1, Message.DEFAULT_TIME_TO_LIVE);
                nonExpiringIds.add(message.getJMSMessageID());
            }

            if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
                session.commit();
            }

            assertEquals("Duplicate JMSMessageID allocated", messageCount,
                         expiringIds.size());

            assertEquals("Duplicate JMSMessageID allocated", messageCount,
                         nonExpiringIds.size());

            // now wait to allow the messages to expire. All of the non
            // expiring messages should be received, and none of the expiring
            // messages
            long sleepTime = EXPIRATION_INTERVAL + timeToLive;
            try {
                log.debug("Sleeping for " + sleepTime
                          + "ms to allow messages to expire");
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignore) {
                // no-op
            }

            for (int i = 0; i < receivers.length; ++i) {
                List<?> messages = receive(receivers[i], messageCount);
                Iterator<?> iterator = messages.iterator();
                while (iterator.hasNext()) {
                    Message received = (Message) iterator.next();
                    String id = received.getJMSMessageID();
                    if (!nonExpiringIds.contains(id)) {
                        if (expiringIds.contains(id)) {
                            fail("Received a message which should have "
                                 + "expired: " + id);
                        } else {
                            fail("Received a message which wasn't sent by this"
                                 + " test: " + id);
                        }
                    }
                }
                // make sure messages are acknowledged, so the destination
                // can be safely destroyed prior to session closure
                acknowledge(messages);
            }
            if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
                session.commit();
            }
        } finally {
            close(sender);
        }
    }

}
