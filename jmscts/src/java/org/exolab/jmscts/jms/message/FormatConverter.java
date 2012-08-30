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
 * Copyright 2000-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: FormatConverter.java,v 1.2 2004/02/02 03:49:55 tanderson Exp $
 */

package org.exolab.jmscts.jms.message;

import javax.jms.MessageFormatException;


/**
 * A simple format converter to help convert an Object type as per the
 * table listed below.
 *
 * <P>A value written as the row type can be read as the column type.
 *
 * <PRE>
 * |        | boolean byte short char int long float double String byte[]
 * |----------------------------------------------------------------------
 * |boolean |    X                                            X
 * |byte    |          X     X         X   X                  X
 * |short   |                X         X   X                  X
 * |char    |                     X                           X
 * |int     |                          X   X                  X
 * |long    |                              X                  X
 * |float   |                                    X     X      X
 * |double  |                                          X      X
 * |String  |    X     X     X         X   X     X     X      X
 * |byte[]  |                                                        X
 * |----------------------------------------------------------------------
 * </PRE>
 *
 * <P>Attempting to read a null value as a Java primitive type must be treated
 * as calling the primitive's corresponding <code>valueOf(String)</code>
 * conversion method with a null value. Since char does not support a String
 * conversion, attempting to read a null value as a char must throw
 * NullPointerException.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:55 $
 * @author      <a href="mailto:mourikis@intalio.com">Jim Mourikis</a>
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
final class FormatConverter {

    /**
     * Prevent construction of utility class
     */
    private FormatConverter() {
    }

    /**
     * Convert value to boolean
     *
     * @param value the object to convert from
     * @return the converted boolean
     * @throws MessageFormatException if the conversion is invalid
     */
    public static boolean getBoolean(Object value)
        throws MessageFormatException {
        boolean result = false;

        if (value instanceof Boolean) {
            result = ((Boolean) value).booleanValue();
        } else if (value instanceof String) {
            result = Boolean.valueOf((String) value).booleanValue();
        } else if (value == null) {
            result = Boolean.valueOf((String) value).booleanValue();
        } else {
            raise(value, boolean.class);
        }
        return result;
    }

    /**
     * Convert value to byte
     *
     * @param value the object to convert from
     * @return the converted byte
     * @throws MessageFormatException if the conversion is invalid
     * @throws NumberFormatException if value is a String and the conversion
     * is invalid
     */
    public static byte getByte(Object value) throws MessageFormatException {
        byte result = 0;

        if (value instanceof Byte) {
            result = ((Byte) value).byteValue();
        } else if (value instanceof String) {
            result = Byte.parseByte((String) value);
        } else if (value == null) {
            result = Byte.valueOf((String) value).byteValue();
        } else {
            raise(value, byte.class);
        }
        return result;
    }


    /**
     * Convert value to short
     *
     * @param value the object to convert from
     * @return the converted short
     * @throws MessageFormatException if the conversion is invalid
     * @throws NumberFormatException if value is a String and the conversion
     * is invalid
     */
    public static short getShort(Object value) throws MessageFormatException {
        short result = 0;

        if (value instanceof Short) {
            result = ((Short) value).shortValue();
        } else if (value instanceof Byte) {
            result = ((Byte) value).shortValue();
        } else if (value instanceof String) {
            result = Short.parseShort((String) value);
        } else if (value == null) {
            result = Short.valueOf((String) value).shortValue();
        } else {
            raise(value, short.class);
        }
        return result;
    }

    /**
     * Convert value to char
     *
     * @param value the object to convert from
     * @return the converted char
     * @throws MessageFormatException if the conversion is invalid
     * @throws NullPointerException if value is null
     */
    public static char getChar(Object value) throws MessageFormatException {
        char result = '\0';
        if (value instanceof Character) {
            result = ((Character) value).charValue();
        } else if (value == null) {
            throw new NullPointerException(
                "Cannot convert null value to char");
        } else {
            raise(value, char.class);
        }
        return result;
    }

    /**
     * Convert value to int
     *
     * @param value the object to convert from
     * @return the converted int
     * @throws MessageFormatException if the conversion is invalid
     * @throws NumberFormatException if value is a String and the conversion
     * is invalid
     */
    public static int getInt(Object value) throws MessageFormatException {
        int result = 0;

        if (value instanceof Integer) {
            result = ((Integer) value).intValue();
        } else if (value instanceof Short) {
            result = ((Short) value).intValue();
        } else if (value instanceof Byte) {
            result = ((Byte) value).intValue();
        } else if (value instanceof String) {
            result = Integer.parseInt((String) value);
        } else if (value == null) {
            result = Integer.valueOf((String) value).intValue();
        } else {
            raise(value, int.class);
        }
        return result;
    }

    /**
     * Convert value to long
     *
     * @param value the object to convert from
     * @return the converted long
     * @throws MessageFormatException if the conversion is invalid
     * @throws NumberFormatException if value is a String and the conversion
     * is invalid
     */
    public static long getLong(Object value) throws MessageFormatException {
        long result = 0;

        if (value instanceof Long) {
            result = ((Long) value).longValue();
        } else if (value instanceof Integer) {
            result = ((Integer) value).longValue();
        } else if (value instanceof Short) {
            result = ((Short) value).longValue();
        } else if (value instanceof Byte) {
            result = ((Byte) value).longValue();
        } else if (value instanceof String) {
            result = Long.parseLong((String) value);
        } else if (value == null) {
            result = Long.valueOf((String) value).longValue();
        } else {
            raise(value, long.class);
        }
        return result;
    }

    /**
     * Convert value to float
     *
     * @param value the object to convert from
     * @return the converted float
     * @throws MessageFormatException if the conversion is invalid
     * @throws NumberFormatException if value is a String and the conversion
     * is invalid
     */
    public static float getFloat(Object value) throws MessageFormatException {
        float result = 0;

        if (value instanceof Float) {
            result = ((Float) value).floatValue();
        } else if (value instanceof String) {
            result = Float.parseFloat((String) value);
        } else if (value == null) {
            result = Float.valueOf((String) value).floatValue();
        } else {
            raise(value, float.class);
        }
        return result;
    }

    /**
     * Convert value to double
     *
     * @param value the object to convert from
     * @return the converted double
     * @throws MessageFormatException if the conversion is invalid
     * @throws NumberFormatException if value is a String and the conversion
     * is invalid
     */
    public static double getDouble(Object value)
        throws MessageFormatException {
        double result = 0;

        if (value instanceof Double) {
            result = ((Double) value).doubleValue();
        } else if (value instanceof Float) {
            result = ((Float) value).doubleValue();
        } else if (value instanceof String) {
            result = Double.parseDouble((String) value);
        } else if (value == null) {
            result = Double.valueOf((String) value).doubleValue();
        } else {
            raise(value, double.class);
        }
        return result;
    }

    /**
     * Convert value to String
     *
     * @param value the object to convert from
     * @return the converted String
     * @throws MessageFormatException if the conversion is invalid
     */
    public static String getString(Object value)
        throws MessageFormatException {
        if (value instanceof byte[]) {
            raise(value, String.class);
        }
        return (value == null) ? null : String.valueOf(value);
    }

    /**
     * Convert value to byte[]
     *
     * @param value the object to convert from. This must be a byte array, or
     * null
     * @return a copy of the supplied array, or null
     * @throws MessageFormatException if value is not a byte array or is not
     * null
     */
    public static byte[] getBytes(Object value)
        throws MessageFormatException {
        byte[] result = null;

        if (value instanceof byte[]) {
            byte[] bytes = (byte[]) value;
            result = new byte[bytes.length];
            System.arraycopy(bytes, 0, result, 0, bytes.length);
        } else if (value != null) {
            raise(value, byte[].class);
        }
        return result;
    }

    /**
     * Helper to raise a MessageFormatException when a conversion cannot be
     * performed
     *
     * @param value the value that cannot be converted
     * @param type the type that the value cannot be converted to
     * @throws MessageFormatException when invoked
     */
    private static void raise(Object value, Class<?> type)
        throws MessageFormatException {

        throw new MessageFormatException(
            "Cannot convert values of type " + value.getClass().getName()
            + " to " + type.getName());
    }

}
