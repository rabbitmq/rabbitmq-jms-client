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
 * $Id: MessageSizePopulator.java,v 1.2 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Arrays;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * A message populator that populates the body of messages with data of
 * a specific size
 *
 * @version     $Revision: 1.2 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class MessageSizePopulator extends AbstractMessagePopulator {

    /** TODO */
    private static final long serialVersionUID = 1L;
    /**
     * The message content
     */
    private final String _content;

    /**
     * Construct an instance with the size of data to populate messages with
     *
     * @param size the size of data to populate messages with
     */
    public MessageSizePopulator(int size) {
        char[] buffer = new char[(size / 2) + (size % 2)];
        Arrays.fill(buffer, 'A');
        _content = new String(buffer);
    }

    /**
     * Populates a BytesMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateBytesMessage(BytesMessage message) throws Exception {
        message.writeUTF(_content);
    }

    /**
     * Populates a MapMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateMapMessage(MapMessage message) throws Exception {
        message.setString("content", _content);
    }

    /**
     * Populates an ObjectMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateObjectMessage(ObjectMessage message) throws Exception {
        message.setObject(_content);
    }

    /**
     * Populates a StreamMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateStreamMessage(StreamMessage message) throws Exception {
        message.writeString(_content);
    }

    /**
     * Populates a TextMessage
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populateTextMessage(TextMessage message) throws Exception {
        message.setText(_content);
    }

}
