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
 * $Id: MethodInvoker.java,v 1.2 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * A helper class for invoking methods, and verifying that
 * any exception thrown matches that expected.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class MethodInvoker {

    /**
     * The class to introspect. May be <code>null</code>
     */
    private Class<?> _clazz;

    /**
     * The expected type of exception
     */
    private final Class<?> _exception;

    /**
     * Construct an instance with that expects no exceptions when a method
     * is invoked
     */
    public MethodInvoker() {
        this(null, null);
    }

    /**
     * Construct an instance that expects a single expected exception when
     * a method is invoked.
     *
     * @param clazz the class to introspect. May be <code>null</code>
     * @param exception the expected exception when methods are invoked,
     * or <code>null</code> if no exceptions are expected.
     */
    public MethodInvoker(Class<?> clazz, Class<?> exception) {
        _clazz = clazz;
        _exception = exception;
    }

    /**
     * Invoke a method taking no arguments
     * <p>
     * If the method completes normally, the value it returns is returned to
     * the caller of invoke; if the value has a primitive type, it is wrapped
     * in an object. If the underlying method return type is void, the
     * invocation returns null.
     *
     * @param object the object to invoke the method on
     * @param method the method name
     * @return the result of invoking the method, or null, if it is void
     * @throws Exception if an exception is thrown and it doesn't match
     * the expected exception. If no exception is expected, then the exception
     * will be propagated
     * @throws NoSuchMethodException if the method doesn't exist
     *
     */
    public Object invoke(Object object, String method)
        throws Exception, NoSuchMethodException {
        return invoke(object, method, new Object[]{});
    }

    /**
     * Invoke a method taking no arguments
     * <p>
     * If the method completes normally, the value it returns is returned to
     * the caller of invoke; if the value has a primitive type, it is wrapped
     * in an object. If the underlying method return type is void, the
     * invocation returns null.
     *
     * @param object the object to invoke the method on
     * @param method the method to invoke
     * @return the result of invoking the method, or null, if it is void
     * @throws Exception if an exception is thrown and it doesn't match
     * the expected exception. If no exception is expected, then the exception
     * will be propagated
     */
    public Object invoke(Object object, Method method)
        throws Exception {
        return invoke(object, method, new Object[]{});
    }

    /**
     * Invoke a method taking a single argument
     * <p>
     * If the method completes normally, the value it returns is returned to
     * the caller of invoke; if the value has a primitive type, it is wrapped
     * in an object. If the underlying method return type is void, the
     * invocation returns null.
     *
     * @param object the object to invoke the method on
     * @param method the method name
     * @param argument the argument to pass to the method
     * @return the result of invoking the method, or null, if it is void
     * @throws Exception if an exception is thrown and it doesn't match
     * the expected exception. If no exception is expected, then the exception
     * will be propagated
     * @throws NoSuchMethodException if the method doesn't exist
     */
    public Object invoke(Object object, String method, Object argument)
        throws Exception, NoSuchMethodException {
        return invoke(object, method, new Object[]{argument});
    }
    
    
    
    public Object invoke(Object object, String method, Object argument, Class type)
        throws Exception, NoSuchMethodException {
        return invoke(object, method, new Object[]{argument}, new Class[]{type});
    }

    /**
     * Invoke a method taking a single argument
     * <p>
     * If the method completes normally, the value it returns is returned to
     * the caller of invoke; if the value has a primitive type, it is wrapped
     * in an object. If the underlying method return type is void, the
     * invocation returns null.
     *
     * @param object the object to invoke the method on
     * @param method the method to invoke
     * @param argument the argument to pass to the method
     * @return the result of invoking the method, or null, if it is void
     * @throws Exception if an exception is thrown and it doesn't match
     * the expected exception. If no exception is expected, then the exception
     * will be propagated
     */
    public Object invoke(Object object, Method method, Object argument)
        throws Exception {
        return invoke(object, method, new Object[]{argument});
    }

    /**
     * Invoke a method taking a list of arguments
     * <p>
     * If the method completes normally, the value it returns is returned to
     * the caller of invoke; if the value has a primitive type, it is wrapped
     * in an object. If the underlying method return type is void, the
     * invocation returns null.
     *
     * @param object the object to invoke the method on
     * @param method the method name
     * @param arguments the arguments to pass to the method
     * @return the result of invoking the method, or null, if it is void
     * @throws Exception if an exception is thrown and it doesn't match
     * the expected exception. If no exception is expected, then the exception
     * will be propagated
     * @throws NoSuchMethodException if the method doesn't exist
     */
    public Object invoke(Object object, String method, Object[] arguments)
        throws Exception, NoSuchMethodException {
        
        Class<?> clazz = (_clazz != null) ? _clazz : object.getClass();
        Method invoker = ClassHelper.getMethod(clazz, method, arguments);
        return invoke(object, invoker, arguments);
    }

    public Object invoke(Object object, String method, Object[] arguments, Class[] types)
        throws Exception, NoSuchMethodException {
        Class<?> clazz = (_clazz != null) ? _clazz : object.getClass();
        Method invoker = ClassHelper.getMethod(clazz, method, arguments,types);
        return invoke(object, invoker, arguments);
    }
    /**
     * Invoke a method taking a list of arguments
     * <p>
     * If the method completes normally, the value it returns is returned to
     * the caller of invoke; if the value has a primitive type, it is wrapped
     * in an object. If the underlying method return type is void, the
     * invocation returns null.
     *
     * @param object the object to invoke the method on
     * @param method the method to invoke
     * @param arguments the arguments to pass to the method
     * @return the result of invoking the method, or null, if it is void
     * @throws Exception if an exception is thrown and it doesn't match
     * the expected exception. If no exception is expected, then the exception
     * will be propagated
     */
    public Object invoke(Object object, Method method, Object[] arguments)
        throws Exception {
        Object result = null;
        try {
            result = method.invoke(object, arguments);
            if (_exception != null) {
                fail(method);
            }
        } catch (InvocationTargetException exception) {
            if (_exception != null) {
                verify(method, (Exception) exception.getTargetException());
            } else {
                throw (Exception) exception.getTargetException();
            }
        }
        return result;
    }

    /**
     * Helper to raise an exception when a method doesn't throw the expected
     * exception
     *
     * @param method the method
     * @throws Exception details the expected exception
     */
    protected void fail(Method method) throws Exception {
        System.out.println("Expected exception of type=" + _exception.getName()
                           + " to be thrown when invoking " + method);
        throw new Exception(
            "Expected exception of type=" + _exception.getName()
            + " to be thrown when invoking " + method);
    }

    /**
     * Helper to verify that an exception matches that expected
     *
     * @param method the method
     * @param exception the exception thrown by the method
     * @throws Exception if <code>exception</code> isn't of the expected type
     */
    protected void verify(Method method, Exception exception)
        throws Exception {

        if (!_exception.isAssignableFrom(exception.getClass())) {
            System.out.println("Expected exception of type=" + _exception.getName()
            + " to be thrown, when invoking " + method
            + " but got exception type=" + exception.getClass().getName()
            + ": " + exception);
            throw new Exception(
                "Expected exception of type=" + _exception.getName()
                + " to be thrown, when invoking " + method
                + " but got exception type=" + exception.getClass().getName()
                + ": " + exception);
        }
    }

}
