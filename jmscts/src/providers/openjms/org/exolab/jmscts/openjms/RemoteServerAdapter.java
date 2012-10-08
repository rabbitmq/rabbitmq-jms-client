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
 * $Id: RemoteServerAdapter.java,v 1.1 2003/05/04 14:12:40 tanderson Exp $
 */
package org.exolab.jmscts.openjms;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

import org.exolab.jms.administration.JmsAdminServerIfc;
import org.exolab.jms.config.AdminConfiguration;
import org.exolab.jms.config.Configuration;
import org.exolab.jms.config.ConfigurationManager;

import org.exolab.jmscts.core.Executor;


/**
 * Adapter for an OpenJMS server, running in a separate JVM.
 *
 * @version     $Revision: 1.1 $ $Date: 2003/05/04 14:12:40 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         org.exolab.jms.server.JmsServer
 */
public class RemoteServerAdapter extends ServerAdapter {

    /**
     * The helper used to run the server
     */
    private ServerRunner _server = null;

    /**
     * The logger 
     */
    private static final Logger _category = 
        Logger.getLogger(RemoteServerAdapter.class.getName());

    /**
     * Construct an instance of the adapter, with the configuration file path
     *
     * @param path the configuration file path
     * @param admin the provider administrator
     */
    public RemoteServerAdapter(String path, OpenJMSAdministrator admin) {
        super(path, admin);
    }

    /**
     * Start the server
     *
     * @throws JMSException if the server cannot be started
     */
    public void start() throws JMSException {
        final int retries = 12;
        final int sleep = 5000;

        Configuration config = ConfigurationManager.getConfig();
        AdminConfiguration adminConfig = config.getAdminConfiguration();

        String command = adminConfig.getScript() + " -config " +
            adminConfig.getConfig();

        _server = new ServerRunner(command.toString());
        _server.start();

        try {
            connect(retries, sleep);
        } catch (JMSException exception) {
            if (_server.getException() != null) {
                // exception was intercepted during startup
                JMSException error = new JMSException(
                    _server.getException().getMessage());
                error.setLinkedException(_server.getException());
                throw error;
            } else {
                throw exception;
            }
        }
    }

    /**
     * Stop the server
     *
     * @throws JMSException if the server cannot be stopped
     */
    public void stop() throws JMSException {
        super.stop();
        if (_server != null) {
            while (true) {
                try {
                    _server.join();
                    break;
                } catch (InterruptedException ignore) {
                }
            } 
            _server = null;
        }
    }

    /**
     * Helper class to run an OpenJMS server
     */
    class ServerRunner extends Thread {
        private final String _command;
        private final Executor _executor;
        private Exception _exception = null;

        public ServerRunner(String command) {
            _command = command;
            _executor = new Executor(_command);
        }

        public void run() {
            try {
                _category.debug("Starting OpenJMS server with command=" +
                                _command);
                _executor.run();
            } catch (Exception exception) {
                _category.debug("Failed to start OpenJMS server", exception);
                _exception = exception;
            } 
        }

        public Exception getException() {
            return _exception;
        }

    } //-- ServerRunner

} //-- RemoteServerAdapter
