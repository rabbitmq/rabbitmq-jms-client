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
 * $Id: TestContextHelper.java,v 1.6 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Connection;
import javax.jms.Session;


/**
 * Helper to create new {@link TestContext} instances using existing contexts
 * as prototypes
 *
 * @version     $Revision: 1.6 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class TestContextHelper {

    /**
     * Prevent construction of utility class
     */
    private TestContextHelper() {
    }

    /**
     * Creates a new connection context with the same connection type as
     * the supplied context.
     *
     * @param context the prototype context
     * @return a new connection context
     * @throws Exception for any error
     */
    public static TestContext createConnectionContext(TestContext context)
        throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (context.getConnectionFactory() == null) {
            throw new IllegalArgumentException(
                "Argument 'context' is not a connection factory context");
        }
        Connection connection = ConnectionFactoryHelper.createConnection(
            context, null);
        return new TestContext(context, connection);
    }

    /**
     * Creates a new session context with the same session type as
     * the supplied context. A new connection will be allocated.
     *
     * @param context the prototype context
     * @return a new session context
     * @throws Exception for any error
     */
    public static TestContext createSessionContext(TestContext context)
        throws Exception {
        return createSessionContext(context, true);
    }

    /**
     * Creates a new session context with the same session type as
     * the supplied context.
     *
     * @param context the prototype context
     * @param newConnection if <code>true</code> allocate a new connection,
     * otherwise use the existing one
     * @return a new session context
     * @throws Exception for any error
     */
    public static TestContext createSessionContext(TestContext context,
                                                   boolean newConnection)
        throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (context.getSession() == null) {
            throw new IllegalArgumentException(
                "Argument 'context' is not a session context");
        }
        TestContext connection;
        if (newConnection) {
            connection = createConnectionContext(context);
        } else {
            connection = context.getParent();
        }
        return createSessionContext(connection, context.getAckType());
    }

    /**
     * Creates a new session context from a connection context
     *
     * @param context the connection context
     * @param type the message acknowledgement type of the session
     * @return a new session context
     * @throws Exception for any error
     */
    public static TestContext createSessionContext(TestContext context,
                                                   AckType type)
        throws Exception {
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
        Session session = ConnectionHelper.createSession(context, type);
        return new TestContext(context, session, type);
    }

    /**
     * Creates a new send/receive context with the same details as
     * the supplied context. A new connection and session will be allocated,
     * but the message and behaviour will refer to the original.
     *
     * @param context the prototype context
     * @return a new send/receive context
     * @throws Exception for any error
     */
    public static TestContext createSendReceiveContext(TestContext context)
        throws Exception {
        return createSendReceiveContext(context, true);
    }

    /**
     * Creates a new send/receive context with the same details as
     * the supplied context. A session will be allocated,
     * but the message type and behaviour will refer to the original.
     *
     * @param context the prototype context
     * @param newConnection if <code>true</code> allocate a new connection,
     * otherwise use the existing one
     * @return a new send/receive context
     * @throws Exception for any error
     */
    public static TestContext createSendReceiveContext(TestContext context,
                                                       boolean newConnection)
        throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (context.getMessagingBehaviour() == null) {
            throw new IllegalArgumentException(
                "Argument 'context' is not a send/receive context");
        }
        return createSendReceiveContext(context, newConnection,
                                        context.getMessagingBehaviour());
    }

    /**
     * Creates a new send/receive context with the same details as
     * the supplied context, but with a different messaging behaviour.
     * A session will be allocated.
     *
     * @param context the prototype context
     * @param newConnection if <code>true</code> allocate a new connection,
     * otherwise use the existing one
     * @param behaviour the messaging behaviour
     * @return a new send/receive context
     * @throws Exception for any error
     */
    public static TestContext createSendReceiveContext(
        TestContext context, boolean newConnection,
        MessagingBehaviour behaviour) throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (behaviour == null) {
            throw new IllegalArgumentException(
                "Argument 'behaviour' is null");
        }
        TestContext session = createSessionContext(context, newConnection);
        return new TestContext(session, context.getMessageType(), behaviour);
    }

}
