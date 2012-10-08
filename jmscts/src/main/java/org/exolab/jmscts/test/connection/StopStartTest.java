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
 * $Id: StopStartTest.java,v 1.2 2004/02/03 07:32:07 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import javax.jms.Connection;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractConnectionTestCase;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of <code>Connection.stop</code> and
 * <code>Connection.start</code>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 */
public class StopStartTest extends AbstractConnectionTestCase {

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(StopStartTest.class);


    /**
     * Construct a new <code>StopStartTest</code>
     *
     * @param name the name of test case
     */
    public StopStartTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createConnectionTest(StopStartTest.class);
    }

    /**
     * Returns if this test can share resources with other test cases.
     * This implementation always returns <code>false</code>, to
     * ensure that a new connection is created for each test.
     *
     * @return <code>false</code>
     */
    @Override
    public boolean share() {
        return false;
    }

    /**
     * Returns if the connection should be started prior to running the test.
     * This implementation always returns <code>false</code> to avoid
     * conflicts with test cases
     *
     * @return <code>false</code>
     */
    @Override
    public boolean startConnection() {
        return false;
    }

    /**
     * Verifies that invoking <code>Connection.stop()</code> for a stopped
     * connection has no effect.
     *
     * @jmscts.requirement connection.stop.stopped
     * @throws Exception for any error
     */
    public void testStopForStoppedConnection() throws Exception {
        TestContext context = getContext();
        Connection connection = context.getConnection();
        try {
            // the connection should already be stopped
            connection.stop();
        } catch (Exception exception) {
            String msg = "Invoking Connection.stop() on a stopped connection "
                + "should be ignored. Test context=" + context;
            log.debug(msg, exception);
            fail(msg);
        }
    }

    /**
     * Verifies that invoking <code>Connection.start()</code> for a started
     * connection has no effect.
     *
     * @jmscts.requirement connection.start.started
     * @throws Exception for any error
     */
    public void testStartForStartedConnection() throws Exception {
        TestContext context = getContext();
        Connection connection = context.getConnection();
        connection.start();
        try {
            connection.start();
        } catch (Exception exception) {
            String msg = "Invoking Connection.start() on a started connection "
                + "should be ignored";
            log.debug(msg, exception);
            fail(msg);
        }
    }

}
