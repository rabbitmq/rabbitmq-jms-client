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
 * $Id: ReadWriteTest.java,v 1.6 2004/02/03 07:31:03 tanderson Exp $
 */
package org.exolab.jmscts.test.message.readwrite;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageNotReadableException;
import javax.jms.StreamMessage;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageVerifier;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.EmptyMessageVerifier;
import org.exolab.jmscts.test.message.util.EmptyPropertyVerifier;
import org.exolab.jmscts.test.message.util.MessagePopulatorVerifier;
import org.exolab.jmscts.test.message.util.MessagePropertyVerifier;
import org.exolab.jmscts.test.message.util.PropertyValues;
import org.exolab.jmscts.test.message.util.PopulatorVerifierFactory;


/**
 * This class tests that message properties and message bodies may be read
 * and written on creation of a new message
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractMessageTestCase
 */
public class ReadWriteTest extends AbstractMessageTestCase
    implements PropertyValues {

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ReadWriteTest.class);


    /**
     * Construct a new <code>ReadWriteTest</code>
     *
     * @param name the name of test case
     */
    public ReadWriteTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(ReadWriteTest.class);
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
     * Verifies that a message is writable on creation.
     *
     * @jmscts.message all
     * @jmscts.requirement message.creation
     * @throws Exception for any error
     */
    public void testWriteableOnCreation() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        try {
            MessagePropertyVerifier populator = new MessagePropertyVerifier();
            populator.populate(message);
        } catch (Exception exception) {
            log.debug(exception, exception);
            fail("Failed to populate message properties on creation: "
                 + exception);
        }

        try {
            MessagePopulatorVerifier populator =
                PopulatorVerifierFactory.create(message, null);
            populator.populate(message);
        } catch (Exception exception) {
            log.debug(exception, exception);
            fail("Failed to populate message body on creation: " + exception);
        }
    }

    /**
     * Verifies that a message is readable on creation.
     *
     * @jmscts.message MapMessage
     * @jmscts.message Message
     * @jmscts.message ObjectMessage
     * @jmscts.message TextMessage
     * @jmscts.requirement message.creation
     * @throws Exception for any error
     */
    public void testReadableOnCreation() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        // verify that the message has no properties set (with the possible
        // exception of provider properties)
        try {
            MessageVerifier verifier = new EmptyPropertyVerifier();
            verifier.verify(message);
        } catch (Exception exception) {
            String msg = "Attempt to read the properties of a new "
                + "message should not throw any exceptions";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        MessageVerifier verifier = new EmptyMessageVerifier();
        try {
            verifier.verify(message);
        } catch (Exception exception) {
            String msg = "Failed to read the message body of a new "
                + "message";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }
     }

    /**
     * Verifies that attempting to read a BytesMessage on creation
     * throws <code>MessageNotReadableException</code>, until
     * <code>BytesMessage.reset()</code> is invoked, and that subsequent
     * reads throw <code>MessageEOFException</code>
     *
     * @jmscts.message BytesMessage
     * @jmscts.requirement message.bytes.creation
     * @jmscts.requirement message.bytes.eof
     * @jmscts.requirement message.creation
     * @throws Exception for any error
     */
    public void testBytesReadableOnCreation() throws Exception {
        checkBytesStreamReadableOnCreation();
    }

    /**
     * Verifies that attempting to read a StreamMessage on creation
     * throws <code>MessageNotReadableException</code>, until
     * <code>StreamMessage.reset()</code> is invoked, and that subsequent
     * reads throw <code>MessageEOFException</code>
     *
     * @jmscts.message StreamMessage
     * @jmscts.requirement message.creation
     * @jmscts.requirement message.stream.creation
     * @jmscts.requirement message.stream.eof
     * @throws Exception for any error
     */
    public void testStreamReadableOnCreation() throws Exception {
        checkBytesStreamReadableOnCreation();
    }

    /**
     * Verifies that a message is writeable after clearing its body.
     *
     * @jmscts.requirement message.method.clearBody
     * @throws Exception for any error
     */
    public void testWriteableOnClear() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        MessagePopulatorVerifier populator =
            PopulatorVerifierFactory.create(message, null);

        try {
            populator.populate(message);
        } catch (Exception exception) {
            log.debug(exception, exception);
            fail("Failed to populate message body on creation: " + exception);
        }

        message.clearBody();

        try {
            populator.populate(message);
        } catch (Exception exception) {
            log.debug(exception, exception);
            fail("Failed to populate message body after invoking clearBody: "
                 + exception);
        }
    }

    /**
     * Verifies that attempting to read a BytesMessage or StreamMessage
     * on creation <code>MessageNotReadableException</code>, until
     * <code>reset()</code> is invoked, and that subsequent reads throw
     * <code>MessageEOFException</code>
     *
     * @throws Exception for any error
     */
    private void checkBytesStreamReadableOnCreation() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        // verify that the message has no properties set (with the possible
        // exception of provider properties)
        try {
            MessageVerifier verifier = new EmptyPropertyVerifier();
            verifier.verify(message);
        } catch (Exception exception) {
            String msg = "Attempt to read the properties of a new "
                + "message should not throw any exceptions";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        // check that each method throws MessageNotReadableException
        MessageVerifier verifier = new EmptyMessageVerifier(
            MessageNotReadableException.class);
        try {
            verifier.verify(message);
        } catch (Exception exception) {
            String msg = "Attempt to read body of a new "
                +  context.getMessageType().getName()
                +  " should throw MessageNotReadableException";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        // reset the message so that it can be read.
        if (message instanceof BytesMessage) {
            ((BytesMessage) message).reset();
        } else {
            ((StreamMessage) message).reset();
        }

        // check that each applicable method throws MessageEOFException
        verifier = new EmptyMessageVerifier(MessageEOFException.class);
        try {
            verifier.verify(message);
        } catch (Exception exception) {
            String msg = "Attempt to read body of a new "
                +  context.getMessageType().getName()
                + " after reset() should throw MessageEOFException";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }
    }

}
