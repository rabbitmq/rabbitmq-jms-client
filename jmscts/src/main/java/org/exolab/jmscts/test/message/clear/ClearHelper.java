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
 * $Id: ClearHelper.java,v 1.2 2004/02/03 07:31:02 tanderson Exp $
 */
package org.exolab.jmscts.test.message.clear;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageNotReadableException;
import javax.jms.StreamMessage;

import org.apache.log4j.Logger;

import junit.framework.Assert;

import org.exolab.jmscts.core.MessageVerifier;
import org.exolab.jmscts.test.message.util.EmptyMessageVerifier;
import org.exolab.jmscts.test.message.util.EmptyPropertyVerifier;


/**
 * This class provides helper methods for testing the behaviour of
 * {@link Message#clearBody} and {@link Message#clearProperties}
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 */
class ClearHelper extends Assert {

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ClearHelper.class);


    /**
     * Check that invoking {@link Message#clearProperties} leaves a message
     * with no properties, but does not affect the message body
     *
     * @param message the message to check
     * @param verifier the verifier to check that the body hasn't changed
     * @param provider true if the message may contain provider properties
     * @throws Exception for any error
     */
    public static void checkClearProperties(
        Message message, MessageVerifier verifier, boolean provider)
        throws Exception {

        try {
            message.clearProperties();
        } catch (Exception exception) {
            String msg = "Message.clearProperties() failed";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        try {
            MessageVerifier empty = new EmptyPropertyVerifier(provider);
            empty.verify(message);
        } catch (Exception exception) {
            String msg = "Expected clearProperties() to clear all "
                + "properties";
            log.debug(msg, exception);
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
            String msg = "Message body should not have changed after "
                + "invoking Message.clearProperties() : " + exception;
            log.debug(msg, exception);
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
    public static void checkClearBody(Message message,
                                      MessageVerifier verifier)
        throws Exception {

        try {
            message.clearBody();
        } catch (Exception exception) {
            String msg = "Message.clearBody() failed";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        try {
            verifier.verify(message);
        } catch (Exception exception) {
            String msg = "Message properties should not have changed after "
                + "invoking Message.clearBody()";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }

        MessageVerifier empty = null;
        if (message instanceof BytesMessage
            || message instanceof StreamMessage) {

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
            String msg = "Message body should not have any content "
                + "after invoking clearBody()";
            log.debug(msg, exception);
            fail(msg + ": " + exception);
        }
    }

    /**
     * Create a new {@link EmptyMessageVerifier} for the supplied message
     *
     * @param message the message to create the verifier for
     * @return a new message verifier
     */
    public static EmptyMessageVerifier createEmptyVerifier(Message message) {
        EmptyMessageVerifier result = null;
        if (message instanceof BytesMessage
            || message instanceof StreamMessage) {
            result = new EmptyMessageVerifier(MessageEOFException.class);
        } else {
            result = new EmptyMessageVerifier();
        }
        return result;
    }

}
