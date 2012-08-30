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
 * $Id: DeliveryTypes.java,v 1.5 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;


/**
 * Helper class used to indicate what delivery, destination type and message
 * receipt modes should be used to run a particular test case against
 *
 * @version     $Revision: 1.5 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         DeliveryType
 * @see         SendReceiveTestCase
 * @see         SendReceiveTestRunner
 */
public class DeliveryTypes {

    /**
     * NON_PERSISTENT delivery mode, administered destinations, no
     * receipt
     */
    public static final DeliveryType SEND_NON_PERSISTENT_ADMINISTERED;

    /**
     * PERSISTENT delivery mode, administered destinations, no
     * receipt
     */
    public static final DeliveryType SEND_PERSISTENT_ADMINISTERED;

    /**
     * NON_PERSISTENT delivery mode, temporary destinations, no
     * receipt
     */
    public static final DeliveryType SEND_NON_PERSISTENT_TEMPORARY;

    /**
     * PERSISTENT delivery mode, temporary destinations, no
     * receipt
     */
    public static final DeliveryType SEND_PERSISTENT_TEMPORARY;

    /**
     * All SEND delivery types
     */
    public static final DeliveryTypes ALL_SEND;

    /**
     * NON_PERSISTENT delivery mode, administered destinations, synchronous
     * receipt
     */
    public static final DeliveryType NON_PERSISTENT_ADMINISTERED_SYNC;

    /**
     * NON_PERSISTENT delivery mode, administered destinations, asynchronous
     * receipt
     */
    public static final DeliveryType NON_PERSISTENT_ADMINISTERED_ASYNC;

    /**
     * NON_PERSISTENT delivery mode, administered destinations, browser receipt
     */
    public static final DeliveryType NON_PERSISTENT_ADMINISTERED_BROWSER;

    /**
     * NON_PERSISTENT delivery mode, temporary destinations, synchronous
     * receipt
     */
    public static final DeliveryType NON_PERSISTENT_TEMPORARY_SYNC;

    /**
     * NON_PERSISTENT delivery mode, temporary destinations, asynchronous
     * receipt
     */
    public static final DeliveryType NON_PERSISTENT_TEMPORARY_ASYNC;

    /**
     * NON_PERSISTENT delivery mode, temporary destinations, browser receipt
     */
    public static final DeliveryType NON_PERSISTENT_TEMPORARY_BROWSER;

    /**
     * PERSISTENT delivery mode, administered destinations, synchronous receipt
     */
    public static final DeliveryType PERSISTENT_ADMINISTERED_SYNC;

    /**
     * PERSISTENT delivery mode, administered destinations, asynchronous
     * receipt
     */
    public static final DeliveryType PERSISTENT_ADMINISTERED_ASYNC;

    /**
     * PERSISTENT delivery mode, administered destinations, browser receipt
     */
    public static final DeliveryType PERSISTENT_ADMINISTERED_BROWSER;

    /**
     * PERSISTENT delivery mode, temporary destinations, synchronous receipt
     */
    public static final DeliveryType PERSISTENT_TEMPORARY_SYNC;

    /**
     * PERSISTENT delivery mode, temporary destinations, asynchronous receipt
     */
    public static final DeliveryType PERSISTENT_TEMPORARY_ASYNC;

    /**
     * PERSISTENT delivery mode, temporary destinations, browser receipt
     */
    public static final DeliveryType PERSISTENT_TEMPORARY_BROWSER;

    /**
     * All NON_PERSISTENT delivery types
     */
    public static final DeliveryTypes NON_PERSISTENT;

    /**
     * All PERSISTENT delivery types
     */
    public static final DeliveryTypes PERSISTENT;

    /**
     * All synchronous delivery types
     */
    public static final DeliveryTypes SYNCHRONOUS;

    /**
     * All asynchronous delivery types
     */
    public static final DeliveryTypes ASYNCHRONOUS;

    /**
     * All temporary destination delivery types
     */
    public static final DeliveryTypes TEMPORARY;

    /**
     * All administered destination delivery types
     */
    public static final DeliveryTypes ADMINISTERED;

    /**
     * All non-persistent consumer delivery types (i.e no browsers)
     */
    public static final DeliveryTypes NON_PERSISTENT_CONSUMER;

    /**
     * All persistent consumer delivery types (i.e no browsers)
     */
    public static final DeliveryTypes PERSISTENT_CONSUMER;

    /**
     * All administered destination consumer delivery types (i.e no browsers)
     */
    public static final DeliveryTypes ADMINISTERED_CONSUMER;

    /**
     * All temporary destination consumer delivery types (i.e no browsers)
     */
    public static final DeliveryTypes TEMPORARY_CONSUMER;

    /**
     * All consumer delivery types (i.e no browsers)
     */
    public static final DeliveryTypes CONSUMER;

    /**
     * All delivery types
     */
    public static final DeliveryTypes ALL;

    /**
     * The delivery types to test against
     */
    private final DeliveryType[] _types;

    /**
     * Construct a new instance to test against a single delivery type
     *
     * @param type the delivery type
     */
    public DeliveryTypes(DeliveryType type) {
        if (type == null) {
            throw new IllegalArgumentException("Argument type is null");
        }
        _types = new DeliveryType[]{type};
    }

    /**
     * Construct a new instance to test against a set of delivery types
     *
     * @param types a list of delivery types
     */
    public DeliveryTypes(DeliveryType[] types) {
        if (types == null) {
            throw new IllegalArgumentException("Argument types is null");
        }
        if (types.length == 0) {
            throw new IllegalArgumentException(
                "Argument types has no elements");
        }
        _types = types;
    }

    /**
     * Return the list of delivery types to test against
     *
     * @return the list of delivery types to test against
     */
    public DeliveryType[] getTypes() {
        return _types;
    }

    /**
     * Return a count of the delivery types
     *
     * @return the number of delivery types
     */
    public int count() {
        return _types.length;
    }

    /**
     * Helper to parse a DeliveryType from a string
     *
     * @param types the strings to parse
     * @return the parsed delivery types
     */
    public static DeliveryTypes fromString(String[] types) {
        DeliveryTypes result = null;
        DeliveryType[] set = new DeliveryType[types.length];
        for (int i = 0; i < types.length; ++i) {
            if ("all".equalsIgnoreCase(types[i])) {
                result = ALL;
                break;
            } else if ("all-send".equalsIgnoreCase(types[i])) {
                result = ALL_SEND;
                break;
            } else if ("administered-consumer".equalsIgnoreCase(types[i])) {
                result = ADMINISTERED_CONSUMER;
                break;
            } else if ("asynchronous".equalsIgnoreCase(types[i])) {
                result = ASYNCHRONOUS;
                break;
            } else if ("consumer".equalsIgnoreCase(types[i])) {
                result = CONSUMER;
                break;
            } else if ("synchronous".equalsIgnoreCase(types[i])) {
                result = SYNCHRONOUS;
                break;
            } else {
                set[i] = DeliveryType.fromString(types[i]);
            }
        }
        if (result == null) {
            result = new DeliveryTypes(set);
        }
        return result;
    }

    static {
        SEND_NON_PERSISTENT_ADMINISTERED = new DeliveryType(false, true);
        SEND_PERSISTENT_ADMINISTERED = new DeliveryType(true, true);
        SEND_NON_PERSISTENT_TEMPORARY = new DeliveryType(false, false);
        SEND_PERSISTENT_TEMPORARY = new DeliveryType(true, false);
        ALL_SEND = new DeliveryTypes(new DeliveryType[]{
            SEND_NON_PERSISTENT_ADMINISTERED,
            SEND_PERSISTENT_ADMINISTERED,
            SEND_NON_PERSISTENT_TEMPORARY,
            SEND_PERSISTENT_TEMPORARY});

        NON_PERSISTENT_ADMINISTERED_SYNC =
            new DeliveryType(false, true, ReceiptType.SYNCHRONOUS);
        NON_PERSISTENT_ADMINISTERED_ASYNC =
            new DeliveryType(false, true, ReceiptType.ASYNCHRONOUS);
        NON_PERSISTENT_ADMINISTERED_BROWSER =
            new DeliveryType(false, true, ReceiptType.BROWSER);
        NON_PERSISTENT_TEMPORARY_SYNC =
            new DeliveryType(false, false, ReceiptType.SYNCHRONOUS);
        NON_PERSISTENT_TEMPORARY_ASYNC =
            new DeliveryType(false, false, ReceiptType.ASYNCHRONOUS);
        NON_PERSISTENT_TEMPORARY_BROWSER =
            new DeliveryType(false, false, ReceiptType.BROWSER);
        PERSISTENT_ADMINISTERED_SYNC =
            new DeliveryType(true, true, ReceiptType.SYNCHRONOUS);
        PERSISTENT_ADMINISTERED_ASYNC =
            new DeliveryType(true, true, ReceiptType.ASYNCHRONOUS);
        PERSISTENT_ADMINISTERED_BROWSER =
            new DeliveryType(true, true, ReceiptType.BROWSER);
        PERSISTENT_TEMPORARY_SYNC =
            new DeliveryType(true, false, ReceiptType.SYNCHRONOUS);
        PERSISTENT_TEMPORARY_ASYNC =
            new DeliveryType(true, false, ReceiptType.ASYNCHRONOUS);
        PERSISTENT_TEMPORARY_BROWSER =
            new DeliveryType(true, false, ReceiptType.BROWSER);

        NON_PERSISTENT = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_ADMINISTERED_SYNC,
            NON_PERSISTENT_ADMINISTERED_ASYNC,
            NON_PERSISTENT_ADMINISTERED_BROWSER,
            NON_PERSISTENT_TEMPORARY_SYNC,
            NON_PERSISTENT_TEMPORARY_ASYNC,
            NON_PERSISTENT_TEMPORARY_BROWSER});

        PERSISTENT = new DeliveryTypes(new DeliveryType[] {
            PERSISTENT_ADMINISTERED_SYNC,
            PERSISTENT_ADMINISTERED_ASYNC,
            PERSISTENT_ADMINISTERED_BROWSER,
            PERSISTENT_TEMPORARY_SYNC,
            PERSISTENT_TEMPORARY_ASYNC,
            PERSISTENT_TEMPORARY_BROWSER});

        SYNCHRONOUS = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_ADMINISTERED_SYNC,
            NON_PERSISTENT_TEMPORARY_SYNC,
            PERSISTENT_ADMINISTERED_SYNC,
            PERSISTENT_TEMPORARY_SYNC});

        ASYNCHRONOUS = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_ADMINISTERED_ASYNC,
            NON_PERSISTENT_TEMPORARY_ASYNC,
            PERSISTENT_ADMINISTERED_ASYNC,
            PERSISTENT_TEMPORARY_ASYNC});

        TEMPORARY = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_TEMPORARY_SYNC,
            NON_PERSISTENT_TEMPORARY_ASYNC,
            NON_PERSISTENT_TEMPORARY_BROWSER,
            PERSISTENT_TEMPORARY_SYNC,
            PERSISTENT_TEMPORARY_ASYNC,
            PERSISTENT_TEMPORARY_BROWSER});

        ADMINISTERED = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_ADMINISTERED_SYNC,
            NON_PERSISTENT_ADMINISTERED_ASYNC,
            NON_PERSISTENT_ADMINISTERED_BROWSER,
            PERSISTENT_ADMINISTERED_SYNC,
            PERSISTENT_ADMINISTERED_ASYNC,
            PERSISTENT_ADMINISTERED_BROWSER});

        NON_PERSISTENT_CONSUMER = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_ADMINISTERED_SYNC,
            NON_PERSISTENT_ADMINISTERED_ASYNC,
            NON_PERSISTENT_TEMPORARY_SYNC,
            NON_PERSISTENT_TEMPORARY_ASYNC});

        PERSISTENT_CONSUMER = new DeliveryTypes(new DeliveryType[] {
            PERSISTENT_ADMINISTERED_SYNC,
            PERSISTENT_ADMINISTERED_ASYNC,
            PERSISTENT_TEMPORARY_SYNC,
            PERSISTENT_TEMPORARY_ASYNC});

        ADMINISTERED_CONSUMER = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_ADMINISTERED_SYNC,
            NON_PERSISTENT_ADMINISTERED_ASYNC,
            PERSISTENT_ADMINISTERED_SYNC,
            PERSISTENT_ADMINISTERED_ASYNC});

        TEMPORARY_CONSUMER = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_TEMPORARY_SYNC,
            NON_PERSISTENT_TEMPORARY_ASYNC,
            PERSISTENT_TEMPORARY_SYNC,
            PERSISTENT_TEMPORARY_ASYNC});

        CONSUMER = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_ADMINISTERED_SYNC,
            NON_PERSISTENT_ADMINISTERED_ASYNC,
            NON_PERSISTENT_TEMPORARY_SYNC,
            NON_PERSISTENT_TEMPORARY_ASYNC,
            PERSISTENT_ADMINISTERED_SYNC,
            PERSISTENT_ADMINISTERED_ASYNC,
            PERSISTENT_TEMPORARY_SYNC,
            PERSISTENT_TEMPORARY_ASYNC});

        ALL = new DeliveryTypes(new DeliveryType[] {
            NON_PERSISTENT_ADMINISTERED_SYNC,
            NON_PERSISTENT_ADMINISTERED_ASYNC,
            NON_PERSISTENT_ADMINISTERED_BROWSER,
            NON_PERSISTENT_TEMPORARY_SYNC,
            NON_PERSISTENT_TEMPORARY_ASYNC,
            NON_PERSISTENT_TEMPORARY_BROWSER,
            PERSISTENT_ADMINISTERED_SYNC,
            PERSISTENT_ADMINISTERED_ASYNC,
            PERSISTENT_ADMINISTERED_BROWSER,
            PERSISTENT_TEMPORARY_SYNC,
            PERSISTENT_TEMPORARY_ASYNC,
            PERSISTENT_TEMPORARY_BROWSER});
    }

}
