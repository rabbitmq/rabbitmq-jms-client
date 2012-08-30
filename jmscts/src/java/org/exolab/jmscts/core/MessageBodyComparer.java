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
 * $Id: MessageBodyComparer.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * A helper class for comparing the body of two messages.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class MessageBodyComparer extends AbstractMessageComparer {

    /**
     * Compare two Message instances. This is a no-op, as Message instances
     * don't have a body
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>true</code> if the messages bodies are equal
     * @throws Exception for any error
     */
    @Override
    public boolean compareMessages(Message message1, Message message2)
        throws Exception {
        return true;
    }

    /**
     * Compare two BytesMessage instances
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>true</code> if the messages bodies are equal
     * @throws Exception for any error
     */
    @Override
    public boolean compareBytesMessages(BytesMessage message1,
                                        BytesMessage message2)
        throws Exception {
        final int size = 1024;

        boolean equal = true;
        byte[] stream1 = new byte[size];
        byte[] stream2 = new byte[stream1.length];
        while (equal) {
            int read1 = message1.readBytes(stream1);
            int read2 = message2.readBytes(stream2);
            if (read1 == read2) {
                if (read1 != -1) {
                    for (int i = 0; i < read1; ++i) {
                        if (stream1[i] != stream2[i]) {
                            equal = false;
                            break;
                        }
                    }
                } else {
                    break; // end of stream
                }
            } else {
                equal = false; // mismatch in bytes read
            }
        }

        return equal;
    }

    /**
     * Compare two MapMessage instances
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>true</code> if the messages bodies are equal
     * @throws Exception for any error
     */
    @Override
    public boolean compareMapMessages(MapMessage message1,
                                      MapMessage message2) throws Exception {
        int count1 = 0; // maintains a count of the elements in each message
        int count2 = 0; // in the unlikely event than a provider allows
                        // duplicate map names
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        Enumeration<?> iter1 = message1.getMapNames();
        Enumeration<?> iter2 = message2.getMapNames();
        while (iter1.hasMoreElements()) {
            String name = (String) iter1.nextElement();
            map1.put(name, message1.getObject(name));
            ++count1;
        }
        while (iter2.hasMoreElements()) {
            String name = (String) iter2.nextElement();
            map2.put(name, message2.getObject(name));
            ++count2;
        }
        return (count1 == count2) ? map1.equals(map2) : false;
    }

    /**
     * Compare two ObjectMessage instances
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>true</code> if the messages bodies are equal
     * @throws Exception for any error
     */
    @Override
    public boolean compareObjectMessages(ObjectMessage message1,
                                         ObjectMessage message2)
        throws Exception {
        boolean equal = false;
        Object value1 = message1.getObject();
        Object value2 = message2.getObject();
        if (value1 != null && value2 != null) {
            equal = value1.equals(value2);
        } else  {
            equal = (value1 == value2);
        }

        return equal;
    }

    /**
     * Compare two StreamMessage instances
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>true</code> if the messages bodies are equal
     * @throws Exception for any error
     */
    @Override
    public boolean compareStreamMessages(StreamMessage message1,
                                         StreamMessage message2)
        throws Exception {
        boolean equal = true;
        while (equal) {
            Object object1 = null;
            Object object2 = null;
            int eof = 0; // count of MessageEOFException generated
            try {
                object1 = message1.readObject();
            } catch (MessageEOFException ignore) {
                ++eof;
            }
            try {
                object2 = message2.readObject();
            } catch (MessageEOFException ignore) {
                ++eof;
            }
            if (eof == 1) {
                equal = false;
            } else if (eof == 2) {
                break;
            } else {
                if (object1 instanceof byte[] && object2 instanceof byte[]) {
                    equal = Arrays.equals((byte[]) object1, (byte[]) object2);
                } else if (object1 != null) {
                    equal = object1.equals(object2);
                } else if (object2 != null) {
                    equal = false;
                }
            }
        }

        return equal;
    }

    /**
     * Compare two TextMessage instances
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>true</code> if the messages bodies are equal
     * @throws Exception for any error
     */
    @Override
    public boolean compareTextMessages(TextMessage message1,
                                       TextMessage message2) throws Exception {
        boolean equal = false;
        String value1 = message1.getText();
        String value2 = message2.getText();
        if (value1 != null && value2 != null) {
            equal = value1.equals(value2);
        } else  {
            equal = (value1 == value2);
        }

        return equal;
    }

}
