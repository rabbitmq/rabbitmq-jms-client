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
 * $Id: PropertyHelper.java,v 1.3 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.message.properties;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;

import org.exolab.jmscts.core.ClassHelper;
import org.exolab.jmscts.test.message.util.PropertyValues;


/**
 * Helper class providing methods to populate messages with properties, and
 * and return the properties in messages
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 */
class PropertyHelper implements PropertyValues {

    /**
     * Sets user properties for each property type
     *
     * @param message the message to set properties on
     * @return the set of property names populated
     * @throws Exception if any of the JMS operations fail
     */
    public static Set setProperties(Message message) throws Exception {
        Set set = new HashSet();
        int count = 0;
        for (int i = 0; i < ALL_VALUES.length; ++i) {
            setPrimitiveProperties(message, ALL_VALUES[i], set);
            setObjectProperties(message, ALL_VALUES[i], set);
            count += ALL_VALUES[i].length * 2;
        }

        // make sure all properties were set
        if (set.size() != count) {
            throw new Exception("Expected " + count + " properties to be " + 
                                "set, but got " + set.size());
        }
        return set;
    }

    /**
     * Helper method to set an array of java primitive properties on a 
     * message.
     * The names assigned to each property is populated in the supplied set.
     *
     * @param message the message to set the properties on
     * @param values the array of java primitive values
     * @param set the set to populate with the property names
     * @throws JMSException if a property cannot be set
     */
    public static void setPrimitiveProperties(
        Message message, Object[] values, Set set) throws JMSException {
    
        for (int i = 0; i < values.length; ++i) {
            Object value = values[i];
            String name = getName(ClassHelper.getPrimitiveType(
                value.getClass()), i);
            setPrimitiveProperty(message, name, value);
            set.add(name);
        }
    }

    /**
     * Helper method to set an array of object properties on a message.
     * The names assigned to each property is populated in the supplied set.
     *
     * @param message the message to set the properties on
     * @param values the array of object values
     * @param set the set to populate with the property names
     * @throws JMSException if a property cannot be set
     */
    public static void setObjectProperties(Message message, Object[] values, 
                                           Set set) throws JMSException {
    
        for (int i = 0; i < values.length; ++i) {
            Object value = values[i];
            String name = null;
            if (value instanceof String) {
                name = "java_lang_String" + i;
                // strings and object properties are generally treated the
                // same way, so prefix with the package name to avoid clashes
                // with the setStringProperty() method
            } else {
                name = getName(value.getClass(), i);
            }
            message.setObjectProperty(name, value);
            set.add(name);
        }
    }

    /**
     * Helper method to set a java primitive property on a message
     *
     * @param message the message to set the property on
     * @param name the name of the property
     * @param value the primitive value, wrapped in its corresponding Object 
     * type
     * @throws JMSException if the property cannot be set
     */
    public static void setPrimitiveProperty(
        Message message, String name, Object value) throws JMSException {

        if (value instanceof Boolean) {
            message.setBooleanProperty(name, ((Boolean) value).booleanValue());
        } else if (value instanceof Byte) {
            message.setByteProperty(name, ((Byte) value).byteValue());
        } else if (value instanceof Short) {
            message.setShortProperty(name, ((Short) value).shortValue());
        } else if (value instanceof Integer) {
            message.setIntProperty(name, ((Integer) value).intValue());
        } else if (value instanceof Long) {
            message.setLongProperty(name, ((Long) value).longValue());
        } else if (value instanceof Float) {
            message.setFloatProperty(name, ((Float) value).floatValue());
        } else if (value instanceof Double) {
            message.setDoubleProperty(name, ((Double) value).doubleValue());
        } else {
            message.setStringProperty(name, (String) value);
        }
    }

    /**
     * Returns all of the user properties of a message 
     *
     * @param message the message to extract the properties from
     * @return a map containing the user properties
     * @throws JMSException if the JMS operations fail
     */
    public static Map getProperties(Message message) throws JMSException {
        Map result = new HashMap();
        Enumeration iter = message.getPropertyNames();
        while (iter.hasMoreElements()) {
            String name = (String) iter.nextElement();
            Object value = message.getObjectProperty(name);
            result.put(name, value);
        }
        return result;
    }

    /**
     * Helper to returns a property identifier name based on its class name 
     * and a int value
     *
     * @param type the property class
     * @param id a unique identifier for the particular property class
     */
    private static String getName(Class type, int id) {
        String name = null;
        if (type.equals(String.class)) {
            name = "String";
        } else {
            if (type.isPrimitive()) {
                name = type.getName();
            } else {
                name = type.getName().replace('.', '_');
            }
        }
        return name + id;
    }
    
} // End PropertyHelper
