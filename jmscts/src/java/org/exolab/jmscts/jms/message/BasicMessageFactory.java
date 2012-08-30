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
 *    please contact jima@intalio.com.
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
 * $Id: BasicMessageFactory.java,v 1.1 2004/01/03 04:09:17 tanderson Exp $
 */
package org.exolab.jmscts.jms.message;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.exolab.jmscts.core.MessageFactory;
import org.exolab.jmscts.core.MessagePopulator;


/**
 * Helper class for creating, and optionally populating messages
 *
 * @version     $Revision: 1.1 $ $Date: 2004/01/03 04:09:17 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessagePopulator
 */
public class BasicMessageFactory extends MessageFactory {

    /**
     * Construct a new <code>BasicMessageFactory</code>
     */
    public BasicMessageFactory() {
    }

    /**
     * Construct a new <code>BasicMessageFactory</code>
     *
     * @param populator the message populator, or null, if messages aren't to
     * be populated
     */
    public BasicMessageFactory(MessagePopulator populator) {
        super(populator);
    }

    /**
     * Create a new <code>BytesMessage</code>
     *
     * @return the new message
     */
    @Override
    protected BytesMessage createBytesMessage() {
        return new BasicBytesMessage();
    }

    /**
     * Create a new <code>MapMessage</code>
     *
     * @return the new message
     */
    @Override
    protected MapMessage createMapMessage() {
        return new BasicMapMessage();
    }

    /**
     * Create a new <code>Message</code>
     *
     * @return the new message
     */
    @Override
    protected Message createMessage() {
        return new BasicMessage();
    }

    /**
     * Create a new <code>ObjectMessage</code>
     *
     * @return the new message
     */
    @Override
    protected ObjectMessage createObjectMessage() {
        return new BasicObjectMessage();
    }

    /**
     * Create a new <code>StreamMessage</code>
     *
     * @return the new message
     */
    @Override
    protected StreamMessage createStreamMessage() {
        return new BasicStreamMessage();
    }

    /**
     * Create a new <code>TextMessage</code>
     *
     * @return the new message
     */
    @Override
    protected TextMessage createTextMessage() {
        return new BasicTextMessage();
    }

}
