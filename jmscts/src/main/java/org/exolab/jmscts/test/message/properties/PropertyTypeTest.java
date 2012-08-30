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
 * $Id: PropertyTypeTest.java,v 1.5 2004/02/03 07:31:03 tanderson Exp $
 */
package org.exolab.jmscts.test.message.properties;

import javax.jms.Message;
import javax.jms.MessageFormatException;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.PropertyValues;


/**
 * This class tests message property types
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractMessageTestCase
 */
public class PropertyTypeTest extends AbstractMessageTestCase
    implements PropertyValues {

    /**
     * Construct a new <code>PropertyTypeTest</code>
     *
     * @param name the name of test case
     */
    public PropertyTypeTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(PropertyTypeTest.class);
    }

    /**
     * Verifies the supported primitive property types.
     *
     * @jmscts.requirement properties.types
     * @throws Exception for any error
     */
    public void testPropertyTypes() throws Exception {

        Message message = getContext().getMessage();
        final String name = "test";

        for (int i = 0; i < BOOLEANS.length; ++i) {
            boolean value = BOOLEANS[i].booleanValue();
            message.setBooleanProperty(name, value);
            assertEquals(value, message.getBooleanProperty(name));
        }

        for (int i = 0; i < BYTES.length; ++i) {
            byte value = BYTES[i].byteValue();
            message.setByteProperty(name, value);
            assertEquals(value, message.getByteProperty(name));
        }

        for (int i = 0; i < SHORTS.length; ++i) {
            short value = SHORTS[i].shortValue();
            message.setShortProperty(name, value);
            assertEquals(value, message.getShortProperty(name));
        }

        for (int i = 0; i < INTS.length; ++i) {
            int value = INTS[i].intValue();
            message.setIntProperty(name, value);
            assertEquals(value, message.getIntProperty(name));
        }

        for (int i = 0; i < LONGS.length; ++i) {
            long value = LONGS[i].longValue();
            message.setLongProperty(name, value);
            assertEquals(value, message.getLongProperty(name));
        }

        for (int i = 0; i < FLOATS.length; ++i) {
            float value = FLOATS[i].floatValue();
            message.setFloatProperty(name, value);
            if (Float.isNaN(value)) {
                assertTrue(Float.isNaN(message.getFloatProperty(name)));
            } else {
                assertEquals(value, message.getFloatProperty(name), 0.0);
            }
        }

        for (int i = 0; i < DOUBLES.length; ++i) {
            double value = DOUBLES[i].doubleValue();
            message.setDoubleProperty(name, value);
            if (Double.isNaN(value)) {
                assertTrue(Double.isNaN(message.getDoubleProperty(name)));
            } else {
                assertEquals(value, message.getDoubleProperty(name), 0.0);
            }
        }

        for (int i = 0; i < STRINGS.length; ++i) {
            String value = STRINGS[i];
            message.setStringProperty(name, value);
            assertEquals(value, message.getStringProperty(name));
        }
    }

    /**
     * Verifies valid and invalid object property types.
     *
     * @jmscts.requirement properties.objects
     * @throws Exception for any error
     */
    public void testObjectPropertyTypes() throws Exception {

        Message message = getContext().getMessage();
        final String name = "test";

        for (int i = 0; i < ALL_VALUES.length; ++i) {
            Object[] values = ALL_VALUES[i];
            for (int j = 0; j < values.length; ++j) {
                Object value = values[j];
                message.setObjectProperty(name, value);

                // verify that the type is the same as that set
                assertEquals(value.getClass(),
                             message.getObjectProperty(name).getClass());

                // test that set/get values are equal
                if (value instanceof Float && ((Float) value).isNaN()) {
                    assertTrue(
                        ((Float) message.getObjectProperty(name)).isNaN());
                } else if (value instanceof Double
                           && ((Double) value).isNaN()) {
                    assertTrue(
                        ((Double) message.getObjectProperty(name)).isNaN());
                } else {
                    assertEquals(value, message.getObjectProperty(name));
                }
            }
        }

        // test nulls
        message.setObjectProperty(name, null);
        assertEquals(null, message.getObjectProperty(name));

        // test an invalid object property
        try {
            message.setObjectProperty(name, new Character('A'));
            fail("Managed to set invalid object property");
        } catch (MessageFormatException expected) {
            // the expected behaviour
        }
    }

}
