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
 * $Id: MessagePropertyComparer.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.Message;


/**
 * A helper class for comparing the properties of two messages.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class MessagePropertyComparer implements MessageComparer {

    /**
     * If true, indicates that messages may contain provider specific
     * properties.
     * Note that this does not include JMSXGroupID or JMSXGroupSeq properties,
     * which may only be set by the client
     */
    private final boolean _provider;

    /**
     * Construct a new instance
     *
     * @param provider if true, either message may contain provider properties
     * i.e those prefixed with JMSX or JMS_. These will be ignored.
     * <br>
     * Note that this does not include JMSXGroupID or JMSXGroupSeq properties,
     * which may only be set by the client
     */
    public MessagePropertyComparer(boolean provider) {
        _provider = provider;
    }

    /**
     * Compare two message's properties
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>true</code> if message1 and message2 are equal
     * @throws Exception for any error
     */
    @Override
    public boolean compare(Message message1, Message message2)
        throws Exception {
        int count1 = 0; // maintains a count of the elements in each message
        int count2 = 0; // in the unlikely event than a provider allows
                        // duplicate map names
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        Enumeration<?> iter1 = message1.getPropertyNames();
        Enumeration<?> iter2 = message2.getPropertyNames();
        while (iter1.hasMoreElements()) {
            String name = (String) iter1.nextElement();
            if (!ignore(name)) {
                map1.put(name, message1.getObjectProperty(name));
                ++count1;
            }
        }
        while (iter2.hasMoreElements()) {
            String name = (String) iter2.nextElement();
            if (!ignore(name)) {
                map2.put(name, message2.getObjectProperty(name));
                ++count2;
            }
        }
        return (count1 == count2) ? map1.equals(map2) : false;
    }

    /**
     * Determines if a property should be ignored
     *
     * @param name the name of the property
     * @return <code>true</code> if the property should be ignored
     */
    private boolean ignore(String name) {
        boolean result = false;
        if (_provider) {
            if ((name.startsWith("JMSX") && !(name.equals("JMSXGroupID")
                                              || name.equals("JMSXGroupSeq")))
                || name.startsWith("JMS_")) {
                result = true;
            }
        }
        return result;
    }

}
