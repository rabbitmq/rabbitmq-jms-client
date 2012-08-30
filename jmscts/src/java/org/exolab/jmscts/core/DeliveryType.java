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
 * $Id: DeliveryType.java,v 1.5 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.StringTokenizer;

import javax.jms.DeliveryMode;


/**
 * Helper class used to indicate what delivery, destination type and message
 * receipt mode should be used to run a particular test case against
 *
 * @version     $Revision: 1.5 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         DeliveryTypes
 * @see         SendReceiveTestCase
 * @see         SendReceiveTestRunner
 */
public class DeliveryType {

    /**
     * The delivery mode. This is either DeliveryMode.PERSISTENT
     * or DeliveryMode.NON_PERSISTENT
     */
    private final int _deliveryMode;

    /**
     * If true, destinations are administered, else they are temporary.
     */
    private final boolean _administered;

    /**
     * The type of message receipt to use
     */
    private final ReceiptType _receipt;


    /**
     * Create a new instance
     *
     * @param persistent if true, messages are to be sent using
     * DeliveryMode.PERSISTENT delivery, else DeliveryMode.NON_PERSISTENT is
     * used.
     * @param administered if true, destinations are to be administered, else
     * they are temporary
     */
    public DeliveryType(boolean persistent, boolean administered) {
        this(persistent, administered, null);
    }

    /**
     * Create a new instance
     *
     * @param persistent if true, messages are to be sent using
     * DeliveryMode.PERSISTENT delivery, else DeliveryMode.NON_PERSISTENT is
     * used.
     * @param administered if true, destinations are to be administered, else
     * they are temporary
     * @param receipt the type of message receipt type.
     * May be <code>null</code>
     */
    public DeliveryType(boolean persistent, boolean administered,
                        ReceiptType receipt) {
        _deliveryMode = (persistent) ? DeliveryMode.PERSISTENT
            : DeliveryMode.NON_PERSISTENT;
        _administered = administered;
        _receipt = receipt;
    }

    /**
     * Returns the delivery mode. This is either
     * <code>DeliveryMode.PERSISTENT</code> or
     * <code>DeliveryMode.NON_PERSISTENT</code>
     *
     * @return the delivery mode
     */
    public int getDeliveryMode() {
        return _deliveryMode;
    }

    /**
     * DeReturns true if destinations are administered, false if destinations
     * are temporary.
     *
     * @return <code>true</code> if destinations are administered,
     * <code>false</code> otherwise
     */
    public boolean getAdministered() {
        return _administered;
    }

    /**
     * Returns the type of message receipt to be used
     *
     * @return the message receipt type, or <code>null</code> if no receipt
     * type is specified
     */
    public ReceiptType getReceiptType() {
        return _receipt;
    }

    /**
     * Helper to parse a DeliveryType from a string
     *
     * @param type the string to parse
     * @return the parsed delivery type
     */
    public static DeliveryType fromString(String type) {
        final int minTokens = 2;
        final int maxTokens = 3;
        DeliveryType result;
        boolean persistent = false;
        boolean administered = false;
        ReceiptType receipt = null;

        StringTokenizer tokens = new StringTokenizer(type, ", ");
        int count = tokens.countTokens();

        if (count < minTokens || count > maxTokens) {
            throw new IllegalArgumentException(
                "Invalid delivery type: " + type);
        }

        String persistentStr = tokens.nextToken();
        String administeredStr = tokens.nextToken();

        if ("PERSISTENT".equals(persistentStr)) {
            persistent = true;
        } else if (!"NON_PERSISTENT".equals(persistentStr)) {
            throw new IllegalArgumentException(
                "Invalid delivery mode: expected one of PERSISTENT "
                + "or NON_PERSISTENT but got " + persistentStr);
        }
        if ("administered".equals(administeredStr)) {
            administered = true;
        } else if (!"temporary".equals(administeredStr)) {
            throw new IllegalArgumentException(
                "Invalid destination type: expected one of administered "
                + "or temporary but got " + administeredStr);
        }

        String receiptStr = null;
        if (count == maxTokens) {
            receiptStr = tokens.nextToken();
            receipt = ReceiptType.fromString(receiptStr);
            result = new DeliveryType(persistent, administered, receipt);
        } else {
            result = new DeliveryType(persistent, administered);
        }
        return result;
    }

}
