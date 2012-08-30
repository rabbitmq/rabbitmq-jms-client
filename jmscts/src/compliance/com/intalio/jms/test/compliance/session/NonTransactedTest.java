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
 * $Id: NonTransactedTest.java,v 1.3 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSessionTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class verifies that methods used only for transacted sessions
 * throw IllegalStateException if invoked
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>session.commit.IllegalStateException</li>
 *   <li>session.rollback.IllegalStateException</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see AbstractSessionTestCase
 * @see org.exolab.jmscts.core.SessionTestRunner
 */
public class NonTransactedTest extends AbstractSessionTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testCommit", "session.commit.IllegalStateException"},
        {"testRollback", "session.rollback.IllegalStateException"}};

    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against all session types.
     *
     * @param name the name of test case
     */
    public NonTransactedTest(String name) {
        super(name, AckTypes.NON_TRANSACTIONAL, REQUIREMENTS);
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
        return TestCreator.createSessionTest(NonTransactedTest.class);
    }

    /**
     * Test that a IllegalStateException is thrown if Session.commit() is
     * invoked for a non-transacted sesssion
     * <ul>
     *   <li>session.commit.IllegalStateException</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testCommit() throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();

        try {
            session.commit();        
            fail("Invoking commit on a non-transacted session should throw " +
                 "IllegalStateException");
        } catch (IllegalStateException ignore) {
        } catch (Exception exception) {
            fail("Invoking commit on a non-transacted session should " +
                 " throw IllegalStateException, but threw exception=" + 
                 exception.getClass().getName() + ", message=" + 
                 exception.getMessage());
        }
    }

    /**
     * Test that a IllegalStateException is thrown if Session.rollback() is
     * invoked for a non-transacted sesssion
     * <ul>
     *   <li>session.rollback.IllegalStateException</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testRollback() throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();

        try {
            session.rollback();        
            fail("Invoking rollback on a non-transacted session should " +
                 " throw IllegalStateException");
        } catch (IllegalStateException ignore) {
        } catch (Exception exception) {
            fail("Invoking rollback on a non-transacted session should " +
                 " throw IllegalStateException, but threw exception=" + 
                 exception.getClass().getName() + ", message=" + 
                 exception.getMessage());
        }
    }
    
} //-- NonTransactedTest
