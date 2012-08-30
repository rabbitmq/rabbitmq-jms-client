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
 * $Id: SendReceiveClearTest.java,v 1.11 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.test.message.clear;

import java.util.Iterator;
import java.util.List;

import javax.jms.Message;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;

import org.exolab.jmscts.test.message.util.MessagePopulatorVerifier;
import org.exolab.jmscts.test.message.util.MessagePropertyVerifier;
import org.exolab.jmscts.test.message.util.PopulatorVerifierFactory;


/**
 * This class tests the behaviour of <code>Message.clearBody()</code> and
 * <code>Message.clearProperties()</code> on send and receipt, against all
 * message, delivery, and transaction types
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.11 $
 * @jmscts.delivery all
 */
public class SendReceiveClearTest extends AbstractSendReceiveTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "SendReceiveClearTest";


    /**
     * Construct a new <code>SendReceiveClearTest</code>
     *
     * @param name the name of test case
     */
    public SendReceiveClearTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(SendReceiveClearTest.class);
    }

    /**
     * Get the message populator. This implementation returns null, as
     * population is handled by the test cases.
     *
     * @return <code>null</code>
     */
    @Override
    public MessagePopulator getMessagePopulator() {
        return null;
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[] {DESTINATION};
    }

    /**
     * Verifies that <code>Message.clearProperties()</code> leaves the message
     * properties empty, and doesn't clear the message body, on receipt of
     * a message.
     *
     * @jmscts.requirement message.method.clearProperties
     * @throws Exception for any error
     */
    public void testClearPropertiesOnReceipt() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        MessagePopulatorVerifier properties = new MessagePropertyVerifier();
        properties.populate(message);

        MessagePopulatorVerifier body = PopulatorVerifierFactory.create(
            message);
        body.populate(message);

        Message received = sendReceive(message, DESTINATION);
        ClearHelper.checkClearProperties(received, body, true);
        acknowledge(received);
    }

    /**
     * Verifies that <code>Message.clearBody()</code> leaves the message
     * body empty, and doesn't clear the message properties, on receipt of
     * a message.
     *
     * @jmscts.message Message
     * @jmscts.message MapMessage
     * @jmscts.message ObjectMessage
     * @jmscts.message TextMessage
     * @jmscts.requirement message.method.clearBody
     * @throws Exception for any error
     */
    public void testClearBodyOnReceipt() throws Exception {
        checkClearBodyOnReceipt();
    }

    /**
     * Verifies that <code>BytesMessage.clearBody()</code> leaves the message
     * body empty, and doesn't clear the message properties, on receipt of
     * a message.
     *
     * @jmscts.message BytesMessage
     * @jmscts.requirement message.method.clearBody
     * @jmscts.requirement message.bytes.method.clearBody
     * @throws Exception for any error
     */
    public void testBytesClearBodyOnReceipt() throws Exception {
        checkClearBodyOnReceipt();
    }

    /**
     * Verifies that <code>StreamMessage.clearBody()</code> leaves the message
     * body empty, and doesn't clear the message properties, on receipt of
     * a message.
     *
     * @jmscts.message StreamMessage
     * @jmscts.requirement message.method.clearBody
     * @jmscts.requirement message.stream.method.clearBody
     * @throws Exception for any error
     */
    public void testStreamClearBodyOnReceipt() throws Exception {
        checkClearBodyOnReceipt();
    }

    /**
     * Verifies that <code>Message.clearProperties()</code> and
     * <code>Message.clearBody()</code> on send doesn't affect the sent message
     *
     * @jmscts.requirement message.method.clearProperties
     * @jmscts.requirement message.method.clearBody
     * @throws Exception for any error
     */
    public void testClearOnSend() throws Exception {
        final int count = 10;
        TestContext context = getContext();
        Message message = context.getMessage();

        MessagePopulatorVerifier properties = new MessagePropertyVerifier();
        MessagePopulatorVerifier body = PopulatorVerifierFactory.create(
            message);

        MessageReceiver receiver = createReceiver(DESTINATION);
        try {
            MessageSender sender = createSender(DESTINATION);
            for (int i = 0; i < count; ++i) {
                message.clearProperties();
                properties.populate(message);
                message.clearBody();
                body.populate(message);
                sender.send(message, 1);
            }

            Session session = context.getSession();
            if (session.getTransacted() && !(session instanceof javax.jms.XASession)) {
                session.commit();
            }

            List<?> messages = receive(receiver, count);
            Iterator<?> iter = messages.iterator();
            while (iter.hasNext()) {
                Message received = (Message) iter.next();
                properties.verify(received);
                body.verify(received);
            }

            // make sure messages are acknowledged, so the destination
            // can be safely destroyed prior to session closure
            acknowledge(messages);
        } finally {
            receiver.remove();
        }
    }

    /**
     * Verifies that <code>Message.clearProperties()</code> leaves the message
     * properties empty, and doesn't clear the message body, on receipt of
     * a message.
     *
     * @throws Exception for any error
     */
    private void checkClearBodyOnReceipt() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        MessagePopulatorVerifier properties = new MessagePropertyVerifier();
        MessagePopulatorVerifier body = PopulatorVerifierFactory.create(
            message);

        properties.populate(message);
        body.populate(message);

        Message received = sendReceive(message, DESTINATION);
        ClearHelper.checkClearBody(received, properties);
        acknowledge(received);
    }

}
