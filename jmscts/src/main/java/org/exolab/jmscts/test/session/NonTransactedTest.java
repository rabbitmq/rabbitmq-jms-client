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
 * $Id: NonTransactedTest.java,v 1.6 2004/02/03 21:52:11 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import javax.jms.IllegalStateException;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSessionTestCase;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class verifies that methods used only for transacted sessions
 * throw <code>IllegalStateException</code> if invoked
 *
 * @jmscts.session AUTO_ACKNOWLEDGE
 * @jmscts.session CLIENT_ACKNOWLEDGE
 * @jmscts.session DUPS_OK_ACKNOWLEDGE
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractSessionTestCase
 */
public class NonTransactedTest extends AbstractSessionTestCase {

    /**
     * Construct a new <code>NonTransactedTest</code>
     *
     * @param name the name of test case
     */
    public NonTransactedTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSessionTest(NonTransactedTest.class);
    }

    /**
     * Verifies that an IllegalStateException is thrown if Session.commit() is
     * invoked for a non-transacted sesssion
     *
     * @jmscts.requirement session.commit.IllegalStateException
     * @throws Exception for any error
     */
    public void testCommit() throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();

        try {
            session.commit();
            fail("Invoking commit on a non-transacted session should throw "
                 + "IllegalStateException");
        } catch (IllegalStateException expected) {
            // the expected behaviour
        } catch (Exception exception) {
            fail("Invoking commit on a non-transacted session should "
                 + " throw IllegalStateException, but threw exception="
                 + exception.getClass().getName() + ", message="
                 + exception.getMessage());
        }
    }

    /**
     * Verifies that an IllegalStateException is thrown if Session.rollback()
     * is invoked for a non-transacted sesssion
     *
     * @jmscts.requirement session.rollback.IllegalStateException
     * @throws Exception for any error
     */
    public void testRollback() throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();

        try {
            session.rollback();
            fail("Invoking rollback on a non-transacted session should "
                 + " throw IllegalStateException");
        } catch (IllegalStateException expected) {
            // the expected behaviour
        } catch (Exception exception) {
            fail("Invoking rollback on a non-transacted session should "
                 + " throw IllegalStateException, but threw exception="
                 + exception.getClass().getName() + ", message="
                 + exception.getMessage());
        }
    }

}
