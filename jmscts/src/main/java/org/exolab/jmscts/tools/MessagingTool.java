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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: MessagingTool.java,v 1.2 2004/02/03 21:52:12 tanderson Exp $
 */
package org.exolab.jmscts.tools;

import java.util.Enumeration;
import java.util.Iterator;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnectionFactory;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestStatistics;
import org.exolab.jmscts.provider.Administrator;
import org.exolab.jmscts.provider.Configuration;
import org.exolab.jmscts.provider.ProviderLoader;


/**
 * Messaging tool helper
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 21:52:12 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public abstract class MessagingTool {

    /**
     * The configuration path
     */
    private String _path;

    /**
     * The connection factory name
     */
    private String _factoryName;

    /**
     * The destination name
     */
    private String _destName;

    /**
     * The provider
     */
    private ProviderLoader _provider;

    /**
     * The provider administrator
     */
    private Administrator _admin;

    /**
     * The connection factory test context
     */
    private TestContext _context;

    /**
     * The destination to send/receive/browse messages
     */
    private Destination _destination;

    /**
     * The number of messages to process
     */
    private int _count;

    /**
     * If <code>true</code>, log message properties
     */
    private boolean _verbose = false;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(MessagingTool.class);


    /**
     * Set the configuration
     *
     * @param path the path to the provider configuration
     */
    public void setConfig(String path) {
        _path = path;
    }

    /**
     * Set the connection factory name
     *
     * @param name the connection factory name
     */
    public void setConnectionFactory(String name) {
        _factoryName = name;
    }

    /**
     * Set the destination name
     *
     * @param name the destination name
     */
    public void setDestination(String name) {
        _destName = name;
    }

    /**
     * Sets number of messages to process
     *
     * @param count the number of messages to process
     */
    public void setCount(int count) {
        _count = count;
    }

    /**
     * Enable/disable verbose logging of messages
     *
     * @param verbose if <code>true</code>, log message properties
     */
    public void setVerbose(boolean verbose) {
        _verbose = verbose;
    }

    /**
     * Invoke the messaging tool
     *
     * @throws Exception for any error
     */
    public void invoke() throws Exception {
        initialise();
        doInvoke();
    }

    /**
     * Invoke the messaging tool
     *
     * @throws Exception for any error
     */
    protected abstract void doInvoke() throws Exception;

    /**
     * Initialise the tool
     *
     * @throws Exception for any error
     */
    protected void initialise() throws Exception {
        // load the provider configuration
        Configuration config = Configuration.read(_path);

        Iterator<?> iterator = config.getProviders().iterator();
        _provider = (ProviderLoader) iterator.next();
        _provider.getProvider().initialise(false);

        _admin = _provider.getProvider().getAdministrator();
        _destination = (Destination) _admin.lookup(_destName);

        Class<?> factoryClass = null;
        ConnectionFactory factory = null;

        if (_factoryName == null) {
            factory = getDefaultConnectionFactory(_destination);
            factoryClass = getDefaultConnectionFactoryType(_destination);
        } else {
            factory = getConnectionFactory(_factoryName);
            factoryClass = getConnectionFactoryType(factory);
        }

        TestContext rootContext = new TestContext(new TestStatistics());
        TestContext providerContext = new TestContext(rootContext, _admin);
        _context = new TestContext(providerContext, factory, factoryClass);
    }

    /**
     * Returns the connection factory test context
     *
     * @return the connection factory test context
     */
    protected TestContext getContext() {
        return _context;
    }

    /**
     * Returns the destination
     *
     * @return the destination
     */
    protected Destination getDestination() {
        return _destination;
    }

    /**
     * Returns the number of messages to process
     *
     * @return the number of messages to process
     */
    protected int getCount() {
        return _count;
    }

    /**
     * Log a message
     *
     * @param message the message to log
     */
    protected void log(Message message) {
        String messageId = null;
        String mode = null;
        new StringBuffer();
        try {
            messageId = message.getJMSMessageID();
            if (message.getJMSDeliveryMode() == DeliveryMode.PERSISTENT) {
                mode = "PERSISTENT";
            } else {
                mode = "NON_PERSISTENT";
            }
        } catch (JMSException exception) {
            log.error(exception);
        }
        System.out.println(messageId + " " + mode);
        if (_verbose) {
            try {
                Enumeration<?> iterator = message.getPropertyNames();
                while (iterator.hasMoreElements()) {
                    String name = (String) iterator.nextElement();
                    Object object = message.getObjectProperty(name);
                    System.out.println(" " + name + "=" + object);
                }
            } catch (JMSException exception) {
                log.error(exception);
            }
        }
    }

    /**
     * Returns the connection factory for the specified name
     *
     * @param name the connection factory name.
     * @return the connection factory
     * @throws NamingException if the connection factory cannot be located
     */
    private ConnectionFactory getConnectionFactory(String name)
        throws NamingException {
        return (ConnectionFactory) _admin.lookup(name);
    }

    /**
     * Returns the type of the connection factory
     *
     * @param factory the connection factory
     * @return the type of the connection factory
     */
    private Class<?> getConnectionFactoryType(ConnectionFactory factory) {
        Class<?> result;
        if (factory instanceof XAQueueConnectionFactory) {
            result = XAQueueConnectionFactory.class;
        } else if (factory instanceof QueueConnectionFactory) {
            result = QueueConnectionFactory.class;
        } else if (factory instanceof XATopicConnectionFactory) {
            result = XATopicConnectionFactory.class;
        } else if (factory instanceof TopicConnectionFactory) {
            result = TopicConnectionFactory.class;
        } else {
            throw new IllegalArgumentException(
                "Unsupported connection factory: " + factory);
        }
        return result;
    }

    /**
     * Returns the default connection factory for the supplied destination
     *
     * @param destination the destination
     * @return the default connection factory
     * @throws NamingException if the connection factory cannot be located
     */
    private ConnectionFactory getDefaultConnectionFactory(
        Destination destination) throws NamingException {

        String name;
        if (destination instanceof Queue) {
            name = _admin.getQueueConnectionFactory();
        } else if (destination instanceof Topic) {
            name = _admin.getTopicConnectionFactory();
        } else {
            throw new IllegalArgumentException("Unsupported destination: "
                                               + destination);
        }

        return getConnectionFactory(name);
    }

    /**
     * Returns the type of the default connection factory for the supplied
     * destination
     *
     * @param destination the destination
     * @return the default connection factory type
     */
    private Class<?> getDefaultConnectionFactoryType(Destination destination) {
        Class<?> result;
        if (destination instanceof Queue) {
            result = QueueConnectionFactory.class;
        } else if (destination instanceof Topic) {
            result = TopicConnectionFactory.class;
        } else {
            throw new IllegalArgumentException("Unsupported destination: "
                                               + destination);
        }
        return result;
    }
}

