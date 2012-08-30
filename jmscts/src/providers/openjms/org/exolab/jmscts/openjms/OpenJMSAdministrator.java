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
 *    please contact jima@intalio.com.
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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: OpenJMSAdministrator.java,v 1.4 2005/06/16 08:03:01 tanderson Exp $
 */
package org.exolab.jmscts.openjms;

import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Topic;

import org.exolab.jms.administration.AdminConnectionFactory;
import org.exolab.jms.administration.JmsAdminServerIfc;
import org.exolab.jms.config.AdminConfiguration;
import org.exolab.jms.config.ConfigHelper;
import org.exolab.jms.config.Configuration;
import org.exolab.jms.config.ConfigurationManager;
import org.exolab.jms.config.ConnectionFactories;
import org.exolab.jms.config.Property;
import org.exolab.jms.config.types.SchemeType;

import org.exolab.jmscts.core.TestProperties;
import org.exolab.jmscts.provider.Administrator;


/**
 * This class provides methods for obtaining and manipulating administered 
 * objects managed by OpenJMS.<br>
 * {@link ConfigurationManager} must be initialised prior to calling any 
 * methods.
 *
 * @version     $Revision: 1.4 $ $Date: 2005/06/16 08:03:01 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         OpenJMSProvider
 * @see         ConfigurationManager
 */
class OpenJMSAdministrator implements Administrator {

    /**
     * The cached initial context.
     */
    private Context _context = null;

    /**
     * The cached admin connection.
     */
    private JmsAdminServerIfc _admin = null;


    /**
     * Returns the name of the QueueConnectionFactory bound in JNDI.
     *
     * @return the default QueueConnectionFactory name
     */
    public String getQueueConnectionFactory() {
        String result = null;
        ConnectionFactories factories = 
            ConfigurationManager.getConnector().getConnectionFactories();
        if (factories.getQueueConnectionFactoryCount() > 0) {
            result = factories.getQueueConnectionFactory(0).getName();
        }
        return result;
    }

    /**
     * Returns the name of the TopicConnectionFactory bound in JNDI.
     *
     * @return the default TopicConnectionFactory name
     */
    public String getTopicConnectionFactory() {
        String result = null;
        ConnectionFactories factories = 
            ConfigurationManager.getConnector().getConnectionFactories();
        if (factories.getTopicConnectionFactoryCount() > 0) {
            result = factories.getTopicConnectionFactory(0).getName();
        }
        return result;
    }

    /**
     * Returns the name of the XAQueueConnectionFactory bound in JNDI.
     *
     * @return the default XAQueueConnectionFactory name
     */
    public String getXAQueueConnectionFactory() {
        String result = null;
        ConnectionFactories factories = 
            ConfigurationManager.getConnector().getConnectionFactories();
        if (factories.getXAQueueConnectionFactoryCount() > 0) {
            result = factories.getXAQueueConnectionFactory(0).getName();
        }
        return result;
    }

    /**
     * Returns the name of the XATopicConnectionFactory bound in JNDI.
     *
     * @return the default XATopicConnectionFactory name
     */
    public String getXATopicConnectionFactory() {
        String result = null;
        ConnectionFactories factories = 
            ConfigurationManager.getConnector().getConnectionFactories();
        if (factories.getXATopicConnectionFactoryCount() > 0) {
            result = factories.getXATopicConnectionFactory(0).getName();
        }
        return result;
    }
    
    /**
     * Look up the named administered object in JNDI.
     *
     * @param name the name that the administered object is bound to
     * @return the administered object bound to name
     * @throws NamingException if the object is not bound, or the lookup fails
     */
    public Object lookup(String name) throws NamingException {
        return getContext().lookup(name);
    }

    /**
     * Create an administered destination.
     *
     * @param name the destination name
     * @param queue if true, create a queue, else create a topic
     * @throws JMSException if the destination cannot be created
     */
    public void createDestination(String name, boolean queue)
        throws JMSException {
        
        if (!getAdmin().addDestination(name, new Boolean(queue))) {
            throw new JMSException("Failed to add destination=" + name);
        }
    }

    /**
     * Destroy an administered destination.
     *
     * @param name the destination name
     * @throws JMSException if the destination cannot be destroyed
     */
    public void destroyDestination(String name)
        throws JMSException {

        try {
            Destination destination = (Destination) getContext().lookup(name);
            JmsAdminServerIfc admin = getAdmin();
            if (destination instanceof Topic) {
                // remove all subscriptions first
                Vector list = admin.getDurableConsumers(name);
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    admin.removeDurableConsumer((String) iter.next());
                }
            }
            if (!getAdmin().removeDestination(name)) {
                throw new JMSException("Failed to destroy destination=" + 
                                       name);
            }
        } catch (JMSException exception) {
            throw exception;
        } catch (Exception exception) {
            JMSException error = new JMSException(exception.getMessage());
            error.setLinkedException(exception);
            throw error;
        }
    }

    /**
     * Returns true if an administered destination exists.
     *
     * @param name the destination name
     * @throws JMSException for any internal JMS provider error
     */
    public boolean destinationExists(String name)
        throws JMSException {

        boolean exists = false;
        try {
            getContext().lookup(name);
            exists = true;
        } catch (NameNotFoundException ignore) {
        } catch (Exception exception) {
            JMSException error = new JMSException(exception.getMessage());
            error.setLinkedException(exception);
            throw error;
        }
        return exists;
    }

    /**
     * Return the OpenJMS administration interface specified by 
     * {@link ConfigurationManager}.
     *
     * @return the OpenJMS administration interface
     * @throws JMSException if the admin connection cannot be created
     */ 
    public synchronized JmsAdminServerIfc getAdmin() throws JMSException {
        if (_admin == null) {

            String user = TestProperties.getString("valid.username", null);
            String password = TestProperties.getString("valid.password", null);

            Configuration config = ConfigurationManager.getConfig();
            SchemeType scheme = 
                ConfigurationManager.getConnector().getScheme();
            String url = ConfigHelper.getAdminURL(scheme, config);
            try {
                _admin = AdminConnectionFactory.create(url, user, password);
            } catch (MalformedURLException exception) {
                throw new JMSException(exception.getMessage());
            }
        }
        return _admin;
    }

    /**
     * Returns the JNDI context used to look up administered objects
     *
     * @return the JNDI context under which administered objects are bound
     * @throws NamingException if the context cannot be obtained
     */
    protected synchronized Context getContext() throws NamingException {
        if (_context == null) {
            String user = TestProperties.getString("valid.username", null);
            String password = TestProperties.getString("valid.password", null);
            Configuration config = ConfigurationManager.getConfig();
            Property[] properties = config.getJndiConfiguration().getProperty();

            ClassLoader loader =
                Thread.currentThread().getContextClassLoader();

            try {
                // need to set the class loader in order for JNDI to load 
                // the factory
                Thread.currentThread().setContextClassLoader(
                    getClass().getClassLoader());

                Hashtable env = new Hashtable();
                for (int i = 0; i < properties.length; ++i) {
                    Property property = properties[i];
                    env.put(property.getName(), property.getValue());
                }
                if (user != null) {
                    env.put(Context.SECURITY_PRINCIPAL, user);
                    env.put(Context.SECURITY_CREDENTIALS, password);
                }
                _context = new InitialContext(env);
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        }
        return _context;
    }

}
