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
 * Copyright 2001-2005 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: SendReceiveReadWriteTest.java,v 1.7 2005/06/16 06:36:54 tanderson Exp $
 */
package org.exolab.jmscts.test.message.readwrite;

import javax.jms.Message;
import javax.jms.MessageNotWriteableException;
import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.SequenceMessagePopulator;
import org.exolab.jmscts.core.SequencePropertyPopulator;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.MessagePopulatorVerifier;
import org.exolab.jmscts.test.message.util.MessagePropertyVerifier;
import org.exolab.jmscts.test.message.util.PropertyValues;
import org.exolab.jmscts.test.message.util.PopulatorVerifierFactory;


/**
 * This class tests that message properties and message bodies are writeable
 * on send, and readable on receipt
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.7 $
 * @see AbstractSendReceiveTestCase
 */
public class SendReceiveReadWriteTest extends AbstractSendReceiveTestCase
    implements PropertyValues {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "SendReceiveReadWriteTest";

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(SendReceiveReadWriteTest.class);


    /**
     * Construct a new <code>SendReceiveReadWriteTest</code>
     *
     * @param name the name of test case
     */
    public SendReceiveReadWriteTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(
            SendReceiveReadWriteTest.class);
    }

    /**
     * Get the message populator. This implementation always returns null
     *
     * @return null
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
     * Verifies that the same message may be populated and sent more than once.
     *
     * @jmscts.requirement message.send
     * @throws Exception for any error
     */
    public void testWriteableOnSend() throws Exception {
        final int count = 10;
        TestContext context = getContext();
        context.getSession();
        Message message = context.getMessage();

        MessageSender sender = createSender(DESTINATION);
        MessagePopulator property = new SequencePropertyPopulator();
        MessagePopulator body = new SequenceMessagePopulator();

        try {
            for (int i = 0; i < count; ++i) {
                try {
                    property.populate(message);
                } catch (Exception exception) {
                    String msg = "Failed to populate message properties on "
                        + i + " iteration prior to send";
                    log.error(msg, exception);
                    fail(msg + ": " + exception);
                }

                try {
                    body.populate(message);
                } catch (Exception exception) {
                    String msg = "Failed to populate message body on " + i
                        + " iteration prior to send";
                    log.error(msg, exception);
                    fail(msg + ": " + exception);
                }
                sender.send(message, 1);
            }
        } finally {
            sender.close();
        }
    }

    /**
     * Verifies that a message's user properties and body are read-only on
     * receipt, and that they may be subsequently cleared.
     *
     * @jmscts.requirement message.receive
     * @throws Exception for any error
     */
    public void testReadOnlyOnReceipt() throws Exception {
        TestContext context = getContext();
        context.getSession();
        Message message = context.getMessage();

        Class<MessageNotWriteableException> exceptionType = MessageNotWriteableException.class;
        MessagePopulatorVerifier properties =
            new MessagePropertyVerifier(exceptionType);
        MessagePopulatorVerifier body =
            PopulatorVerifierFactory.create(message, exceptionType);

        Message received = sendReceive(message, DESTINATION);

        // verify that that MessageNotWriteableException is thrown for
        // each user property type, and each set/write method of the
        // message. An exception will only be thrown if another type
        // of exception is generated, or one of the operations doesn't
        // throw MessageNotWriteableException
        try {
            properties.populate(received);
        } catch (Exception exception) {
            fail("Expected MessageNotWriteableException to be thrown when "
                 + "populating the properties of a received message, but "
                 + " got exception=" + exception.getClass().getName()
                 + ", message=" + exception.getMessage());
        }
        try {
            body.populate(received);
        } catch (Exception exception) {
            fail("Expected MessageNotWriteableException to be thrown when "
                 + "populating the body of a received message, but "
                 + " got exception=" + exception.getClass().getName()
                 + ", message=" + exception.getMessage());
        }

        try {
            received.clearProperties();
        } catch (Exception exception) {
            fail("Attempt to clear the properties of a received message "
                 + "threw exception=" + exception.getClass().getName()
                 + ", message=" + exception.getMessage());
        }

        try {
            received.clearBody();
        } catch (Exception exception) {
            fail("Attempt to clear the body of a received message "
                 + "threw exception=" + exception.getClass().getName()
                 + ", message=" + exception.getMessage());
        }

        acknowledge(received);
    }

}
