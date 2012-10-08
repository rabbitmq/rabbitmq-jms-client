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
 * $Id: CloseTest.java,v 1.6 2004/02/03 21:52:11 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.IllegalStateException;
import javax.jms.QueueSession;
import javax.jms.Topic;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSessionTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.MethodInvoker;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of <code>Session.close</code>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractSessionTestCase
 */
public class CloseTest extends AbstractSessionTestCase {

    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(CloseTest.class);


    /**
     * Construct a new <code>CloseTest</code>
     *
     * @param name the name of test case
     */
    public CloseTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSessionTest(CloseTest.class);
    }

    /**
     * Returns if this test can share resources with other test cases.
     * This implementation always returns <code>false</code>, to
     * ensure that a new session is created for each test.
     *
     * @return <code>false</code>
     */
    @Override
    public boolean share() {
        return false;
    }

    /**
     * Verifies that IllegalStateException is thrown for any Session method
     * (except Session.close()) when invoking methods on a closed session.
     *
     * @jmscts.requirement session.close
     * @throws Exception for any error
     */
    public void testExceptionOnClose() throws Exception {
        final String name = "CloseTest";
        TestContext context = getContext();
        Session session = context.getSession();
        session.close();

        // set up the destination
        DestinationHelper.destroy(name, context.getAdministrator());
        Destination destination = DestinationHelper.create(
            name, context.isQueue(), context.getAdministrator());

        try {
            invokeSessionMethods(destination);
        } finally {
            DestinationHelper.destroy(name, context.getAdministrator());
        }
    }

    /**
     * Verifies that closing a closed session has no effect.
     *
     * @jmscts.requirement session.close.closed
     * @throws Exception for any error
     */
    public void testCloseForClosedSession() throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();
        session.close();

        try {
            session.close();
        } catch (Exception exception) {
            String msg = "Closing a closed session shouldn't generate an "
                + "exception";
            log.debug(msg, exception);
            fail(msg);
        }
    }

    /**
     * Invoke session methods
     *
     * @param destination the destination to use
     * @throws Exception for any error
     */
    private void invokeSessionMethods(Destination destination)
        throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();
        MethodInvoker invoker = new MethodInvoker(
            context.getSessionType(), IllegalStateException.class);

        invoker.invoke(session, "createBytesMessage");
        invoker.invoke(session, "createMapMessage");
        invoker.invoke(session, "createObjectMessage");
        invoker.invoke(session, "createObjectMessage", "ABC");
        invoker.invoke(session, "createStreamMessage");
        invoker.invoke(session, "createTextMessage");
        invoker.invoke(session, "createTextMessage", "ABC");
        invoker.invoke(session, "getTransacted");

        if (context.getAckType() == AckTypes.TRANSACTED) {
            invoker.invoke(session, "commit");
            invoker.invoke(session, "rollback");
        } else {
            invoker.invoke(session, "recover");
        }

        // @todo - need to add support for get/setMessageListener, run
        // and XA

        if (context.getSessionType().equals(QueueSession.class)) {
            invoker.invoke(session, "createBrowser", destination, Queue.class);
            invoker.invoke(session, "createBrowser",
                           new Object[]{destination, "1=1"}, new Class[] {Queue.class, String.class});
            invoker.invoke(session, "createQueue", "dummy");
            invoker.invoke(session, "createReceiver", destination, Queue.class);
            invoker.invoke(session, "createReceiver",
                           new Object[]{destination, "1=1"}, new Class[] {Queue.class, String.class});
            invoker.invoke(session, "createSender", destination, Queue.class);
            invoker.invoke(session, "createTemporaryQueue");
        } else {
            invoker.invoke(session, "createDurableSubscriber",
                           new Object[]{destination, "ABC"}, new Class[] {Topic.class, String.class});
            invoker.invoke(session, "createDurableSubscriber",
                           new Object[]{destination, "CDE", "1=1", Boolean.TRUE}, 
                           new Class[] {Topic.class, String.class, String.class, Boolean.TYPE });
            invoker.invoke(session, "createPublisher", destination, Topic.class);
            invoker.invoke(session, "createSubscriber", destination, Topic.class);
            invoker.invoke(session, "createSubscriber",
                           new Object[]{destination, "1=1", Boolean.FALSE}, 
                           new Class[] {Topic.class, String.class, Boolean.TYPE});
            invoker.invoke(session, "createTemporaryTopic");
            invoker.invoke(session, "createTopic", "dummy");
            invoker.invoke(session, "unsubscribe", "ABC");
        }
    }

}
