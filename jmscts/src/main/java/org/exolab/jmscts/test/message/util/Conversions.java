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
 * $Id: Conversions.java,v 1.2 2004/02/03 07:31:04 tanderson Exp $
 */
package org.exolab.jmscts.test.message.util;

import java.util.HashMap;


/**
 * A helper class returning the conversion types supported by MapMessage and
 * StreamMessage
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 07:31:04 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
@SuppressWarnings("rawtypes")
public final class Conversions {

    /**
     * All types supported by MapMessage and StreamMessage
     */
    public static final Class[] TYPES = {
        Boolean.class, Byte.class, Short.class, Character.class,
        Integer.class, Long.class, Float.class, Double.class, String.class,
        byte[].class};

    /**
     * Valid boolean conversions
     */
    private static final Class[] BOOLEAN = {Boolean.class, String.class};

    /**
     * Valid byte conversions
     */
    private static final Class[] BYTE = {
        Byte.class, Short.class, Integer.class, Long.class, String.class};

    /**
     * Valid short conversions
     */
    private static final Class[] SHORT = {Short.class, Integer.class,
                                          Long.class, String.class};

    /**
     * Valid char conversions
     */
    private static final Class[] CHAR = {Character.class, String.class};

    /**
     * Valid int conversions
     */
    private static final Class[] INT = {Integer.class, Long.class,
                                        String.class};

    /**
     * Valid long conversions
     */
    private static final Class[] LONG = {Long.class, String.class};

    /**
     * Valid float conversions
     */
    private static final Class[] FLOAT = {Float.class, Double.class,
                                          String.class};

    /**
     * Valid double conversions
     */
    private static final Class[] DOUBLE = {Double.class, String.class};

    /**
     * Valid String conversions
     */
    private static final Class[] STRING = {
        Boolean.class, Byte.class, Short.class, Integer.class, Long.class,
        Float.class, Double.class, String.class};

    /**
     * Valid byte[] conversions
     */
    private static final Class[] BYTES = {byte[].class};

    /**
     * The map of valid conversions
     */
    private static final HashMap<Object, Object> VALID;

    /**
     * The map of invalid conversions
     */
    private static final HashMap<Object, Object> INVALID;


    /**
     * Prevent construction of utility class
     */
    private Conversions() {
    }

    /**
     * Returns the list of valid conversions for a particular type
     *
     * @param type the type to convert from. For primitives, this must be the
     * corresponding objectified type
     * @return the list of valid conversions for type
     */
    public static Class[] getValidConversions(Class<?> type) {
        Class[] result = (Class[]) VALID.get(type);
        if (result == null) {
            throw new IllegalArgumentException(
                "Class=" + type.getName() + " is not a type supported by "
                + "MapMessage or StreamMessage");
        }
        return result;
    }

    /**
     * Returns the list of invalid conversions for a particular type
     *
     * @param type the type to convert from. For primitives, this must be the
     * corresponding objectified type
     * @return the list of invalid conversions for type
     */
    public static Class[] getInvalidConversions(Class<?> type) {
        Class[] result = (Class[]) INVALID.get(type);
        if (result == null) {
            throw new IllegalArgumentException(
                "Class=" + type.getName() + " is not a type supported by "
                + "MapMessage or StreamMessage");
        }
        return result;
    }

    /**
     * Returns true if it is valid to convert a value to a particular type
     *
     * @param value the value to convert from.
     * @param type the type to convert to. For primitives, this must be the
     * corresponding objectified type
     * @return true if it is valid to perform a conversion
     */
    public static boolean isValidConversion(Object value, Class<?> type) {
        boolean result = false;
        Class[] valid = getValidConversions(value.getClass());
        for (int i = 0; i < valid.length; ++i) {
            if (valid[i].equals(type)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Converts an object from one type to another
     *
     * @param value the value to convert from.
     * @param type the type to convert to. For primitives, this must be the
     * corresponding objectified type
     * @return the converted object
     */
    public static Object convert(Object value, Class<?> type) {
        Object result = null;
        if (value instanceof Boolean) {
            result = convertBoolean((Boolean) value, type);
        } else if (value instanceof Byte) {
            result = convertByte((Byte) value, type);
        } else if (value instanceof Short) {
            result = convertShort((Short) value, type);
        } else if (value instanceof Character) {
            result = convertChar((Character) value, type);
        } else if (value instanceof Integer) {
            result = convertInt((Integer) value, type);
        } else if (value instanceof Long) {
            result = convertLong((Long) value, type);
        } else if (value instanceof Float) {
            result = convertFloat((Float) value, type);
        } else if (value instanceof Double) {
            result = convertDouble((Double) value, type);
        } else if (value instanceof String) {
            result = convertString((String) value, type);
        } else if (value instanceof byte[]) {
            if (type.equals(byte[].class)) {
                result = value;
            } else {
                illegalConversion(value, type);
            }
        }
        return result;
    }

    /**
     * Convert a boolean to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertBoolean(Boolean value, Class<?> type) {
        Object result = null;
        if (type.equals(Boolean.class)) {
            result = value;
        } else if (type.equals(String.class)) {
            result = value.toString();
        } else {
            illegalConversion(value, type);
        }
        return result;
    }

    /**
     * Convert a byte to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertByte(Byte value, Class<?> type) {
        return convertNumber(value, type);
    }

    /**
     * Convert a short to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertShort(Short value, Class<?> type) {
        return convertNumber(value, type);
    }

    /**
     * Convert a char to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertChar(Character value, Class<?> type) {
        Object result = null;
        if (type.equals(Character.class)) {
            result = value;
        } else if (type.equals(String.class)) {
            result = value.toString();
        } else {
            illegalConversion(value, type);
        }
        return result;
    }

    /**
     * Convert an int to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertInt(Integer value, Class<?> type) {
        return convertNumber(value, type);
    }

    /**
     * Convert a long to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertLong(Long value, Class<?> type) {
        return convertNumber(value, type);
    }

    /**
     * Convert a float to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertFloat(Float value, Class<?> type) {
        return convertNumber(value, type);
    }

    /**
     * Convert a double to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertDouble(Double value, Class<?> type) {
        return convertNumber(value, type);
    }

    /**
     * Convert a String to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    public static Object convertString(String value, Class<?> type) {
        Object result = null;
        if (type.equals(Boolean.class)) {
            result = Boolean.valueOf(value);
        } else if (type.equals(Byte.class)) {
            result = Byte.valueOf(value);
        } else if (type.equals(Short.class)) {
            result = Short.valueOf(value);
        } else if (type.equals(Integer.class)) {
            result = Integer.valueOf(value);
        } else if (type.equals(Float.class)) {
            result = Float.valueOf(value);
        } else if (type.equals(Double.class)) {
            result = Double.valueOf(value);
        } else if (type.equals(String.class)) {
            result = value;
        } else {
            illegalConversion(value, type);
        }
        return result;
    }

    /**
     * Convert a Number to the specified type
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @return the converted value
     */
    private static Object convertNumber(Number value, Class<?> type) {
        Object result = null;
        if (!isValidConversion(value, type)) {
            illegalConversion(value, type);
        }
        if (type.equals(Byte.class)) {
            result = new Byte(value.byteValue());
        } else if (type.equals(Short.class)) {
            result = new Short(value.shortValue());
        } else if (type.equals(Integer.class)) {
            result = new Integer(value.intValue());
        } else if (type.equals(Long.class)) {
            result = new Long(value.longValue());
        } else if (type.equals(Float.class)) {
            result = new Float(value.floatValue());
        } else if (type.equals(Double.class)) {
            result = new Double(value.doubleValue());
        } else if (type.equals(String.class)) {
            result = value.toString();
        }
        return result;
    }

    /**
     * Raises an IllegalArgumentExcption for an invalid conversion
     *
     * @param value the value to convert
     * @param type the type to convert to
     * @throws IllegalArgumentException containing a formatted reason
     */
    private static void illegalConversion(Object value, Class<?> type) {
        throw new IllegalArgumentException(
            "Type=" + type.getName() + " is not a valid conversion for "
            + value.getClass().getName());
    }

    /**
     * Returns the list of invalid conversions for a particular type
     *
     * @param type the type
     * @return the invalid conversions for <code>type</code>
     */
    private static Class[] getInvalid(Class<?> type) {
        Class[] valid = getValidConversions(type);
        Class[] invalid = new Class[TYPES.length - valid.length];
        int index = 0;
        for (int i = 0; i < TYPES.length; ++i) {
            boolean found = false;
            for (int j = 0; j < valid.length; ++j) {
                if (TYPES[i].equals(valid[j])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                invalid[index++] = TYPES[i];
            }
        }
        return invalid;
    }

    static {
        VALID = new HashMap<Object,Object>();
        VALID.put(Boolean.class, BOOLEAN);
        VALID.put(Byte.class, BYTE);
        VALID.put(Short.class, SHORT);
        VALID.put(Character.class, CHAR);
        VALID.put(Integer.class, INT);
        VALID.put(Long.class, LONG);
        VALID.put(Float.class, FLOAT);
        VALID.put(Double.class, DOUBLE);
        VALID.put(String.class, STRING);
        VALID.put(byte[].class, BYTES);

        INVALID = new HashMap<Object,Object>();
        INVALID.put(Boolean.class, getInvalid(Boolean.class));
        INVALID.put(Byte.class, getInvalid(Byte.class));
        INVALID.put(Short.class, getInvalid(Short.class));
        INVALID.put(Character.class, getInvalid(Character.class));
        INVALID.put(Integer.class, getInvalid(Integer.class));
        INVALID.put(Long.class, getInvalid(Long.class));
        INVALID.put(Float.class, getInvalid(Float.class));
        INVALID.put(Double.class, getInvalid(Double.class));
        INVALID.put(String.class, getInvalid(String.class));
        INVALID.put(byte[].class, getInvalid(byte[].class));
    }

}
