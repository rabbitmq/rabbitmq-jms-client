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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: ThreadedSender.java,v 1.3 2005/06/16 06:26:07 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Message;


/**
 * Helper class which performs message production in a separate thread
 *
 * @version     $Revision: 1.3 $ $Date: 2005/06/16 06:26:07 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class ThreadedSender extends ThreadedAction {

    /**
     * The message sender
     */
    private final MessageSender _sender;

    /**
     * The message to send
     */
    private final Message _message;

    /**
     * The no. of times to send the message
     */
    private final int _count;


    /**
     * Construct a new <code>ThreadedSender</code>
     *
     * @param sender the message sender
     * @param message the message to send
     * @param count the no. of times to send the message
     */
    public ThreadedSender(MessageSender sender, Message message, int count) {
        this(sender, message, count, null);
    }

    /**
     * Construct a new <code>ThreadedSender</code>
     *
     * @param sender the message sender
     * @param message the message to send
     * @param count the no. of times to send the message
     * @param listener the listener to notify on completion
     */
    public ThreadedSender(MessageSender sender, Message message, int count,
                          CompletionListener listener) {
        super(listener);
        if (sender == null) {
            throw new IllegalArgumentException("Argument 'sender' is null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Argument 'message' is null");
        }
        _sender = sender;
        _message = message;
        _count = count;
    }

    /**
     * Run the action
     *
     * @throws Exception for any error
     */
    @Override
    public void runProtected() throws Exception {
        _sender.send(_message, _count);
    }

}
