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
 * Copyright 2001-2005 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: AbstractMessageSender.java,v 1.5 2005/06/16 06:31:11 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.QueueSender;
import javax.jms.TopicPublisher;
import javax.jms.MessageListener;


/**
 * Helper that implements behaviour common to both QueueMessageSender and
 * TopicMessageSender
 *
 * @version     $Revision: 1.5 $ $Date: 2005/06/16 06:31:11 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessageSender
 * @see         QueueMessageSender
 * @see         TopicMessageSender
 */
abstract class AbstractMessageSender implements MessageSender {

    /**
     * The producer used to send messages
     */
    private final MessageProducer _producer;

    /**
     * The messaging behaviour
     */
    private final MessagingBehaviour _behaviour;

    /**
     * The listener to notify each time a message is sent. May be
     * <code>null</code>
     */
    private MessageListener _listener;

    /**
     * Construct an instance with the producer to send messages with
     *
     * @param producer the producer used to send messages
     * @param behaviour the messaging behaviour
     */
    public AbstractMessageSender(MessageProducer producer,
                                 MessagingBehaviour behaviour) {
        if (producer == null) {
            throw new IllegalArgumentException("Argument producer is null");
        }
        if (behaviour == null) {
            throw new IllegalArgumentException("Argument behaviour is null");
        }
        _producer = producer;
        _behaviour = behaviour;
    }

    /**
     * Send messages to a destination
     *
     * @param message the message to send
     * @param count the number of times to send the message
     * @throws JMSException if any of the JMS operations fail
     */
    @Override
    public void send(Message message, int count) throws JMSException {
        send(message, count, null, _behaviour.getTimeToLive());
    }

    /**
     * Send messages to a destination
     *
     * @param message the message to send
     * @param count the number of times to send the message
     * @param timeToLive the message time-to-live
     * @throws JMSException if any of the JMS operations fail
     */
    @Override
    public void send(Message message, int count, long timeToLive)
        throws JMSException {
        send(message, count, null, timeToLive);
    }

    /**
     * Send messages to a destination, invoking a message populator prior
     * to each message being sent
     *
     * @param message the message to send
     * @param count the number of times to send the message
     * @param populator the message populator, or null, if no population is
     * required
     * @throws JMSException if any of the JMS operations fail
     */
    @Override
    public void send(Message message, int count, MessagePopulator populator)
        throws JMSException {
        send(message, count, populator, _behaviour.getTimeToLive());
    }

    /**
     * Return the destination associated with the MessageProducer
     *
     * @return the destination to receive messages from
     * @throws JMSException if the operation fails
     */
    @Override
    public Destination getDestination() throws JMSException {
        Destination result = null;
        if (_producer instanceof QueueSender) {
            result = ((QueueSender) _producer).getQueue();
        } else {
            result = ((TopicPublisher) _producer).getTopic();
        }
        return result;
    }

    /**
     * Sets the listener for sent messages
     *
     * @param listener the listener
     */
    @Override
    public void setMessageListener(MessageListener listener) {
        _listener = listener;
    }

    /**
     * Returns the listener for sent messages
     *
     * @return the listener, or <code>null</code> if no listener is set
     */
    @Override
    public MessageListener getMessageListener() {
        return _listener;
    }

    /**
     * Close the underlying MessageProducer
     *
     * @throws JMSException if the operation fails
     */
    @Override
    public void close() throws JMSException {
        if (_producer != null) {
            _producer.close();
        }
    }

    /**
     * Returns the underlying message producer
     *
     * @return the underlying message producer
     */
    protected MessageProducer getProducer() {
        return _producer;
    }

    /**
     * Returns the messaging behaviour
     *
     * @return the messaging behaviour
     */
    protected MessagingBehaviour getBehaviour() {
        return _behaviour;
    }

}
