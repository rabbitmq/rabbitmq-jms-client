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
 * $Id: Conversions.java,v 1.3 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.message.util;

import java.util.HashMap;


/**
 * A helper class returning the conversion types supported by MapMessage and 
 * StreamMessage
 *
 * @version     $Revision: 1.3 $ $Date: 2003/05/04 14:12:38 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class Conversions {

    /**
     * All types supported by MapMessage and StreamMessage
     */
    public static final Class[] TYPES = {
        Boolean.class, Byte.class, Short.class, Character.class, 
        Integer.class, Long.class, Float.class, Double.class, String.class, 
        byte[].class};

    private static final Class[] BOOLEAN = {Boolean.class, String.class};
    private static final Class[] BYTE = {
        Byte.class, Short.class, Integer.class, Long.class, String.class};
    private static final Class[] SHORT = {Short.class, Integer.class, 
                                          Long.class, String.class};
    private static final Class[] CHAR = {Character.class, String.class};
    private static final Class[] INT = {Integer.class, Long.class, 
                                        String.class};
    private static final Class[] LONG = {Long.class, String.class};
    private static final Class[] FLOAT = {Float.class, Double.class, 
                                          String.class};
    private static final Class[] DOUBLE = {Double.class, String.class};
    private static final Class[] STRING = {
        Boolean.class, Byte.class, Short.class, Integer.class, Long.class, 
        Float.class, Double.class, String.class};
    private static final Class[] BYTES = {byte[].class};

    /**
     * The map of valid conversions
     */
    private static final HashMap _valid;

    /**
     * The map of invalid conversions
     */
    private static final HashMap _invalid;

    /**
     * Returns the list of valid conversions for a particular type
     *
     * @param type the type to convert from. For primitives, this must be the
     * corresponding objectified type
     * @return the list of valid conversions for type
     * @throws IlegalArgumentException if type is not supported
     */
    public static Class[] getValidConversions(Class type) {
        Class[] result = (Class[]) _valid.get(type);
        if (result == null) {
            throw new IllegalArgumentException(
                "Class=" + type.getName() + " is not a type supported by " +
                "MapMessage or StreamMessage");
        }
        return result;
    }

    /**
     * Returns the list of invalid conversions for a particular type
     *
     * @param type the type to convert from. For primitives, this must be the
     * corresponding objectified type
     * @return the list of invalid conversions for type
     * @throws IlegalArgumentException if type is not supported
     */
    public static Class[] getInvalidConversions(Class type) {
        Class[] result = (Class[]) _invalid.get(type);
        if (result == null) {
            throw new IllegalArgumentException(
                "Class=" + type.getName() + " is not a type supported by " +
                "MapMessage or StreamMessage");
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
     * @throws IlegalArgumentException if value is not supported
     */
    public static boolean isValidConversion(Object value, Class type) {
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
     * @throws IlegalArgumentException if the conversion is invalid
     */
    public static Object convert(Object value, Class type) {
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

    public static Object convertBoolean(Boolean value, Class type) {
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

    public static Object convertByte(Byte value, Class type) {
        return convertNumber(value, type);
    }

    public static Object convertShort(Short value, Class type) {
        return convertNumber(value, type);
    }

    public static Object convertChar(Character value, Class type) {
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

    public static Object convertInt(Integer value, Class type) {
        return convertNumber(value, type);
    }

    public static Object convertLong(Long value, Class type) {
        return convertNumber(value, type);
    }

    public static Object convertFloat(Float value, Class type) {
        return convertNumber(value, type);
    }

    public static Object convertDouble(Double value, Class type) {
        return convertNumber(value, type);
    }

    public static Object convertString(String value, Class type) {
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

    private static Object convertNumber(Number value, Class type) {
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
    
    private static void illegalConversion(Object value, Class type) {
        throw new IllegalArgumentException(
            "Type=" + type.getName() + " is not a valid conversion for " +
            value.getClass().getName());
    }

    private static Class[] getInvalid(Class type) {
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
        _valid = new HashMap();
        _valid.put(Boolean.class, BOOLEAN);
        _valid.put(Byte.class, BYTE);
        _valid.put(Short.class, SHORT);
        _valid.put(Character.class, CHAR);
        _valid.put(Integer.class, INT);
        _valid.put(Long.class, LONG);
        _valid.put(Float.class, FLOAT);
        _valid.put(Double.class, DOUBLE);
        _valid.put(String.class, STRING);
        _valid.put(byte[].class, BYTES);

        _invalid = new HashMap();        
        _invalid.put(Boolean.class, getInvalid(Boolean.class));
        _invalid.put(Byte.class, getInvalid(Byte.class));
        _invalid.put(Short.class, getInvalid(Short.class));
        _invalid.put(Character.class, getInvalid(Character.class));
        _invalid.put(Integer.class, getInvalid(Integer.class));
        _invalid.put(Long.class, getInvalid(Long.class));
        _invalid.put(Float.class, getInvalid(Float.class));
        _invalid.put(Double.class, getInvalid(Double.class));
        _invalid.put(String.class, getInvalid(String.class));
        _invalid.put(byte[].class, getInvalid(byte[].class));
    }
    
} //-- Conversions
