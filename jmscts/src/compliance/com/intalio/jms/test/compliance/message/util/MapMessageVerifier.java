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
 * $Id: MapMessageVerifier.java,v 1.4 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.message.util;

import javax.jms.MapMessage;
import javax.jms.Message;

import org.exolab.jmscts.core.MethodCache;


/**
 * A helper class for populating and verifying the content of MapMessage 
 * instances.
 *
 * @version     $Revision: 1.4 $ $Date: 2003/05/04 14:12:38 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessagePopulatorVerifier
 */
class MapMessageVerifier extends MessagePopulatorVerifier {

    /**
     * Method cache for MapMessage
     */
    private static MethodCache _methods = null;

    /**
     * Seed value used for value names
     */
    private int _seed = 0;

    /**
     * Construct a new instance. No exceptions are expected to be thrown
     * when invoking methods
     */
    public MapMessageVerifier() {
    }

    /**
     * Construct an instance with the expected exception thrown when
     * methods are invoked
     *
     * @param exception the expected exception type when methods are invoked
     */
    public MapMessageVerifier(Class exception) {
        super(exception);
    }

    /**
     * Attempt to populate a MapMessage instance with data
     * 
     * @param message the message to populate
     * @throws Exception for any error
     */
    public void populateMapMessage(MapMessage message) throws Exception {
        byte[] bytes = new byte[10];
        _seed = 0;
        set(message, "setBoolean", Boolean.TRUE);
        set(message, "setByte", new Byte(Byte.MIN_VALUE));
        set(message, "setBytes", bytes);

        Object[] args = {bytes, new Integer(1), new Integer(bytes.length - 2)};
        set(message, "setBytes", args);
        set(message, "setChar", new Character(Character.MIN_VALUE));
        set(message, "setDouble", new Double(Double.MIN_VALUE));
        set(message, "setFloat", new Float(Float.MIN_VALUE));
        set(message, "setInt", new Integer(Integer.MIN_VALUE));
        set(message, "setLong", new Long(Long.MIN_VALUE));
        set(message, "setShort", new Short(Short.MIN_VALUE));
        set(message, "setString", "ABC");

        set(message, "setObject", Boolean.TRUE);
        set(message, "setObject", new Byte(Byte.MAX_VALUE));
        set(message, "setObject", bytes);
        set(message, "setObject", new Character(Character.MAX_VALUE));
        set(message, "setObject", new Double(Double.MAX_VALUE));
        set(message, "setObject", new Float(Float.MAX_VALUE));
        set(message, "setObject", new Integer(Integer.MAX_VALUE));
        set(message, "setObject", new Long(Long.MAX_VALUE));
        set(message, "setObject", new Short(Short.MAX_VALUE));
        set(message, "setObject", "ABC");
    }

    /**
     * Attempt to verify the content of a MapMessage populated via the above
     * {@link #populateMapMessage}. 
     * 
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verifyMapMessage(MapMessage message) throws Exception {
        _seed = 0;
        get(message, "getBoolean", Boolean.TRUE);
        get(message, "getByte", new Byte(Byte.MIN_VALUE));
        get(message, "getBytes", new byte[10]);

        get(message, "getBytes", new byte[8]);
        get(message, "getChar", new Character(Character.MIN_VALUE));
        get(message, "getDouble", new Double(Double.MIN_VALUE));
        get(message, "getFloat", new Float(Float.MIN_VALUE));
        get(message, "getInt", new Integer(Integer.MIN_VALUE));
        get(message, "getLong", new Long(Long.MIN_VALUE));
        get(message, "getShort", new Short(Short.MIN_VALUE));
        get(message, "getString", "ABC");

        get(message, "getObject", Boolean.TRUE);
        get(message, "getObject", new Byte(Byte.MAX_VALUE));
        get(message, "getObject", new byte[10]);
        get(message, "getObject", new Character(Character.MAX_VALUE));
        get(message, "getObject", new Double(Double.MAX_VALUE));
        get(message, "getObject", new Float(Float.MAX_VALUE));
        get(message, "getObject", new Integer(Integer.MAX_VALUE));
        get(message, "getObject", new Long(Long.MAX_VALUE));
        get(message, "getObject", new Short(Short.MAX_VALUE));
        get(message, "getObject", "ABC");
    }

    protected void set(Message message, String method, Object value) 
        throws Exception {
        Object[] args = {"name" + ++_seed, value};
        invoke(message, method, args);
    }

    protected void set(Message message, String method, Object[] values) 
        throws Exception {
        Object[] args = new Object[values.length + 1];
        args[0] = "name" + ++_seed;
        System.arraycopy(values, 0, args, 1, values.length);
        invoke(message, method, args);
    }

    protected void get(Message message, String method, Object expected) 
        throws Exception {
        Object[] args = {"name" + ++_seed};
        expect(message, method, args, expected);
    }

    protected void get(Message message, String method, Object[] values, 
                       Object expected) throws Exception {
        Object[] args = new Object[values.length + 1];
        args[0] = "name" + ++_seed;
        System.arraycopy(values, 0, args, 1, values.length);
        expect(message, method, args, expected);
    }

    protected synchronized MethodCache getMethods() throws SecurityException {
        if (_methods == null) {
            _methods = new MethodCache(MapMessage.class);
        }
        return _methods;
    }

} //-- MapMessagePopulator
