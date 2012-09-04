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
 * $Id: PropertyConversionTest.java,v 1.5 2004/02/03 07:31:03 tanderson Exp $
 */
package org.exolab.jmscts.test.message.properties;

import javax.jms.Message;
import javax.jms.MessageFormatException;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.ClassHelper;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.PropertyValues;


/**
 * This class tests message property conversion
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractMessageTestCase
 */
@SuppressWarnings("rawtypes")
public class PropertyConversionTest extends AbstractMessageTestCase
    implements PropertyValues {

    /**
     * Invalid boolean conversions
     */
    private static final Class[] INVALID_BOOLEANS = {
        byte.class, short.class, int.class, long.class, float.class,
        double.class};

    /**
     * Invalid byte conversions
     */
    private static final Class[] INVALID_BYTES = {
        boolean.class, float.class, double.class};

    /**
     * Invalid short conversions
     */
    private static final Class[] INVALID_SHORTS = {
        boolean.class, byte.class, float.class, double.class};

    /**
     * Invalid int conversions
     */
    private static final Class[] INVALID_INTS = {
        boolean.class, byte.class, short.class, float.class, double.class};

    /**
     * Invalid long conversions
     */
    private static final Class[] INVALID_LONGS = {
        boolean.class, byte.class, short.class, int.class, float.class,
        double.class};

    /**
     * Invalid float conversions
     */
    private static final Class[] INVALID_FLOATS = {
        boolean.class, byte.class, short.class, int.class, long.class};

    /**
     * Invalid double conversions
     */
    private static final Class[] INVALID_DOUBLES = {
        boolean.class, byte.class, short.class, int.class, long.class,
        float.class};

    // Note: for the primitive type A -> string -> primitive type B tests
    // to work correctly, the range of values for numeric primitives must
    // not overlap those of smaller numeric primitives eg. the set of
    // values defined by INTS must exceed the range of the values defined
    // by SHORTS and so on.


    /**
     * Construct a new <code>PropertyConversionTest</code>
     *
     * @param name the name of test case
     */
    public PropertyConversionTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(PropertyConversionTest.class);
    }

    /**
     * Verifies boolean property conversion
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testBoolean() throws Exception {
        final String name = "boolean";

        Message message = getContext().getMessage();

        for (int i = 0; i < BOOLEANS.length; ++i) {
            boolean value = BOOLEANS[i].booleanValue();
            message.setObjectProperty(name, BOOLEANS[i]);
            Boolean property = (Boolean) message.getObjectProperty(name);
            assertEquals(value, property.booleanValue());

            message.setBooleanProperty(name, value);

            // check valid conversions
            assertEquals(value, message.getBooleanProperty(name));
            assertTrue(message.getObjectProperty(name) instanceof Boolean);
            property = (Boolean) message.getObjectProperty(name);
            assertEquals(value, property.booleanValue());

            String str = message.getStringProperty(name);
            assertEquals(value, Boolean.valueOf(str).booleanValue());

            // check invalid conversions
            for (int j = 0; j < INVALID_BOOLEANS.length; ++j) {
                expectException(name, boolean.class, INVALID_BOOLEANS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Verifies byte property conversion
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testByte() throws Exception {
        final String name = "byte";

        Message message = getContext().getMessage();

        for (int i = 0; i < BYTES.length; ++i) {
            byte value = BYTES[i].byteValue();
            message.setObjectProperty(name, BYTES[i]);
            assertEquals(
                value, ((Byte) message.getObjectProperty(name)).byteValue());

            message.setByteProperty(name, value);

            // check valid conversions
            assertEquals(value, message.getByteProperty(name));
            assertEquals(value, message.getShortProperty(name));
            assertEquals(value, message.getIntProperty(name));
            assertEquals(value, message.getLongProperty(name));
            assertTrue(message.getObjectProperty(name) instanceof Byte);
            assertEquals(
                value, ((Byte) message.getObjectProperty(name)).byteValue());

            String str = message.getStringProperty(name);
            assertEquals(value, Byte.parseByte(str));

            // check invalid conversions
            for (int j = 0; j < INVALID_BYTES.length; ++j) {
                expectException(name, byte.class, INVALID_BYTES[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Verifies short property conversion
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testShort() throws Exception {
        final String name = "short";

        Message message = getContext().getMessage();

        for (int i = 0; i < SHORTS.length; ++i) {
            short value = SHORTS[i].shortValue();
            message.setObjectProperty(name, SHORTS[i]);
            assertEquals(
                value, ((Short) message.getObjectProperty(name)).shortValue());

            message.setShortProperty(name, value);

            // check valid conversions
            assertEquals(value, message.getShortProperty(name));
            assertEquals(value, message.getIntProperty(name));
            assertEquals(value, message.getLongProperty(name));

            String str = message.getStringProperty(name);
            assertEquals(value, Short.parseShort(str));

            // check invalid conversions
            for (int j = 0; j < INVALID_SHORTS.length; ++j) {
                expectException(name, short.class, INVALID_SHORTS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Verifies int property conversion
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testInt() throws Exception {
        final String name = "int";

        Message message = getContext().getMessage();

        for (int i = 0; i < INTS.length; ++i) {
            int value = INTS[i].intValue();
            message.setObjectProperty(name, INTS[i]);
            assertTrue(((Integer) message.getObjectProperty(name)).intValue()
                       == value);

            message.setIntProperty(name, value);

            // check valid conversions
            assertEquals(value, message.getIntProperty(name));
            assertEquals(value, message.getLongProperty(name));
            assertTrue(message.getObjectProperty(name) instanceof Integer);
            assertEquals(
                value, ((Integer) message.getObjectProperty(name)).intValue());

            String str = message.getStringProperty(name);
            assertEquals(value, Integer.parseInt(str));

            // check invalid conversions
            for (int j = 0; j < INVALID_INTS.length; ++j) {
                expectException(name, int.class, INVALID_INTS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Verifies long property conversion
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testLong() throws Exception {
        final String name = "long";

        Message message = getContext().getMessage();

        for (int i = 0; i < LONGS.length; ++i) {
            long value = LONGS[i].longValue();
            message.setObjectProperty(name, LONGS[i]);
            assertEquals(
                value, ((Long) message.getObjectProperty(name)).longValue());

            message.setLongProperty(name, value);

            // check valid conversions
            assertEquals(value, message.getLongProperty(name));
            assertTrue(message.getObjectProperty(name) instanceof Long);
            assertEquals(
                value, ((Long) message.getObjectProperty(name)).longValue());

            String str = message.getStringProperty(name);
            assertEquals(value, Long.parseLong(str));

            // check invalid conversions
            for (int j = 0; j < INVALID_LONGS.length; ++j) {
                expectException(name, long.class, INVALID_LONGS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Verifies float property conversion
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testFloat() throws Exception {
        final String name = "float";

        Message message = getContext().getMessage();

        for (int i = 0; i < FLOATS.length; ++i) {
            // check valid conversions
            float value = FLOATS[i].floatValue();
            message.setObjectProperty(name, FLOATS[i]);
            checkFloat(name, value);

            message.setFloatProperty(name, value);

            assertTrue(message.getObjectProperty(name) instanceof Float);
            checkFloat(name, value);

            String str = message.getStringProperty(name);
            if (str.equals(Float.toString(Float.NaN))) {
                assertTrue(Float.isNaN(value));
            } else if (str.equals(Float.toString(Float.NEGATIVE_INFINITY))) {
                assertTrue(value == Float.NEGATIVE_INFINITY);
            } else if (str.equals(Float.toString(Float.POSITIVE_INFINITY))) {
                assertTrue(value == Float.POSITIVE_INFINITY);
            } else {
                assertTrue(Float.parseFloat(str) == FLOATS[i].floatValue());
            }

            // check invalid conversions
            for (int j = 0; j < INVALID_FLOATS.length; ++j) {
                expectException(name, float.class, INVALID_FLOATS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Verifies double property conversion
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testDouble() throws Exception {
        final String name = "double";

        Message message = getContext().getMessage();

        for (int i = 0; i < DOUBLES.length; ++i) {
            // check valid conversions
            double value = DOUBLES[i].doubleValue();
            message.setObjectProperty(name, DOUBLES[i]);
            checkDouble(name, value);

            message.setDoubleProperty(name, value);

            assertTrue(message.getObjectProperty(name) instanceof Double);
            checkDouble(name, value);

            String str = message.getStringProperty(name);
            if (str.equals(Double.toString(Double.NaN))) {
                assertTrue(Double.isNaN(value));
            } else if (str.equals(Double.toString(Double.NEGATIVE_INFINITY))) {
                assertTrue(value == Double.NEGATIVE_INFINITY);
            } else if (str.equals(Double.toString(Double.POSITIVE_INFINITY))) {
                assertTrue(value == Double.POSITIVE_INFINITY);
            } else {
                assertTrue(Double.parseDouble(str) == value);
            }

            // check invalid conversions
            for (int j = 0; j < INVALID_DOUBLES.length; ++j) {
                expectException(name, double.class, INVALID_DOUBLES[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Verifies string &lt;-&gt; object property conversions
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testStringObject() throws Exception {
        final String name = "stringObject";

        Message message = getContext().getMessage();

        for (int i = 0; i < STRINGS.length; ++i) {
            message.setObjectProperty(name, STRINGS[i]);
            assertTrue(((String) message.getObjectProperty(name)).equals(
                STRINGS[i]));

            message.setStringProperty(name, STRINGS[i]);

            assertEquals(STRINGS[i], message.getStringProperty(name));
            assertTrue(message.getObjectProperty(name) instanceof String);
            assertEquals(STRINGS[i], message.getObjectProperty(name));
        }

        // check null
        message.setObjectProperty(name, null);
        assertNull(message.getStringProperty(name));
    }

    /**
     * Verifies string &lt;-&gt; boolean property conversions
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testStringBoolean() throws Exception {
        final String name = "stringBoolean";

        Message message = getContext().getMessage();

        // check valid string <-> boolean conversions
        for (int i = 0; i < BOOLEANS.length; ++i) {
            String value = BOOLEANS[i].toString();
            message.setStringProperty(name, value);
            assertEquals(value, message.getStringProperty(name));
            assertEquals(BOOLEANS[i].booleanValue(),
                         message.getBooleanProperty(name));
        }

        // check invalid string <-> boolean conversions. The Boolean.valueOf()
        // operation treats anything not 'true' (case is ignored) as being
        // false, so no exception is expected.
        Object[][] invalidBooleans = {BYTES, SHORTS, INTS, LONGS, FLOATS,
                                      DOUBLES};
        for (int i = 0; i < invalidBooleans.length; ++i) {
            for (int j = 0; j < invalidBooleans[i].length; ++j) {
                String value = invalidBooleans[i][j].toString();
                message.setStringProperty(name, value);
                assertFalse(message.getBooleanProperty(name));
            }
        }
    }

    /**
     * Verifies string &lt;-&gt; byte property conversions
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testStringByte() throws Exception {
        final String name = "stringByte";

        Message message = getContext().getMessage();

        // check valid string <-> byte conversions
        for (int i = 0; i < BYTES.length; ++i) {
            String value = BYTES[i].toString();
            message.setStringProperty(name, value);
            assertEquals(value, message.getStringProperty(name));
            assertEquals(BYTES[i].byteValue(), message.getByteProperty(name));
        }

        // check invalid string -> byte conversions
        final Class[] invalidBytes = {boolean.class, short.class, int.class,
                                      long.class, float.class, double.class};
        expectConversionException(byte.class, invalidBytes,
                                  NumberFormatException.class);
    }

    /**
     * Verifies string &lt;-&gt; short property conversions
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testStringShort() throws Exception {
        final String name = "stringShort";

        Message message = getContext().getMessage();

        // check valid string <-> short conversions
        for (int i = 0; i < SHORTS.length; ++i) {
            String value = SHORTS[i].toString();
            message.setStringProperty(name, value);
            assertEquals(value, message.getStringProperty(name));
            assertEquals(SHORTS[i].shortValue(),
                         message.getShortProperty(name));
        }

        // check invalid string -> short conversions
        final Class[] invalidShorts = {boolean.class, int.class, long.class,
                                       float.class, double.class};
        expectConversionException(short.class, invalidShorts,
                                  NumberFormatException.class);

    }

    /**
     * Verifies string &lt;-&gt; int property conversions
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testStringInt() throws Exception {
        final String name = "stringInt";

        Message message = getContext().getMessage();

        // check valid string <-> int conversions
        for (int i = 0; i < INTS.length; ++i) {
            String value = INTS[i].toString();
            message.setStringProperty(name, value);
            assertEquals(value, message.getStringProperty(name));
            assertEquals(INTS[i].intValue(), message.getIntProperty(name));
        }

        // check invalid string -> int conversions
        final Class[] invalidInts = {boolean.class, long.class, float.class,
                                     double.class};
        expectConversionException(int.class, invalidInts,
                                  NumberFormatException.class);
    }

    /**
     * Verifies string &lt;-&gt; long property conversions
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testStringLong() throws Exception {
        final String name = "stringLong";

        Message message = getContext().getMessage();

        // check valid string <-> long conversions
        for (int i = 0; i < LONGS.length; ++i) {
            String value = LONGS[i].toString();
            message.setStringProperty(name, value);
            assertEquals(value, message.getStringProperty(name));
            assertEquals(LONGS[i].longValue(), message.getLongProperty(name));
        }

        // check invalid string -> long conversions
        final Class[] invalidLongs = {boolean.class, float.class,
                                      double.class};
        expectConversionException(long.class, invalidLongs,
                                  NumberFormatException.class);
    }

    /**
     * Verifies string &lt;-&gt; float property conversions
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testStringFloat() throws Exception {
        final String name = "stringFloat";

        Message message = getContext().getMessage();

        // check valid string <-> float conversions
        for (int i = 0; i < FLOATS.length; ++i) {
            String value = FLOATS[i].toString();
            message.setStringProperty(name, value);
            assertTrue(message.getStringProperty(name).equals(value));

            if (value.equals(Float.toString(Float.NaN))) {
                assertTrue(FLOATS[i].isNaN());
            } else if (value.equals(Float.toString(Float.NEGATIVE_INFINITY))) {
                assertTrue(FLOATS[i].floatValue() == Float.NEGATIVE_INFINITY);
            } else if (value.equals(Float.toString(Float.POSITIVE_INFINITY))) {
                assertTrue(FLOATS[i].floatValue() == Float.POSITIVE_INFINITY);
            } else {
                assertTrue(message.getFloatProperty(name)
                           == FLOATS[i].floatValue());
            }
        }

        // check invalid string -> float conversions
        final Class[] invalidFloats = {boolean.class};
        expectConversionException(float.class, invalidFloats,
                                  NumberFormatException.class);
        // NOTE: the JDK 1.3 implementation of floats treats numbers that
        // exceed the range of a float as being infinity, hence the following:
        for (int i = 0; i < DOUBLES.length; ++i) {
            double value = DOUBLES[i].doubleValue();
            if (value == Double.MAX_VALUE || value == Double.MIN_VALUE) {
                message.setStringProperty(name, DOUBLES[i].toString());
                if (value == Double.MIN_VALUE) {
                    if (message.getFloatProperty(name) != 0.0) {
                        fail("Expected string conversion from double to "
                             + "float to underflow for double=" + value
                             + ", but got " + message.getFloatProperty(name));
                    }
                } else {
                    if (!Float.isInfinite(message.getFloatProperty(name))) {
                        fail("Expected string conversion from double to "
                             + "float to be infinite for double=" + value
                             + ", but got " + message.getFloatProperty(name));
                    }
                }
            } else {
                /* FILIP HANIK - removed
                expectConversionException(float.class,
                                          new Double[]{DOUBLES[i]},
                                          NumberFormatException.class);
               */
            }
        }
    }

    /**
     * Verifies string &lt;-&gt; double property conversions
     *
     * @jmscts.requirement properties.conversion
     * @throws Exception for any error
     */
    public void testStringDouble() throws Exception {
        final String name = "stringDouble";

        Message message = getContext().getMessage();

        // check valid string <-> double conversions
        for (int i = 0; i < DOUBLES.length; ++i) {
            String value = DOUBLES[i].toString();
            message.setStringProperty(name, value);
            assertTrue(message.getStringProperty(name).equals(value));

            if (value.equals(Double.toString(Double.NaN))) {
                assertTrue(DOUBLES[i].isNaN());
            } else if (value.equals(Double.toString(
                Double.NEGATIVE_INFINITY))) {
                assertTrue(DOUBLES[i].doubleValue()
                           == Double.NEGATIVE_INFINITY);
            } else if (value.equals(Double.toString(
                Double.POSITIVE_INFINITY))) {
                assertTrue(DOUBLES[i].doubleValue()
                           == Double.POSITIVE_INFINITY);
            } else {
                assertTrue(message.getDoubleProperty(name)
                           == DOUBLES[i].doubleValue());
            }
        }

        // check invalid string -> double conversions
        /* FILIP HANIK - removed
        final Class[] invalidDoubles = {boolean.class};
        expectConversionException(double.class, invalidDoubles,
                                  NumberFormatException.class);
        */
    }

    /**
     * Verifies that attempting to read a null as a primitive type is
     * equivalent to calling the primitive's corresponding valueOf(String)
     * conversion method with a null value.
     *
     * @jmscts.requirement properties.conversion.null
     * @throws Exception for any error
     */
    public void testNullToPrimitive() throws Exception {
        final String name = "nulltest";

        Message message = getContext().getMessage();
        message.setObjectProperty(name, null);

        // boolean - for booleans, anything other than "true"
        // (case is ignored) is considered false
        assertFalse(message.getBooleanProperty(name));

        // byte
        expectException(name, byte.class, byte.class,
                        NumberFormatException.class);

        // short
        expectException(name, short.class, short.class,
                        NumberFormatException.class);

        // int
        expectException(name, int.class, int.class,
                        NumberFormatException.class);

        // long
        expectException(name, long.class, long.class,
                        NumberFormatException.class);

        // float
        expectException(name, float.class, float.class,
                        NumberFormatException.class);

        // double
        expectException(name, double.class, double.class,
                        NumberFormatException.class);
    }

    /**
     * Checks that a float property equals the expected value
     *
     * @param name the name of the property
     * @param expected the expected value
     * @throws Exception if the value doesn't match the expected value
     */
    private void checkFloat(String name, float expected) throws Exception {

        Message message = getContext().getMessage();

        if (Float.isNaN(expected)) {
            assertTrue(Float.isNaN(message.getFloatProperty(name)));
            assertTrue(Double.isNaN(message.getDoubleProperty(name)));
            assertTrue(((Float) message.getObjectProperty(name)).isNaN());
        } else {
            assertTrue(message.getFloatProperty(name) == expected);
            assertTrue(message.getDoubleProperty(name) == expected);
            assertTrue(((Float) message.getObjectProperty(name)).floatValue()
                       == expected);
        }
    }

    /**
     * Checks that a double property equals the expected value
     *
     * @param name the name of the property
     * @param expected the expected value
     * @throws Exception if the value doesn't match the expected value
     */
    private void checkDouble(String name, double expected) throws Exception {

        Message message = getContext().getMessage();

        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(message.getDoubleProperty(name)));
            assertTrue(((Double) message.getObjectProperty(name)).isNaN());
        } else {
            assertTrue(message.getDoubleProperty(name) == expected);
            assertTrue(((Double) message.getObjectProperty(name)).doubleValue()
                       == expected);
        }
    }

    /**
     * For each class 'A' listed in classes, converts the range of values
     * associated with 'A' to a String property, and then verifies that the
     * conversion to an instance of the primitive fails
     *
     * @param primitive the class of the primitive type
     * @param classes the list of primitive types to attempt conversion for
     * @param exceptionType the class of the expected exception
     * @throws Exception if the operation succeeds, or the wrong exception is
     * thrown
     */
    private void expectConversionException(Class<?> primitive, Class[] classes,
                                           Class<?> exceptionType)
        throws Exception {

        for (int i = 0; i < classes.length; ++i) {
            if (classes[i].equals(boolean.class)) {
                expectConversionException(primitive, BOOLEANS, exceptionType);
            } else if (classes[i].equals(byte.class)) {
                expectConversionException(primitive, BYTES, exceptionType);
            } else if (classes[i].equals(short.class)) {
                expectConversionException(primitive, SHORTS, exceptionType);
            } else if (classes[i].equals(int.class)) {
                expectConversionException(primitive, INTS, exceptionType);
            } else if (classes[i].equals(long.class)) {
                expectConversionException(primitive, LONGS, exceptionType);
            } else if (classes[i].equals(float.class)) {
                expectConversionException(primitive, FLOATS, exceptionType);
            } else if (classes[i].equals(double.class)) {
                expectConversionException(primitive, DOUBLES, exceptionType);
            } else {
                throw new Exception("Don't support " + classes[i].getName());
            }
        }
    }

    /**
     * For each value in an array, convert them to a string, and attempt
     * conversion using the supplied primitive's
     * message accessor method. The operation is expected to fail.
     *
     * @param primitive the class of the primitive type
     * @param values the array of values to convert
     * @param exceptionType the class of the expected exception
     * @throws Exception if the operation succeeds, or the wrong exception is
     * thrown
     */
    private void expectConversionException(Class<?> primitive, Object[] values,
                                           Class<?> exceptionType)
        throws Exception {

        Message message = getContext().getMessage();

        for (int i = 0; i < values.length; ++i) {
            Class<? extends Object> type = values[i].getClass();
            String name = ClassHelper.getPrimitiveType(type).getName();
            message.setStringProperty(name, values[i].toString());
            expectException(name, String.class, primitive, exceptionType);
        }
    }

    /**
     * Expect an exception when converting from one primitive type to another
     *
     * @param name the property name
     * @param from the type to convert from
     * @param to the type to convert to
     * @param exceptionType the expected exception
     * @throws Exception if the operation succeeds, or the wrong exception is
     * thrown
     */
    private void expectException(String name, Class<?> from, Class<?> to,
                                 Class<?> exceptionType) throws Exception {

        Message message = getContext().getMessage();
        try {
            if (to.equals(boolean.class)) {
                message.getBooleanProperty(name);
            } else if (to.equals(byte.class)) {
                message.getByteProperty(name);
            } else if (to.equals(byte.class)) {
                message.getByteProperty(name);
            } else if (to.equals(short.class)) {
                message.getShortProperty(name);
            } else if (to.equals(int.class)) {
                message.getIntProperty(name);
            } else if (to.equals(long.class)) {
                message.getLongProperty(name);
            } else if (to.equals(float.class)) {
                message.getFloatProperty(name);
            } else if (to.equals(double.class)) {
                message.getDoubleProperty(name);
            } else if (to.equals(String.class)) {
                message.getStringProperty(name);
            }

            Object value = message.getObjectProperty(name);
            String text = (value == null) ? "<null>" : value.toString();
            fail("Conversion from " + from.getName() + "=" + text
                 + " to " + to.getName() + " should fail");
        } catch (Exception exception) {
            if (!exception.getClass().equals(exceptionType)) {
                Object value = message.getObjectProperty(name);
                String text = (value == null) ? "<null>" : value.toString();
                fail("Conversion from " + from.getName() + "=" + text
                     + " to " + to.getName() + " threw "
                     + exception.getClass().getName() + " - it should throw "
                     + exceptionType.getName());
            }
        }
    }

}
