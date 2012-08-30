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
 * $Id: ThreadedReceiver.java,v 1.4 2004/02/03 21:52:06 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.List;


/**
 * Helper class which performs message receipt in a separate thread
 *
 * @version     $Revision: 1.4 $ $Date: 2004/02/03 21:52:06 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class ThreadedReceiver extends ThreadedAction {

    /**
     * The receiver to receive messages with
     */
    private final MessageReceiver _receiver;

    /**
     * The expected no. of messages to receive
     */
    private final int _count;

    /**
     * The maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     */
    private final long _timeout;

    /**
     * Listener for asynchronous receipt
     */
    private final CountingListener _listener;

    /**
     * List of received messages
     */
    private volatile List<?> _messages = null;


    /**
     * Construct a new <code>ThreadedReceiver</code>, for synchronous receipt
     *
     * @param receiver the message receiver
     * @param count the expected no. of messages to receive
     * @param timeout the maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     */
    public ThreadedReceiver(MessageReceiver receiver, int count,
                            long timeout) {
        this(receiver, count, timeout, null, null);
    }

    /**
     * Construct a new <code>ThreadedReceiver</code>, for synchronous receipt
     *
     * @param receiver the message receiver
     * @param count the expected no. of messages to receive
     * @param timeout the maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     * @param completion the listener to notify on completion
     */
    public ThreadedReceiver(MessageReceiver receiver, int count,
                            long timeout, CompletionListener completion) {
        this(receiver, count, timeout, null, completion);
    }

    /**
     * Construct a new <code>ThreadedReceiver</code>, for asynchronous receipt
     *
     * @param receiver the message receiver
     * @param timeout the maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     * @param listener the listener to handle received messages
     */
    public ThreadedReceiver(MessageReceiver receiver,
                            long timeout, CountingListener listener) {
        this(receiver, 0, timeout, listener, null);
    }

    /**
     * Construct a new <code>ThreadedReceiver</code>, for asynchronous receipt
     *
     * @param receiver the message receiver
     * @param count the expected no. of messages to receive
     * @param timeout the maximum time to wait for each message. If set to 0,
     * then it waits until a message becomes available.
     * @param listener the listener to handle received messages
     * @param completion the listener to notify on completion
     */
    public ThreadedReceiver(MessageReceiver receiver, int count,
                            long timeout, CountingListener listener,
                            CompletionListener completion) {
        super(completion);
        _receiver = receiver;
        _count = count;
        _timeout = timeout;
        _listener = listener;
    }

    /**
     * The received messages, when synchronous receipt is used
     *
     * @return the received messages, when synchronous receipt is used,
     * or <code>null</code> if no message has been received or this hasn't
     * completed
     */
    public List<?> getMessages() {
        return _messages;
    }

    /**
     * Run the action
     *
     * @throws Exception for any error
     */
    @Override
    public void runProtected() throws Exception {
        if (_listener == null) {
            _messages = _receiver.receive(_count, _timeout);
        } else {
            _receiver.receive(_timeout, _listener);
        }
    }

}
