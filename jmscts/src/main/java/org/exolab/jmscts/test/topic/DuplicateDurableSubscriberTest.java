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
 * $Id: DuplicateDurableSubscriberTest.java,v 1.6 2004/02/03 21:52:12 tanderson Exp $
 */
package org.exolab.jmscts.test.topic;

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSessionTestCase;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.TestContext;


/**
 * This class tests the behaviour of durable TopicSubscribers<br/>
 * @todo - test duplicate durable subscriber in another JVM
 * @todo - needs to be modified when OpenJMS supports clientId
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractSessionTestCase
 * @jmscts.factory TopicConnectionFactory
 * @jmscts.factory XATopicConnectionFactory
 */
public class DuplicateDurableSubscriberTest extends AbstractSessionTestCase {

    /**
     * The destination to create prior to running the test
     */
    private static final String DESTINATION = "DuplicateDurableSubscriberTest";

    /**
     * Construct a new <code>DupublicDurableSubscriberTest</code>
     *
     * @param name the name of test case
     */
    public DuplicateDurableSubscriberTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSessionTest(
            DuplicateDurableSubscriberTest.class);
    }

    /**
     * Verifies that creating a duplicate durable subscriber in the same
     * session throws JMSException
     *
     * @jmscts.requirement subscriber.durable.unique
     * @throws Exception for any error
     */
    public void testDuplicateSubscriberPerSession() throws Exception {
        final String name = "duplicate";
        TestContext context = getContext();
        TopicSession session = (TopicSession) context.getSession();
        DestinationHelper.destroy(DESTINATION, context.getAdministrator());
        Topic topic = (Topic) DestinationHelper.create(context, DESTINATION);
        TopicSubscriber subscriber1 = session.createDurableSubscriber(
            topic, name);
        try {
            TopicSubscriber subscriber2 = session.createDurableSubscriber(
                topic, name);
            subscriber2.close();
            fail("Managed to create a duplicate durable subscriber in the "
                 + "same session");
        } catch (JMSException expected) {
            // the expected behaviour
        } finally {
            subscriber1.close();
            session.unsubscribe(name);
        }
    }

    /**
     * Verifies that creating a duplicate subscriber in a different session
     * but same connection throws JMSException
     *
     * @jmscts.requirement subscriber.durable.unique
     * @throws Exception for any error
     */
    public void testDuplicateSubscriber() throws Exception {
        final String name = "duplicate";
        TestContext context = getContext();
        TopicSession session1 = (TopicSession) context.getSession();
        DestinationHelper.destroy(DESTINATION, context.getAdministrator());
        Topic topic = (Topic) DestinationHelper.create(context, DESTINATION);
        TopicSubscriber subscriber1 = session1.createDurableSubscriber(
            topic, name);
        TopicSession session2 =
            (TopicSession) ConnectionHelper.createSession(context);
        try {
            TopicSubscriber subscriber2 = session2.createDurableSubscriber(
                topic, name);
            subscriber2.close();
            fail("Managed to create a duplicate durable subscriber in "
                 + "another session, sharing the same connection");
        } catch (JMSException expected) {
            // the expected behaviour
        } finally {
            subscriber1.close();
            session1.unsubscribe(name);
            session2.close();
        }
    }

}
