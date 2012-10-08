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
 * $Id: WaitingListener.java,v 1.4 2004/07/04 12:09:03 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;


/**
 * This class implements the {@link MessageListener} interface and enables
 * clients to synchronize behaviour with the {@link #onMessage} method.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 */
public class WaitingListener extends DelegatingListener {

    /**
     * The number of times that {@link #onMessage} is expected to be invoked.
     * If set to -1, then {@link #onMessage} can be invoked any number of
     * times.
     */
    private int _count = -1;

    /**
     * The number of times that {@link #onMessage} has been invoked.
     */
    private int _invoked = 0;

    /**
     * Semaphore used to synchronize on message receipt
     */
    private Semaphore _receiptLock = new Semaphore(0);

    /**
     * Semaphore used to synchronize message processing
     */
    private Semaphore _processLock = new Semaphore(0);

    /**
     * Semaphore used to synchronize process completion
     */
    private Semaphore _completedLock = new Semaphore(0);

    /**
     * The logger
     */
    private static final Logger _log =
        Logger.getLogger(WaitingListener.class);


    /**
     * Construct an instance with the listener to delegate messages.
     * The {@link #onMessage} method may be called any number of times.
     *
     * @param listener the message listener that messages will be delegated to
     */
    public WaitingListener(MessageListener listener) {
        this(listener, -1);
    }

    /**
     * Construct an instance with the listener to delegate messages, and
     * the expected number of times that {@link #onMessage} will be invoked.
     *
     * @param listener the message listener that messages will be delegated to
     * @param count the expected number of times that the {@link #onMessage}
     * will be called
     */
    public WaitingListener(MessageListener listener, int count) {
        super(listener);
        _count = count;
    }

    /**
     * Wait for the listener to receive a message
     *
     * @throws InterruptedException if interrupted
     */
    public void waitForReceipt() throws InterruptedException {
        _receiptLock.acquire();
    }

    /**
     * Wait at most <code>msecs</code> milliseconds for the listener to 
     * receive a message
     *
     * @param msecs the no. of milliseconds to wait
     * @return <code>true</code> if a message was received, otherwise
     * <code>false</code>
     * @throws InterruptedException if interrupted
     */
    public boolean waitForReceipt(long msecs) throws InterruptedException {
        return _receiptLock.attempt(msecs);
    }

    /**
     * Notify the listener that it may continue processing
     */
    public void notifyContinue() {
        _processLock.release();
    }

    /**
     * Wait for the listener to complete processing
     *
     * @throws InterruptedException if interrupted
     */
    public void waitForCompletion() throws InterruptedException {
        _completedLock.acquire();
    }

    /**
     * Invoked when the consumer asynchronously receives a message.
     * On entry, it notifies the client blocked on {@link #waitForReceipt}.
     * It then waits for the client to invoke {@link #notifyContinue},
     * before invoking the listener passed at construction.
     * This enables clients to synchronize their behaviour with the listener.
     * <br>
     * On completion of the listener, it notifies the client blocked on
     * {@link #waitForCompletion}
     * <p>
     * If an invocation count was specified at construction, and
     * {@link #onMessage} is invoked more than the specified count, an error
     * is logged, and the method returns immediately.
     *
     * @param message the received message
     */
    @Override
    public void onMessage(Message message) {
        _log.debug("WaitingListener.onMessage() - begin");
        boolean fail = false;
        synchronized (this) {
            // notify the client that a message has been received
            _receiptLock.release();
            if (_count != -1) {
                if (++_invoked > _count) {
                    _log.error(
                        "WaitingListener.onMessage() has been invoked too "
                        + "many times. Expected invocation=" + _count
                        + ", current invocation=" + _invoked);
                    fail = true;
                }
            }
            if (!fail) {
                try {
                    _log.debug("WaitingListener.onMessage() - waiting");
                    _processLock.acquire();
                    _log.debug(
                        "WaitingListener.onMessage() - done waiting");
                } catch (InterruptedException ignore) {
                    _log.debug(
                        "WaitingListener.onMessage() - interrupted");
                }
            }
        }

        try {
            if (!fail) {
                // delegate behaviour to the listener
                super.onMessage(message);
            }
        } catch (RuntimeException exception) {
            _log.error(exception, exception);
            throw exception;
        } finally {
            _completedLock.release();
            _log.debug("WaitingListener.onMessage() - end");
        }
    }

}
