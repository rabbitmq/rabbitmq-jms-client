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
 * $Id: TopicPublisherTest.java,v 1.3 2004/02/03 07:33:32 tanderson Exp $
 */
package org.exolab.jmscts.test.producer.ttl;

import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

import junit.framework.Test;

import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of the time-to-live methods and their
 * affect on the JMSExpiration, for <code>TopicPublisher</code>s
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @jmscts.factory TopicConnectionFactory
 * @jmscts.factory XATopicConnectionFactory
 */
public class TopicPublisherTest extends TimeToLiveTestCase {

    /**
     * The default destination
     */
    private static final String DESTINATION = "TopicPublisherTest";

    /**
     * Time to live, in milliseconds (ie. 50 secs)
     */
    private static final long TTL = 50000;


    /**
     * Construct a new <code>TopicPublisherTest</code>
     *
     * @param name the name of test case
     */
    public TopicPublisherTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(TopicPublisherTest.class);
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
     * Verifies that the default time-to-live for a publisher equals
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

        // construct a publisher
        TopicPublisher publisher = (TopicPublisher)
            SessionHelper.createProducer(context.getSession(),
                                         getDestination(DESTINATION));

        assertEquals("Default time-to-live incorrect for TopicPublisher",
                     timeToLive, publisher.getTimeToLive());

        verifyPublish(publisher, context.getMessage(), timeToLive);
        publisher.close();
    }

    /**
     * Verifies that the default time-to-live for a publisher constructed
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
    public void testDefaultTTLForAnonTopic() throws Exception {
        final long timeToLive = 0;
        TestContext context = getContext();
        Topic topic = (Topic) getDestination(DESTINATION);
        TopicPublisher publisher = (TopicPublisher)
            SessionHelper.createProducer(context.getSession(), null);

        assertEquals("Default time-to-live incorrect for TopicPublisher "
                     + "created with an anonymous topic",
                     timeToLive, publisher.getTimeToLive());

        verifyPublishWithTopic(publisher, topic, context.getMessage(),
                               timeToLive);
        publisher.close();
    }

    /**
     * Verifies that the time-to-live can be set on a publisher,
     * that the value is used when no time-to-live is provided when a
     * message is sent, and that the JMSExpiration property on the received
     * message equals that sent.
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

        // construct a publisher, and set its ttl
        TopicPublisher publisher = (TopicPublisher)
            SessionHelper.createProducer(context.getSession(),
                                         getDestination(DESTINATION));
        publisher.setTimeToLive(timeToLive);
        assertEquals("Failed to set the time-to-live on TopicPublisher",
                     timeToLive, publisher.getTimeToLive());

        verifyPublish(publisher, context.getMessage(), timeToLive);
        publisher.close();
    }

    /**
     * Verifies that the time-to-live can be set on a publisher created
     * with no default topic, that the value is used when no time-to-live
     * is provided when a message is sent, and that the JMSExpiration property
     * on the received message equals that sent.
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.requirement producer.ttl.set
     * @jmscts.message ObjectMessage
     * @throws Exception for any error
     */
    public void testSetTTLWithAnonTopic() throws Exception {
        final long timeToLive = TTL;
        TestContext context = getContext();
        Topic topic = (Topic) getDestination(DESTINATION);

        // construct a publisher, and set its ttl
        TopicPublisher publisher = (TopicPublisher)
            SessionHelper.createProducer(context.getSession(), null);
        publisher.setTimeToLive(timeToLive);

        assertEquals("Failed to set the time-to-live on TopicPublisher "
                     + "with an anonymous destination",
                     timeToLive, publisher.getTimeToLive());

        verifyPublishWithTopic(publisher, topic, context.getMessage(),
                               timeToLive);
        publisher.close();
    }

    /**
     * Verifies that the time-to-live set at publication is used when a
     * message is sent, and that the JMSExpiration property on the received
     * message equals that sent.
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.message StreamMessage
     * @throws Exception for any error
     */
    public void testPublishWithTTL() throws Exception {
        final long timeToLive = TTL;
        TestContext context = getContext();
        Message message = context.getMessage();
        int deliveryMode = context.getMessagingBehaviour().getDeliveryMode();

        // construct a publisher
        TopicPublisher publisher = (TopicPublisher)
            SessionHelper.createProducer(context.getSession(),
                                         getDestination(DESTINATION));

        // construct a receiver to consume the message
        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            // now verify that a message sent via the publisher has its
            // JMSExpiration set correctly
            long start = System.currentTimeMillis();
            publisher.publish(message, deliveryMode, 1, timeToLive);
            long end = System.currentTimeMillis();
            checkExpiration(message, start, end, timeToLive);

            // now receive the message and verify it has the same JMSExpiration
            checkSameExpiration(message, receiver);
        } finally {
            close(receiver);
            publisher.close();
        }
    }

    /**
     * Verifies that the time-to-live set at publication is used when a
     * message is sent, using a TopicPublisher created with no default topic,
     * and that the JMSExpiration property on the received message equals
     * that sent.
     *
     * @jmscts.requirement message.expiration.send
     * @jmscts.requirement message.expiration.receive
     * @jmscts.message TextMessage
     * @throws Exception for any error
     */
    public void testPublishWithTTLAndAnonTopic() throws Exception {
        final long timeToLive = TTL;
        TestContext context = getContext();
        Message message = context.getMessage();
        int deliveryMode = context.getMessagingBehaviour().getDeliveryMode();
        Topic topic = (Topic) getDestination(DESTINATION);

        // construct a publisher
        TopicPublisher publisher = (TopicPublisher)
            SessionHelper.createProducer(context.getSession(), null);

        // construct a receiver to consume the message
        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            // now verify that a message sent via the publisher has its
            // JMSExpiration set correctly
            long start = System.currentTimeMillis();
            publisher.publish(topic, message, deliveryMode, 1, timeToLive);
            long end = System.currentTimeMillis();
            checkExpiration(message, start, end, timeToLive);

            // now receive the message and verify it has the same JMSExpiration
            checkSameExpiration(message, receiver);
        } finally {
            close(receiver);
            publisher.close();
        }
    }

    /**
     * Publishes a message using the TopicPublisher.publish(Message) method
     * and verifies that the JMSExpiration property is set correctly on
     * publish, and is the same on receipt
     *
     * @param publisher the publisher
     * @param message the message to publish
     * @param timeToLive the expected time-to-live
     * @throws Exception for any error
     */
    protected void verifyPublish(TopicPublisher publisher, Message message,
                                 long timeToLive)
        throws Exception {

        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            long start = System.currentTimeMillis();
            publisher.publish(message);
            long end = System.currentTimeMillis();
            checkExpiration(message, start, end, timeToLive);

            // now receive the message and verify it has the same JMSExpiration
            checkSameExpiration(message, receiver);
        } finally {
            close(receiver);
        }
    }

    /**
     * Publishes a message using the TopicPublisher.publish(Topic, Message)
     * method and verifies that the JMSExpiration property is set correctly
     *
     * @param publisher the publisher
     * @param topic the topic to publish to
     * @param message the message to publish
     * @param timeToLive the expected time-to-live
     * @throws Exception for any error
     */
    protected void verifyPublishWithTopic(TopicPublisher publisher,
                                          Topic topic, Message message,
                                          long timeToLive)
        throws Exception {

        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            long start = System.currentTimeMillis();
            publisher.publish(topic, message);
            long end = System.currentTimeMillis();
            checkExpiration(message, start, end, timeToLive);

            // now receive the message and verify it has the same JMSExpiration
            checkSameExpiration(message, receiver);
        } finally {
            close(receiver);
        }
    }

}
