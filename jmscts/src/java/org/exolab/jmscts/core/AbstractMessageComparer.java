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
 * $Id: AbstractMessageComparer.java,v 1.2 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * A helper class for comparing messages. This class provides no comparison
 * functionality, other than to verify that the supplied messages are of
 * the same type and not the same instance. Comparison is left to subclasses.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class AbstractMessageComparer implements MessageComparer {

    /**
     * Compare two messages. This method delegates the comparison to
     * the appropriate method if the messages are of the same type, and not
     * the same instance.
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>true</code> if message1 and message2 are equal
     * @throws Exception for any error
     */
    @Override
    public boolean compare(Message message1, Message message2)
        throws Exception {

        if (message1 == null) {
            throw new IllegalArgumentException("Argument message1 is null");
        }
        if (message2 == null) {
            throw new IllegalArgumentException("Argument message2 is null");
        }

        boolean equal = (message1 == message2);
        if (!equal) {
            Class<?> type1 = MessageTypes.getType(message1);
            Class<?> type2 = MessageTypes.getType(message2);
            if (type1 == BytesMessage.class && type2 == BytesMessage.class) {
                equal = compareBytesMessages((BytesMessage) message1,
                                             (BytesMessage) message2);
            } else if (type1 == MapMessage.class
                       && type2 == MapMessage.class) {
                equal = compareMapMessages((MapMessage) message1,
                                           (MapMessage) message2);
            } else if (type1 == ObjectMessage.class
                       && type2 == ObjectMessage.class) {
                equal = compareObjectMessages((ObjectMessage) message1,
                                              (ObjectMessage) message2);
            } else if (type1 == StreamMessage.class
                       && type2 == StreamMessage.class) {
                equal = compareStreamMessages((StreamMessage) message1,
                                              (StreamMessage) message2);
            } else if (type1 == TextMessage.class
                       && type2 == TextMessage.class) {
                equal = compareTextMessages((TextMessage) message1,
                                            (TextMessage) message2);
            } else if (type1 == Message.class && type2 == Message.class) {
                equal = compareMessages(message1, message2);
            }
        }
        return equal;
    }

    /**
     * Compare two Message instances. This method does nothing -
     * subclasses needing the functionality must implement it.
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>false</code> if invoked
     * @throws Exception for any error
     */
    public boolean compareMessages(Message message1, Message message2)
        throws Exception {
        return false;
    }

    /**
     * Compare two BytesMessage instances. This method does nothing -
     * subclasses needing the functionality must implement it.
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>false</code> if invoked
     * @throws Exception for any error
     */
    public boolean compareBytesMessages(BytesMessage message1,
                                        BytesMessage message2)
        throws Exception {
        return false;
    }

    /**
     * Compare two MapMessage instances. This method does nothing -
     * subclasses needing the functionality must implement it.
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>false</code> if invoked
     * @throws Exception for any error
     */
    public boolean compareMapMessages(MapMessage message1,
                                      MapMessage message2) throws Exception {
        return false;
    }

    /**
     * Compare two ObjectMessage instances. This method does nothing -
     * subclasses needing the functionality must implement it.
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>false</code> if invoked
     * @throws Exception for any error
     */
    public boolean compareObjectMessages(ObjectMessage message1,
                                         ObjectMessage message2)
        throws Exception {
        return false;
    }

    /**
     * Compare two StreamMessage instances. This method does nothing -
     * subclasses needing the functionality must implement it.
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>false</code> if invoked
     * @throws Exception for any error
     */
    public boolean compareStreamMessages(StreamMessage message1,
                                         StreamMessage message2)
        throws Exception {
        return false;
    }

    /**
     * Compare two TextMessage instances. This method does nothing -
     * subclasses needing the functionality must implement it.
     *
     * @param message1 one message to be tested for equality
     * @param message2 the other message to be tested for equality
     * @return <code>false</code> if invoked
     * @throws Exception for any error
     */
    public boolean compareTextMessages(TextMessage message1,
                                       TextMessage message2) throws Exception {
        return false;
    }

}
