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
 * $Id: ClearTest.java,v 1.6 2004/02/03 07:31:02 tanderson Exp $
 */
package org.exolab.jmscts.test.message.clear;

import javax.jms.Message;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;

import org.exolab.jmscts.test.message.util.EmptyMessageVerifier;
import org.exolab.jmscts.test.message.util.EmptyPropertyVerifier;
import org.exolab.jmscts.test.message.util.MessagePopulatorVerifier;
import org.exolab.jmscts.test.message.util.MessagePropertyVerifier;
import org.exolab.jmscts.test.message.util.PopulatorVerifierFactory;


/**
 * This class tests the behaviour of <code>Message.clearBody()</code> and
 * <code>Message.clearProperties()</code>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 */
public class ClearTest extends AbstractMessageTestCase {

    /**
     * Construct a new <code>ClearTest</code>
     *
     * @param name the name of test case
     */
    public ClearTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(ClearTest.class);
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
     * Verifies that <code>Message.clearProperties()</code> can be invoked
     * for a new message.
     *
     * @jmscts.requirement message.method.clearProperties
     * @throws Exception for any error
     */
    public void testClearPropertiesOnCreation() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        EmptyMessageVerifier empty = ClearHelper.createEmptyVerifier(message);
        ClearHelper.checkClearProperties(message, empty, false);
    }

    /**
     * Verifies that <code>Message.clearBody()</code> can be invoked for a new
     * message.
     *
     * @jmscts.message Message
     * @jmscts.message MapMessage
     * @jmscts.message ObjectMessage
     * @jmscts.message TextMessage
     * @jmscts.requirement message.method.clearBody
     * @throws Exception for any error
     */
    public void testClearBodyOnCreation() throws Exception {
        checkClearBodyOnCreation();
    }

    /**
     * Verifies that <code>BytesMessage.clearBody()</code> can be invoked
     * for a new message
     *
     * @jmscts.message BytesMessage
     * @jmscts.requirement message.method.clearBody
     * @jmscts.requirement message.bytes.method.clearBody
     * @throws Exception for any error
     */
    public void testBytesClearBodyOnCreation() throws Exception {
        checkClearBodyOnCreation();
    }

    /**
     * Verifies that <code>StreamMessage.clearBody()</code> can be invoked
     * for a new message
     *
     * @jmscts.message StreamMessage
     * @jmscts.requirement message.method.clearBody
     * @jmscts.requirement message.stream.method.clearBody
     * @throws Exception for any error
     */
    public void testStreamClearBodyOnCreation() throws Exception {
        checkClearBodyOnCreation();
    }

    /**
     * Verifies that <code>Message.clearProperties()</code> leaves the message
     * properties empty, and doesn't clear the message body.
     *
     * @jmscts.requirement message.method.clearProperties
     * @throws Exception for any error
     */
    public void testClearProperties() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        MessagePopulatorVerifier properties = new MessagePropertyVerifier();
        properties.populate(message);

        MessagePopulatorVerifier body = PopulatorVerifierFactory.create(
            message);
        body.populate(message);
        ClearHelper.checkClearProperties(message, body, false);
    }

    /**
     * Verifies that <code>Message.clearBody()</code> leaves the message body
     * empty, and doesn't clear the message properties.
     *
     * @jmscts.message Message
     * @jmscts.message MapMessage
     * @jmscts.message ObjectMessage
     * @jmscts.message TextMessage
     * @jmscts.requirement message.method.clearBody
     * @throws Exception for any error
     */
    public void testClearBody() throws Exception {
        checkClearBody();
    }

    /**
     * Verifies that <code>BytesMessage.clearBody()</code> leaves the message
     * body empty, and doesn't clear the message properties.
     *
     * @jmscts.message BytesMessage
     * @jmscts.requirement message.method.clearBody
     * @jmscts.requirement message.bytes.method.clearBody
     * @throws Exception for any error
     */
    public void testBytesClearBody() throws Exception {
        checkClearBody();
    }

    /**
     * Verifies that <code>StreamMessage.clearBody()</code> leaves the message
     * body empty, and doesn't clear the message properties.
     *
     * @jmscts.message StreamMessage
     * @jmscts.requirement message.method.clearBody
     * @jmscts.requirement message.stream.method.clearBody
     * @throws Exception for any error
     */
    public void testStreamClearBody() throws Exception {
        checkClearBody();
    }

    /**
     * Verifies that <code>Message.clearBody()</code> can be invoked for a new
     * message.
     *
     * @throws Exception for any error
     */
    private void checkClearBodyOnCreation() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        ClearHelper.checkClearBody(message, new EmptyPropertyVerifier());
    }

    /**
     * Verifies that <code>Message.clearBody()</code> leaves the message body
     * empty, and doesn't clear the message properties.
     *
     * @throws Exception for any error
     */
    private void checkClearBody() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        MessagePopulatorVerifier properties = new MessagePropertyVerifier();
        properties.populate(message);

        MessagePopulatorVerifier body = PopulatorVerifierFactory.create(
            message);
        body.populate(message);
        ClearHelper.checkClearBody(message, properties);
    }

}
