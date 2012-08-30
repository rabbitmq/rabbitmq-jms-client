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
 * $Id: AckType.java,v 1.4 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Session;


/**
 * Helper class used to indicate what message acknowledgement type should be
 * used to run a particular test case against
 *
 * @version     $Revision: 1.4 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         AckTypes
 * @see         SessionTestCase
 * @see         SessionTestRunner
 */
public final class AckType {

    /**
     * Transacted session
     */
    public static final AckType TRANSACTED;

    /**
     * Non-transacted session, session automatically acknowledges messages
     */
    public static final AckType AUTO_ACKNOWLEDGE;

    /**
     * Non-transacted session, client ackwnowledges messages
     */
    public static final AckType CLIENT_ACKNOWLEDGE;

    /**
     * Non-transacted session, messages are lazily acknowledged
     */
    public static final AckType DUPS_OK_ACKNOWLEDGE;


    /**
     * If true, the messages should be acknowledged by a transacted session
     */
    private final boolean _transacted;

    /**
     * The acknowledgement mode, for non-transacted sessions
     */
    private final int _acknowledgeMode;

    /**
     * Create an instance for a transacted session
     */
    private AckType() {
        _transacted = true;
        _acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
    }

    /**
     * Create an instance for a non-transacted session
     *
     * @param acknowledgeMode indicates whether the consumer or the client
     * will acknowledge any messages it receives. Legal values are
     * <code>Session.AUTO_ACKNOWLEDGE</code>,
     * <code>Session.CLIENT_ACKNOWLEDGE</code> and
     * <code>Session.DUPS_OK_ACKNOWLEDGE</code>.
     */
    private AckType(int acknowledgeMode) {
        _transacted = false;
        _acknowledgeMode = acknowledgeMode;
    }

    /**
     * Returns if the session should be transactional
     *
     * @return true if the session should be transactional
     */
    public boolean getTransacted() {
        return _transacted;
    }

    /**
     * Returns the acknowledgement mode. This is only applicable for
     * non-transacted sessions.
     *
     * @return the session acknowledgement mode
     */
    public int getAcknowledgeMode() {
        return _acknowledgeMode;
    }

    /**
     * Determines if this acknowledgement mode is equal to another object
     *
     * @param obj the object to compare
     * @return <code>true</code> if equal, otherwise <code>false</code>
     */
    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj instanceof AckType) {
            AckType type = (AckType) obj;
            if (_transacted == type._transacted
                && _acknowledgeMode == type._acknowledgeMode) {
                equal = true;
            }
        }
        return equal;
    }

    /**
     * Returns a hash code value for the object
     *
     * @return a hash code value for the object
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Returns a string representation of the acknowledgement type
     *
     * @return a string representation of this
     */
    @Override
    public String toString() {
        String result = null;
        if (_transacted) {
            result = "TRANSACTED";
        } else {
            switch (_acknowledgeMode) {
                case Session.AUTO_ACKNOWLEDGE:
                    result = "AUTO_ACKNOWLEDGE";
                    break;
                case Session.CLIENT_ACKNOWLEDGE:
                    result = "CLIENT_ACKNOWLEDGE";
                    break;
                default:
                    result = "DUPS_OK_ACKNOWLEDGE";
            }
        }
        return result;
    }

    /**
     * Helper to convert a stringified acknowledgement type to a
     * <code>AckType</code> instance
     *
     * @param type the acknowledgement type
     * @return an instance corresponding to <code>type</code>
     */
    public static AckType fromString(String type) {
        AckType result = null;
        if (type.equalsIgnoreCase("AUTO_ACKNOWLEDGE")) {
            result = AUTO_ACKNOWLEDGE;
        } else if (type.equalsIgnoreCase("CLIENT_ACKNOWLEDGE")) {
            result = CLIENT_ACKNOWLEDGE;
        } else if (type.equalsIgnoreCase("DUPS_OK_ACKNOWLEDGE")) {
            result = DUPS_OK_ACKNOWLEDGE;
        } else if (type.equalsIgnoreCase("TRANSACTED")) {
            result = TRANSACTED;
        } else {
            throw new IllegalArgumentException("Invalid ack type: " + type);
        }
        return result;
    }

    static {
        TRANSACTED = new AckType();
        AUTO_ACKNOWLEDGE = new AckType(Session.AUTO_ACKNOWLEDGE);
        CLIENT_ACKNOWLEDGE = new AckType(Session.CLIENT_ACKNOWLEDGE);
        DUPS_OK_ACKNOWLEDGE = new AckType(Session.DUPS_OK_ACKNOWLEDGE);
    }

}
