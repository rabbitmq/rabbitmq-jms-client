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
 * $Id: PropertyTest.java,v 1.6 2005/06/16 06:36:53 tanderson Exp $
 */
package org.exolab.jmscts.test.message.properties;

import java.util.Map;
import java.util.Set;

import javax.jms.Message;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests that message properties set before a message is sent are
 * identical to those received.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractSendReceiveTestCase
 */
public class PropertyTest extends AbstractSendReceiveTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "PropertyTest";


    /**
     * Construct a new <code>PropertyTest</code>
     *
     * @param name the name of test case
     */
    public PropertyTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(PropertyTest.class);
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
     * Verifies that user properties are not modified when a message is sent
     * and subsequently received.
     *
     * @jmscts.requirement properties.integrity
     * @jmscts.requirement properties.method.getPropertyNames
     * @throws Exception for any error
     */
    public void testPropertyIntegrity() throws Exception {
        Message message = getContext().getMessage();

        // the properties prior to the message being sent
        Map<?, ?> beforeProperties = null;

        // the properties after the message is sent
        Map<?, ?> afterProperties = null;

        // the properties of the received message
        Map<?, ?> receivedProperties = null;

        Set<?> names = PropertyHelper.setProperties(message);
        beforeProperties = PropertyHelper.getProperties(message);

        // verify that all of the expected properties are available
        if (!names.equals(beforeProperties.keySet())) {
            fail("The list of property names returned by getPropertyNames() "
                 + "is different to that set");
        }

        // send the message to the provider, and wait to receive it back
        Message received = sendReceive(message, DESTINATION);

        // check the properties of the message after it was sent
        afterProperties = PropertyHelper.getProperties(message);
        if (!beforeProperties.equals(afterProperties)) {
            fail("The list of property names returned by getPropertyNames() "
                 + "on the received message is different to that set");
        }

        // check the properties of the received message, after removing
        // any provider properties
        receivedProperties = PropertyHelper.getProperties(received);
        Object[] keys = receivedProperties.keySet().toArray();
        for (int i = 0; i < keys.length; ++i) {
            String name = (String) keys[i];
            if (name.startsWith("JMSX") || name.startsWith("JMS_")) {
                receivedProperties.remove(name);
            }
        }

        if (!beforeProperties.equals(receivedProperties)) {
            String msg = "Properties sent don't match those received";
            fail(msg);
        }

        // check that the properties can be set after they are cleared
        received.clearProperties();
        PropertyHelper.setProperties(received);

        acknowledge(received);
    }

    /**
     * Verifies that JMS standard header fields are not returned by
     * getPropertyNames().
     *
     * @jmscts.requirement properties.method.getPropertyNames
     * @throws Exception for any error
     */
    public void testPropertyNames() throws Exception {
        // the list of JMS header field names
        final String[] headerNames = {
            "JMSDestination", "JMSDeliveryMode", "JMSExpiration",
            "JMSPriority", "JMSMessageID", "JMSTimestamp", "JMSCorrelationID",
            "JMSReplyTo", "JMSType", "JMSRedelivered"};

        // make sure no header properties are available prior to sending
        Message message = getContext().getMessage();
        for (int i = 0; i < headerNames.length; ++i) {
            if (message.propertyExists(headerNames[i])) {
                fail("Message.propertyExists() returned true for JMS header "
                     + "field=" + headerNames[i]);
            }
        }

        // make sure no header properties are available after sending, and on
        // receipt
        Message received = sendReceive(message, DESTINATION);
        for (int i = 0; i < headerNames.length; ++i) {
            if (message.propertyExists(headerNames[i])) {
                fail("Message.propertyExists() returned true for JMS header "
                     + "field=" + headerNames[i] + " after the message "
                     + "was sent");
            }
            if (received.propertyExists(headerNames[i])) {
                fail("Message.propertyExists() returned true for JMS header "
                     + "field=" + headerNames[i] + " for a received message");
            }
        }

        acknowledge(received);
    }

}
