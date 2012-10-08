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
 * $Id: EchoListener.java,v 1.4 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.log4j.Logger;


/**
 * This helper class implements the {@link MessageListener} interface and
 * echoes messages to another destination
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 */
public class EchoListener implements MessageListener {

    /**
     * The underlying session
     */
    private Session _session;

    /**
     * The sender to echo messages
     */
    private MessageSender _sender;

    /**
     * The number of messages to send
     */
    private int _count = 0;

    /**
     * Determines if transacted sessions should be committed
     */
    private boolean _commit = false;

    /**
     * The logging category
     */
    private static final Logger log =
        Logger.getLogger(EchoListener.class.getName());


    /**
     * Construct an instance with the sender, the number of messages to
     * send when {@link #onMessage} is invoked
     *
     * @param sender the message sender
     * @param count the number of messages to send
     */
    public EchoListener(MessageSender sender, int count) {
        this(null, sender, count, false);
    }

    /**
     * Construct an instance with the session, sender, the number of messages
     * to send when {@link #onMessage} is invoked.
     *
     * @param session the underlying session
     * @param sender the message sender
     * @param count the number of messages to send
     * @param commit if <code>true</code> and the session is transacted, commit
     * the session after sending the messages
     */
    public EchoListener(Session session, MessageSender sender, int count,
                        boolean commit) {
        _session = session;
        _sender = sender;
        _count = count;
        _commit = commit;
    }

    /**
     * Invoked when the consumer asynchronously receives a message
     *
     * @param message the received message
     */
    @Override
    public void onMessage(Message message) {
        try {
            _sender.send(message, _count);
            if (_commit) {
                if (_session != null && !(_session instanceof javax.jms.XASession) && _session.getTransacted()) {
                    _session.commit();
                }
            }
        } catch (Exception exception) {
            log.error(exception, exception);
        }
    }

}
