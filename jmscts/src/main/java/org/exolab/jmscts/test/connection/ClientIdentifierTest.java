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
 * $Id: ClientIdentifierTest.java,v 1.8 2004/02/03 07:32:07 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractConnectionTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.ConnectionFactoryHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class verifies that the client identifier can be set, but only
 * on creation of a connection.
 *
 * @todo: need to cover connection consumers
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.8 $
 * @see AbstractConnectionTestCase
 * @see org.exolab.jmscts.core.ConnectionTestRunner
 */
public class ClientIdentifierTest extends AbstractConnectionTestCase {

    /**
     * The client identifier
     */
    private static final String CLIENT_ID = "myClientID";

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ClientIdentifierTest.class);


    /**
     * Create a new <code>ClientIdentifierTest</code>
     *
     * @param name the name of test case
     */
    public ClientIdentifierTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createConnectionTest(ClientIdentifierTest.class);
    }

    /**
     * Verifies that the client identifier can be set on a connection, just
     * after it is created, but not subsequently.
     *
     * @jmscts.requirement connection.method.setClientID
     * @jmscts.requirement connection.clientID.administered
     * @throws Exception for any error
     */
    public void testSetOnCreation() throws Exception {
        TestContext context = getContext();
        Connection connection = context.getConnection();
        try {
            connection.setClientID(CLIENT_ID);
        } catch (IllegalStateException exception) {
            context.getCoverage().setUnsupported(
                "connection.clientID.administered");
        } catch (Exception exception) {
            String msg = "Expected IllegalStateException to be thrown if an "
                + "administered client identifier exists for a connection, "
                + "but got: " + exception.getClass().getName();
            log.debug(msg, exception);
            fail(msg);
        }
        expectFailure();
    }

    /**
     * Verifies that two connections may not have the same client identifier
     * @todo - check variations of queue and topic connections
     *
     * @jmscts.requirement connection.clientID.duplicate
     * @throws Exception for any error
     */
    public void testDuplicateClientID() throws Exception {
        TestContext context = getContext();
        Connection first = context.getConnection();
        Connection second = null;
        boolean test = true;

        try {
            second = ConnectionFactoryHelper.createConnection(context,
                                                              CLIENT_ID);
        } catch (IllegalStateException unsupported) {
            // can't continue to test - the provider uses administered
            // client identifiers
            test = false;
        }
        if (test) {
            try {
                first.setClientID(CLIENT_ID);
                fail("Managed to set the same client identifier on two "
                     + "different connections");
            } catch (InvalidClientIDException expected) {
                // the expected behaviour
            } catch (Exception exception) {
                fail("Expected setClientID to throw , InvalidClientIDException"
                     + " but got: " + exception);
            } finally {
                second.close();
            }

            // verify that the client identifier may now be set
            first.setClientID(CLIENT_ID);
        }
    }

    /**
     * Verifies that the client identifier cannot be set on a closed connection
     *
     * @jmscts.requirement connection.method.setClientID
     * @throws Exception for any error
     */
    public void testSetAfterClose() throws Exception {
        Connection connection = getContext().getConnection();
        connection.close();
        expectFailure();
    }

    /**
     * Verifies that the client identifier cannot be set after an exception
     * listener has been registered
     *
     * @jmscts.requirement connection.method.setClientID
     * @throws Exception for any error
     */
    public void testSetAfterListenerRegistration() throws Exception {
        Connection connection = getContext().getConnection();
        connection.setExceptionListener(new ExceptionListener() {
            @Override
            public void onException(JMSException exception) {
            }
        });
        expectFailure();
    }

    /**
     * Verifies that the client identifier cannot be set after the connection
     * has been started
     *
     * @jmscts.requirement connection.method.setClientID
     * @throws Exception for any error
     */
    public void testSetAfterStart() throws Exception {
        Connection connection = getContext().getConnection();
        connection.start();
        expectFailure();
    }

    /**
     * Verifies that the client identifier cannot be set after the connection
     * has stopped
     *
     * @jmscts.requirement connection.method.setClientID
     * @throws Exception for any error
     */
    public void testSetAfterStop() throws Exception {
        Connection connection = getContext().getConnection();
        connection.stop();
        expectFailure();
    }

    /**
     * Verifies that the client identifier cannot be set after a session has
     * been created
     *
     * @jmscts.requirement connection.method.setClientID
     * @throws Exception for any error
     */
    public void testSetAfterCreateSession() throws Exception {
        TestContext context = getContext();
        ConnectionHelper.createSession(
            context, AckTypes.AUTO_ACKNOWLEDGE);
        expectFailure();
    }

    /**
     * Returns true if the connection should be started prior to running the
     * test. This implementation returns false, as it would affect the
     * behaviour of {@link #testSetOnCreation}
     *
     * @return false
     */
    @Override
    public boolean startConnection() {
        return false;
    }

    /**
     * Expect the setClientID operation to fail
     *
     * @throws Exception if setClientID is successful or the wrong exception is
     * thrown
     */
    private void expectFailure() throws Exception {
        try {
            getContext().getConnection().setClientID(CLIENT_ID);
            fail("setClientID should throw IllegalStateException");
        } catch (IllegalStateException expected) {
            // the expected behaviour
        } catch (Exception exception) {
            fail("Expected setClientID to throw IllegalStateException, "
                 + "but got: " + exception);
        }
    }

}
