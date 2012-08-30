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
 * $Id: MessageReceiver.java,v 1.7 2004/02/03 21:52:06 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;


/**
 * Interface for receiving messages from a {@link javax.jms.MessageConsumer} or
 * {@link javax.jms.QueueBrowser}.
 *
 * @version     $Revision: 1.7 $ $Date: 2004/02/03 21:52:06 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public interface MessageReceiver {

    /**
     * Return a list of {@link javax.jms.Message} instances from a
     * MessageConsumer or QueueBrowser
     *
     * @param count the number of messages expected. This is indicative only;
     * implementations may return less (if messages aren't available) or more
     * (if an asynchronous implementation is used) messages than the specified
     * value.
     * @param timeout the maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     * @return a list of messages, or null, if no messages were received in
     * the specified time
     * @throws JMSException if the operation fails
     */
    List<?> receive(int count, long timeout) throws JMSException;

    /**
     * Receive messages, delegating received messages to a
     * <code>CountingListener</code>
     *
     * @param timeout the maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     * @param listener the listener to delegate messages to
     * @throws JMSException if the operation fails
     */
    void receive(long timeout, CountingListener listener) throws JMSException;

    /**
     * Return the destination associated with the MessageConsumer
     *
     * @return the destination to receive messages from
     * @throws JMSException if the operation fails
     */
    Destination getDestination() throws JMSException;

    /**
     * Returns the message selector associated with the MessageConsumer
     *
     * @return the message selector
     * @throws JMSException if the operation fails
     */
    String getSelector() throws JMSException;

    /**
     * Returns the name of the subscriber, if the consumer is a durable topic
     * subscriber
     *
     * @return the name of the subscriber, or <code>null</code> if the
     * consumer is not a durable topic subscriber
     */
    String getName();

    /**
     * Returns the no-local value, if the consumer is a topic subscriber
     *
     * @return the no-local value, if the consumer is a topic subscriber;
     * otherwise <code>false</code>
     * @throws JMSException if the operation fails
     */
    boolean getNoLocal() throws JMSException;

    /**
     * Close the underlying MessageConsumer or QueueBrowser
     *
     * @throws JMSException if the operation fails
     */
    void close() throws JMSException;

    /**
     * Close the underlying MessageConsumer or QueueBrowser. For durable topic
     * subscribers, unsubscribe it from the session.
     *
     * @throws JMSException if the operation fails
     */
    void remove() throws JMSException;

}
