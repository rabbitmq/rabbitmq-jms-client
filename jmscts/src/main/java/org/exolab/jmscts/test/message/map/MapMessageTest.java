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
 * $Id: MapMessageTest.java,v 1.6 2004/02/03 07:31:03 tanderson Exp $
 */
package org.exolab.jmscts.test.message.map;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.jms.MapMessage;
import javax.jms.MessageFormatException;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.ClassHelper;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.Conversions;
import org.exolab.jmscts.test.message.util.MessageValues;


/**
 * This class tests the <code>MapMessage</code> message type.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.6 $
 * @see javax.jms.MapMessage
 * @see AbstractMessageTestCase
 * @see org.exolab.jmscts.core.MessageTestRunner
 * @jmscts.message MapMessage
 */
@SuppressWarnings("rawtypes")
public class MapMessageTest extends AbstractMessageTestCase
    implements MessageValues {

    /**
     * Values to test conversions against. String conversions are handled
     * separately, by the {@link #STRING_CONVERSION_VALUES} attribute
     */
     private static final Object[][] CONVERSION_VALUES = {
        BOOLEANS, BYTES, SHORTS, CHARS, INTS, LONGS, FLOATS, DOUBLES,
        BYTE_ARRAYS};

    /**
     * Float values to test string conversions against. String conversions
     * don't have to support NaN, or +-Infinity, hence the reason they are not
     * included here
     */
    private static final Float[] FLOAT_CONVERSION_VALUES = {
        new Float(Float.MIN_VALUE), new Float(Float.MAX_VALUE)};

    /**
     * Double values to test string conversions against. String conversions
     * don't have to support NaN, or +-Infinity, hence the reason they are not
     * included here
     */
    private static final Double[] DOUBLE_CONVERSION_VALUES = {
        new Double(Double.MIN_VALUE), new Double(Double.MAX_VALUE)};

    /**
     * Values to test string conversions against
     */
    private static final Object[][] STRING_CONVERSION_VALUES = {
        BOOLEANS, BYTES, SHORTS, INTS, LONGS, FLOAT_CONVERSION_VALUES,
        DOUBLE_CONVERSION_VALUES, STRINGS};


    /**
     * Construct a new <code>MapMessageTest</code>
     *
     * @param name the name of test case
     */
    public MapMessageTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(MapMessageTest.class);
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
     * Verifies valid conversions for all types except String
     * (this is handled by testStringConversion()).
     *
     * @jmscts.requirement message.map.conversion
     * @throws Exception for any error
     */
    @SuppressWarnings("unchecked")
    public void testConversion() throws Exception {
        final String prefix = "testConversion";
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        int id = 0;
        for (int i = 0; i < CONVERSION_VALUES.length; ++i) {
            for (int j = 0; j < CONVERSION_VALUES[i].length; ++j, ++id) {
                Object value = CONVERSION_VALUES[i][j];
                set(message, value, prefix + id);
                Class<?> type = value.getClass();
                Class[] valid = Conversions.getValidConversions(type);
                for (int k = 0; k < valid.length; ++k) {
                    Object result = get(message, valid[k], prefix + id);
                    Object converted = Conversions.convert(value, valid[k]);
                    if (converted instanceof byte[]) {
                        // byte arrays cannot be converted
                        if (!Arrays.equals((byte[]) result,
                                           (byte[]) converted)) {
                            fail("Result byte array different to that set");
                        }
                    } else if (!converted.equals(result)) {
                        fail("Conversion of type="
                             + ClassHelper.getPrimitiveName(type) + " to type="
                             + ClassHelper.getPrimitiveName(valid[k])
                             + " failed. Expected value=" + converted
                             + ", but got value=" + result);
                    }
                }
            }
        }
    }

    /**
     * Verifies valid string conversions
     *
     * @jmscts.requirement message.map.conversion
     * @throws Exception for any error
     */
    public void testStringConversion() throws Exception {
        final String prefix = "testStringConversion";
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        int id = 0;
        for (int i = 0; i < STRING_CONVERSION_VALUES.length; ++i) {
            for (int j = 0; j < STRING_CONVERSION_VALUES[i].length; ++j) {
                Object value = STRING_CONVERSION_VALUES[i][j];
                set(message, value.toString(), prefix + id);
                Class<?> type = value.getClass();
                Object result = get(message, type, prefix + id);
                if (!value.equals(result)) {
                    fail("Conversion of type=String to type="
                         + ClassHelper.getPrimitiveName(type)
                         + " failed. Expected value=" + value
                         + ", but got value=" + result);
                }
                ++id;
            }
        }
    }

    /**
     * Verifies that attempting to perform invalid invalid conversions
     * throws MessageFormatException.
     *
     * @jmscts.requirement message.map.conversion
     * @throws Exception for any error
     */
    @SuppressWarnings("unchecked")
    public void testInvalidConversion() throws Exception {
        final String prefix = "testInvalidConversion";
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        int id = 0;
        for (int i = 0; i < ALL_VALUES.length; ++i) {
            for (int j = 0; j < ALL_VALUES[i].length; ++j) {
                Object value = ALL_VALUES[i][j];
                set(message, value, prefix + id);
                Class<?> type = value.getClass();
                Class[] invalid = Conversions.getInvalidConversions(type);
                for (int k = 0; k < invalid.length; ++k) {
                    try {
                        get(message, invalid[k], prefix + id);
                        fail("Expected MessageFormatException to be thrown "
                             + "when getting type="
                             + ClassHelper.getPrimitiveName(invalid[k])
                             + " for type="
                             + ClassHelper.getPrimitiveName(type));
                    } catch (MessageFormatException expected) {
                        // the expected behaviour
                    } catch (Exception exception) {
                        fail("Expected MessageFormatException to be thrown "
                             + "when getting type="
                             + ClassHelper.getPrimitiveName(invalid[k])
                             + " for type="
                             + ClassHelper.getPrimitiveName(type)
                             + ", but got exception="
                             + exception.getClass().getName() + ", message="
                             + exception.getMessage());
                    }
                }
                Object result = get(message, type, prefix + id);
                compare(value, result);
                ++id;
            }
        }
    }

    /**
     * Verifies that attempting to perform invalid invalid string to numeric
     * conversions throws NumberFormatException
     *
     * @jmscts.requirement message.map.conversion
     * @throws Exception for any error
     */
    @SuppressWarnings("unchecked")
    public void testInvalidNumericConversion() throws Exception {
        final String prefix = "testInvalidNumericConversion";
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();
        Class[] numerics = {Byte.class, Short.class, Integer.class, Long.class,
                            Float.class, Double.class};
        String[] invalidNos = {"a", "0x00", "NaN", "-Infinity", "+Infinity"};
        
        int id = 0;
        for (int i = 0; i < invalidNos.length; ++i) {
            String value = invalidNos[i];
            set(message, value, prefix + id);
            for (int j = 0; j < numerics.length; ++j) {
                try {
                    get(message, numerics[j], prefix + id);
                    if (Float.class.equals(numerics[j]) || Double.class.equals(numerics[j])) {
                         
                    } else {
                        fail("Expected NumberFormatException to be thrown when "
                                + "getting value=" + value + " as type="
                                + ClassHelper.getPrimitiveName(numerics[j]));
                    }
                } catch (NumberFormatException expected) {
                    // the expected behaviour
                } catch (Exception exception) {
                    fail("Expected NumberFormatException to be thrown when "
                         + "getting value=" + value + " as type="
                         + ClassHelper.getPrimitiveName(numerics[j])
                         + ", but got exception="
                         + exception.getClass().getName() + ", message="
                         + exception.getMessage());
                }
            }
            ++id;
        }
    }

    /**
     * Verifies behaviour of conversions from null
     *
     * @jmscts.requirement message.map.null
     * @throws Exception for any error
     */
    public void testNullConversion() throws Exception {
        final String key = "testNullConversion";
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        try {
            set(message, null, key);
        } catch (Exception exception) {
            fail("Failed to set a null object value to MapMessage, "
                 + "exception=" + exception.getClass().getName()
                 + ", message=" + exception.getMessage());
        }
        Object result = getNull(message, Boolean.class, key, null);
        if (!result.equals(Boolean.FALSE)) {
            fail("Expected MapMessage.getBoolean() to return false for a "
                 + "null value");
        }

        result = getNull(message, String.class, key, null);
        if (result != null) {
            fail("Expected MapMessage.getString() to return null for a "
                 + "null value");
        }

        result = getNull(message, byte[].class, key, null);
        if (result != null) {
            fail("Expected MapMessage.getBytes() to return null for a "
                 + "null value");
        }

        getNull(message, Byte.class, key, NumberFormatException.class);
        getNull(message, Short.class, key, NumberFormatException.class);
        getNull(message, Character.class, key, NumberFormatException.class);
        getNull(message, Integer.class, key, NumberFormatException.class);
        getNull(message, Long.class, key, NumberFormatException.class);
        getNull(message, Float.class, key, NumberFormatException.class);
        getNull(message, Double.class, key, NumberFormatException.class);
    }

    /**
     * Verifies that setObject() can handle all supported types
     *
     * @jmscts.requirement message.map.method.setObject
     * @throws Exception for any error
     */
    public void testSetObject() throws Exception {
        final String prefix = "testSetObject";
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        int id = 0;
        for (int i = 0; i < ALL_VALUES.length; ++i) {
            for (int j = 0; j < ALL_VALUES[i].length; ++j) {
                message.setObject(prefix + id, ALL_VALUES[i][j]);
                ++id;
            }
        }
        id = 0;
        for (int i = 0; i < ALL_VALUES.length; ++i) {
            for (int j = 0; j < ALL_VALUES[i].length; ++j) {
                Object value = message.getObject(prefix + id);
                if (ALL_VALUES[i][j] instanceof byte[]) {
                    byte[] source = (byte[]) ALL_VALUES[i][j];
                    byte[] result = (byte[]) value;
                    assertTrue(Arrays.equals(source, result));
                } else {
                    assertTrue(ALL_VALUES[i][j].equals(value));
                }
                ++id;
            }
        }
    }

    /**
     * Verifies that attempting to set an invalid object using the setObject()
     * method throws MessageFormatException
     *
     * @jmscts.requirement message.map.method.setObject
     * @throws Exception for any error
     */
    public void testInvalidObject() throws Exception {
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        try {
            message.setObject("testInvalidObject",
                              new java.math.BigDecimal(0.0));
            fail("MapMessage.setObject() should only support "
                 + "objectified primitives");
        } catch (MessageFormatException expected) {
            // the expected behaviour
        }
    }

    /**
     * Verifies that elements populated in a map are identical to those
     * retrieved
     *
     * @jmscts.requirement message.map
     * @throws Exception for any error
     */
    public void testMap() throws Exception {
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        HashMap<String, Object> elements = populate(message);

        Iterator<?> iter = elements.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry element = (Map.Entry) iter.next();
            String key = (String) element.getKey();
            Object value = element.getValue();

            // get the value using the primitive methods
            Object result = get(message, value.getClass(), key);
            compare(value, result);

            // get the value using the object method
            result = message.getObject(key);
            compare(value, result);
        }
    }

    /**
     * Verifies that getMapNames() returns a name for each element in the set
     *
     * @jmscts.requirement message.map.method.getMapNames
     * @throws Exception for any error
     */
    @SuppressWarnings("unchecked")
    public void testGetMapNames() throws Exception {
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        checkEmpty(message); // ensure that no names exist prior to population
        HashMap<String, Object> elements = populate(message);

        LinkedList<Object> names = new LinkedList<Object>();
        Enumeration<Object> enum1 = message.getMapNames();
        while (enum1.hasMoreElements()) {
            names.add(enum1.nextElement());
        }

        if (elements.size() != names.size()) {
            fail("getMapNames() returned a different number of names to that"
                 + " expected. Expected count=" + elements.size()
                 + " but got count=" + names.size());
        }
        if (!names.containsAll(elements.keySet())) {
            fail("getMapNames() returned a different set of names to that "
                 + "expected");
        }

        // clear the message, and verify that getMapNames() returns no names
        message.clearBody();
        checkEmpty(message);
    }

    /**
     * Verifies the behaviour of the itemExists() method
     *
     * @jmscts.requirement message.map.method.itemExists
     * @throws Exception for any error
     */
    public void testItemExists() throws Exception {
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        HashMap<String, Object> elements = populate(message);

        Iterator<String> iter = elements.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            if (!message.itemExists(name)) {
                fail("itemExists() returned false for an existing element");
            }
        }

        if (message.itemExists("bad_item_name")) {
            fail("itemExists() returned true for a non-existent element");
        }
    }

    /**
     * Verifies that getting a MapMessage field for a field name that has not
     * been set is handled as if the field exists with a null value.
     *
     * @jmscts.requirement message.map.unset
     * @throws Exception for any error
     */
    public void testUnset() throws Exception {
        final String key = "testUnset";
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        Object result = getNull(message, Boolean.class, key, null);
        assertEquals(Boolean.FALSE, result);

        result = getNull(message, String.class, key, null);
        assertEquals(null, result);

        result = getNull(message, byte[].class, key, null);
        assertEquals(null, result);

        getNull(message, Byte.class, key, NumberFormatException.class);
        getNull(message, Short.class, key, NumberFormatException.class);
        getNull(message, Character.class, key, NumberFormatException.class);
        getNull(message, Integer.class, key, NumberFormatException.class);
        getNull(message, Long.class, key, NumberFormatException.class);
        getNull(message, Float.class, key, NumberFormatException.class);
        getNull(message, Double.class, key, NumberFormatException.class);
    }

    /**
     * Verifies that field names are case sensitive
     * NOTE: this is not explicitly mentioned in the specification, but is
     * in keeping with property name requirements
     *
     * @jmscts.requirement message.map.case
     * @throws Exception for any error
     */
    public void testCase() throws Exception {
        final String upperKey = "ABC";
        final String lowerKey = upperKey.toLowerCase();
        TestContext context = getContext();
        MapMessage message = (MapMessage) context.getMessage();

        // check case sensitivity across like types
        for (int i = 0; i < ALL_VALUES.length; ++i) {
            if (ALL_VALUES[i].length < 2) {
                throw new Exception("Each entry in ALL_VALUES must have at "
                                    + "least 2 distinct values");
            }
            Object value1 = ALL_VALUES[i][0];
            Object value2 = ALL_VALUES[i][1];
            set(message, value1, upperKey);
            set(message, value2, lowerKey);

            compare(value1, get(message, value1.getClass(), upperKey));
            compare(value2, get(message, value2.getClass(), lowerKey));
        }

        // check case sensitivity across different types
        message.clearBody();
        String value1 = STRINGS[0];
        Integer value2 = INTS[0];
        message.setString(upperKey, value1);
        message.setInt(lowerKey, value2.intValue());
        compare(value1, get(message, value1.getClass(), upperKey));
        compare(value2, get(message, value2.getClass(), lowerKey));
    }

    /**
     * Verifies that a getMapNames() returns an empty Enumeration for an
     * empty message
     *
     * @param message the message to check
     * @throws Exception for any error
     */
    private void checkEmpty(MapMessage message) throws Exception {
        Enumeration<?> enum1 = message.getMapNames();
        int count = 0;
        while (enum1.hasMoreElements()) {
            count++;
        }
        if (count != 0) {
            fail("Expected getMapNames to return no elements for an empty "
                 + "MapMessage, but returned=" + count + " names");
        }
    }

    /**
     * Compares two objects, failing if they are not the same
     *
     * @param expected the expected value
     * @param result the actual value
     */
    private void compare(Object expected, Object result) {
        if (expected instanceof byte[]) {
            if (!Arrays.equals((byte[]) expected, (byte[]) result)) {
                fail("Byte array returned by MapMessage does not "
                     + "match that expected");
            }
        } else if (!expected.equals(result)) {
            fail("Value returned by MapMessage does not match that "
                 + "expected. Expected value=" + expected + ", but got value="
                 +  result);
        }
    }

    /**
     * Populates a message with values
     *
     * @param message the message to populate
     * @return a map of the populated values, keyed on their names
     * @throws Exception for any error
     */
    private HashMap<String, Object> populate(MapMessage message) throws Exception {
        final String prefix = "element";
        HashMap<String, Object> result = new HashMap<String, Object>();
        int id = 0;
        // populate using primitive methods
        for (int i = 0; i < ALL_VALUES.length; ++i) {
            for (int j = 0; j < ALL_VALUES[i].length; ++j) {
                String name = prefix + id;
                set(message, ALL_VALUES[i][j], name);
                result.put(name, ALL_VALUES[i][j]);
                id++;
            }
        }

        // populate using setObject
        for (int i = 0; i < ALL_VALUES.length; ++i) {
            for (int j = 0; j < ALL_VALUES[i].length; ++j) {
                String name = prefix + id;
                message.setObject(name, ALL_VALUES[i][j]);
                result.put(name, ALL_VALUES[i][j]);
                id++;
            }
        }

        // populate using alternative setBytes
        for (int i = 0; i < BYTE_ARRAYS.length; ++i) {
            String name = prefix + id;
            message.setBytes(name, BYTE_ARRAYS[i], 0, BYTE_ARRAYS[i].length);
            result.put(name, BYTE_ARRAYS[i]);
            id++;
        }
        return result;
    }

    /**
     * Helper to set a message property, invoking the appropriate set() method
     * based on the type of the property
     *
     * @param message the message to populate
     * @param value the property value to set
     * @param key the name of the property
     * @throws Exception for any error
     */
    private void set(MapMessage message, Object value, String key)
        throws Exception {
        if (value instanceof Boolean) {
            message.setBoolean(key, ((Boolean) value).booleanValue());
        } else if (value instanceof Byte) {
            message.setByte(key, ((Byte) value).byteValue());
        } else if (value instanceof Short) {
            message.setShort(key, ((Short) value).shortValue());
        } else if (value instanceof Character) {
            message.setChar(key, ((Character) value).charValue());
        } else if (value instanceof Integer) {
            message.setInt(key, ((Integer) value).intValue());
        } else if (value instanceof Long) {
            message.setLong(key, ((Long) value).longValue());
        } else if (value instanceof Float) {
            message.setFloat(key, ((Float) value).floatValue());
        } else if (value instanceof Double) {
            message.setDouble(key, ((Double) value).doubleValue());
        } else if (value instanceof String) {
            message.setString(key, (String) value);
        } else if (value instanceof byte[]) {
            message.setBytes(key, (byte[]) value);
        } else {
            // let the message deal with the exception
            message.setObject(key, value);
        }
    }

    /**
     * Helper to get a message property, invoking the appropriate get() method
     * based on the type of the property
     *
     * @param message the message
     * @param type the type of the property
     * @param key the name of the property
     * @return the value of the property
     * @throws Exception for any error
     */
    private Object get(MapMessage message, Class<?> type, String key)
        throws Exception {
        Object result = null;
        if (type.equals(Boolean.class)) {
            result = new Boolean(message.getBoolean(key));
        } else if (type.equals(Byte.class)) {
            result = new Byte(message.getByte(key));
        } else if (type.equals(Short.class)) {
            result = new Short(message.getShort(key));
        } else if (type.equals(Character.class)) {
            result = new Character(message.getChar(key));
        } else if (type.equals(Integer.class)) {
            result = new Integer(message.getInt(key));
        } else if (type.equals(Long.class)) {
            result = new Long(message.getLong(key));
        } else if (type.equals(Float.class)) {
            result = new Float(message.getFloat(key));
        } else if (type.equals(Double.class)) {
            result = new Double(message.getDouble(key));
        } else if (type.equals(String.class)) {
            result = message.getString(key);
        } else if (type.equals(byte[].class)) {
            result = message.getBytes(key);
        }
        return result;
    }

    /**
     * Helper to attempt to convert a null property
     *
     * @param message the message
     * @param type the type of the property
     * @param key the name of the property
     * @param exceptionType if non-null, indicates the expected exception to
     * be thrown by the conversion
     * @return the value of the property
     * @throws Exception for any error
     */
    private Object getNull(MapMessage message, Class<?> type, String key,
                           Class<?> exceptionType) throws Exception {
        Object result = null;
        try {
            result = get(message, type, key);
            if (exceptionType != null) {
                fail("Expected exception, type=" + exceptionType.getName()
                     + " to be thrown when getting null as type="
                     + ClassHelper.getPrimitiveName(type));
            }
        } catch (Exception exception) {
            if (exceptionType == null) {
                fail("Did not expect exception to be thrown when getting "
                     + "null as type=" + ClassHelper.getPrimitiveName(type)
                     + " but got exception="  + exception.getClass().getName()
                     + ", message=" + exception.getMessage());
            } else if (!exceptionType.isAssignableFrom(exception.getClass())) {
                fail("Expected exception, type=" + exceptionType.getName()
                     + " to be thrown when getting null as type="
                     + ClassHelper.getPrimitiveName(type) + ", but got "
                     + "exception=" + exception.getClass().getName()
                     + ", message=" + exception.getMessage());
            }
        }
        return result;
    }

}
