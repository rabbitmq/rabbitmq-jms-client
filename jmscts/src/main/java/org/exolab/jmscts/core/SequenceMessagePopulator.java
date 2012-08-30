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
 * $Id: SequenceMessagePopulator.java,v 1.2 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * A message populator that populates the body of messages with a sequence
 * number. The sequence is incremented with each invocation, starting at 1.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class SequenceMessagePopulator extends AbstractMessagePopulator {

    /** TODO */
    private static final long serialVersionUID = 1L;
    /**
     * The sequence number
     */
    private volatile int _sequence = 0;


    /**
     * Construct a new <code>SequenceMessagePopulator</code>
     */
    public SequenceMessagePopulator() {
    }

    /**
     * Populates a Message. This is a no-op
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateMessage(Message message) throws Exception {
    }

    /**
     * Populates a BytesMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateBytesMessage(BytesMessage message) throws Exception {
        message.writeInt(++_sequence);
    }

    /**
     * Populates a MapMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateMapMessage(MapMessage message) throws Exception {
        message.setInt("key", ++_sequence);
    }

    /**
     * Populates an ObjectMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateObjectMessage(ObjectMessage message) throws Exception {
        message.setObject(new Integer(++_sequence));
    }

    /**
     * Populates a StreamMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateStreamMessage(StreamMessage message) throws Exception {
        message.writeInt(++_sequence);
    }

    /**
     * Populates a TextMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateTextMessage(TextMessage message) throws Exception {
        message.setText(new Integer(++_sequence).toString());
    }

}
