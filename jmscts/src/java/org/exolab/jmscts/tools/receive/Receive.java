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
 * $Id: Receive.java,v 1.2 2004/02/03 21:52:12 tanderson Exp $
 */
package org.exolab.jmscts.tools.receive;

import javax.jms.Message;

import org.exolab.jmscts.core.AckType;
import org.exolab.jmscts.core.CountingListener;
import org.exolab.jmscts.core.MessageCreator;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.ReceiptType;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.tools.MessagingTool;


/**
 * Helper to receive messages
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 21:52:12 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class Receive extends MessagingTool {

    /**
     * The receipt type
     */
    private ReceiptType _receipt;

    /**
     * The selector. May be <code>null</code>.
     */
    private String _selector;


    /**
     * Sets the receipt type
     *
     * @param type the receipt type
     */
    public void setReceiptType(ReceiptType type) {
        _receipt = type;
    }

    /**
     * Sets the selector
     *
     * @param selector the selector
     */
    public void setSelector(String selector) {
        _selector = selector;
    }

    /**
     * Receive messages
     *
     * @throws Exception for any error
     */
    @Override
    protected void doInvoke() throws Exception {
        TestContext connectionContext =
            TestContextHelper.createConnectionContext(getContext());
        TestContext sessionContext = TestContextHelper.createSessionContext(
            connectionContext, AckType.AUTO_ACKNOWLEDGE);
        MessageCreator creator =
            new MessageCreator(sessionContext.getSession(), null);
        MessagingBehaviour behaviour = new MessagingBehaviour();
        behaviour.setReceiptType(_receipt);
        TestContext messageContext = new TestContext(
            sessionContext, creator.create(Message.class), behaviour);

        int count = getCount();
        MessageReceiver receiver = SessionHelper.createReceiver(
            messageContext, getDestination(), _selector, false);
        CountingListener listener = new Listener(count);

        messageContext.getConnection().start();

        long timeout = behaviour.getTimeout();
        receiver.receive(timeout, listener);
        listener.waitForCompletion(timeout * (count + 1));
        connectionContext.close();
    }

    /**
     * Message listener which logs each message it receives
     */
    private class Listener extends CountingListener {

        /**
         * Construct a new <code>Listener</code>
         *
         * @param expected the expected no. of messages
         */
        public Listener(int expected) {
            super(expected);
        }

        /**
         * Handle a message
         *
         * @param message the message
         */
        @Override
        public void onMessage(Message message) {
            super.onMessage(message);
            log(message);
        }
    }

}
