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
 * $Id: EmptyPropertyVerifier.java,v 1.2 2004/02/03 07:31:04 tanderson Exp $
 */
package org.exolab.jmscts.test.message.util;

import java.util.Enumeration;

import javax.jms.Message;

import org.exolab.jmscts.core.MessageVerifier;


/**
 * A helper class for verifying that a message has no properties
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 07:31:04 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessageVerifier
 */
public class EmptyPropertyVerifier implements MessageVerifier {

    /** TODO */
    private static final long serialVersionUID = 1L;
    /**
     * If true, indicates that the message may contain provider specific
     * properties.
     * Note that this does not include JMSXGroupID or JMSXGroupSeq properties,
     * which may only be set by the client
     */
    private boolean _provider = false;

    /**
     * Construct a new instance. An exception will be thrown if the message
     * contains any properties.
     */
    public EmptyPropertyVerifier() {
    }

    /**
     * Construct a new instance
     *
     * @param provider if true, the message may contain provider properties
     * i.e those prefixed with JMSX or JMS_
     * <br>
     * Note that this does not include JMSXGroupID or JMSXGroupSeq properties,
     * which may only be set by the client
     */
    public EmptyPropertyVerifier(boolean provider) {
        _provider = provider;
    }

    /**
     * Verify that a message instance has no properties
     *
     * @param message the message to verify
     * @throws Exception for any error
     */
    @Override
    public void verify(Message message) throws Exception {
        Enumeration<?> iter = message.getPropertyNames();
        if (iter.hasMoreElements() && !_provider) {
            String names = null;
            while (iter.hasMoreElements()) {
                String name = (String) iter.nextElement();
                if (names == null) {
                    names = name;
                } else {
                    names += ", " + name;
                }
            }
            throw new Exception("Expected message to contain no properties, "
                                + "but contains properties for: " + names);
        }

        while (iter.hasMoreElements()) {
            String name = (String) iter.nextElement();
            if (name.startsWith("JMSX")) {
                if (name.equals("JMSXGroupID")
                    || name.equals("JMSXGroupSeq")) {
                    throw new Exception(
                        "Message should not contain property=" + name);
                }
            } else if (!name.startsWith("JMS_")) {
                throw new Exception(
                    "Expected message to contain no properties");
            }
        }
    }

}
