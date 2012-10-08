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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: AttributeHelper.java,v 1.5 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.lang.reflect.Method;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import org.exolab.jmscts.core.meta.Attribute;
import org.exolab.jmscts.core.meta.ClassMeta;
import org.exolab.jmscts.core.meta.Meta;
import org.exolab.jmscts.core.meta.MetaData;
import org.exolab.jmscts.core.meta.MethodMeta;


/**
 * A helper class for retrieving attributes from class meta-data.
 *
 * @version     $Revision: 1.5 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class AttributeHelper {

    /**
     * A map of class names to their corresponding ClassMeta instances
     */
    private static final HashMap<String, ClassMeta> METADATA;

    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(
        AttributeHelper.class.getName());

    /**
     * Empty list
     */
    private static final String[] EMPTY = new String[0];

    /**
     * The class meta data resource path
     */
    private static final String PATH = "/metadata.xml";


    /**
     * Prevent construction of utility class
     */
    private AttributeHelper() {
    }

    /**
     * Returns the boolean value of an attribute, associated with a
     * particular class
     *
     * @param clazz the class to look up attributes for
     * @param attribute the attribute name
     * @return the boolean value of <code>attribute</code>, or
     * <code>false</code> if <code>attribute</code> doesn't exists
     */
    public static boolean getBoolean(Class<?> clazz, String attribute) {
        boolean result = false;
        getAttributes(clazz, attribute);
        return result;
    }

    /**
     * Returns the boolean value of an attribute, associated with a
     * particular method
     *
     * @param clazz the class to look up attributes for
     * @param methodName the name of the method. The method must take no args.
     * May be null.
     * @param attribute the attribute name
     * @return the boolean value of <code>attribute</code>, or
     * <code>false</code> if <code>attribute</code> doesn't exists
     */
    public static boolean getBoolean(Class<?> clazz, String methodName,
                                     String attribute) {
        String[] values = getAttributes(clazz, methodName, attribute);
        return getBoolean(values);
    }

    /**
     * Returns the boolean value of an attribute, associated with a
     * particular method and/or class
     *
     * @param clazz the class to look up attributes for
     * @param methodName the name of the method. The method must take no args.
     * May be null.
     * @param attribute the attribute name
     * @param all if <code>true</code> include method and class attributes
     * @return the boolean value of <code>attribute</code>, or
     * <code>false</code> if <code>attribute</code> doesn't exists
     */
    public static boolean getBoolean(Class<?> clazz, String methodName,
                                     String attribute, boolean all) {
        String[] values = getAttributes(clazz, methodName, attribute, all);
        return getBoolean(values);
    }

    /**
     * Return a list of attribute values for the specified attribute name,
     * associated with a particular class
     *
     * @param clazz the class to look up attributes for
     * @param attribute the attribute name
     * @return a list of attribute values for the specified attribute name.
     */
    public static String[] getAttributes(Class<?> clazz, String attribute) {
        String[] result = EMPTY;
        ClassMeta meta = METADATA.get(clazz.getName());
        if (meta != null) {
            result = getValues(meta, attribute);
        } else {
            log.warn("Failed to locate meta data for class="
                     + clazz.getName());
        }
        if (log.isDebugEnabled()) {
            log.debug("Attribute values for " + clazz.getName() + "["
                      + attribute + "] = " + format(result));
        }
        return result;
    }

    /**
     * Return a list of attribute values for the specified attribute name,
     * associated with a particular method and/or class.
     *
     * @param clazz the class to look up attributes for
     * @param methodName the name of the method. The method must take no args.
     * May be null.
     * @param attribute the attribute name
     * @param all if <code>true</code> include method and class attributes
     * @return a list of attribute values for the specified attribute name.
     */
    public static String[] getAttributes(Class<?> clazz, String methodName,
                                         String attribute, boolean all) {
        String[] result = EMPTY;
        String[] methodAtts = EMPTY;
        String[] classAtts = EMPTY;
        if (methodName != null) {
            methodAtts = getAttributes(clazz, methodName, attribute);
        }
        if (methodAtts.length == 0 || all) {
            classAtts = getAttributes(clazz, attribute);
        }

        if (methodAtts.length != 0 || classAtts.length != 0) {
            result = new String[methodAtts.length + classAtts.length];
            System.arraycopy(methodAtts, 0, result, 0, methodAtts.length);
            System.arraycopy(classAtts, 0, result, methodAtts.length,
                             classAtts.length);
        }
        return result;
    }

    /**
     * Return a list of attribute values for the specified attribute name,
     * associated with a particular method
     *
     * @param clazz the class to look up attributes for
     * @param methodName the name of the method. The method must take no args.
     * @param attribute the attribute name
     * @return a list of attribute values for the specified attribute name.
     */
    public static String[] getAttributes(Class<?> clazz, String methodName,
                                         String attribute) {
        String[] result = EMPTY;
        try {
            Method method = clazz.getMethod(methodName, new Class[0]);
            result = getAttributes(method, attribute);
        } catch (Exception exception) {
            log.warn("Failed to locate meta data for class=" + clazz.getName(),
                     exception);
        }
        if (log.isDebugEnabled()) {
            log.debug("Attribute values for " + clazz.getName() + "."
                      + methodName + "[" + attribute + "] = "
                      + format(result));
        }
        return result;
    }

    /**
     * Return a list of attribute values for the specified attribute name
     * associated with a particular method
     *
     * @param method the method
     * @param attribute the attribute name
     * @return a list of attribute values for the specified attribute name.
     */
    public static String[] getAttributes(Method method, String attribute) {
        String[] result = EMPTY;
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        ClassMeta classMeta = METADATA.get(className);
        if (classMeta != null) {
            MethodMeta[] methods = classMeta.getMethodMeta();
            for (int i = 0; i < methods.length; ++i) {
                MethodMeta methodMeta = methods[i];
                if (methodMeta.getName().equals(methodName)) {
                    result = getValues(methodMeta, attribute);
                }
            }
        } else {
            log.warn("Failed to locate meta data for class=" + className);
        }
        return result;
    }

    /**
     * Returns the first boolean value from a set of values
     *
     * @param values the set of values
     * @return the first boolean value from a set of values, or
     * <code>false</code> if <code>values</code> is empty
     */
    private static boolean getBoolean(String[] values) {
        boolean result = false;
        if (values.length > 0) {
            result = Boolean.valueOf(values[0]).booleanValue();
        }
        return result;
    }

    /**
     * Returns the values of an attribute
     *
     * @param meta the meta data
     * @param name the name of the attribute
     * @return a list of attribute values. May be empty
     */
    private static String[] getValues(Meta meta, String name) {
        List<String> result = new ArrayList<String>();
        Attribute[] attributes = meta.getAttribute();
        for (int i = 0; i < attributes.length; ++i) {
            Attribute attribute = attributes[i];
            if (attribute.getName().equals(name)) {
                result.add(attribute.getValue());
            }
        }
        return result.toArray(EMPTY);
    }

    /**
     * Format values for logging
     *
     * @param values the list of values
     * @return the formatted values
     */
    private static String format(String[] values) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < values.length; ++i) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(values[i]);
        }
        return result.toString();
    }

    static {
        METADATA = new HashMap<String, ClassMeta>();

        InputStream stream = AttributeHelper.class.getResourceAsStream(PATH);
        if (stream == null) {
            try {
                File f = new File(System.getProperty("jmscts.home)","resources"+PATH));
                if (f.exists()) {
                    stream = new FileInputStream(f);
                }
            } catch (Exception x) {
                log.error("Unable to load metadata.xml from file system.",x);
            }
            if (stream==null) {
                String msg = "Failed to locate meta data: " + PATH;
                log.error(msg);
                throw new RuntimeException(msg);
            }
        }
        MetaData metaData;
        try {
            metaData = MetaData.unmarshal(new InputStreamReader(stream));
        } catch (Exception exception) {
            String msg = "Failed to read meta data: " + PATH;
            log.error(msg, exception);
            throw new RuntimeException(msg);
        }


        ClassMeta[] classes = metaData.getClassMeta();
        for (int i = 0; i < classes.length; ++i) {
            ClassMeta clazz = classes[i];
            METADATA.put(clazz.getName(), clazz);
        }
    }

}
