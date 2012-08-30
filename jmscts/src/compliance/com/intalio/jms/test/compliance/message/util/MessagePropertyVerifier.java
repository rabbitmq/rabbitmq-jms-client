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
 * $Id: MessagePropertyVerifier.java,v 1.4 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.message.util;

import javax.jms.Message;

import org.exolab.jmscts.core.MethodCache;


/**
 * A helper class for exercising the interfaces of messages
 *
 * @version     $Revision: 1.4 $ $Date: 2003/05/04 14:12:38 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessagePopulatorVerifier
 * @see         PropertyValues
 */
public class MessagePropertyVerifier extends MessagePopulatorVerifier
    implements PropertyValues {

    /**
     * Method cache for Message 
     */
    private static MethodCache _methods = null;
    
    /**
     * Construct a new instance. No exceptions are expected to be thrown
     * when invoking methods
     */
    public MessagePropertyVerifier() {
    }

    /**
     * Construct an instance with the expected exception thrown when
     * methods are invoked
     *
     * @param exception the expected exception type when methods are invoked
     */
    public MessagePropertyVerifier(Class exception) {
        super(exception);
    }

    /**
     * Populate a message's user properties 
     * 
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void populate(Message message) throws Exception {
        int seed = 0;
        for (int i = 0; i < BOOLEANS.length; ++i) {
            set(message, "setBooleanProperty", BOOLEANS[i], ++seed);
            set(message, "setObjectProperty", BOOLEANS[i], ++seed);
        }
        for (int i = 0; i < BYTES.length; ++i) {
            set(message, "setByteProperty", BYTES[i], ++seed);
            set(message, "setObjectProperty", BYTES[i], ++seed);
        }
        for (int i = 0; i < SHORTS.length; ++i) {
            set(message, "setShortProperty", SHORTS[i], ++seed);
            set(message, "setObjectProperty", SHORTS[i], ++seed); 
       }
        for (int i = 0; i < INTS.length; ++i) {
            set(message, "setIntProperty", INTS[i], ++seed);
            set(message, "setObjectProperty", INTS[i], ++seed);
        }
        for (int i = 0; i < LONGS.length; ++i) {
            set(message, "setLongProperty", LONGS[i], ++seed);
            set(message, "setObjectProperty", LONGS[i], ++seed);
        }
        for (int i = 0; i < FLOATS.length; ++i) {
            set(message, "setFloatProperty", FLOATS[i], ++seed);
            set(message, "setObjectProperty", FLOATS[i], ++seed);
        }
        for (int i = 0; i < DOUBLES.length; ++i) {
            set(message, "setDoubleProperty", DOUBLES[i], ++seed);
            set(message, "setObjectProperty", DOUBLES[i], ++seed);
        }
        for (int i = 0; i < STRINGS.length; ++i) {
            set(message, "setStringProperty", STRINGS[i], ++seed);
            set(message, "setObjectProperty", STRINGS[i], ++seed);
        }
    }

    /**
     * Verify a message's user properties, populated by the {@link #populate}
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verify(Message message) throws Exception {
        int seed = 0;
        for (int i = 0; i < BOOLEANS.length; ++i) {
            get(message, "getBooleanProperty", BOOLEANS[i], ++seed);
            get(message, "getObjectProperty", BOOLEANS[i], ++seed);
        }
        for (int i = 0; i < BYTES.length; ++i) {
            get(message, "getByteProperty", BYTES[i], ++seed);
            get(message, "getObjectProperty", BYTES[i], ++seed);
        }
        for (int i = 0; i < SHORTS.length; ++i) {
            get(message, "getShortProperty", SHORTS[i], ++seed);
            get(message, "getObjectProperty", SHORTS[i], ++seed);
       }
        for (int i = 0; i < INTS.length; ++i) {
            get(message, "getIntProperty", INTS[i], ++seed);
            get(message, "getObjectProperty", INTS[i], ++seed);
        }
        for (int i = 0; i < LONGS.length; ++i) {
            get(message, "getLongProperty", LONGS[i], ++seed);
            get(message, "getObjectProperty", LONGS[i], ++seed);
        }
        for (int i = 0; i < FLOATS.length; ++i) {
            get(message, "getFloatProperty", FLOATS[i], ++seed);
            get(message, "getObjectProperty", FLOATS[i], ++seed);
        }
        for (int i = 0; i < DOUBLES.length; ++i) {
            get(message, "getDoubleProperty", DOUBLES[i], ++seed);
            get(message, "getObjectProperty", DOUBLES[i], ++seed);
        }
        for (int i = 0; i < STRINGS.length; ++i) {
            get(message, "getStringProperty", STRINGS[i], ++seed);
            get(message, "getObjectProperty", STRINGS[i], ++seed);
        }
    }

    private void set(Message message, String method, Object value, int seed)
        throws Exception {
        Object args[] = new Object[]{"property" + seed, value};
        invoke(message, method, args);
    }

    private void get(Message message, String method, Object expected, int seed)
        throws Exception {
        Object args[] = new Object[]{"property" + seed};
        expect(message, method, args, expected);
    }

    protected synchronized MethodCache getMethods() throws SecurityException {
        if (_methods == null) {
            _methods = new MethodCache(Message.class);
        }
        return _methods;
    }

} //-- MessagePropertyVerifier
