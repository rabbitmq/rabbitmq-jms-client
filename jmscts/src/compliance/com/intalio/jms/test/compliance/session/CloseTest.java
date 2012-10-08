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
 * $Id: CloseTest.java,v 1.8 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.QueueSession;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSessionTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MethodInvoker;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of {@link Session#close}
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>session.close</li>
 *   <li>session.close.closed</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.8 $
 * @see AbstractSessionTestCase
 * @see org.exolab.jmscts.core.SessionTestRunner
 */
public class CloseTest extends AbstractSessionTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testExceptionOnClose", "session.close"},
        {"testCloseForClosedSession", "session.close.closed"}};

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(CloseTest.class.getName());


    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against all session types.
     *
     * @param name the name of test case
     */
    public CloseTest(String name) {
        super(name, AckTypes.ALL, REQUIREMENTS);
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
        return TestCreator.createSessionTest(CloseTest.class);
    }

    /**
     * Returns if this test can share resources with other test cases.
     * This implementation always returns <code>false</code>, to 
     * ensure that a new session is created for each test.
     *
     * @return <code>false</code>
     */
    public boolean share() {
        return false;
    }

    /**
     * Test that a IllegalStateException is thrown for a closed
     * session. This covers requirements:
     * <ul>
     *   <li>session.close</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testExceptionOnClose() throws Exception {
        final String name = "CloseTest";        
        TestContext context = getContext();
        Session session = context.getSession();
        session.close();

        boolean queue = context.isQueueConnectionFactory() ||
            context.isXAQueueConnectionFactory();

        // set up the destination
        DestinationHelper.destroy(name, context.getAdministrator());
        Destination destination = DestinationHelper.create(
            name, queue, context.getAdministrator());
                                          
        try {
            invokeSessionMethods(destination);
        } finally {
            DestinationHelper.destroy(name, context.getAdministrator());
        }
    }

    /**
     * Test that closing a closed session has no effect. 
     * This covers requirements:
     * <ul>
     *   <li>session.close.closed</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testCloseForClosedSession() throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();
        session.close();

        try {
            session.close();        
        } catch (Exception exception) {
            String msg = "Closing a closed session shouldn't generate an " +
                "exception";
            _log.debug(msg, exception);
            fail(msg);
        }
    }

    private void invokeSessionMethods(Destination destination) 
        throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();

        MethodInvoker invoker = new MethodInvoker(IllegalStateException.class);
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

        // TODO - need to add support for get/setMessageListener, run
        // and XA

        if (session instanceof QueueSession) {
            invoker.invoke(session, "createBrowser", destination);
            invoker.invoke(session, "createBrowser", 
                           new Object[]{destination, "1=1"});
            invoker.invoke(session, "createQueue", "dummy");
            invoker.invoke(session, "createReceiver", destination);
            invoker.invoke(session, "createReceiver", 
                           new Object[]{destination, "1=1"});
            invoker.invoke(session, "createSender", destination);
            invoker.invoke(session, "createTemporaryQueue");
        } else {
            invoker.invoke(session, "createDurableSubscriber", 
                           new Object[]{destination, "ABC"});
            invoker.invoke(session, "createDurableSubscriber", 
                           new Object[]{destination, "CDE", "1=1", 
                                        Boolean.TRUE});
            invoker.invoke(session, "createPublisher", destination);
            invoker.invoke(session, "createSubscriber", destination);
            invoker.invoke(session, "createSubscriber", 
                           new Object[]{destination, "1=1", Boolean.FALSE});
            invoker.invoke(session, "createTemporaryTopic");
            invoker.invoke(session, "createTopic", "dummy");
            invoker.invoke(session, "unsubscribe", "ABC");
        }
    }
    
} //-- CloseTest
