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
 * $Id: MessageTypes.java,v 1.5 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;


/**
 * Helper class used to indicate what JMS message types to run a particular
 * test case against
 *
 * @version     $Revision: 1.5 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessageTestCase
 * @see         MessageTestRunner
 */
@SuppressWarnings("rawtypes")
public final class MessageTypes {

    /**
     * The 'message' message type
     */
    public static final MessageTypes MESSAGE;

    /**
     * The bytes message type
     */
    public static final MessageTypes BYTES;

    /**
     * The map message type
     */
    public static final MessageTypes MAP;

    /**
     * The object message type
     */
    public static final MessageTypes OBJECT;

    /**
     * The stream message type
     */
    public static final MessageTypes STREAM;

    /**
     * The text message type
     */
    public static final MessageTypes TEXT;

    /**
     * All message types
     */
    public static final MessageTypes ALL;

    /**
     * The messages types to run the test case against
     */
    private final Class[] _types;

    /**
     * The available message types.
     * NOTE: Message.class *must* be the last element in the list.
     */
    private static final Class[] MESSAGE_TYPES = {
        BytesMessage.class, MapMessage.class, ObjectMessage.class,
        StreamMessage.class, TextMessage.class, Message.class};


    /**
     * Construct a new instance to test against a single message type
     *
     * @param type the JMS message type
     */
    private MessageTypes(Class<?> type) {
        Class[] types = new Class[]{type};
        _types = types;
    }

    /**
     * Construct a new instance to test against a set of message types
     *
     * @param types a list of JMS message types
     */
    private MessageTypes(Class[] types) {
        _types = types;
    }

    /**
     * Return the list of message types to test against
     *
     * @return the list of message types to test against
     */
    public Class[] getTypes() {
        return _types;
    }

    /**
     * Helper to return the type that a particular message implements
     *
     * @param message the message
     * @return the type of the message
     */
    @SuppressWarnings("unchecked")
    public static Class<?> getType(Message message) {
        Class<?> result = null;
        for (int i = 0; i < MESSAGE_TYPES.length; ++i) {
            if (MESSAGE_TYPES[i].isAssignableFrom(message.getClass())) {
                result = MESSAGE_TYPES[i];
                break;
            }
        }
        return result;
    }

    /**
     * Helper to convert a message name to a <code>MessageTypes</code>
     * instance
     *
     * @param name the message name
     * @return a MessageTypes instance corresponding to <code>name</code>,
     * or <code>null</code> if <code>name</code> is invalid
     */
    public static MessageTypes fromString(String name) {
        MessageTypes result = null;
        if ("all".equalsIgnoreCase(name)) {
            result = MessageTypes.ALL;
        } else {
            name = "javax.jms." + name;
            for (int i = 0; i < MESSAGE_TYPES.length; ++i) {
                if (MESSAGE_TYPES[i].getName().equals(name)) {
                    result = new MessageTypes(MESSAGE_TYPES[i]);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Helper to convert a set of type strings to a <code>MessageTypes</code>
     * instance
     *
     * @param names the message names
     * @return a MessageTypes instance corresponding to <code>names</code>
     */
    public static MessageTypes fromString(String[] names) {
        MessageTypes result = null;
        Class[] types = new Class[names.length];
        for (int i = 0; i < names.length; ++i) {
            if ("all".equalsIgnoreCase(names[i])) {
                result = MessageTypes.ALL;
                break;
            } else {
                String name = "javax.jms." + names[i];
                for (int j = 0; j < MESSAGE_TYPES.length; ++j) {
                    if (MESSAGE_TYPES[j].getName().equals(name)) {
                        types[i] = MESSAGE_TYPES[j];
                        break;
                    }
                }
                if (types[i] == null) {
                    throw new IllegalArgumentException(
                        "Invalid message type: " + names[i]);
                }
            }
        }
        if (result == null) {
            result = new MessageTypes(types);
        }
        return result;
    }

    /**
     * Return a count of the message types
     *
     * @return the number of message types
     */
    public int count() {
        return _types.length;
    }

    static {
        MESSAGE = new MessageTypes(Message.class);
        BYTES = new MessageTypes(BytesMessage.class);
        MAP = new MessageTypes(MapMessage.class);
        OBJECT = new MessageTypes(ObjectMessage.class);
        STREAM = new MessageTypes(StreamMessage.class);
        TEXT = new MessageTypes(TextMessage.class);
        ALL = new MessageTypes(MESSAGE_TYPES);
    }

}

