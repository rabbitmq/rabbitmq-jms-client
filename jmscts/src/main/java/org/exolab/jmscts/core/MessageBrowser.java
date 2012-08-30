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
 * $Id: MessageBrowser.java,v 1.6 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueSession;


/**
 * Receives messages using a {@link javax.jms.QueueBrowser}
 *
 * @version     $Revision: 1.6 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessageReceiver
 * @see         AsynchronousReceiver
 * @see         SynchronousReceiver
 */
class MessageBrowser implements MessageReceiver {

    /**
     * The session that owns the browser
     */
    private QueueSession _session = null;

    /**
     * The queue to receive messages from
     */
    private Queue _queue = null;

    /**
     * The browser used to receive messages
     */
    private QueueBrowser _browser = null;

    /**
     * The message selector
     */
    private String _selector = null;

    /**
     * If true, a selector was specified at construction. The selector may be
     * null - this is to ensure that the appropriate createBrowser method is
     * invoked.
     */
    private boolean _hasSelector = false;


    /**
     * Construct an instance with the session and queue to receive messages
     * from
     *
     * @param session the queue session
     * @param queue the queue to receive messages from
     * @throws JMSException if a <code>QueueBrowser</code> cannot be created
     */
    public MessageBrowser(QueueSession session, Queue queue)
        throws JMSException {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null");
        }
        if (queue == null) {
            throw new IllegalArgumentException("Argument queue is null");
        }
        _session = session;
        _queue = queue;
        getBrowser();
    }

    /**
     * Construct an instance with the session and queue to receive messages
     * from, using a message selector
     *
     * @param session the queue session
     * @param queue the queue to receive messages from
     * @param selector the message selector
     * @throws JMSException if a <code>QueueBrowser</code> cannot be created
     */
    public MessageBrowser(QueueSession session, Queue queue, String selector)
        throws JMSException {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null");
        }
        if (queue == null) {
            throw new IllegalArgumentException("Argument queue is null");
        }
        _session = session;
        _queue = queue;
        _selector = selector;
        _hasSelector = true;
        getBrowser();
    }

    /**
     * Return a list of {@link javax.jms.Message} instances from the underlying
     * QueueBrowser.
     *
     * @param count the number of messages expected
     * @param timeout the maximum time to wait for each message. This is ignore
     * ignored by this implementation
     * @return a list of messages, or null, if no messages were received
     * @throws JMSException if any of the JMS operation fail
     */
    @Override
    public List<Message> receive(int count, long timeout) throws JMSException {
        List<Message> result = null;
        QueueBrowser browser = getBrowser();
        Enumeration<?> iter = browser.getEnumeration();
        if (count == 0) {
            // don't expect any messages to be received. Attempt to receive
            // one message.
            ++count;
        }

        while (iter.hasMoreElements() && count > 0) {
            Message message = (Message) iter.nextElement();
            if (result == null) {
                result = new LinkedList<Message>();
            }
            result.add(message);
            --count;
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

        QueueBrowser browser = getBrowser();
        Enumeration<?> iter = browser.getEnumeration();

        int count = listener.getExpected();
        if (count == 0) {
            // don't expect any messages to be received. Attempt to receive
            // one message.
            if (iter.hasMoreElements()) {
                Message message = (Message) iter.nextElement();
                listener.onMessage(message);
            }
        } else {
            while (iter.hasMoreElements()) {
                Message message = (Message) iter.nextElement();
                listener.onMessage(message);
                if (listener.getReceived() == count) {
                    break;
                }
            }
        }
    }

    /**
     * Return the destination associated with the QueueBrowser
     *
     * @return the destination to receive messages from
     */
    @Override
    public Destination getDestination() {
        return _queue;
    }

    /**
     * Returns the message selector associated with the MessageConsumer
     *
     * @return the message selector
     * @throws JMSException if the operation fails
     */
    @Override
    public String getSelector() throws JMSException {
        return _selector;
    }

    /**
     * Returns the name of the subscriber, if the consumer is a durable topic
     * subscriber
     *
     * @return <code>null</code>
     */
    @Override
    public String getName() {
        return null;
    }

    /**
     * Returns the no-local value, if the consumer is a topic subscriber
     *
     * @return <code>false</code>
     */
    @Override
    public boolean getNoLocal() {
        return false;
    }

    /**
     * Close the underlying MessageBrowser
     *
     * @throws JMSException if the operation fails
     */
    @Override
    public void close() throws JMSException {
        if (_browser != null) {
            _browser.close();
            _browser = null;
        }
    }

    /**
     * Close the underlying QueueBrowser
     *
     * @throws JMSException if the operation fails
     */
    @Override
    public void remove() throws JMSException {
        close();
    }

    /**
     * Returns the QueueBrowser, constructing a new one if necessary
     *
     * @return the underlying queue browser
     * @throws JMSException if a <code>QueueBrowser</code> cannot be created
     */
    private QueueBrowser getBrowser() throws JMSException {
        if (_browser != null && !_browser.getEnumeration().hasMoreElements()) {
            _browser.close();
            _browser = null;
        }
        if (_browser == null) {
            if (_hasSelector) {
                _browser = _session.createBrowser(_queue, _selector);
            } else {
                _browser = _session.createBrowser(_queue);
            }
        }
        return _browser;
    }

}
