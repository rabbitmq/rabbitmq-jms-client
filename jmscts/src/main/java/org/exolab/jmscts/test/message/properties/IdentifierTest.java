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
 * $Id: IdentifierTest.java,v 1.6 2004/02/03 21:52:10 tanderson Exp $
 */
package org.exolab.jmscts.test.message.properties;

import java.util.Enumeration;

import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;
import javax.jms.Message;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.PropertyValues;


/**
 * This class tests message property identifiers.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractMessageTestCase
 */
public class IdentifierTest extends AbstractMessageTestCase
    implements PropertyValues {

    /**
     * Valid identifiers
     */
    private static final String[] VALID_IDENTIFIERS = {
        "A", "_A", "$A", "a1", "_a1", "$a1", "a_a", "a$a",
        "\u0041\u0061\u0391\u0430\u0301"};

    /**
     * Invalid identifiers
     */
    private static final String[] INVALID_IDENTIFIERS = {
        null, "1", "+1", "-1", "'1'", "a.b", "\u0030"};

    /**
     * Names reserved for message selectors. These are case-insensitive.
     */
    private static final String[] RESERVED = {
        "NULL", "TRUE", "FALSE", "NOT", "AND", "OR", "BETWEEN", "LIKE", "IN",
        "IS", "ESCAPE"};

    /**
     * JMSX prefixed identifiers that may only be set by the provider
     */
    private static final String[] JMSX_PROVIDER_IDENTIFIERS = {
        "JMSXUserID", "JMSXAppID", "JMSXDeliveryCount", "JMSXProducerTXID",
        "JMSXConsumerTXID", "JMSXRcvTimestamp", "JMSXState"};

    /**
     * Create an instance of this class for a specific test case, to be run
     * against all message types
     *
     * @param name the name of test case
     */
    public IdentifierTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(IdentifierTest.class);
    }

    /**
     * Verifies that valid identifiers may be used as property names
     *
     * @jmscts.requirement properties.identifier
     * @throws Exception for any error
     */
    public void testValidIdentifiers() throws Exception {
        checkIdentifiers(VALID_IDENTIFIERS, false);
    }

    /**
     * Verifies that invalid identifiers can't be used as property names
     *
     * @jmscts.requirement properties.identifier
     * @throws Exception for any error
     */
    public void testInvalidIdentifiers() throws Exception {
        checkIdentifiers(INVALID_IDENTIFIERS, true);
    }

    /**
     * Verifies that property names are case sensitive
     *
     * @jmscts.requirement properties.identifier.case
     * @throws Exception for any error
     */
    public void testIdentifierCase() throws Exception {
        final String upper = "ABC";
        final String lower = upper.toLowerCase();
        TestContext context = getContext();
        Message message = context.getMessage();

        for (int i = 0; i < ALL_VALUES.length; ++i) {
            for (int j = 0; j < ALL_VALUES[i].length; ++j) {
                message.setObjectProperty(upper, ALL_VALUES[i][j]);
                if (message.getObjectProperty(lower) != null) {
                    fail("getObjectProperty() is not treating names as being"
                         + " case sensitive");
                }
                message.setObjectProperty(upper, null);
                message.setObjectProperty(lower, ALL_VALUES[i][j]);
                if (message.getObjectProperty(upper) != null) {
                    fail("getObjectProperty() is not treating names as being"
                         + " case sensitive");
                }
                message.setObjectProperty(lower, null);
            }
        }
    }

    /**
     * Verifies that using a reserved word as a property name throws an
     * exception.
     *
     * @jmscts.requirement properties.identifier.reserved
     * @throws Exception for any error
     */
    public void testReservedWords() throws Exception {
        checkIdentifiers(RESERVED, true);
        checkIdentifiers(toLowerCase(RESERVED), true);
    }

    /**
     * Tests that JMSX prefixed identifiers set by the provider cannot be
     * set by clients. This should only apply to those identifiers that the
     * provider supports
     *
     * @jmscts.requirement properties.provider
     * @throws Exception for any error
     */
    public void testJMSXProviderIdentifiers() throws Exception {
        TestContext context = getContext();
        ConnectionMetaData metaData = context.getConnection().getMetaData();
        Enumeration<?> iter = metaData.getJMSXPropertyNames();
        while (iter.hasMoreElements()) {
            String name = (String) iter.nextElement();
            for (int i = 0; i < JMSX_PROVIDER_IDENTIFIERS.length; ++i) {
                if (name.equals(JMSX_PROVIDER_IDENTIFIERS[i])) {
                    checkIdentifiers(new String[]{name}, true);
                }
            }
        }
    }

    /**
     * Check that a list of identifiers can/can't be used as property names
     *
     * @param identifiers the list of identifiers
     * @param fail if true, the test is expected to fail
     * @throws Exception for any error
     */
    private void checkIdentifiers(String[] identifiers, boolean fail)
        throws Exception {
        for (int i = 0; i < identifiers.length; ++i) {
            String identifier = identifiers[i];
            for (int j = 0; j < ALL_VALUES.length; ++j) {
                Object[] values = ALL_VALUES[j];
                for (int k = 0; k < values.length; ++k) {
                    Object value = values[k];
                    if (fail) {
                        expectFailure(identifier, value);
                    } else {
                        expectSuccess(identifier, value);
                    }
                }
            }
        }
    }

    /**
     * Expect the set property methods to succeed for a valid identifier
     *
     * @param identifier the valid identifier
     * @param value the property value
     * @throws Exception for any error
     */
    private void expectSuccess(String identifier, Object value)
        throws Exception {

        Message message = getContext().getMessage();
        message.setObjectProperty(identifier, value);
        PropertyHelper.setPrimitiveProperty(message, identifier, value);
    }

    /**
     * Expect the set property methods to fail for an invalid identifier
     *
     * @param identifier the invalid identifier
     * @param value the property value
     * @throws Exception for any error
     */
    private void expectFailure(String identifier, Object value)
        throws Exception {
        String packagePrefix = "java.lang.";

        Message message = getContext().getMessage();

        try {
            PropertyHelper.setPrimitiveProperty(message, identifier, value);
            String type = value.getClass().getName();
            if (type.startsWith(packagePrefix)) {
                type = type.substring(packagePrefix.length());
            }
            String method = "set" + type + "Property()";
            fail("Expected " + method + " to fail for invalid identifier="
                 + identifier + ", value=" + value);
        } catch (JMSException expected) {
            // the expected behaviour
        } catch (IllegalArgumentException expected) {
            // the expected behaviour
        }

        try {
            message.setObjectProperty(identifier, value);
            fail("Expected setObjectProperty() to fail for invalid "
                 + "identifier=" + identifier + ", value=" + value);
        } catch (IllegalArgumentException expected) {
            // the expected behaviour
        } catch (JMSException expected) {
            // the expected behaviour
        }
    }

    /**
     * Returns a lowercase version of an array of Strings
     *
     * @param strings a list of strings to convert to lowercase
     * @return the corresponding array of lowercase strings
     */
    private String[] toLowerCase(String[] strings) {
        String[] result = new String[strings.length];
        for (int i = 0; i < strings.length; ++i) {
            result[i] = strings[i].toLowerCase();
        }
        return result;
    }

}
