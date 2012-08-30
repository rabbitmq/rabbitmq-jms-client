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
 * $Id: EmbeddedServerAdapter.java,v 1.1 2003/05/04 14:12:40 tanderson Exp $
 */
package org.exolab.jmscts.openjms;

import javax.jms.JMSException;

import org.exolab.jms.server.EmbeddedJmsServer;


/**
 * Adapter for a JMS server located in the current JVM
 *
 * @version     $Revision: 1.1 $ $Date: 2003/05/04 14:12:40 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         org.exolab.jms.server.EmbeddedJmsServer
 */
public class EmbeddedServerAdapter extends ServerAdapter {

    /**
     * The intravm JMS server
     */
    private EmbeddedJmsServer _server = null;

    /**
     * The thread used to run the intravm server
     */
    private volatile Thread _serverThread = null;


    /**
     * Construct an instance of the adapter, with the configuration file path
     *
     * @param path the configuration file path
     * @param admin the provider administrator
     */
    public EmbeddedServerAdapter(String path, OpenJMSAdministrator admin) {
        super(path, admin);
    }

    /**
     * Start the server
     *
     * @throws JMSException if the server cannot be started
     */
    public void start() throws JMSException {

        try {
            _server = new EmbeddedJmsServer(getConfigPath());
        } catch (Exception exception) {
            JMSException error = new JMSException(exception.getMessage());
            error.setLinkedException(exception);
            throw error;
        }
        
        _serverThread = new Thread(_server);
        _serverThread.start();

        int retries = 12;
        long sleep = 5000;
        connect(retries, sleep);
    }

    /**
     * Stop the server
     *
     * @throws JMSException if the server cannot be stopped
     */
    public void stop() throws JMSException {
        super.stop();
        if (_serverThread != null) {
            boolean done = false;
            while (!done) {
                try {
                    _serverThread.join();
                    done = true;
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

} //-- EmbeddedServerAdapter
