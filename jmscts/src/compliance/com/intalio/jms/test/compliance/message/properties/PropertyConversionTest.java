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
 * $Id: PropertyConversionTest.java,v 1.6 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.message.properties;

import javax.jms.Message;
import javax.jms.MessageFormatException;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.ClassHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.PropertyValues;


/**
 * This class tests message property conversion
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>properties.conversion</li>
 *   <li>properties.conversion.null</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see AbstractMessageTestCase
 */
public class PropertyConversionTest extends AbstractMessageTestCase 
    implements PropertyValues {

    /**
     * Requirements covered by this test case
     */
    private static final String CONVERSION_REQUIREMENT = 
        "properties.conversion";
    private static final String NULL_REQUIREMENT = 
        "properties.conversion.null";

    private static final String[][] REQUIREMENTS = {
        {"testBoolean", CONVERSION_REQUIREMENT},
        {"testByte", CONVERSION_REQUIREMENT},
        {"testShort", CONVERSION_REQUIREMENT},
        {"testInt", CONVERSION_REQUIREMENT},
        {"testLong", CONVERSION_REQUIREMENT},
        {"testFloat", CONVERSION_REQUIREMENT},
        {"testDouble", CONVERSION_REQUIREMENT},
        {"testString", CONVERSION_REQUIREMENT},
        {"testNullToPrimitive", NULL_REQUIREMENT}};

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


    /**
     * Create an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public PropertyConversionTest(String name) {
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
        return TestCreator.createMessageTest(PropertyConversionTest.class);
    }

    /**
     * Test boolean property conversion
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testBoolean() throws Exception {
        final String name = "boolean";

        Message message = getContext().getMessage();

        for (int i = 0; i < BOOLEANS.length; ++i) {
            boolean value = BOOLEANS[i].booleanValue();
            message.setObjectProperty(name, BOOLEANS[i]);
            Boolean property = (Boolean) message.getObjectProperty(name);
            assertTrue(property.booleanValue() == value);

            message.setBooleanProperty(name, value);

            // check valid conversions
            assertTrue(message.getBooleanProperty(name) == value);
            assertTrue(message.getObjectProperty(name) instanceof Boolean);
            property = (Boolean) message.getObjectProperty(name);
            assertTrue(property.booleanValue() == value);

            String str = message.getStringProperty(name);
            assertTrue(Boolean.valueOf(str).booleanValue() == value);

            // check invalid conversions
            for (int j = 0; j < INVALID_BOOLEANS.length; ++j) {
                expectException(name, boolean.class, INVALID_BOOLEANS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Test byte property conversion
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testByte() throws Exception {
        final String name = "byte";

        Message message = getContext().getMessage();

        for (int i = 0; i < BYTES.length; ++i) {
            byte value = BYTES[i].byteValue();
            message.setObjectProperty(name, BYTES[i]);
            assertTrue(((Byte) message.getObjectProperty(name)).byteValue() ==
                       value);

            message.setByteProperty(name, value);

            // check valid conversions
            assertTrue(message.getByteProperty(name) == value);
            assertTrue(message.getShortProperty(name) == value);
            assertTrue(message.getIntProperty(name) == value);
            assertTrue(message.getLongProperty(name) == value);
            assertTrue(message.getObjectProperty(name) instanceof Byte);
            assertTrue(((Byte) message.getObjectProperty(name)).byteValue() ==
                       value);

            String str = message.getStringProperty(name);
            assertTrue(Byte.parseByte(str) == value);

            // check invalid conversions
            for (int j = 0; j < INVALID_BYTES.length; ++j) {
                expectException(name, byte.class, INVALID_BYTES[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Test short property conversion
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testShort() throws Exception {
        final String name = "short";

        Message message = getContext().getMessage();

        for (int i = 0; i < SHORTS.length; ++i) {
            short value = SHORTS[i].shortValue();
            message.setObjectProperty(name, SHORTS[i]);
            assertTrue(((Short) message.getObjectProperty(name)).shortValue() 
                       == value);

            message.setShortProperty(name, value);

            // check valid conversions
            assertTrue(message.getShortProperty(name) == value);
            assertTrue(message.getIntProperty(name) == value);
            assertTrue(message.getLongProperty(name) == value);

            String str = message.getStringProperty(name);
            assertTrue(Short.parseShort(str) == value);

            // check invalid conversions
            for (int j = 0; j < INVALID_SHORTS.length; ++j) {
                expectException(name, short.class, INVALID_SHORTS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Test int property conversion
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion</li>
     * </ul>
     *
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
            assertTrue(message.getIntProperty(name) == value);
            assertTrue(message.getLongProperty(name) == value);
            assertTrue(message.getObjectProperty(name) instanceof Integer);
            assertTrue(((Integer) message.getObjectProperty(name)).intValue() 
                       == value);

            String str = message.getStringProperty(name);
            assertTrue(Integer.parseInt(str) == value);

            // check invalid conversions
            for (int j = 0; j < INVALID_INTS.length; ++j) {
                expectException(name, int.class, INVALID_INTS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Test long property conversion
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testLong() throws Exception {
        final String name = "long";

        Message message = getContext().getMessage();

        for (int i = 0; i < LONGS.length; ++i) {
            long value = LONGS[i].longValue();
            message.setObjectProperty(name, LONGS[i]);
            assertTrue(((Long) message.getObjectProperty(name)).longValue() ==
                       value);

            message.setLongProperty(name, value);

            // check valid conversions
            assertTrue(message.getLongProperty(name) == value);
            assertTrue(message.getObjectProperty(name) instanceof Long);
            assertTrue(((Long) message.getObjectProperty(name)).longValue() ==
                       value);

            String str = message.getStringProperty(name);
            assertTrue(Long.parseLong(str) == value);

            // check invalid conversions
            for (int j = 0; j < INVALID_LONGS.length; ++j) {
                expectException(name, long.class, INVALID_LONGS[j],
                                MessageFormatException.class);
            }
        }
    }

    /**
     * Test float property conversion
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion</li>
     * </ul>
     *
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
     * Test double property conversion
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion</li>
     * </ul>
     *
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
     * Test string property conversion
     * <p>
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion</li>
     * </ul>
     *
     * Note: for the primitive type A -> string -> primitive type B tests to
     * work correctly, the range of values for numeric primitives must not 
     * overlap those of smaller numeric primitives eg. the set of values 
     * defined by {@link #INTS} must exceed the range of the values defined by
     * {@link #SHORTS} and so on.
     *
     * @throws Exception for any error
     */
    public void testString() throws Exception {
        final String name = "string";

        Message message = getContext().getMessage();

        for (int i = 0; i < STRINGS.length; ++i) {
            message.setObjectProperty(name, STRINGS[i]);
            assertTrue(((String) message.getObjectProperty(name)).equals(
                STRINGS[i]));

            message.setStringProperty(name, STRINGS[i]);

            assertTrue(message.getStringProperty(name).equals(STRINGS[i]));
            assertTrue(message.getObjectProperty(name) instanceof String);
            assertTrue(message.getObjectProperty(name).equals(STRINGS[i]));
        }

        // check valid string <-> boolean conversions
        for (int i = 0; i < BOOLEANS.length; ++i) {
            String value = BOOLEANS[i].toString();
            message.setStringProperty(name, value);
            assertTrue(message.getStringProperty(name).equals(value));
            assertTrue(message.getBooleanProperty(name) == 
                       BOOLEANS[i].booleanValue());
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
                assertTrue(message.getBooleanProperty(name) == false);
            }
        }

        // check valid string <-> byte conversions
        for (int i = 0; i < BYTES.length; ++i) {
            String value = BYTES[i].toString();
            message.setStringProperty(name, value);
            assertTrue(message.getStringProperty(name).equals(value));
            assertTrue(message.getByteProperty(name) == BYTES[i].byteValue());
        }

        // check invalid string -> byte conversions
        final Class[] invalidBytes = {boolean.class, short.class, int.class,
                                      long.class, float.class, double.class};
        expectConversionException(byte.class, invalidBytes,
                                  NumberFormatException.class);

        // check valid string <-> short conversions
        for (int i = 0; i < SHORTS.length; ++i) {
            String value = SHORTS[i].toString();
            message.setStringProperty(name, value);
            assertTrue(message.getStringProperty(name).equals(value));
            assertTrue(message.getShortProperty(name) == 
                       SHORTS[i].shortValue());
        }

        // check invalid string -> short conversions
        final Class[] invalidShorts = {boolean.class, int.class, long.class,
                                       float.class, double.class};
        expectConversionException(short.class, invalidShorts,
                                  NumberFormatException.class);

        // check valid string <-> int conversions
        for (int i = 0; i < INTS.length; ++i) {
            String value = INTS[i].toString();
            message.setStringProperty(name, value);
            assertTrue(message.getStringProperty(name).equals(value));
            assertTrue(message.getIntProperty(name) == INTS[i].intValue());
        }

        // check invalid string -> int conversions
        final Class[] invalidInts = {boolean.class, long.class, float.class, 
                                     double.class};
        expectConversionException(int.class, invalidInts,
                                  NumberFormatException.class);

        // check valid string <-> long conversions
        for (int i = 0; i < LONGS.length; ++i) {
            String value = LONGS[i].toString();
            message.setStringProperty(name, value);
            assertTrue(message.getStringProperty(name).equals(value));
            assertTrue(message.getLongProperty(name) == LONGS[i].longValue());
        }

        // check invalid string -> long conversions
        final Class[] invalidLongs = {boolean.class, float.class, 
                                      double.class};
        expectConversionException(long.class, invalidLongs,
                                  NumberFormatException.class);

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
                assertTrue(message.getFloatProperty(name) == 
                           FLOATS[i].floatValue());
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
                        fail("Expected string conversion from double to " +
                             "float to underflow for double=" + value + 
                             ", but got " + message.getFloatProperty(name));
                    }
                } else {
                    if (!Float.isInfinite(message.getFloatProperty(name))) {
                        fail("Expected string conversion from double to " +
                             "float to be infinite for double=" + value + 
                             ", but got " + message.getFloatProperty(name));
                    }
                }
            } else {
                expectConversionException(float.class, 
                                          new Double[]{DOUBLES[i]}, 
                                          NumberFormatException.class);
            }
        }

        // check valid string <-> double conversions
        for (int i = 0; i < DOUBLES.length; ++i) {
            String value = DOUBLES[i].toString();
            message.setStringProperty(name, value);
            assertTrue(message.getStringProperty(name).equals(value));

            if (value.equals(Double.toString(Double.NaN))) {
                assertTrue(DOUBLES[i].isNaN());
            } else if (value.equals(Double.toString(
                Double.NEGATIVE_INFINITY))) {
                assertTrue(DOUBLES[i].doubleValue() == 
                           Double.NEGATIVE_INFINITY);
            } else if (value.equals(Double.toString(
                Double.POSITIVE_INFINITY))) {
                assertTrue(DOUBLES[i].doubleValue() == 
                           Double.POSITIVE_INFINITY);
            } else {
                assertTrue(message.getDoubleProperty(name) == 
                           DOUBLES[i].doubleValue());
            }
        }

        // check invalid string -> double conversions
        final Class[] invalidDoubles = {boolean.class};
        expectConversionException(double.class, invalidDoubles,
                                  NumberFormatException.class);

        // check null
        message.setObjectProperty(name, null);
        assertTrue(message.getStringProperty(name) == null);
    }

    /**
     * Test that attempting to read a null as a primitive type is equivalent
     * as calling the primitive's corresponding valueOf(String) conversion 
     * method with a null value.
     * This covers requirements:
     * <ul>
     *   <li>properties.conversion.null</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testNullToPrimitive() throws Exception {
        final String name = "nulltest";

        Message message = getContext().getMessage();
        message.setObjectProperty(name, null);

        // boolean - for booleans, anything other than "true" 
        // (case is ignored) is considered false
        assertTrue(message.getBooleanProperty(name) == false);

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
                        NullPointerException.class);

        // double
        expectException(name, double.class, double.class, 
                        NullPointerException.class);
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
    private void expectConversionException(Class primitive, Class[] classes,
                                           Class exceptionType)
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
    private void expectConversionException(Class primitive, Object[] values, 
                                           Class exceptionType) 
        throws Exception {

        Message message = getContext().getMessage();

        for (int i = 0; i < values.length; ++i) {
            Class type = values[i].getClass();
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
    private void expectException(String name, Class from, Class to, 
                                 Class exceptionType) throws Exception {

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
                fail("Conversion from " + from.getName() + "=" + text +
                     " to " + to.getName() + " threw " + 
                     exception.getClass().getName() + " - it should throw " + 
                     exceptionType.getName());
            }
        }
    }
    
} //-- PropertyConversionTest
