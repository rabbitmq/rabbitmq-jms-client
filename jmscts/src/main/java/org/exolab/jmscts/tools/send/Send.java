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
 * $Id: Send.java,v 1.2 2004/02/03 21:52:13 tanderson Exp $
 */
package org.exolab.jmscts.tools.send;

import javax.jms.Message;

import org.exolab.jmscts.core.AckType;
import org.exolab.jmscts.core.DeliveryType;
import org.exolab.jmscts.core.MessageCreator;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.SequencePropertyPopulator;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestContextHelper;
import org.exolab.jmscts.tools.MessagingTool;


/**
 * Helper to send messages
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 21:52:13 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class Send extends MessagingTool {

    /**
     * The delivery type
     */
    private DeliveryType _delivery;


    /**
     * Set the delivery type
     *
     * @param type the delivery type
     */
    public void setDeliveryType(DeliveryType type) {
        _delivery = type;
    }

    /**
     * Send messages
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
        Message message = creator.create(Message.class);
        MessagingBehaviour behaviour = new MessagingBehaviour(_delivery);
        TestContext messageContext = new TestContext(
            sessionContext, message, behaviour);

        MessageSender sender = SessionHelper.createSender(
            messageContext, getDestination());

        int count = getCount();
        MessagePopulator populator = new SequencePropertyPopulator();
        for (int i = 0; i < count; ++i) {
            sender.send(message, 1, populator);
            log(message);
        }

        connectionContext.close();
    }
}

