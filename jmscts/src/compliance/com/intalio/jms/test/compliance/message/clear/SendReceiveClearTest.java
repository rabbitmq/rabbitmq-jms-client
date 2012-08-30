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
 * $Id: SendReceiveClearTest.java,v 1.6 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.message.clear;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.StreamMessage;

import junit.framework.Test;

import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessagingHelper;
import org.exolab.jmscts.core.SendReceiveTestCase;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;

import org.exolab.jmscts.test.message.util.MessagePopulatorVerifier;
import org.exolab.jmscts.test.message.util.MessagePropertyVerifier;
import org.exolab.jmscts.test.message.util.PopulatorVerifierFactory;


/**
 * This class tests the behaviour of {@link Message#clearBody} and 
 * {@link Message#clearProperties} on send and receipt, against all message,
 * delivery, and transaction types
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>message.method.clearProperties</li>
 *   <li>message.method.clearBody</li>
 *   <li>message.bytes.method.clearBody</li>
 *   <li>message.stream.method.clearBody</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see ClearTestCase
 * @see SendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 */
public class SendReceiveClearTest extends ClearTestCase 
    implements SendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testClearPropertiesOnReceipt", PROPERTY_REQUIREMENT},
        {"testClearBodyOnReceipt", BODY_REQUIREMENT},
        {"testClearOnSend", PROPERTY_REQUIREMENT, BODY_REQUIREMENT}};

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "SendReceiveClearTest";

    /**
     * The destinations to test against. This is a map of Destination 
     * instances keyed by name.
     */
    private Map _destinations = null;


    /**
     * Create an instance of this class for a specific test case, testing 
     * against all message and delivery types
     * 
     * @param name the name of test case
     */
    public SendReceiveClearTest(String name) {
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
     * Sets up the test suite, to be run against all session transaction types
     *
     * @return an instance of this class that may be run by 
     * {@link JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(SendReceiveClearTest.class);
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
        if (test.equals("testClearBodyOnReceipt")) {
            Message message = getContext().getMessage();
            if (message instanceof BytesMessage) {
                result = new String[]{BODY_REQUIREMENT, BYTE_BODY_REQUIREMENT};
            } else if (message instanceof StreamMessage) {
                result = new String[]{BODY_REQUIREMENT, 
                                      STREAM_BODY_REQUIREMENT};
            } else {
                result = super.getRequirements(test);
            }
        } else {
            result = super.getRequirements(test);
        }
        return result;
    }
    
    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return new String[] {DESTINATION};
    }

    /**
     * Sets the pre-created destinations corresponding to those returned by
     * {@link #getDestinations}.
     *
     * @param destinations a map of Destination instances keyed on their name
     */
    public void setDestinations(Map destinations) {
        _destinations = destinations;
    }

    /**
     * Return the delivery types to run this test case against. This test
     * runs against all delivery types
     *
     * @return the delivery types to run this test case against
     */
    public DeliveryTypes getDeliveryTypes() {
        return DeliveryTypes.ALL;
    }

    /**
     * Returns true if this test case only supports durable topic subscribers.
     * If false, it supports both durable and non-durable topic subscribers.
     * Note that this does not prevent the test case from supporting queue
     * consumers or browsers.
     *
     * @return false
     */
    public boolean getDurableOnly() {
        return false;
    }

    /**
     * Test that <code>Message.clearProperties()</code> leaves the message
     * properties empty, and doesn't clear the message body, on receipt of
     * a message. This covers requirements:
     * <ul>
     *   <li>message.method.clearProperties</li>
     * </ul>
     *
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

        Destination destination = (Destination) _destinations.get(DESTINATION);
        Message received = MessagingHelper.sendReceive(
            context, message, destination);

        checkClearProperties(received, body, true);
    }

    /**
     * Test that <code>Message.clearBody()</code> leaves the message
     * body empty, and doesn't clear the message properties, on receipt of
     * a message. This covers requirements:
     * <ul>
     *   <li>message.method.clearBody</li>
     *   <li>message.bytes.method.clearBody</li>
     *   <li>message.stream.method.clearBody</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testClearBodyOnReceipt() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

        MessagePopulatorVerifier properties = new MessagePropertyVerifier();
        MessagePopulatorVerifier body = PopulatorVerifierFactory.create(
            message);

        properties.populate(message);
        body.populate(message);

        Destination destination = (Destination) _destinations.get(DESTINATION);
        Message received = MessagingHelper.sendReceive(
            context, message, destination);
        checkClearBody(received, properties);
    }

    /**
     * Test that <code>Message.clearProperties()<code> and 
     * <code>Message.clearBody()<code> on send doesn't affect the sent message
     * This covers requirements:
     * <ul>
     *   <li>message.method.clearProperties</li>
     *   <li>message.method.clearBody</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testClearOnSend() throws Exception {
        final int count = 10;
        TestContext context = getContext();
        Message message = context.getMessage();

        MessagePopulatorVerifier properties = new MessagePropertyVerifier();
        MessagePopulatorVerifier body = PopulatorVerifierFactory.create(
            message);

        Destination destination = (Destination) _destinations.get(DESTINATION);
        MessageReceiver receiver = SessionHelper.createReceiver(
            context, destination);
        try {
            MessageSender sender = SessionHelper.createSender(
                context, destination);
            for (int i = 0; i < count; ++i) {
                message.clearProperties();
                properties.populate(message);
                message.clearBody();
                body.populate(message);
                sender.send(message, 1);
            }

            Session session = context.getSession();
            if (session.getTransacted()) {
                session.commit();
            }

            List messages = MessagingHelper.receive(context, receiver, count);
            Iterator iter = messages.iterator();
            while (iter.hasNext()) {
                Message received = (Message) iter.next();
                properties.verify(received);
                body.verify(received);
            }
        } finally {
            receiver.remove();
        }
    }
    
} //-- SendReceiveClearTest
