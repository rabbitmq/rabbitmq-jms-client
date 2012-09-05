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
 * $Id: ClassHelper.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.exolab.jmscts.core.types.PropertyTypeType;


/**
 * Helper for loading classes and setting/getting any public properties.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
@SuppressWarnings("rawtypes")
public final class ClassHelper {


    /**
     * A map of objectified primitive types, and their corresponding primitive
     * types
     */
    private static final Class[][] TYPES = {
        {Boolean.class, boolean.class}, {Byte.class, byte.class},
        {Short.class, short.class}, {Character.class, char.class},
        {Integer.class, int.class}, {Long.class, long.class},
        {Float.class, float.class}, {Double.class, double.class}};


    /**
     * Prevent construction of utility class
     */
    private ClassHelper() {
    }

    /**
     * Create a new instance of an object identified by its class name,
     * and set its properties
     *
     * @param className the name of the class
     * @param properties the list of properties to set
     * @return a new instance the specified class
     * @throws ClassNotFoundException if the class cannot be located
     * @throws IllegalAccessException if the class or initializer is not
     * accessible
     * @throws InstantiationException if the instantiation fails
     * @throws InvocationTargetException if the underlying method throws an
     * exception
     * @throws NoSuchMethodException if a matching method is not found
     */
    public static Object instantiate(String className,
                                     Property[] properties)
        throws ClassNotFoundException, IllegalAccessException,
               InstantiationException, InvocationTargetException,
               NoSuchMethodException {

        Object object = Class.forName(className).newInstance();
        for (int i = 0; i < properties.length; ++i) {
            Property property = properties[i];
            Class<?> type = Class.forName(property.getType().toString());
            Class[] types = new Class[] {type};
            String name = "set" + property.getName();
            Method method = object.getClass().getMethod(name, types);
            Object arg = PropertyHelper.create(property);
            Object[] args = new Object[]{arg};
            method.invoke(object, args);
        }
        return object;
    }

    /**
     * Helper to returns the properties of an object
     *
     * @param object the object to retrieve properties for
     * @return the set of properties of object
     * @throws IllegalAccessException if the class or initializer is not
     * accessible
     * @throws InvocationTargetException if the underlying method throws an
     * exception
     */
    public static Property[] getProperties(Object object)
        throws IllegalAccessException, InvocationTargetException {
        final String prefix = "get";

        ArrayList<Property> result = new ArrayList<Property>();
        Method[] methods = object.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            Method method = methods[i];
            int modifier = method.getModifiers();
            if (method.getParameterTypes().length == 0
                && Modifier.isPublic(modifier) && !Modifier.isStatic(modifier)
                && method.getName().startsWith(prefix)) {

                Class<?> type = method.getReturnType();
                if (type.equals(Boolean.class)
                    || type.equals(Short.class)
                    || type.equals(Integer.class)
                    || type.equals(Long.class)
                    || type.equals(Float.class)
                    || type.equals(Double.class)
                    || type.equals(String.class)) {
                    Property property = new Property();
                    String name = method.getName().substring(prefix.length());
                    property.setName(name);
                    property.setType(PropertyTypeType.valueOf(type.getName()));
                    Object value = method.invoke(object, new Object[]{});
                    property.setValue(value.toString());
                    result.add(property);
                }
            }
        }
        return result.toArray(new Property[]{});
    }

    public static Method getMethod(Class<?> type, String name, Object[] args, Class[] types)
            throws NoSuchMethodException {
        return type.getMethod(name, types);
    }

    
    /**
     * Helper to return a method given its name and a list of arguments
     *
     * @param type the class to locate the method on
     * @param name the method name
     * @param args the list of arguments to match against, or null if the
     * method takes no arguments
     * @return the method matching the name and list of arguments
     * @throws NoSuchMethodException if the method cannot be found
     */
    public static Method getMethod(Class<?> type, String name, Object[] args)
        throws NoSuchMethodException {

        boolean containsNull = false;
        Class[] types = null;
        if (args != null) {
            types = new Class[args.length];
            for (int i = 0; i < args.length; ++i) {
                if (args[i] != null) {
                    types[i] = args[i].getClass();
                } else {
                    containsNull = true;
                }
            }
        } else {
            types = new Class[]{};
        }

        Method method = null;
        if (!containsNull) {
            try {
                // try a direct lookup
                method = type.getMethod(name, types);
            } catch (NoSuchMethodException ignore) {
                if (types.length != 0) {
                    // try converting any objectified types to primitives
                    Class[] converted = new Class[types.length];
                    for (int i = 0; i < args.length; ++i) {
                        converted[i] = getPrimitiveType(args[i].getClass());
                    }
                    try {
                        method = type.getMethod(name, types);
                    } catch (NoSuchMethodException ignore2) {
                        // no-op
                    }
                }
            }
        }
        if (method == null) {
            // try and find it iteratively, using the class' public declared
            // methods
            Method[] methods = type.getDeclaredMethods();
            for (int i = 0; i < methods.length; ++i) {
                if (Modifier.isPublic(methods[i].getModifiers())
                    && methods[i].getName().equals(name)) {
                    if (checkParameters(methods[i], types)) {
                        method = methods[i];
                        break;
                    }
                }
            }
            if (method == null) {
                // try and find it iteratively, using the MUCH slower
                // getMethods() method
                methods = type.getMethods();
                for (int i = 0; i < methods.length; ++i) {
                    if (methods[i].getName().equals(name)) {
                        if (checkParameters(methods[i], types)) {
                            method = methods[i];
                            break;
                        }
                    }
                }
            }
            if (method == null) {
                String msg = "No method found for name=" + name
                    + ", argument types=(";
                if (args != null && args.length > 0) {
                    for (int i = 0; i < args.length; ++i) {
                        if (i > 0) {
                            msg += ", ";
                        }
                        if (args[i] != null) {
                            msg += args[i].getClass().getName();
                        } else {
                            msg += "Object";
                        }
                    }
                }
                msg += ")";

                throw new NoSuchMethodException(msg);
            }
        }
        return method;
    }

    /**
     * Helper to return the primitive type associated with an object wrapper
     * type
     *
     * @param wrapper the object wrapper class
     * @return the associated primitive type, or the wrapper type, if there
     * is no associated primitive type
     */
    public static Class<?> getPrimitiveType(Class<? extends Object> wrapper) {
        Class<?> result = null;
        for (int i = 0; i < TYPES.length; ++i) {
            if (wrapper.equals(TYPES[i][0])) {
                result = TYPES[i][1];
                break;
            }
        }

        return (result != null) ? result : wrapper;
    }

    /**
     * Helper to return the primitive type name associated with an object
     * wrapper type
     *
     * @param wrapper the object wrapper class
     * @return the associated primitive type name, or the wrapper type name,
     * if there is no associated primitive type. Array types are returned
     * in the form '<class>[]'
     */
    public static String getPrimitiveName(Class<? extends Object> wrapper) {
        String result = null;
        Class<?> type = getPrimitiveType(wrapper);
        if (type.isArray()) {
            result = type.getComponentType().getName() + "[]";
        } else {
            result = type.getName();
        }
        return result;
    }

    /**
     * Helper to determine if a list of argument types match that of
     * a method
     *
     * @param method the method to check
     * @param types the argument types
     * @return <code>true</code> if the argument types are compatible
     */
    @SuppressWarnings("unchecked")
    private static boolean checkParameters(Method method, Class[] types) {
        boolean result = true;
        Class[] parameters = method.getParameterTypes();
        if (parameters.length != types.length) {
            result = false;
        } else {
            for (int i = 0; i < parameters.length; ++i) {
                Class<?> parameter = parameters[i];
                if (types[i] == null) {
                    if (parameter.isPrimitive()) {
                        result = false;
                        break;
                    }
                } else if (!parameter.isAssignableFrom(types[i])
                           && !parameter.isAssignableFrom(
                               getPrimitiveType(types[i]))) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

}
