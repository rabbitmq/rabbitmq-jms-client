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
 * $Id: ServerAdapter.java,v 1.2 2003/07/29 06:36:42 tanderson Exp $
 */
package org.exolab.jmscts.openjms;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.exolab.jms.config.ConfigurationManager;
import org.exolab.jms.config.ConnectionFactories;
import org.exolab.jms.config.Connector;

import org.exolab.jmscts.core.TestProperties;


/**
 * Base class adapter for the available OpenJMS providers
 *
 * @version     $Revision: 1.2 $ $Date: 2003/07/29 06:36:42 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public abstract class ServerAdapter {

    /**
     * The configuration file path
     */
    private String _path;

    /**
     * The provider administrator
     */
    private OpenJMSAdministrator _admin;


    /**
     * Construct an instance with the server's configuration file path
     *
     * @param path the configuration file path
     * @param admin the provider administrator
     */
    public ServerAdapter(String path, OpenJMSAdministrator admin) {
        _path = path;
        _admin = admin;
    }

    /**
     * Start the server
     *
     * @throws JMSException if the server cannot be started
     */
    public abstract void start() throws JMSException;

    /**
     * Stop the server
     *
     * @throws JMSException if the server cannot be stopped
     */
    public void stop() throws JMSException {
        _admin.getAdmin().stopServer();
    }

    /**
     * Return the configuration file path
     */
    public String getConfigPath() {
        return _path;
    }

    /**
     * Attempt to connect to the server
     *
     * @param retries the number of times to attempt connection, before giving
     * up
     * @param sleep the interval between retries, in milliseconds
     * @throws JMSException if connection cannot be made in the required time
     */
    protected void connect(int retries, long sleep) throws JMSException {
        String user = TestProperties.getString("valid.username", null);
        String password = TestProperties.getString("valid.password", null);

        Connector connector = ConfigurationManager.getConnector();
        ConnectionFactories factories = connector.getConnectionFactories();
        String factoryName = factories.getTopicConnectionFactory(0).getName();

        for (int count = 0; count < retries && !terminated(); ++count) {
            try {
                TopicConnectionFactory factory = (TopicConnectionFactory) 
                    _admin.lookup(factoryName);
                TopicConnection connection = factory.createTopicConnection(
                    user, password);
                connection.close();
               break;
            } catch (Exception exception) {
                if (count == retries - 1) {
                    if (exception instanceof JMSException) {
                        throw (JMSException) exception;
                    } else {
                        JMSException error = new JMSException(
                            exception.getMessage());
                        error.setLinkedException(exception);
                        throw error;
                    }
                }
            }
            try {
                Thread.currentThread().sleep(sleep);
            } catch (InterruptedException ignore) {
            }
        }
        if (terminated()) {
            throw new JMSException("Couldn't connect to server, server has " +
                                   "terminated");
        }
    }

    /**
     * Returns true if the server has terminated. This is used by 
     * {@link #connect} to abort connection attempts if the server is no
     * longer running. The default implementation always returns false
     *
     * @return false
     */
    protected boolean terminated() {
        return false;
    }

} //-- ServerAdapter
