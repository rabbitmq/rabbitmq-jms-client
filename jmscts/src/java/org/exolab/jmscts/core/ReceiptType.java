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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: ReceiptType.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;


/**
 * Helper class used to indicate what message receipt type should be used to
 * run a particular test case with
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         DeliveryType
 * @see         MessagingBehaviour
 */
public final class ReceiptType {

    /**
     * Specifies to use asynchronous message receipt
     */
    public static final ReceiptType ASYNCHRONOUS;

    /**
     * Specifies to use synchronous message receipt
     */
    public static final ReceiptType SYNCHRONOUS;

    /**
     * Specifies to use a browser to receive messages. This is only applicable
     * for QueueSession sessions.
     */
    public static final ReceiptType BROWSER;

    /**
     * All receipt types
     */
    private static final ReceiptType[] ALL;

    /**
     * The type id
     */
    private int _type;

    /**
     * The name of this receipt type
     */
    private transient String _name;


    /**
     * Construct a new <code>ReceiptType</code>
     *
     * @param type the type id
     * @param name the name of the type
     */
    private ReceiptType(int type, String name) {
        _type = type;
        _name = name;
    }

    /**
     * Returns a hash code value for the object
     *
     * @return a hash code value for the object
     */
    @Override
    public int hashCode() {
        return _type;
    }

    /**
     * Determines if this is equal to another object
     *
     * @param object the object to compare to
     * @return <code>true</code> if equal, <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object object) {
        boolean equal = (object == this);
        if (!equal && object instanceof ReceiptType) {
            equal = (_type == ((ReceiptType) object)._type);
        }
        return equal;
    }

    /**
     * Returns a string representation of this type
     *
     * @return a string representation of this type
     */
    @Override
    public String toString() {
        return _name;
    }

    /**
     * Helper to parse a ReceiptType from a string
     *
     * @param type the string to parse
     * @return the parsed receipt type
     */
    public static ReceiptType fromString(String type) {
        ReceiptType result = null;
        for (int i = 0; i < ALL.length; ++i) {
            if (ALL[i]._name.equals(type)) {
                result = ALL[i];
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException(
                "Invalid receipt type: " + type);
        }
        return result;
    }

    static {
        ASYNCHRONOUS = new ReceiptType(0, "asynchronous");
        SYNCHRONOUS = new ReceiptType(1, "synchronous");
        BROWSER = new ReceiptType(2, "browser");

        ALL = new ReceiptType[]{ASYNCHRONOUS, SYNCHRONOUS, BROWSER};
    }

}
