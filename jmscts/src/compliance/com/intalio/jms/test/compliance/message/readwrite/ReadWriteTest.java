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
 *    please contact jima@intalio.com.
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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: ReadWriteTest.java,v 1.6 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.message.readwrite;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;
import javax.jms.StreamMessage;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.JMSTestRunner;
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
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>message.bytes.creation</li>
 *   <li>message.bytes.eof</li>
 *   <li>message.creation</li>
 *   <li>message.method.clearBody</li>
 *   <li>message.stream.creation</li>
 *   <li>message.stream.eof</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractMessageTestCase
 */
public class ReadWriteTest extends AbstractMessageTestCase 
    implements PropertyValues {

    /**
     * Requirements covered by this test case
     */
    private static final String BYTES_EOF_REQUIREMENT = "message.bytes.eof";
    private static final String BYTES_REQUIREMENT = "message.bytes.creation";
    private static final String CREATION_REQUIREMENT = "message.creation";
    private static final String CLEAR_REQUIREMENT = "message.method.clearBody";
    private static final String STREAM_EOF_REQUIREMENT = "message.stream.eof";
    private static final String STREAM_REQUIREMENT = "message.stream.creation";

    private static final String[] BYTES_READABLE_ON_CREATION = {
        BYTES_EOF_REQUIREMENT, BYTES_REQUIREMENT, CREATION_REQUIREMENT};

    private static final String[] STREAM_READABLE_ON_CREATION = {
        STREAM_EOF_REQUIREMENT, STREAM_REQUIREMENT, CREATION_REQUIREMENT};

    private static final String[][] REQUIREMENTS = {
        {"testWriteableOnCreation", CREATION_REQUIREMENT},
        {"testWriteableOnClear", CLEAR_REQUIREMENT}};

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(ReadWriteTest.class.getName());


    /**
     * Create an instance of this class for a specific test case, testing 
     * against all message and delivery types
     * 
     * @param name the name of test case
     */
    public ReadWriteTest(String name) {
        super(name, REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner test = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(test);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by 
     * {@link JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(ReadWriteTest.class);
    }

    /**
     * Returns the requirement identifiers covered by a test case.
     * This overrides the 
     * {@link org.exolab.jmscts.core.JMSTestCase#getRequirements} method
     * in order to return requirement coverage on a test context basis
     *
     * @param test the name of the test method
     * @return a list of requirement identifiers covered by the test, or null 
     * if the test doesn't cover any requirements
     */
    public String[] getRequirements(String test) {
        String[] result = null;
        if (test.equals("testReadableOnCreation")) {
            Message message = getContext().getMessage();
            if (message instanceof BytesMessage) {
                result = BYTES_READABLE_ON_CREATION;
            } else if (message instanceof StreamMessage) {
                result = STREAM_READABLE_ON_CREATION;
            } else {
                result = new String[]{CREATION_REQUIREMENT};
            }
        } else {
            result = super.getRequirements(test);
        }
        return result;
    }

    /**
     * Get the message populator. This implementation always returns null
     *
     * @return null
     */
    public MessagePopulator getMessagePopulator() {
        return null;
    }

    /**
     * Test that a message is writable on creation.
     * This covers requirements:
     * <ul>
     *   <li>message.creation</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testWriteableOnCreation() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        try {
            MessagePropertyVerifier populator = new MessagePropertyVerifier();
            populator.populate(message);
        } catch (Exception exception) {
            _log.debug(exception, exception);
            fail("Failed to populate message properties on creation: " +
                 exception);
        }

        try {
            MessagePopulatorVerifier populator = 
                    PopulatorVerifierFactory.create(message, null);
            populator.populate(message);
        } catch (Exception exception) {
            _log.debug(exception, exception);
            fail("Failed to populate message body on creation: " + exception);
        }
    }

    /**
     * Test that a message is readable on creation, with the exception of
     * BytesMessage and StreamMessage - these should both throw 
     * MessageNotReadableException, until reset is invoked.
     * This covers requirements:
     * <ul>
     *   <li>message.creation</li>
     *   <li>message.bytes.creation</li>
     *   <li>message.bytes.eof</li>
     *   <li>message.stream.creation</li>
     *   <li>message.stream.eof</li>
     * </ul>
     *
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
            String msg = "Attempt to read the properties of a new " +
                "message should not throw any exceptions";
            _log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        if  (message instanceof BytesMessage || 
             message instanceof StreamMessage) {

            MessageVerifier verifier;
            // check that each method throws MessageNotReadableException
            verifier = new EmptyMessageVerifier(
                MessageNotReadableException.class);
            try {
                verifier.verify(message);
            } catch (Exception exception) {
                String msg = "Attempt to read body of a new " + 
                    context.getMessageType().getName() + 
                    " should throw MessageNotReadableException";
                _log.debug(msg, exception);
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
                String msg = "Attempt to read body of a new " + 
                    context.getMessageType().getName() + 
                    " after reset() should throw MessageEOFException";
                _log.debug(msg, exception);
                fail(msg + ": " + exception);
            }
        } else {
            MessageVerifier verifier = new EmptyMessageVerifier();
            try {
                verifier.verify(message);
            } catch (Exception exception) {
                String msg = "Failed to read the message body of a new " +
                    "message";
                _log.debug(msg, exception);
                fail(msg + ": " + exception);
            } 
        }       
     }

    /**
     * Test that a message is writeable after clearing its body. This covers
     * requirements:
     * <ul>
     *   <li>message.method.clearBody</li>
     * </ul>
     *
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
            _log.debug(exception, exception);
            fail("Failed to populate message body on creation: " + exception);
        }

        message.clearBody();
        
        try {
            populator.populate(message);
        } catch (Exception exception) {
            _log.debug(exception, exception);
            fail("Failed to populate message body after invoking clearBody: "
                 + exception);
        }
    }
    
} //-- ReadWriteTest
