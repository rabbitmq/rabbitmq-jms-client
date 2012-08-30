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
 * $Id: ThreadedActions.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Helper class to run and wait for completion of a set of
 * {@link ThreadedAction} instances.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class ThreadedActions extends ThreadedAction {

    /**
     * A list of <code>ThreadedAction</code> instances
     */
    private List<ThreadedAction> _actions = new ArrayList<ThreadedAction>();

    /**
     * Construct a new <code>ThreadedActions</code>
     */
    public ThreadedActions() {
    }

    /**
     * Add an action to be started and monitored by this
     *
     * @param action the action to add
     */
    public void addAction(ThreadedAction action) {
        _actions.add(action);
    }

    /**
     * Run the actions in separate threads
     *
     * @throws Exception for any error
     */
    @Override
    public void runProtected() throws Exception {
        Iterator<ThreadedAction> iterator = _actions.iterator();
        while (iterator.hasNext()) {
            ThreadedAction action = iterator.next();
            action.start();
        }
        iterator = _actions.iterator();
        while (iterator.hasNext()) {
            ThreadedAction action = iterator.next();
            action.waitForCompletion();
        }
    }

    /**
     * Returns any exception thrown by {@link #runProtected}. If
     * <code>null</code> returns any exception thrown by a child action,
     * otherwise <code>null</code>, if no exception was thrown
     *
     * @return any exception thrown, or <code>null</code>, if none was
     * thrown
     */
    @Override
    public Exception getException() {
        Exception result = super.getException();
        if (result == null) {
            Iterator<ThreadedAction> iterator = _actions.iterator();
            while (iterator.hasNext()) {
                ThreadedAction action = iterator.next();
                result = action.getException();
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

}
