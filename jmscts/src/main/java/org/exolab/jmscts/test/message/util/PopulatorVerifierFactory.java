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
 * $Id: PopulatorVerifierFactory.java,v 1.2 2004/02/03 07:31:04 tanderson Exp $
 */
package org.exolab.jmscts.test.message.util;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * Factory to create the appropriate {@link MessagePopulatorVerifier}
 * for a given message
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 */
public final class PopulatorVerifierFactory {

    /**
     * Prevent construct of utility class
     */
    private PopulatorVerifierFactory() {
    }

    /**
     * Create a new MessagePopulatorVerifier for the supplied message
     *
     * @param message the message to create the MessagePopulatorVerifier for
     * @return a new MessagePopulatorVerifier
     */
    public static MessagePopulatorVerifier create(Message message) {
        return create(message, null);
    }

    /**
     * Create a new MessagePopulatorVerifier for the supplied message
     *
     * @param message the message to create the MessagePopulatorVerifier for
     * @param exception the exception type expected to be thrown by
     * the MessagePopulatorVerifier when invoking a message's methods
     * @return a new MessagePopulatorVerifier
     */
    public static MessagePopulatorVerifier create(Message message,
                                                  Class<?> exception) {
        MessagePopulatorVerifier result = null;
        if (message instanceof BytesMessage) {
            result = new BytesMessageVerifier(exception);
        } else if (message instanceof ObjectMessage) {
            result = new ObjectMessageVerifier(exception);
        } else if (message instanceof MapMessage) {
            result = new MapMessageVerifier(exception);
        } else if (message instanceof StreamMessage) {
            result = new StreamMessageVerifier(exception);
        } else if (message instanceof TextMessage) {
            result = new TextMessageVerifier(exception);
        } else {
            result = new MessageVerifier(exception);
        }
        return result;
    }

}
