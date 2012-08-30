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
 * $Id: ConnectionFactoryHelper.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnectionFactory;


/**
 * Helper for performing connection factory operations
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class ConnectionFactoryHelper {

    /**
     * Prevent construction of utility class
     */
    private ConnectionFactoryHelper() {
    }

    /**
     * Create a connection.
     * This uses the properties 'valid.user' and 'valid.password' when
     * creating the connection.
     *
     *
     * @param context the test context
     * @param clientID the connection client identifier, or null, if no
     * identifier is to be assigned to the connection
     * @return a new connection
     * @throws JMSException if any of the JMS operations fail
     */
    public static Connection createConnection(TestContext context,
                                              String clientID)
       throws JMSException {

        String user = TestProperties.getString("valid.username", null);
        String password = TestProperties.getString("valid.password", null);

        Connection result = null;
        if (context.isQueueConnectionFactory()) {
            QueueConnectionFactory factory =
                (QueueConnectionFactory) context.getConnectionFactory();
            result = factory.createQueueConnection(user, password);
        } else if (context.isTopicConnectionFactory()) {
            TopicConnectionFactory factory =
                (TopicConnectionFactory) context.getConnectionFactory();
            result = factory.createTopicConnection(user, password);
        } else if (context.isXAQueueConnectionFactory()) {
            XAQueueConnectionFactory factory =
                (XAQueueConnectionFactory) context.getConnectionFactory();
            result = factory.createXAQueueConnection(user, password);
        } else if (context.isXATopicConnectionFactory()) {
            XATopicConnectionFactory factory =
                (XATopicConnectionFactory) context.getConnectionFactory();
            result = factory.createXATopicConnection(user, password);
        } else {
            throw new JMSException("Unknown connection factory type");
        }
        if (clientID != null) {
            result.setClientID(clientID);
        }
        return result;
    }

}
