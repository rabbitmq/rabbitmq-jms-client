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
 * $Id: SynchronousReceiver.java,v 1.5 2004/02/03 21:52:06 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.log4j.Logger;


/**
 * Synchronously receives messages from a {@link javax.jms.MessageConsumer}
 *
 * @version     $Revision: 1.5 $ $Date: 2004/02/03 21:52:06 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
class SynchronousReceiver extends AbstractMessageReceiver {

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(SynchronousReceiver.class);


    /**
     * Construct an instance with the consumer to receive messages with
     *
     * @param session the session that created the consumer
     * @param consumer the consumer used to receive messages
     * @param name the name of the subscriber, for durable topic subscribers.
     * May be null.
     */
    public SynchronousReceiver(Session session, MessageConsumer consumer,
                               String name) {
        super(session, consumer, name);
    }

    /**
     * Return a list of {@link javax.jms.Message} instances from a
     * MessageConsumer. Messages are received using the blocking
     * {@link javax.jms.MessageConsumer#receive} method.
     *
     * @param count the number of messages expected
     * @param timeout the maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     * @return a list of messages, or null, if no messages were received in
     * the specified time
     * @throws JMSException if any of the JMS operation fail
     */
    @Override
    public List<Message> receive(int count, long timeout) throws JMSException {
        List<Message> result = null;

        if (log.isDebugEnabled()) {
            log.debug("Expecting to receive count=" + count + " messages "
                      + "[destination=" + getDestination()
                      + ", timeout=" + timeout + "]");
        }

        if (count == 0) {
            // don't expect any messages to be received
            Message message = getConsumer().receive(timeout);
            if (message != null) {
                result = new ArrayList<Message>();
                result.add(message);
            }
        } else {
            for (int i = 0; i < count; ++i) {
                Message message = getConsumer().receive(timeout);
                if (message != null) {
                    if (result == null) {
                        result = new ArrayList<Message>(count);
                    }
                    result.add(message);
                }
            }
        }
        return result;
    }

    /**
     * Receive messages, delegating received messages to a
     * <code>CountingListener</code>
     *
     * @param timeout the maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     * @param listener the listener to delegate messages to
     * @throws JMSException if the operation fails
     */
    @Override
    public void receive(long timeout, CountingListener listener)
        throws JMSException {

        int count = listener.getExpected();
        if (log.isDebugEnabled()) {
            log.debug("Expecting to receive count=" + count + " messages "
                      + "[destination=" + getDestination()
                      + ", timeout=" + timeout + "]");
        }

        if (count == 0) {
            // don't expect any messages to be received
            Message message = getConsumer().receive(timeout);
            if (message != null) {
                listener.onMessage(message);
            }
        } else {
            while (listener.getReceived() < count) {
                Message message = getConsumer().receive(timeout);
                if (message != null) {
                    listener.onMessage(message);
                }
            }
        }
    }

}
