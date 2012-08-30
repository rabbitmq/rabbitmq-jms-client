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
 * $Id: EmptyMessageVerifier.java,v 1.2 2004/02/03 07:31:04 tanderson Exp $
 */
package org.exolab.jmscts.test.message.util;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.exolab.jmscts.core.AbstractMessageVerifier;
import org.exolab.jmscts.core.MethodInvoker;


/**
 * A helper class for verifying that a message has no body
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 07:31:04 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         AbstractMessageVerifier
 */
public class EmptyMessageVerifier extends AbstractMessageVerifier {

    /** TODO */
    private static final long serialVersionUID = 1L;
    /**
     * Byte array size
     */
    private static final int BYTE_ARRAY_SIZE = 10;


    /**
     * Construct a new instance. No exceptions are expected to be thrown
     * when invoking methods
     */
    public EmptyMessageVerifier() {
    }

    /**
     * Construct an instance with the expected exception thrown when
     * methods are invoked
     *
     * @param exception the expected exception type when methods are invoked
     */
    public EmptyMessageVerifier(Class<?> exception) {
        super(exception);
    }

    /**
     * Verify a Message instance has no data.
     * This method is a no-op, as a Message instance has no body
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    @Override
    public void verifyMessage(Message message) throws Exception {
    }

    /**
     * Verify a BytesMessage instance has no data
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    @Override
    public void verifyBytesMessage(BytesMessage message) throws Exception {
        final int size = BYTE_ARRAY_SIZE;
        expect(message, "readBoolean", null);
        expect(message, "readByte", null);

        if (MessageEOFException.class.equals(getExpectedException())) {
            // don't expect any exceptions from readBytes if the expected
            // exception type is MessageEOFException. In this case,
            // readBytes should return -1
            MethodInvoker invoker = new MethodInvoker();
            Integer result = new Integer(-1);
            equal(invoker.invoke(message, "readBytes", new byte[size]),
                  result);

            Object[] args = {new byte[size], new Integer(size)};
            equal(invoker.invoke(message, "readBytes", args), result);
        } else {
            expect(message, "readBytes", new Object[]{new byte[size]}, null);
            Object[] args = {new byte[size], new Integer(size)};
            expect(message, "readBytes", args, null);
        }

        expect(message, "readChar", null);
        expect(message, "readDouble", null);
        expect(message, "readFloat", null);
        expect(message, "readInt", null);
        expect(message, "readLong", null);
        expect(message, "readShort", null);
        expect(message, "readUTF", null);
    }

    /**
     * Verify a MapMessage instance has no data
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    @Override
    public void verifyMapMessage(MapMessage message) throws Exception {
        Enumeration<?> iter = message.getMapNames();
        if (iter.hasMoreElements()) {
            // let the expect operation handle the exception
            String name = (String) iter.nextElement();
            expect(message, "getObject", new Object[]{name}, null);
        }
    }

    /**
     * Verify a MapMessage instance has no data
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    @Override
    public void verifyObjectMessage(ObjectMessage message) throws Exception {
        expect(message, "getObject", null);
    }

    /**
     * Verify a StreamMessage has no data
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    @Override
    public void verifyStreamMessage(StreamMessage message) throws Exception {
        expect(message, "readBoolean", null);
        expect(message, "readByte", null);

        expect(message, "readBytes", new Object[]{new byte[BYTE_ARRAY_SIZE]},
               null);
        // StreamMessage is different to BytesMessage in its implementation
        // of readBytes in that it may throw MessageEOFException. For
        // an empty message, this is the expected result as type information
        // must typically be inserted into the stream to indicate the type
        // and field length

        expect(message, "readChar", null);
        expect(message, "readDouble", null);
        expect(message, "readFloat", null);
        expect(message, "readInt", null);
        expect(message, "readLong", null);
        expect(message, "readShort", null);
        expect(message, "readString", null);
        expect(message, "readObject", null);
    }

    /**
     * Verify a TextMessage instance has no data
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    @Override
    public void verifyTextMessage(TextMessage message) throws Exception {
        expect(message, "getText", null);
    }

}
