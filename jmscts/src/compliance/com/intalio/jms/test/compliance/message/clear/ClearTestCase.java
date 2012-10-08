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
 * $Id: ClearTestCase.java,v 1.6 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.message.clear;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageNotReadableException;
import javax.jms.StreamMessage;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.MessageVerifier;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.EmptyMessageVerifier;
import org.exolab.jmscts.test.message.util.EmptyPropertyVerifier;


/**
 * This class provides helper methods for testing the behaviour of 
 * {@link Message#clearBody} and {@link Message#clearProperties}
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractMessageTestCase
 */
abstract class ClearTestCase extends AbstractMessageTestCase {

    /**
     * Requirements covered by all clear test cases
     */
    protected static final String BODY_REQUIREMENT = 
        "message.method.clearBody";
    protected static final String PROPERTY_REQUIREMENT = 
        "message.method.clearProperties";
    protected static final String BYTE_BODY_REQUIREMENT =
        "message.bytes.method.clearBody";
    protected static final String STREAM_BODY_REQUIREMENT =
        "message.stream.method.clearBody";

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(ClearTestCase.class.getName());
    

    /**
     * Create an instance of this class for a specific test case, testing 
     * against all message types
     * 
     * @param name the name of test case
     * @param requirements a list of test methods and requirements that 
     * they cover. The first element in each list is the method name and the 
     * remaining elements the requirements that it covers.
     */
    public ClearTestCase(String name, String[][] requirements) {
        super(name, requirements);
    }

    /**
     * Create an instance of this class for a specific test case, testing 
     * against a set of message types
     * 
     * @param name the name of test case
     * @param types the message types to test against
     * @param requirements a list of test methods and requirements that 
     * they cover. The first element in each list is the method name and the 
     * remaining elements the requirements that it covers.
     */
    public ClearTestCase(String name, MessageTypes types, 
                         String[][] requirements) {
        super(name, types, requirements);
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
     * Check that invoking {@link Message#clearProperties} leaves a message 
     * with no properties, but does not affect the message body
     *
     * @param message the message to check
     * @param verifier the verifier to check that the body hasn't changed
     * @param provider true if the message may contain provider properties
     * @throws Exception for any error
     */
    protected void checkClearProperties(
        Message message, MessageVerifier verifier, boolean provider) 
        throws Exception {
        TestContext context = getContext();

        try {
            message.clearProperties();
        } catch (Exception exception) {
            String msg = "Message.clearProperties() failed";
            _log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        try {
            MessageVerifier empty = new EmptyPropertyVerifier(provider);
            empty.verify(message);
        } catch (Exception exception) {
            String msg = "Expected clearProperties() to clear all " +
                "properties";
            _log.debug(msg, exception);
            fail(msg);
        }

        // need to reset BytesMessage and StreamMessage instances to make
        // them readable
        if (message instanceof BytesMessage) {
            ((BytesMessage) message).reset();
        } else if (message instanceof StreamMessage) {
            ((StreamMessage) message).reset();
        }

        try {
            verifier.verify(message);
        } catch (Exception exception) {
            String msg = "Message body should not have changed after " +
                "invoking Message.clearProperties() : " + exception;
            _log.debug(msg, exception);
            fail(msg);
        }
    }

    /**
     * Verify that a message has no body, and that the message properties
     * haven't changed
     *
     * @param message the message to check
     * @param verifier the verifier to check that the properties haven't 
     * changed
     * @throws Exception for any error
     */
    protected void checkClearBody(Message message, MessageVerifier verifier)
        throws Exception {
        TestContext context = getContext();

        try {
            message.clearBody();
        } catch (Exception exception) {
            String msg = "Message.clearBody() failed";
            _log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        try {
            verifier.verify(message);
        } catch (Exception exception) {
            String msg = "Message properties should not have changed after " +
                "invoking Message.clearBody()";
            _log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        MessageVerifier empty = null;
        if (message instanceof BytesMessage || 
            message instanceof StreamMessage) {

            // check that invoking methods on BytesMessage or StreamMessage
            // throws MessageNotReadableException after clearBody has been
            // invoked
            empty = new EmptyMessageVerifier(
                MessageNotReadableException.class);
            empty.verify(message);
            
            // now reset them to make them readable
            if (message instanceof BytesMessage) {
                ((BytesMessage) message).reset();
            } else if (message instanceof StreamMessage) {
                ((StreamMessage) message).reset();
            }
        } 
          
        empty = createEmptyVerifier(message);

        try {
            empty.verify(message);
        } catch (Exception exception) {
            String msg = "Message body should not have any content " +
                "after invoking clearBody()";
            _log.debug(msg, exception);
            fail(msg + ": " + exception);
        }
    }

    /**
     * Create a new {@link EmptyMessageVerifier} for the supplied message
     *
     * @param message the message to create the verifier for
     * @return a new message verifier
     */
    protected MessageVerifier createEmptyVerifier(Message message) {
        MessageVerifier result = null;
        if (message instanceof BytesMessage || 
            message instanceof StreamMessage) {
            result = new EmptyMessageVerifier(MessageEOFException.class);
        } else {
            result = new EmptyMessageVerifier();
        }
        return result;
    }
    
} //-- ClearTestCase
