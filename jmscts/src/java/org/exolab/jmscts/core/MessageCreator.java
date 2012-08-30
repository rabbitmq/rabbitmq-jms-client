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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: MessageCreator.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * Helper class for creating, and optionally populating messages
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessagePopulator
 */
public class MessageCreator extends MessageFactory {

    /**
     * The session to create messages with
     */
    private final Session _session;


    /**
     * Construct a new <code>MesssageCreator</code>
     *
     * @param session the session to create messages with
     * @param populator the message populator, or null, if messages aren't to
     * be populated
     */
    public MessageCreator(Session session, MessagePopulator populator) {
        super(populator);
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null");
        }
        _session = session;
    }

    /**
     * Create a new <code>BytesMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    @Override
    protected BytesMessage createBytesMessage() throws JMSException {
        return _session.createBytesMessage();
    }

    /**
     * Create a new <code>MapMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    @Override
    protected MapMessage createMapMessage() throws JMSException {
        return _session.createMapMessage();
    }

    /**
     * Create a new <code>Message</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    @Override
    protected Message createMessage() throws JMSException {
        return _session.createMessage();
    }

    /**
     * Create a new <code>ObjectMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    @Override
    protected ObjectMessage createObjectMessage() throws JMSException {
        return _session.createObjectMessage();
    }

    /**
     * Create a new <code>StreamMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    @Override
    protected StreamMessage createStreamMessage() throws JMSException {
        return _session.createStreamMessage();
    }

    /**
     * Create a new <code>TextMessage</code>
     *
     * @return the new message
     * @throws JMSException for any error
     */
    @Override
    protected TextMessage createTextMessage() throws JMSException {
        return _session.createTextMessage();
    }

}
