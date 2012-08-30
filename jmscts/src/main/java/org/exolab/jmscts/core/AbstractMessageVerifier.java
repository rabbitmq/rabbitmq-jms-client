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
 * $Id: AbstractMessageVerifier.java,v 1.4 2004/02/03 21:52:06 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Arrays;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * A helper class for verifying the content of messages. This class provides
 * no verification functionality - that is left to sublasses.
 * <p>
 * It does provide helper methods to:
 * <ul>
 * <li>invoke methods on messages</li>
 * <li>invoke methods on messages and verify that any exception thrown matches
 *     that expected</li>
 * <li>verify the return value of a method against an expected result
 * </ul>
 *
 * @version     $Revision: 1.4 $ $Date: 2004/02/03 21:52:06 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see MessageVerifier
 */
public abstract class AbstractMessageVerifier implements MessageVerifier {

    /** TODO */
    private static final long serialVersionUID = 1L;

    /**
     * The expected exception type, or null, if no exceptions are expected
     */
    private Class<?> _exception = null;

    /**
     * The method invoker
     */
    private MethodInvoker _invoker = null;


    /**
     * Construct a new instance. No exceptions are expected to be thrown
     * when invoking methods
     */
    public AbstractMessageVerifier() {
        _invoker = new MethodInvoker();
    }

    /**
     * Construct an instance with the expected exception thrown when
     * methods are invoked
     *
     * @param exception the expected exception type when methods are invoked
     */
    public AbstractMessageVerifier(Class<?> exception) {
        _exception = exception;
        _invoker = new MethodInvoker(null, _exception);
    }

    /**
     * Verify a message's content. This method delegates the message to
     * the appropriate verifier method.
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    @Override
    public void verify(Message message) throws Exception {
        if (message instanceof BytesMessage) {
            verifyBytesMessage((BytesMessage) message);
        } else if (message instanceof MapMessage) {
            verifyMapMessage((MapMessage) message);
        } else if (message instanceof ObjectMessage) {
            verifyObjectMessage((ObjectMessage) message);
        } else if (message instanceof StreamMessage) {
            verifyStreamMessage((StreamMessage) message);
        } else if (message instanceof TextMessage) {
            verifyTextMessage((TextMessage) message);
        } else {
            verifyMessage(message);
        }
    }

    /**
     * Verify a Message instance. This method throws
     * UnsupportedOperationException if invoked. Subclasses needing the
     * functionality must implement it.
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verifyMessage(Message message) throws Exception {
        throw new UnsupportedOperationException(
            "verifyMessage() not implemented by " + getClass().getName());
    }

    /**
     * Verify a BytesMessage instance. This method throws
     * UnsupportedOperationException if invoked. Subclasses needing the
     * functionality must implement it.
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verifyBytesMessage(BytesMessage message) throws Exception {
        throw new UnsupportedOperationException(
            "verifyBytesMessage() not implemented by " + getClass().getName());
    }

    /**
     * Verify a MapMessage instance. This method throws
     * UnsupportedOperationException if invoked. Subclasses needing the
     * functionality must implement it.
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verifyMapMessage(MapMessage message) throws Exception {
        throw new UnsupportedOperationException(
            "verifyMapMessage() not implemented by " + getClass().getName());
    }

    /**
     * Verify an ObjectMessage instance. This method throws
     * UnsupportedOperationException if invoked. Subclasses needing the
     * functionality must implement it.
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verifyObjectMessage(ObjectMessage message) throws Exception {
        throw new UnsupportedOperationException(
            "verifyObjectMessage() not implemented by "
            + getClass().getName());
    }

    /**
     * Verify a StreamMessage instance. This method throws
     * UnsupportedOperationException if invoked. Subclasses needing the
     * functionality must implement it.
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verifyStreamMessage(StreamMessage message) throws Exception {
        throw new UnsupportedOperationException(
            "verifyStreamMessage() not implemented by "
            + getClass().getName());
    }

    /**
     * Verify a TextMessage instance. This method throws
     * UnsupportedOperationException if invoked. Subclasses needing the
     * functionality must implement it.
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verifyTextMessage(TextMessage message) throws Exception {
        throw new UnsupportedOperationException(
            "verifyTextMessage() not implemented by " + getClass().getName());
    }

    /**
     * Return the exception type expected to be thrown by methods
     *
     * @return the expected exception type, or null, if no exceptions are
     * expected
     */
    public Class<?> getExpectedException() {
        return _exception;
    }

    /**
     * Invoke a method taking no arguments
     *
     * @param message the message to invoke the method on
     * @param method the method to invoke
     * @return the result of the method invocation, or null if it returns
     * void, or didn't complete
     * @throws Exception for any error
     */
    protected Object invoke(Message message, String method) throws Exception {
        MethodCache cache = getMethods();
        Object result = null;
        if (cache != null) {
            result = _invoker.invoke(message, cache.getMethod(method));
        } else {
            result = _invoker.invoke(message, method);
        }
        return result;
    }

    /**
     * Invoke a method taking a single argument
     *
     * @param message the message to invoke the method on
     * @param method the method to invoke
     * @param value the argument to pass to the method
     * @return the result of the method invocation, or null if it returns
     * void, or didn't complete
     * @throws Exception for any error
     */
    protected Object invoke(Message message, String method, Object value)
        throws Exception {
        MethodCache cache = getMethods();
        Object result = null;
        if (cache != null) {
            result = _invoker.invoke(
                message, cache.getMethod(method, 1), value);
        } else {
            result = _invoker.invoke(message, method, value);
        }
        return result;
    }

    /**
     * Invoke a method taking multiple arguments
     *
     * @param message the message to invoke the method on
     * @param method the method to invoke
     * @param values the arguments to pass to the method
     * @return the result of the method invocation, or null if it returns
     * void, or didn't complete
     * @throws Exception for any error
     */
    protected Object invoke(Message message, String method, Object[] values)
        throws Exception {
        MethodCache cache = getMethods();
        Object result;
        if (cache != null) {
            result = _invoker.invoke(message,
                                     cache.getMethod(method, values.length),
                                     values);
        } else {
            result = _invoker.invoke(message, method, values);
        }
        return result;
    }

    /**
     * Helper to invoke a method taking no arguments, and verify that the
     * result matches that expected
     *
     * @param message the message to invoke the method on
     * @param method the method to invoke
     * @param expected the expected return value
     * @throws Exception for any error
     */
    protected void expect(Message message, String method, Object expected)
        throws Exception {
        equal(invoke(message, method), expected);
    }

    /**
     * Helper to invoke a method taking a single argument, and verify that the
     * result matches that expected
     *
     * @param message the message to invoke the method on
     * @param method the method to invoke
     * @param value the argument to pass to the method
     * @param expected the expected return value
     * @throws Exception for any error
     */
    protected void expect(Message message, String method, Object value,
                          Object expected)
        throws Exception {
        equal(invoke(message, method, value), expected);
    }

    /**
     * Helper to invoke a method taking a list of arguments, and verify that
     * the result matches that expected
     *
     * @param message the message to invoke the method on
     * @param method the method to invoke
     * @param values the arguments to pass to the method
     * @param expected the expected return value
     * @throws Exception for any error
     */
    protected void expect(Message message, String method, Object[] values,
                          Object expected)
        throws Exception {
        equal(invoke(message, method, values), expected);
    }

    /**
     * Helper to compare two objects for equality
     *
     * @param value the value to compare against
     * @param expected the expected value
     * @throws Exception if the objects aren't equal
     */
    protected void equal(Object value, Object expected) throws Exception {
        if (expected == null && value == null) {
            // no-op
        } else if (expected == null || value == null) {
            throw new Exception("Expected value=" + expected
                                + ", but got value=" + value);
        } else if (expected.getClass() == value.getClass()) {
            if (expected instanceof byte[]) {
                if (!Arrays.equals((byte[]) expected, (byte[]) value)) {
                    throw new Exception("Byte arrays are not equal");
                }
            } else if (!expected.equals(value)) {
                throw new Exception("Expected value=" + expected
                                    + ", but got value=" + value);
            }
        } else {
            throw new Exception(
                "Class mismatch. Expected class="
                + expected.getClass().getName() + ", but got class="
                + value.getClass().getName());
        }
    }

    /**
     * Returns a cache of methods for a message, to avoid expensive reflect
     * operations. This implementation returns null - subclasses needing
     * improved performance should provide their own implementation
     *
     * @return null
     */
    protected MethodCache getMethods() {
        return null;
    }

}
