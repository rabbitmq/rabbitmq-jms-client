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
 * $Id: TimeToLiveTestCase.java,v 1.4 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.test.producer.ttl;

import javax.jms.Message;
import javax.jms.Session;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.ReceiptType;


/**
 * This class provides helper methods for message time-to-live test cases
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 */
class TimeToLiveTestCase extends AbstractSendReceiveTestCase {

    /**
     * Construct a new <code>TimeToLiveTestCase</code>
     *
     * @param name the name of test case
     */
    public TimeToLiveTestCase(String name) {
        super(name);
    }

    /**
     * Verifies that a message's JMSExpiration property falls in an expected
     * range
     *
     * @param message the message to check
     * @param start the time just prior to the message being sent
     * @param end the time just after to the message being sent
     * @param timeToLive the time-to-live used to publish the message
     * @throws Exception for any error
     */
    protected void checkExpiration(Message message, long start, long end,
                                   long timeToLive) throws Exception {
        long expiryTime = message.getJMSExpiration();
        if (timeToLive == 0) {
            if (expiryTime != 0) {
                // message should never expire, but expiry has been set
                fail("JMSExpiration should be 0 (never expires) for a "
                     + "time-to-live of 0");
            }
        } else {
            long min = start + timeToLive;
            long max = end + timeToLive;

            if (expiryTime < min) {
                fail("JMSExpiration < expected expiration. Expected value > "
                     + min + ", but got value=" + expiryTime);
            }
            if (expiryTime >  max) {
                fail("JMSExpiration > expected expiration. Expected value < "
                     + max + ", but got value=" + expiryTime);
            }
        }
    }

    /**
     * Verifies that a message received via the supplied MessageReceiver
     * has the same JMSExpiration as that of a message.
     * If the session is transacted, it will be committed prior to proceeding
     *
     * @param message the sent message to compare with
     * @param receiver the receiver used to receive a message
     * @throws Exception for any error
     */
    protected void checkSameExpiration(Message message,
                                       MessageReceiver receiver)
        throws Exception {
        long expected = message.getJMSExpiration();
        TestContext context = getContext();

        Session session = context.getSession();
        if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
            session.commit();
        }

        Message received = receive(receiver);
        assertEquals("JMSExpiration different to that on send",
                     expected, received.getJMSExpiration());
        MessagingBehaviour behaviour = context.getMessagingBehaviour();
        ReceiptType type = behaviour.getReceiptType();
        if (!type.equals(ReceiptType.BROWSER)) {
            received.acknowledge();
        }
    }

}
