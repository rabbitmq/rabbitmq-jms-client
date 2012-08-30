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
 * $Id: ConnectionHelper.java,v 1.4 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XATopicConnection;


/**
 * Helper for performing connection operations
 *
 * @version     $Revision: 1.4 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class ConnectionHelper {

    /**
     * Prevent construction of utility class
     */
    private ConnectionHelper() {
    }

    /**
     * Create a session
     *
     * @param context the test context
     * @param type the message acknowledgement type
     * @return a new session
     * @throws JMSException if any of the JMS operations fail
     */
    public static Session createSession(TestContext context, AckType type)
        throws JMSException {

        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' is null");
        }
        if (context.getConnection() == null) {
            throw new IllegalArgumentException(
                "Argument 'context' is not a connection context");
        }

        Session result = null;
        if (context.isQueueConnectionFactory()) {
            QueueConnection connection =
                (QueueConnection) context.getConnection();
            result = connection.createQueueSession(type.getTransacted(),
                                                   type.getAcknowledgeMode());
        } else if (context.isTopicConnectionFactory()) {
            TopicConnection connection =
                (TopicConnection) context.getConnection();
            result = connection.createTopicSession(type.getTransacted(),
                                                   type.getAcknowledgeMode());
        } else if (context.isXAQueueConnectionFactory()) {
            XAQueueConnection connection =
                (XAQueueConnection) context.getConnection();
            result = connection.createXAQueueSession();
        } else {
            XATopicConnection connection =
                (XATopicConnection) context.getConnection();
            result = connection.createXATopicSession();
        }
        return result;
    }

    /**
     * Create a session of with the same behaviour as that of the supplied
     * context
     *
     * @param context the test context
     * @return a new session
     * @throws JMSException if any of the JMS operations fail
     */
    public static Session createSession(TestContext context)
        throws JMSException {

        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (context.getSession() == null) {
            throw new IllegalArgumentException(
                "Argument 'context' is not a session context");
        }
        return createSession(context, context.getAckType());
    }

    /**
     * Create a connection consumer
     *
     * @param context the test context
     * @param destination the destination to access
     * @param name if non-null, and the connection is a TopicConnection, then
     * a durable connection consumer will be returned, otherwise the argument
     * is ignored.
     * @param selector the message selector. May be <code>null</code>
     * @param pool the server session pool to associate with the consumer
     * @param maxMessages the maximum number of messages that can be assigned
     * to a server session at one time
     * @return the new connection consumer
     * @throws JMSException if any of the JMS operations fail
     */
    public static ConnectionConsumer createConnectionConsumer(
        TestContext context, Destination destination, String name,
        String selector, ServerSessionPool pool, int maxMessages)
        throws JMSException {

        ConnectionConsumer result;

        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (pool == null) {
            throw new IllegalArgumentException("Argument 'pool' is null");
        }
        if (context.getConnection() == null) {
            throw new IllegalArgumentException(
                "Argument 'context' is not a connection context");
        }

        if (context.isQueue()) {
            QueueConnection connection =
                (QueueConnection) context.getConnection();
            result = connection.createConnectionConsumer(
                (Queue) destination, selector, pool, maxMessages);
        } else {
            TopicConnection connection =
                (TopicConnection) context.getConnection();
            if (name == null) {
                result = connection.createConnectionConsumer(
                    (Topic) destination, selector, pool, maxMessages);
            } else {
                result = connection.createDurableConnectionConsumer(
                    (Topic) destination, name, selector, pool, maxMessages);
            }
        }
        return result;
    }

}
