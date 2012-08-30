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
 * $Id: ConnectionFactoryTypes.java,v 1.5 2005/06/16 08:14:00 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnectionFactory;


/**
 * Helper class used to indicate what JMS connection factory types to run a
 * particular test case against
 *
 * @version     $Revision: 1.5 $ $Date: 2005/06/16 08:14:00 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         ConnectionFactoryTestCase
 * @see         ProviderTestRunner
 */
@SuppressWarnings("rawtypes")
public final class ConnectionFactoryTypes {

    /**
     * Queue connection factory
     */
    public static final ConnectionFactoryTypes QUEUE;

    /**
     * Topic connection factory
     */
    public static final ConnectionFactoryTypes TOPIC;


    /**
     * All queue connection factory types
     */
    public static final ConnectionFactoryTypes ALL_QUEUE;

    /**
     * All topic connection factory types
     */
    public static final ConnectionFactoryTypes ALL_TOPIC;

    /**
     * All connection factory types
     */
    public static final ConnectionFactoryTypes ALL;


    /**
     * The available connection factory types.
     */
    public static final Class[] TYPES = {
        QueueConnectionFactory.class, TopicConnectionFactory.class};

    /**
     * The connection factory types to run the test case against
     */
    private final Class[] _types;

    /**
     * Construct a new instance to test against a single factory type
     *
     * @param type the connection factory type
     */
    private ConnectionFactoryTypes(Class<?> type) {
        this(new Class[]{type});
    }

    /**
     * Construct a new instance to test against a set of factory types
     *
     * @param types a list of connection factory types
     */
    private ConnectionFactoryTypes(Class[] types) {
        _types = types;
    }

    /**
     * Return the list of connection factory types to test against
     *
     * @return the list of connection factory types to test against
     */
    public Class[] getTypes() {
        return _types;
    }

    /**
     * Returns a stringified version of this, for debugging purposes
     *
     * @return a stringified version of this
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < _types.length; ++i) {
            if (i >  0) {
                result.append(",");
            }
            result.append(_types[i].getName());
        }
        return result.toString();
    }

    /**
     * Return a count of the connection factory types
     *
     * @return the number of connection factory types
     */
    public int count() {
        return _types.length;
    }

    /**
     * Helper to convert a factory name to a
     * <code>ConnectionFactoryTypes</code> instance
     *
     * @param name the factory name
     * @return an instance corresponding to <code>name</code>,
     * or <code>null</code> if <code>name</code> is invalid
     */
    public static ConnectionFactoryTypes fromString(String name) {
        ConnectionFactoryTypes result = null;
        if ("all".equalsIgnoreCase(name)) {
            result = ConnectionFactoryTypes.ALL;
        } else {
            name = "javax.jms." + name;
            Class[] types = ALL.getTypes();
            for (int i = 0; i < types.length; ++i) {
                if (types[i].getName().equals(name)) {
                    result = new ConnectionFactoryTypes(types[i]);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Helper to convert a set of factory names to a
     * <code>ConnectionFactoryTypes</code> instance
     *
     * @param names the factory names
     * @return an instance corresponding to <code>names</code>,
     * or <code>null</code> if <code>names</code> is invalid
     */
    public static ConnectionFactoryTypes fromString(String[] names) {
        ConnectionFactoryTypes result = null;
        Class[] classes = new Class[names.length];
        for (int i = 0; i < names.length; ++i) {
            if ("all".equalsIgnoreCase(names[i])) {
                result = ALL;
                break;
            } else {
                Class[] types = ALL.getTypes();
                String name = "javax.jms." + names[i];
                for (int j = 0; j < types.length; ++j) {
                    if (types[j].getName().equals(name)) {
                        classes[i] = types[j];
                        break;
                    }
                }
                if (classes[i] == null) {
                    throw new IllegalArgumentException(
                        "Invalid factory: " + names[i]);
                }
            }
        }
        if (result == null) {
            result = new ConnectionFactoryTypes(classes);
        }
        return result;
    }

    static {
        QUEUE = new ConnectionFactoryTypes(new Class[]{
            QueueConnectionFactory.class});
        TOPIC = new ConnectionFactoryTypes(new Class[]{
            TopicConnectionFactory.class});

        ALL_QUEUE = new ConnectionFactoryTypes(new Class[]{
            QueueConnectionFactory.class, XAQueueConnectionFactory.class});
            //QueueConnectionFactory.class});
        ALL_TOPIC = new ConnectionFactoryTypes(new Class[]{
            TopicConnectionFactory.class, XATopicConnectionFactory.class});
            //TopicConnectionFactory.class});

        ALL = new ConnectionFactoryTypes(new Class[]{
            QueueConnectionFactory.class, TopicConnectionFactory.class, XATopicConnectionFactory.class, XAQueueConnectionFactory.class
            //QueueConnectionFactory.class, TopicConnectionFactory.class
            });
    }

}
