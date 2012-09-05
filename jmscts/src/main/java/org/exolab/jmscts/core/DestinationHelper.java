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
 * $Id: DestinationHelper.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.XAQueueSession;
import javax.jms.XATopicSession;
import javax.naming.NamingException;

import org.exolab.jmscts.provider.Administrator;


/**
 * Helper for creating, destroying and comparing Destination objects
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class DestinationHelper {

    /**
     * Prevent construction of utility class
     */
    private DestinationHelper() {
    }

    /**
     * Create a destination for the supplied name, using a test context
     * If the context specifies a messaging behaviour, then that will
     * used to determine if an administered or temporary destination will
     * be created, otherwise an administered destination will be created.
     *
     * @param context the test context
     * @param name the destination name
     * @return the new destination
     * @throws JMSException if the destination cannot be created
     * @throws NamingException if the new administered destination cannot be
     * located in JNDI
     */
    public static Destination create(TestContext context, String name)
        throws JMSException, NamingException {
        Destination result = null;
        boolean isQueue = context.isQueueConnectionFactory()
            || context.isXAQueueConnectionFactory();
        MessagingBehaviour behaviour = context.getMessagingBehaviour();
        if (behaviour == null || behaviour.getAdministered()) {
            result = create(name, isQueue, context.getAdministrator());
        } else {
            result = create(context.getSession(), isQueue);
        }
        return result;
    }

    /**
     * Create an administered destination for the supplied name.
     *
     * @param name the name of the destination
     * @param queue true if the destination is a Queue
     * @param admin the JMS provider administration interface
     * @return the new destination
     * @throws JMSException if the destination cannot be created
     * @throws NamingException if the new administered destination cannot be
     * be located in JNDI
     */
    public static Destination create(String name, boolean queue,
                                     Administrator admin)
        throws JMSException, NamingException {

        admin.createDestination(name, queue);
        return (Destination) admin.lookup(name);
    }

    /**
     * Create a temporary destination
     *
     * @param session the session to create the destination with
     * @return the new destination
     * @throws JMSException if the destination cannot be created
     */
    public static Destination create(Session session, boolean isQueue) throws JMSException {
        Destination result = null;
        if (session instanceof XAQueueSession) {
            session = ((XAQueueSession) session).getQueueSession();
        } else if (session instanceof XATopicSession) {
            session = ((XATopicSession) session).getTopicSession();
        }

        if (isQueue) {
            result = ((QueueSession) session).createTemporaryQueue();
        } else {
            result = ((TopicSession) session).createTemporaryTopic();
        }
        return result;
    }

    /**
     * Destroy a destination if it exists, using a test context
     *
     * @param context the test context
     * @param destination the destination to destroy
     * @throws JMSException if the destination cannot be destroyed
     */
    public static void destroy(TestContext context, Destination destination)
        throws JMSException {
        destroy(destination, context.getAdministrator());
    }

    /**
     * Destroy the named destination, if it exists
     *
     * @param name the name of the destination
     * @param admin the provider administration interface to remove the
     * destination with, if the destination exists
     * @throws JMSException if the destination cannot be destroyed
     */
    public static void destroy(String name, Administrator admin)
        throws JMSException {
        if (admin.destinationExists(name)) {
            admin.destroyDestination(name);
        }
    }

    /**
     * Destroy a destination
     *
     * @param destination the destination to destroy
     * @param admin the provider administration interface to remove the
     * destination with, if the destination is administered and exists
     * @throws JMSException if the destination cannot be destroyed
     */
    public static void destroy(Destination destination, Administrator admin)
        throws JMSException {
        if (destination instanceof TemporaryQueue) {
            ((TemporaryQueue) destination).delete();
        } else if (destination instanceof TemporaryTopic) {
            ((TemporaryTopic) destination).delete();
        } else {
            destroy(getName(destination), admin);
        }
    }

    /**
     * Compares two destinations for equality
     *
     * @param a the first destination to compare
     * @param b the second destination to compare
     * @return true if the destinations are equal
     * @throws JMSException if the destination names cannot be accessed
     */
    public static boolean equal(Destination a, Destination b)
        throws JMSException {
        boolean equal = false;
        if (a instanceof Queue && b instanceof Queue) {
            String nameA = ((Queue) a).getQueueName();
            String nameB = ((Queue) b).getQueueName();
            equal = nameA.equals(nameB);
        } else if (a instanceof Topic && b instanceof Topic) {
            String nameA = ((Topic) a).getTopicName();
            String nameB = ((Topic) b).getTopicName();
            equal = nameA.equals(nameB);
        }
        return equal;
    }

    /**
     * Returns the name of a destination
     *
     * @param destination the destination
     * @return the name of the destination
     * @throws JMSException if the name cannot be accessed
     */
    public static String getName(Destination destination) throws JMSException {
        String name = null;
        if (destination instanceof Queue) {
            name = ((Queue) destination).getQueueName();
        } else {
            name = ((Topic) destination).getTopicName();
        }
        return name;
    }

}
