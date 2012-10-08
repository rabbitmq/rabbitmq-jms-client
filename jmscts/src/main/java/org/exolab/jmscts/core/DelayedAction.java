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
 * $Id: DelayedAction.java,v 1.6 2004/02/03 21:52:06 tanderson Exp $
 */
package org.exolab.jmscts.core;

import org.apache.log4j.Logger;


/**
 * Helper class to run an action in a separate thread after a delay,
 * and catch any exception that the action generates.
 *
 * @version     $Revision: 1.6 $ $Date: 2004/02/03 21:52:06 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public abstract class DelayedAction extends ThreadedAction {

    /**
     * The time for the thread to sleep, in milliseconds
     */
    private final long _sleep;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(DelayedAction.class);


    /**
     * Construct a new <code>DelayedAction</code>
     *
     * @param sleep the time to wait, in milliseconds, before invoking the
     * action
     */
    public DelayedAction(long sleep) {
        _sleep = sleep;
    }

    /**
     * Construct a new <code>DelayedAction</code>, with a listener to notify
     * on completion
     *
     * @param sleep the time to wait, in milliseconds, before invoking the
     * action
     * @param listener the listener to notify on completion
     */
    public DelayedAction(long sleep, CompletionListener listener) {
        super(listener);
        _sleep = sleep;
    }

    /**
     * Run the action. This sleeps for the specified time, before invoking
     * {@link #runProtected}.<br/>
     * If a {@link CompletionListener} was supplied, it will be notified
     * on completion of the action.
     */
    @Override
    public void run() {
        try {
            Thread.currentThread();
            Thread.sleep(_sleep);
        } catch (InterruptedException ignore) {
            log.debug("action interrupted while sleeping - continuing");
        }
        super.run();
    }

}
