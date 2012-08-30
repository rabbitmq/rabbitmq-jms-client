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
 * $Id: MessageFactory.java,v 1.2 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * A factory for creating, and optionally populating messages
 *
 * @version     $Revision: 1.2 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessagePopulator
 */
public abstract class MessageFactory {

    /**
     * The message populator, or null if messages aren't to be populated
     */
    private final MessagePopulator _populator;


    /**
     * Construct a new <code>MessageFactory</code>
     */
    public MessageFactory() {
        this(null);
    }

    /**
     * Construct a new <code>MessageFactory</code>
     *
     * @param populator the message populator, or null, if messages aren't to
     * be populated
     */
    public MessageFactory(MessagePopulator populator) {
        _populator = populator;
    }

    /**
     * Create a message of the specified type. If a populator was supplied at
     * construction, it will be used to populate the message with data.
     *
     * @param type the JMS message class type
     * @return the new message
     * @throws Exception for any error
     */
    public Message create(Class<?> type) throws Exception {
        Message message = null;
        if (type.equals(BytesMessage.class)) {
            message = createBytesMessage();
        } else if (type.equals(MapMessage.class)) {
            message = createMapMessage();
        } else if (type.equals(ObjectMessage.class)) {
            message = createObjectMessage();
        } else if (type.equals(StreamMessage.class)) {
            message = createStreamMessage();
        } else if (type.equals(TextMessage.class)) {
            message = createTextMessage();
        } else if (type.equals(Message.class)) {
            message = createMessage();
        } else {
            throw new IllegalArgumentException(
                "Argument type=" + type + " is not a valid JMS Message type");
        }

        if (_populator != null) {
            populate(message);
        }
        return message;
    }

    /**
     * Create a new <code>BytesMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    protected abstract BytesMessage createBytesMessage() throws JMSException;

    /**
     * Create a new <code>MapMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    protected abstract MapMessage createMapMessage() throws JMSException;

    /**
     * Create a new <code>Message</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    protected abstract Message createMessage() throws JMSException;

    /**
     * Create a new <code>ObjectMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    protected abstract ObjectMessage createObjectMessage() throws JMSException;

    /**
     * Create a new <code>StreamMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    protected abstract StreamMessage createStreamMessage() throws JMSException;

    /**
     * Create a new <code>TextMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    protected abstract TextMessage createTextMessage() throws JMSException;

    /**
     * Populate a message
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    protected void populate(Message message) throws Exception {
        _populator.populate(message);
    }

}
