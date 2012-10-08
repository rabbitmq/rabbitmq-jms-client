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
 * $Id: BasicServerSession.java,v 1.2 2004/02/02 03:49:55 tanderson Exp $
 */
package org.exolab.jmscts.jms.asf;

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.Session;

import org.apache.log4j.Logger;


/**
 * Implementation of the javax.jms.ServerSession interface.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:55 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class BasicServerSession implements ServerSession {

    /**
     * The pool which manages this
     */
    private BasicServerSessionPool _pool;

    /**
     * The underlying session
     */
    private Session _session;

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log =
        Logger.getLogger(BasicServerSession.class);


    /**
     * Construct a new <code>BasicServerSession</code>
     *
     * @param pool the pool which manages this
     * @param session the underlying session
     * @throws JMSException for any error
     */
    public BasicServerSession(BasicServerSessionPool pool, Session session)
        throws JMSException {
        if (pool == null) {
            throw new IllegalArgumentException("Argument 'pool' is null");
        }
        if (session == null) {
            throw new IllegalArgumentException("Argument 'session' is null");
        }
        _pool = pool;
        _session = session;
    }

    /**
     * Return the server session's session
     *
     * @return the server session's session
     */
    @Override
    public Session getSession() {
        return _session;
    }

    /**
     * Close the server session
     *
     * @throws JMSException if the underlying session can't be closed
     */
    public void close() throws JMSException {
        _session.close();
    }

    /**
     * Cause the session's run method to be called to process messages that
     * were just assigned to it
     *
     * @throws JMSException if JMS fails to start the server session to
     * process messages
     */
    @Override
    public void start() throws JMSException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    _session.run();
                } finally {
                    _pool.release(BasicServerSession.this);
                }
            }
        };
        thread.start();
    }

}
