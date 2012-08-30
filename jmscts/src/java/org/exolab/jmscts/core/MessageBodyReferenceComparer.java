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
 * $Id: MessageBodyReferenceComparer.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * A helper class for comparing if the body of two messages refer to the
 * same object(s).
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class MessageBodyReferenceComparer extends AbstractMessageComparer {

    /**
     * Compare two BytesMessage instances. It is impossible to tell via
     * the BytesMessage API if the message bodies refer to the same byte
     * array, so this implementation always returns <code>false</code>
     *
     * @param message1 the message to compare
     * @param message2 the message to compare
     * @return <code>false</code> when invoked
     */
    @Override
    public boolean compareBytesMessages(BytesMessage message1,
                                        BytesMessage message2) {
        return false;
    }

    /**
     * Compare two MapMessage instances
     *
     * @param message1 the message to compare
     * @param message2 the message to compare
     * @return <code>true</code> if the messages bodies contain references
     * to the same objects
     * @throws Exception for any error
     */
    @Override
    public boolean compareMapMessages(MapMessage message1,
                                      MapMessage message2) throws Exception {
        boolean equal = false;
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        Enumeration<?> iter1 = message1.getMapNames();
        Enumeration<?> iter2 = message2.getMapNames();
        while (iter1.hasMoreElements()) {
            String name = (String) iter1.nextElement();
            map1.put(name, message1.getObject(name));
        }
        while (iter2.hasMoreElements()) {
            String name = (String) iter2.nextElement();
            map2.put(name, message2.getObject(name));
        }
        map1.keySet().toArray();
        Object[] set2 = map2.keySet().toArray();
        Iterator<String> setIter = map1.keySet().iterator();
        while (!equal && setIter.hasNext()) {
            String name = setIter.next();
            if (map2.keySet().contains(name)) {
                for (int i = 0; i < set2.length; ++i) {
                    // check if the keys are identical
                    if (name.equals(set2[i]) && name == set2[i]) {
                        equal = true;
                        break;
                    }
                }
                if (!equal) {
                    // check if the values are identical
                    if (map1.get(name) == map2.get(name)) {
                        equal = true;
                    }
                }
            }
        }

        return equal;
    }

    /**
     * Compare two ObjectMessage instances
     *
     * @param message1 the message to compare
     * @param message2 the message to compare
     * @return <code>true</code> if the messages bodies contain references
     * to the same objects
     * @throws Exception for any error
     */
    @Override
    public boolean compareObjectMessages(ObjectMessage message1,
                                         ObjectMessage message2)
        throws Exception {
        boolean equal = false;
        Object value1 = message1.getObject();
        Object value2 = message2.getObject();
        if (value1 != null) {
            equal = (value1 == value2);
        }
        return equal;
    }

    /**
     * Compare two StreamMessage instances
     *
     * @param message1 the message to compare
     * @param message2 the message to compare
     * @return <code>true</code> if the messages bodies contain references
     * to the same objects
     * @throws Exception for any error
     */
    @Override
    public boolean compareStreamMessages(StreamMessage message1,
                                         StreamMessage message2)
        throws Exception {
        boolean equal = false;
        Object value1 = message1.readObject();
        Object value2 = message2.readObject();
        if (value1 != null) {
            equal = (value1 == value2);
        }
        return equal;
    }

    /**
     * Compare two TextMessage instances
     *
     * @param message1 the message to compare
     * @param message2 the message to compare
     * @return <code>true</code> if the messages bodies contain references
     * to the same objects
     * @throws Exception for any error
     */
    @Override
    public boolean compareTextMessages(TextMessage message1,
                                       TextMessage message2) throws Exception {
        boolean equal = false;
        String value1 = message1.getText();
        String value2 = message2.getText();
        if (value1 != null) {
            equal = (value1 == value2);
        }

        return equal;
    }

}
