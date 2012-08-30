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
 * $Id: StreamMessageVerifier.java,v 1.4 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.message.util;

import javax.jms.StreamMessage;

import org.exolab.jmscts.core.MethodCache;


/**
 * A helper class for populating and verifying the content of StreamMessage 
 * instances.
 *
 * @version     $Revision: 1.4 $ $Date: 2003/05/04 14:12:38 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessagePopulatorVerifier
 */
class StreamMessageVerifier extends MessagePopulatorVerifier {


    /**
     * Method cache for StreamMessage 
     */
    private static MethodCache _methods = null;

    /**
     * Construct a new instance. No exceptions are expected to be thrown
     * when invoking methods
     */
    public StreamMessageVerifier() {
    }

    /**
     * Construct an instance with the expected exception thrown when
     * methods are invoked
     *
     * @param exception the expected exception type when methods are invoked
     */
    public StreamMessageVerifier(Class exception) {
        super(exception);
    }

    /**
     * Attempt to populate a StreamMessage instance with data
     * 
     * @param message the message to populate
     * @throws Exception for any error
     */
    public void populateStreamMessage(StreamMessage message) throws Exception {
        byte[] bytes = populateByteArray(10, 0);
        invoke(message, "writeBoolean", Boolean.TRUE);
        invoke(message, "writeByte", new Byte(Byte.MIN_VALUE));
        invoke(message, "writeBytes", bytes);

        Object[] args = {bytes, new Integer(1), new Integer(bytes.length - 2)};
        invoke(message, "writeBytes", args);
        invoke(message, "writeShort", new Short(Short.MIN_VALUE));
        invoke(message, "writeChar", new Character(Character.MIN_VALUE));
        invoke(message, "writeInt", new Integer(Integer.MIN_VALUE));
        invoke(message, "writeLong", new Long(Long.MIN_VALUE));
        invoke(message, "writeFloat", new Float(Float.MIN_VALUE));
        invoke(message, "writeDouble", new Double(Double.MIN_VALUE));
        invoke(message, "writeString", "ABC");

        invoke(message, "writeObject", Boolean.TRUE);
        invoke(message, "writeObject", new Byte(Byte.MAX_VALUE));
        invoke(message, "writeObject", bytes);
        invoke(message, "writeObject", new Short(Short.MAX_VALUE));
        invoke(message, "writeObject", new Character(Character.MAX_VALUE));
        invoke(message, "writeObject", new Integer(Integer.MAX_VALUE));
        invoke(message, "writeObject", new Long(Long.MAX_VALUE));
        invoke(message, "writeObject", new Float(Float.MAX_VALUE));
        invoke(message, "writeObject", new Double(Double.MAX_VALUE));
        invoke(message, "writeObject", "ABC");
    }

    /**
     * Attempt to verify the content of a StreamMessage invoked via the above
     * {@link #populateStreamMessage}. 
     * 
     * @param message the message to verify
     * @throws Exception for any error
     */
    public void verifyStreamMessage(StreamMessage message) throws Exception {
        expect(message, "readBoolean", Boolean.TRUE);
        expect(message, "readByte", new Byte(Byte.MIN_VALUE));

        byte[] buffer1 = new byte[10];
        expect(message, "readBytes", buffer1, new Integer(buffer1.length));
        equal(buffer1, populateByteArray(buffer1.length, 0));
        expect(message, "readBytes", buffer1, new Integer(-1));

        byte[] buffer2 = new byte[8];
        expect(message, "readBytes", buffer2, new Integer(buffer2.length));
        equal(buffer2, populateByteArray(buffer2.length, 1));
        expect(message, "readBytes", buffer2, new Integer(-1));

        expect(message, "readShort", new Short(Short.MIN_VALUE));
        expect(message, "readChar", new Character(Character.MIN_VALUE));
        expect(message, "readInt", new Integer(Integer.MIN_VALUE));
        expect(message, "readLong", new Long(Long.MIN_VALUE));
        expect(message, "readFloat", new Float(Float.MIN_VALUE));
        expect(message, "readDouble", new Double(Double.MIN_VALUE));
        expect(message, "readString", "ABC");

        expect(message, "readObject", Boolean.TRUE);
        expect(message, "readObject", new Byte(Byte.MAX_VALUE));
        expect(message, "readObject", populateByteArray(10, 0));
        expect(message, "readObject", new Short(Short.MAX_VALUE));
        expect(message, "readObject", new Character(Character.MAX_VALUE));
        expect(message, "readObject", new Integer(Integer.MAX_VALUE));
        expect(message, "readObject", new Long(Long.MAX_VALUE));
        expect(message, "readObject", new Float(Float.MAX_VALUE));
        expect(message, "readObject", new Double(Double.MAX_VALUE));
        expect(message, "readObject", "ABC");
    }

    protected synchronized MethodCache getMethods() throws SecurityException {
        if (_methods == null) {
            _methods = new MethodCache(StreamMessage.class);
        }
        return _methods;
    }

} //-- StreamMessageVerifier
