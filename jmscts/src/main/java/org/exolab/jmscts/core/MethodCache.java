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
 * $Id: MethodCache.java,v 1.2 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Helper for caching the methods of a class to avoid expensive
 * {@link Class#getDeclaredMethods} and {@link Class#getMethod} calls
 *
 * @version     $Revision: 1.2 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class MethodCache {

    /**
     * The class for which the methods are being cached
     */
    private final Class<?> _type;

    /**
     * A cache of methods, keyed by name. Where multiple instances exist
     * with the same name, a list is used
     */
    private final HashMap<String, Object> _cache = new HashMap<String, Object>();


    /**
     * Cache all public methods for the specified class type
     *
     * @param type the class type to cache methods for
     */
    @SuppressWarnings("unchecked")
    public MethodCache(Class<?> type) {
        _type = type;
        Method[] methods = _type.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            Method method = methods[i];
            String name = method.getName();
            Object value = _cache.get(name);
            if (value == null) {
                _cache.put(name, method);
            } else {
                if (value instanceof ArrayList) {
                    ((ArrayList<Method>) value).add(method);
                } else {
                    ArrayList<Object> list = new ArrayList<Object>();
                    list.add(value);   // add the existing method
                    list.add(method);
                    _cache.put(name, list);
                }
            }
        }
    }

    /**
     * Get the named method. If more than one method exists with the same
     * name, the first will be returned
     *
     * @param name the name of the method
     * @return the first method matching name
     * @throws NoSuchMethodException if the method doesn't exist
     */
    public Method getMethod(String name) throws NoSuchMethodException {
        Object value = _cache.get(name);
        if (value == null) {
            throw new NoSuchMethodException(
                "Method=" + name + " not found for class=" + _type.getName());
        }
        Method result = null;
        if (value instanceof Method) {
            result = (Method) value;
        } else {
            result = (Method) ((ArrayList<?>) value).get(0);
        }
        return result;
    }

    /**
     * Get the named method, with the specified number of arguments.
     * If more than one method exists the first will be returned
     *
     * @param name the name of the method
     * @param args the number of arguments
     * @return the first method matching name
     * @throws NoSuchMethodException if the method doesn't exist
     */
    public Method getMethod(String name, int args)
        throws NoSuchMethodException {

        Object value = _cache.get(name);
        if (value == null) {
            throw new NoSuchMethodException(
                "Method=" + name + " not found for class=" + _type.getName());
        }
        Method result = null;
        if (value instanceof Method) {
            result = (Method) value;
            if (result.getParameterTypes().length != args) {
                throw new NoSuchMethodException(
                    "Method=" + name + " taking parameters=" + args
                    + " not found for class=" + _type.getName());
            }
        } else {
            ArrayList<?> list = (ArrayList<?>) value;
            Iterator<?> iter = list.iterator();
            Method next = null;
            while (iter.hasNext()) {
                next = (Method) iter.next();
                if (next.getParameterTypes().length == args) {
                    result = next;
                    break;
                }
            }
            if (result == null) {
                throw new NoSuchMethodException(
                    "Method=" + name + " taking parameters=" + args
                    + " not found for class=" + _type.getName());
            }
        }
        return result;
    }

}
