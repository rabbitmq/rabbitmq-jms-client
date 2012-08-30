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
 * $Id: AckTypes.java,v 1.4 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;


/**
 * Helper class used to indicate what message acknowledgement types to run a
 * particular test case against
 *
 * @version     $Revision: 1.4 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         AckType
 * @see         SessionTestCase
 * @see         SessionTestRunner
 */
public class AckTypes {

    /**
     * Transacted session
     */
    public static final AckType TRANSACTED = AckType.TRANSACTED;

    /**
     * Non-transacted session, session automatically acknowledges messages
     */
    public static final AckType AUTO_ACKNOWLEDGE = AckType.AUTO_ACKNOWLEDGE;

    /**
     * Non-transacted session, client ackwnowledges messages
     */
    public static final AckType CLIENT_ACKNOWLEDGE =
        AckType.CLIENT_ACKNOWLEDGE;

    /**
     * Non-transacted session, messages are lazily acknowledged
     */
    public static final AckType DUPS_OK_ACKNOWLEDGE =
        AckType.DUPS_OK_ACKNOWLEDGE;

    /**
     * All session message acknowledgement types
     */
    public static final AckTypes ALL;

    /**
     * Non transacted sessions
     */
    public static final AckTypes NON_TRANSACTIONAL;

    /**
     * Transacted sessions
     */
    public static final AckTypes TRANSACTIONAL;

    /**
     * The message acknowledgement types to test against
     */
    private AckType[] _types;

    /**
     * Construct a new instance to test against a set of message
     * acknowledgement types
     *
     * @param types a list of message acknowledgement types
     */
    public AckTypes(AckType[] types) {
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
     * Construct a new instance to test against a single acknowledgement type
     *
     * @param type the message acknowledgement type
     */
    public AckTypes(AckType type) {
        if (type == null) {
            throw new IllegalArgumentException("Argument type is null");
        }
        _types = new AckType[]{type};
    }

    /**
     * Returns the list of message acknowledgement types to test against
     *
     * @return the list of message acknowledgement types to test against
     */
    public AckType[] getTypes() {
        return _types;
    }

    /**
     * Helper to convert a set of stringified acknowledgement types to a
     * <code>AckTypes</code> instance
     *
     * @param types the acknowledgement types
     * @return an instance corresponding to <code>types</code>,
     * or <code>null</code> if <code>types</code> is invalid
     */
    public static AckTypes fromString(String[] types) {
        AckTypes result = null;
        AckType[] set = new AckType[types.length];
        for (int i = 0; i < types.length; ++i) {
            if ("all".equalsIgnoreCase(types[i])) {
                result = ALL;
                break;
            } else {
                set[i] = AckType.fromString(types[i]);
            }
        }
        if (result == null) {
            result = new AckTypes(set);
        }
        return result;
    }

    static {
        ALL = new AckTypes(new AckType[]{
            TRANSACTED, AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE,
            DUPS_OK_ACKNOWLEDGE});
        NON_TRANSACTIONAL = new AckTypes(new AckType[]{AUTO_ACKNOWLEDGE,
                                                       CLIENT_ACKNOWLEDGE,
                                                       DUPS_OK_ACKNOWLEDGE});
        TRANSACTIONAL = new AckTypes(TRANSACTED);
    }

}

