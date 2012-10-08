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
 * Copyright 2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: BasicServerSessionPool.java,v 1.2 2004/02/02 03:49:55 tanderson Exp $
 */
package org.exolab.jmscts.jms.asf;

import java.util.LinkedList;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueSession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicSession;

import org.apache.log4j.Logger;


/**
 * Implementation of the <code>javax.jms.ServerSessionPool</code> interface.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:55 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class BasicServerSessionPool implements ServerSessionPool {

    /**
     * The maximum no. of sessions in the pool
     */
    private final int _size;

    /**
     * The connection to allocate sessions from
     */
    private Connection _connection;

    /**
     * If <code>true</code>, allocate transacted sessions
     */
    private boolean _transacted;

    /**
     * The message acknowledgement mode, for non-transacted sessions
     */
    private int _ackMode;

    /**
     * The listener to delegate messages to
     */
    private MessageListener _listener;

    /**
     * The pool of server sessions
     */
    private LinkedList<ServerSession> _pool = new LinkedList<ServerSession>();

    /**
     * True if the pool is closed
     */
    private boolean _closed = false;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(BasicServerSessionPool.class);


    /**
     * Construct a new <code>BasicServerSessionPool</code>
     *
     * @param size the maximum no. of sessions to allocate
     * @param connection the connection to allocate sessions from
     * @param transacted if <code>true</code>, allocate transacted sessions
     * @param ackMode the message acknowledgement mode, for non-transacted
     * sessions
     * @param listener the listener to delegate messages to
     * @throws JMSException if sessions cannot be allocated
     */
    public BasicServerSessionPool(int size, Connection connection,
                                  boolean transacted, int ackMode,
                                  MessageListener listener)
        throws JMSException {

        if (size < 1) {
            throw new IllegalArgumentException("Argument 'size' must be > 0");
        }
        if (connection == null) {
            throw new IllegalArgumentException(
                "Argument 'connection' is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Argument 'listener' is null");
        }
        _size = size;
        _connection = connection;
        _transacted = transacted;
        _ackMode = ackMode;
        _listener = new SingleThreadedListener(listener);

        // pre-allocate server sessions
        for (int i = 0; i < _size; ++i) {
            _pool.add(create());
        }
    }

    /**
     * Return a server session from the pool
     *
     * @return a server session from the pool
     * @throws JMSException if an application server fails to return a server
     * session from its pool
     */
    @Override
    public ServerSession getServerSession() throws JMSException {
        ServerSession session = null;
        synchronized (_pool) {
            while (session == null) {
                if (_closed) {
                    throw new JMSException("Pool has been closed");
                }
                if (!_pool.isEmpty()) {
                    session = _pool.removeFirst();
                } else {
                    try {
                        _pool.wait();
                    } catch (InterruptedException ignore) {
                        // no-op
                    }
                }
            }
        }
        return session;
    }

    /**
     * Close the pool, destroying any allocated sessions
     *
     * @throws JMSException if the underlying session's can't be closed
     */
    public void close() throws JMSException {
        synchronized (_pool) {
            _closed = true;
            while (!_pool.isEmpty()) {
                BasicServerSession session =
                    (BasicServerSession) _pool.removeFirst();
                session.close();
            }

            _pool.notify();
        }
    }

    /**
     * Release a server session back to the pool
     *
     * @param session the session to release
     */
    protected void release(BasicServerSession session) {
        synchronized (_pool) {
            if (_closed) {
                try {
                    session.close();
                } catch (JMSException exception) {
                    log.error(exception);
                }
            } else {
                _pool.add(session);
                _pool.notify();
            }
        }
    }

    /**
     * Create a new server session
     *
     * @return the new server session
     * @throws JMSException if the session cannot be created
     */
    protected ServerSession create() throws JMSException {
        Session session = null;
        if (_connection instanceof XAQueueConnection) {
            XAQueueSession xaSession =
                ((XAQueueConnection) _connection).createXAQueueSession();
            session = xaSession.getQueueSession();
        } else if (_connection instanceof XATopicConnection) {
            XATopicSession xaSession =
                ((XATopicConnection) _connection).createXATopicSession();
            session = xaSession.getTopicSession();
        } else if (_connection instanceof QueueConnection) {
            session = ((QueueConnection) _connection).createQueueSession(
                _transacted, _ackMode);
        } else if (_connection instanceof TopicConnection) {
            session = ((TopicConnection) _connection).createTopicSession(
                _transacted, _ackMode);
        } else {
            throw new JMSException("Invalid connection: " + _connection);
        }
        session.setMessageListener(_listener);

        return new BasicServerSession(this, session);
    }

    /**
     * Helper class to ensure that a <code>MessageListener</code> is only
     * invoked by a single thread at a time
     */
    static class SingleThreadedListener implements MessageListener {

        /**
         * The listener to single thread
         */
        private MessageListener _listener;

        /**
         * Construct a new <code>SingleThreadedListener</code>
         *
         * @param listener the listener to single thread
         */
        public SingleThreadedListener(MessageListener listener) {
            _listener = listener;
        }

        /**
         * Handle a message, delegating it to the listener
         *
         * @param message the message
         */
        @Override
        public void onMessage(Message message) {
            synchronized (_listener) {
                _listener.onMessage(message);
            }
        }
    }

}
