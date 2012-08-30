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
 * $Id: MessagingHelper.java,v 1.5 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.core;

import java.util.List;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.XASession;


/**
 * Helper class for performing messaging operations
 *
 * @version     $Revision: 1.5 $ $Date: 2006/09/19 19:55:12 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see SessionHelper
 * @see TestContext
 */
public final class MessagingHelper {

    /**
     * Prevent construction of utility class
     */
    private MessagingHelper() {
    }

    /**
     * Helper to send messages to a destination, using the message supplied by
     * the test context
     * <p>
     * If the session is transactional, it will be committed.
     *
     * @param context the test context
     * @param destination the destination
     * @param count the number of times to send the message
     * @throws Exception for any error
     */
    public static void send(TestContext context, Destination destination,
                            int count) throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        send(context, context.getMessage(), destination, count);
    }

    /**
     * Helper to send messages to a destination.
     * <p>
     * If the session is transactional, it will be committed.
     *
     * @param context the test context
     * @param message the message to send
     * @param destination the destination
     * @param count the number of times to send the message
     * @throws Exception for any error
     */
    public static void send(TestContext context, Message message,
                            Destination destination, int count)
        throws Exception {

        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Argument 'message' is null");
        }
        if (destination == null) {
            throw new IllegalArgumentException(
                "Argument 'destination' is null");
        }

        MessageSender sender = SessionHelper.createSender(
            context, destination);
        try {
            sender.send(message, count);
            Session session = context.getSession();
            if (session.getTransacted() && !(session instanceof XASession)) {
                session.commit();
            }
        } finally {
            sender.close();
        }
    }

    /**
     * Helper to send a message to a destination, and return a message from
     * the same destination. The message is obtained from the test context.
     * <p>
     * If the session is transactional, it will be committed.
     * <br>
     * Note: the persistent state of the receiver is removed on completion,
     * so tests for durable topic subcribers should not use this method if
     * more than one message is expected.
     *
     * @param context the test context
     * @param destination the destination
     * @return the message received from the destination
     * @throws Exception if the message cannot be sent, no message is received,
     * or more than one message is received.
     */
    public static Message sendReceive(TestContext context,
                                      Destination destination)
        throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }

        return sendReceive(context, context.getMessage(), destination);
    }

    /**
     * Helper to send a message to a destination, and return a message from
     * the same destination.
     * <p>
     * If the session is transactional, it will be committed.
     * <br>
     * Note: the persistent state of the receiver is removed on completion,
     * so tests for durable topic subcribers should not use this method if
     * more than one message is expected.
     *
     * @param context the test context
     * @param message the message to send
     * @param destination the destination
     * @return Message the message received from the destination
     * @throws Exception if the message cannot be sent, no message is received,
     * or more than one message is received.
     */
    public static Message sendReceive(TestContext context, Message message,
                                      Destination destination)
        throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Argument 'message' is null");
        }
        if (destination == null) {
            throw new IllegalArgumentException(
                "Argument 'destination' is null");
        }

        MessageReceiver receiver = null;
        MessageSender sender = null;
        List<?> messages = null;
        try {
            receiver = SessionHelper.createReceiver(context, destination);
            sender = SessionHelper.createSender(context, destination);
            sender.send(message, 1);
            Session session = context.getSession();
            if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
                session.commit();
            }
            long timeout = context.getMessagingBehaviour().getTimeout();
            messages = receiver.receive(1, timeout);
            if (messages == null) {
                throw new Exception(
                    "Failed to receive a message from destination="
                    + DestinationHelper.getName(destination));
            }
            if (messages.size() != 1) {
                throw new Exception("Expected one message from destination="
                                    + DestinationHelper.getName(destination)
                                    + " but got " + messages.size());
            }
            if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
                session.commit();
            }
        } finally {
            if (receiver != null) {
                receiver.remove();
            }
            if (sender != null) {
                sender.close();
            }
        }
        return (Message) messages.get(0);
    }

    /**
     * Helper to receive messages
     *
     * @param context the test context
     * @param receiver the message receiver
     * @param count the expected number of messages
     * @return the list of messages, or null, if no messages were expected
     * @throws Exception if the receive fails, or the wrong number of
     * messages were returned
     */
    public static List<?> receive(TestContext context, MessageReceiver receiver,
                               int count) throws Exception {
        if (context == null) {
            throw new IllegalArgumentException("Argument 'context' is null");
        }
        MessagingBehaviour behaviour = context.getMessagingBehaviour();
        if (behaviour == null) {
            throw new IllegalArgumentException(
                "Argument context has no MessagingBehaviour instance");
        }

        return receive(receiver, count, behaviour.getTimeout());
    }

    /**
     * Helper to receive messages
     *
     * @param receiver the message receiver
     * @param count the expected number of messages
     * @param timeout the maximum time to wait for each message
     * @return the list of messages, or null, if no messages were expected
     * @throws Exception if the receive fails, or the wrong number of
     * messages were returned
     */
    public static List<?> receive(MessageReceiver receiver, int count,
                               long timeout) throws Exception {
        List<?> result = receiver.receive(count, timeout);
        if (result == null) {
            if (count != 0) {
                throw new Exception(
                    "Failed to receive any messages from destination="
                    + DestinationHelper.getName(receiver.getDestination()));
            }
        } else if (result.size() != count) {
            throw new Exception(
                "Expected " + count + " messages from destination="
                + DestinationHelper.getName(receiver.getDestination())
                + " but got " + result.size());
        }
        return result;
    }

}
