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
 * $Id: CompletionListener.java,v 1.4 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;


/**
 * Helper class to synchronize on the completion of tasks
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @see DelayedAction
 */
public class CompletionListener {

    /**
     * The number of tasks expected to complete
     */
    private int _expected;

    /**
     * Semaphore used to synchronize process completion
     */
    private Semaphore _completedLock;


    /**
     * Construct a new <code>CompletionListener</code>
     *
     * @param expected the number of tasks expected to complete
     */
    public CompletionListener(int expected) {
        _expected = expected;
        _completedLock = new Semaphore(-expected + 1);
    }

    /**
     * Notify completion of a task
     */
    public void completed() {
        _completedLock.release();
    }

    /**
     * Wait for the listener to complete processing
     *
     * @param timeout the number of milleseconds to wait. An argument less
     * than or equal to zero means not to wait at all
     * @throws InterruptedException if interrupted while waiting
     * @return <code>true</code> if the listener completed in the given time
     * frame
     */
    public boolean waitForCompletion(long timeout)
        throws InterruptedException {
        boolean completed = _completedLock.attempt(timeout);
        if (completed) {
            _completedLock.release();
        }
        return completed;
    }

    /**
     * Wait indefinitely for the tasks to complete processing
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public void waitForCompletion() throws InterruptedException {
        _completedLock.acquire();
        _completedLock.release();
    }

    /**
     * Returns the number of completed tasks
     *
     * @return the number of completed tasks
     */
    public int getCompleted() {
        return _expected + (int) _completedLock.permits() - 1;
    }

}
