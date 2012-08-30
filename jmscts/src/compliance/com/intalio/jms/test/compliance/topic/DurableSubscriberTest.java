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
 * $Id: DurableSubscriberTest.java,v 1.5 2003/05/04 14:12:39 tanderson Exp $
 */
package org.exolab.jmscts.test.topic;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.ConnectionFactoryTypes;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.DeliveryType;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.MessagingHelper;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.TestContext;


/**
 * This class tests the behaviour of durable TopicSubscribers
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>subscriber.durable</li>
 *   <li>subscriber.durable.clientId</li>
 *   <li>subscriber.durable.changing</li>
 *   <li>subscriber.durable.unsubscribe</li>
 * </ul>
 * TODO - needs to be modified when OpenJMS supports clientId
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractSessionTestCase
 * @see org.exolab.jmscts.core.SessionTestRunner
 */
public class DurableSubscriberTest extends AbstractSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testDurableSubscriber", "subscriber.durable"},
        {"testChangeSubscription", "subscriber.durable.changing"},
        {"testUnsubscribe", "subscriber.durable.unsubscribe"}};

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {
        "DurableSubscriberTest1", "DurableSubscriberTest2"};

    /**
     * The delivery types supported by this test case
     */
    private static final DeliveryTypes DELIVERY = new DeliveryTypes(
        new DeliveryType[] {
            DeliveryTypes.PERSISTENT_ADMINISTERED_SYNC, 
            DeliveryTypes.PERSISTENT_ADMINISTERED_ASYNC});
    
    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against topic connections, all session types,
     * using text messages and synchronous and asynchronous delivery
     *
     * @param name the name of test case
     */
    public DurableSubscriberTest(String name) {
        super(name, MessageTypes.TEXT, DELIVERY, REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner runner = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(runner);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by 
     * {@link JMSTestRunner}
     */
    public static Test suite() {
        ConnectionFactoryTypes types = ConnectionFactoryTypes.ALL_TOPIC;
        return TestCreator.createSendReceiveTest(DurableSubscriberTest.class,
                                                 types, AckTypes.ALL);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Returns true as this test case only supports durable topic subscribers.
     *
     * @return true
     */
    public boolean getDurableOnly() {
        return true;
    }

    /**
     * Tests that a durable subscriber can be created for a session, closed, 
     * and recreated. This covers requirements:
     * <ul>
     *   <li>subscriber.durable</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testDurableSubscriber() throws Exception {
        final String name = "durableSubscriber";
        final int count = 10;

        TestContext context = getContext();
        Destination destination = getDestination(DESTINATIONS[0]);
        MessageReceiver subscriber = null;

        subscriber = SessionHelper.createReceiver(context, destination, name,
                                                  null, false);
        MessagingHelper.send(context, destination, count);

        subscriber.close();

        subscriber = SessionHelper.createReceiver(context, destination, name,
                                                  null, false);
        MessagingHelper.receive(context, subscriber, count);

        subscriber.remove();
    }

    /**
     * Test that a client can change an existing durable subscription by 
     * creating a durable subscriber with the same name and a new topic.
     *
     * This covers requirements:
     * <ul>
     *   <li>subscriber.durable.changing</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testChangeSubscription() throws Exception {
        final String name = "changeSubscription";
        final int count = 10;

        TestContext context = getContext();
        Destination destination1 = getDestination(DESTINATIONS[0]);
        Destination destination2 = getDestination(DESTINATIONS[1]);
        MessageReceiver subscriber = null;

        subscriber = SessionHelper.createReceiver(context, destination1, name,
                                                  null, false);
        MessagingHelper.send(context, destination1, count);

        subscriber.close();

        subscriber = SessionHelper.createReceiver(context, destination2, name,
                                                  null, false);
        // verify that the subscription has changed by ensuring that no
        // messages are received.
        MessagingHelper.receive(context, subscriber, 0);
        MessagingHelper.send(context, destination2, count);
        MessagingHelper.receive(context, subscriber, count);

        subscriber.remove();
    }

    /**
     * Tests that a durable subscriber can be created for a session, 
     * unsubscribed, and recreated
     * This covers requirements:
     * <ul>
     *   <li>subscriber.durable.unsubscribe</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testUnsubscribe() throws Exception {
        final String name = "unsubscribe";
        final int count = 10;

        TestContext context = getContext();
        TopicSession session = (TopicSession) context.getSession();
        Destination destination = getDestination(DESTINATIONS[0]);
        MessageReceiver subscriber = null;

        subscriber = SessionHelper.createReceiver(context, destination, name,
                                                  null, false);
        MessagingHelper.send(context, destination, count);

        subscriber.close();
        try {
            session.unsubscribe(name);
        } catch (Exception exception) {
            fail("Failed to unsubscribe durable subscriber");
        }
        
        // resubscribe. The specification does not provide reliability 
        // requirements, so no guarantee can be made if the subscriber will
        // receive prior messages.
        subscriber = SessionHelper.createReceiver(context, destination, name,
                                                  null, false);
        subscriber.remove();
    }
    
} //-- DurableSubscriberTest
