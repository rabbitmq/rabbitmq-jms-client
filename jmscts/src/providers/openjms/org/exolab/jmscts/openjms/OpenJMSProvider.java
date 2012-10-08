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
 * $Id: OpenJMSProvider.java,v 1.1 2003/05/04 14:12:40 tanderson Exp $
 */
package org.exolab.jmscts.openjms;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

import org.exolab.jms.config.AdminConfiguration;
import org.exolab.jms.config.Configuration;
import org.exolab.jms.config.ConfigurationManager;
import org.exolab.jms.config.Connector;
import org.exolab.jms.config.types.SchemeType;

import org.exolab.jmscts.provider.Administrator;
import org.exolab.jmscts.provider.Provider;


/**
 * This class enables OpenJMS test cases to be run against different OpenJMS 
 * configurations.
 * <p>
 * The {@link #setPath} method must be invoked prior to {@link #initialise}
 * being invoked. 
 *
 * @version     $Revision: 1.1 $ $Date: 2003/05/04 14:12:40 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class OpenJMSProvider implements Provider {

    /**
     * The configuration file path
     */
    private String _path = null;

    /**
     * The server adapter, used for starting and stopping the OpenJMS server
     */
    private ServerAdapter _server = null;
    
    /**
     * The administration interface
     */
    private OpenJMSAdministrator _admin = new OpenJMSAdministrator();

    /**
     * The logger 
     */
    private static final Logger _category = 
        Logger.getLogger(OpenJMSProvider.class);

    /**
     * Construct an instance of the interface to the OpenJMS provider
     */
    public OpenJMSProvider() {
    }

    /**
     * Sets the configuration file path. This must be done prior to invoking
     * {@link #initialise}
     *
     * @param path the configuration file path
     */
    public void setPath(String path) {
        _path = path;
    }

    /**
     * Returns the configuration file path.
     *
     * @return the configuration file path, or null, if it is not set
     */
    public String getPath() {
        return _path;
    }

    /**
     * Reads the configuration file specified by the arguments passed at 
     * construction and optionally starts the OpenJMS provider.
     *
     * @param start if true, starts the OpenJMS provider
     * @throws JMSException if the configuration file is invalid, or the
     * OpenJMS provider cannot be started.
     */
    public void initialise(boolean start) throws JMSException {
        if (_path == null) {
            throw new JMSException("Path property not set");
        }

        Configuration config = null;

        try {
            ConfigurationManager.setConfig(_path);
            config = ConfigurationManager.getConfig();
        } catch (Exception exception) {
            _category.debug(exception, exception);
            throw new JMSException(exception.getMessage());
        }

        if (start) {
            _category.debug("Starting OpenJMS server");
            Connector connector = ConfigurationManager.getConnector(
                SchemeType.EMBEDDED);
            if (connector != null) {
                // if the embedded connector is configured, start an 
                // embedded server
                _server = new EmbeddedServerAdapter(_path, _admin);
            } else {
                _server = new RemoteServerAdapter(_path, _admin);
            }
            _server.start();
        }
    }

    /**
     * Returns the administration interface 
     */
    public Administrator getAdministrator() {
        return _admin;
    }

    /**
     * Release any resources used by the provider
     *
     * @param stop if true, stops the OpenJMS provider
     * @throws JMSException if the provider cannot be stopped
     */
    public void cleanup(boolean stop) throws JMSException {
        if (stop && _server != null) {
            _category.debug("Shutting down OpenJMS server");
            _server.stop();
        }
    }

} //-- OpenJMSProvider
