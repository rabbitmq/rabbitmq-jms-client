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
 * $Id: AuthTest.java,v 1.2 2004/02/03 07:32:07 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractConnectionFactoryTestCase;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.TestProperties;


/**
 * This class tests connection authorisation
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 */
public class AuthTest extends AbstractConnectionFactoryTestCase {


    /**
     * Create a new <code>AuthTest</code>.
     *
     * @param name the name of test case
     */
    public AuthTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createConnectionFactoryTest(AuthTest.class);
    }

    /**
     * Verifies that a QueueConnection can be created, using a valid
     * username and password
     *
     * @jmscts.requirement connection.authentication
     * @jmscts.factory QueueConnectionFactory
     * @throws Exception for any error
     */
    public void testQueueAuth() throws Exception {
        TestContext context = getContext();
        QueueConnectionFactory factory =
            (QueueConnectionFactory) context.getConnectionFactory();
        QueueConnection connection = null;
        String user = TestProperties.getString("valid.username", "CHANGE_ME");
        String password = TestProperties.getString("valid.password",
                                                   "CHANGE_ME");

        try {
            connection = factory.createQueueConnection(user, password);
            assertNotNull("QueueConnection is null", connection);
        } catch (JMSException exception) {
            fail("Expected creation of QueueConnection to succeed with "
                 + "username=" + user + ", password=" + password);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Verifies that a QueueConnection cannot be created, when using an invalid
     * username and password
     *
     * @jmscts.requirement connection.authentication
     * @jmscts.factory QueueConnectionFactory
     * @throws Exception for any error
     */
    public void testInvalidQueueAuth() throws Exception {
        TestContext context = getContext();
        QueueConnectionFactory factory =
            (QueueConnectionFactory) context.getConnectionFactory();
        QueueConnection connection = null;
        String user = TestProperties.getString("invalid.username",
                                               "CHANGE_ME");
        String password = TestProperties.getString("invalid.password",
                                                   "CHANGE_ME");

        try {
            connection = factory.createQueueConnection(user, password);
            fail("Expected creation of QueueConnection to fail with "
                 + "username=" + user + ", password=" + password);
        } catch (JMSSecurityException ignore) {
            // expected behaviour
        } catch (JMSException exception) {
            fail("Expected JMSSecurityException to be thrown, but got "
                 + "exception type=" + exception.getClass().getName() + ": "
                 + exception);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Verifies that a TopicConnection can be created, using a valid
     * username and password
     *
     * @jmscts.requirement connection.authentication
     * @jmscts.factory TopicConnectionFactory
     * @throws Exception for any error
     */
    public void testTopicAuth() throws Exception {
        TestContext context = getContext();
        TopicConnectionFactory factory =
            (TopicConnectionFactory) context.getConnectionFactory();
        TopicConnection connection = null;
        String user = TestProperties.getString("valid.username", "CHANGE_ME");
        String password = TestProperties.getString("valid.password",
                                                   "CHANGE_ME");

        try {
            connection = factory.createTopicConnection(user, password);
            assertNotNull("TopicConnection is null", connection);
        } catch (JMSException exception) {
            fail("Expected creation of TopicConnection to succeed with user="
                 + user + ", password=" + password);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Verifies that a TopicConnection cannot be created, when using an invalid
     * username and password
     *
     * @jmscts.requirement connection.authentication
     * @jmscts.factory TopicConnectionFactory
     * @throws Exception for any error
     */
    public void testInvalidTopicAuth() throws Exception {
        TestContext context = getContext();
        TopicConnectionFactory factory =
            (TopicConnectionFactory) context.getConnectionFactory();
        TopicConnection connection = null;
        String user = TestProperties.getString("invalid.username",
                                               "CHANGE_ME");
        String password = TestProperties.getString("invalid.password",
                                                   "CHANGE_ME");

        try {
            connection = factory.createTopicConnection(user, password);
            fail("Expected creation of TopicConnection to fail with user="
                 + user + ", password=" + password);
        } catch (JMSSecurityException ignore) {
            // expected behaviour
        } catch (JMSException exception) {
            fail("Expected JMSSecurityException to be thrown, but got "
                 + "exception type=" + exception.getClass().getName() + ": "
                 + exception);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

}
