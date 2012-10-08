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
 * $Id: MessengerService.java,v 1.2 2004/02/02 03:49:20 tanderson Exp $
 */
package org.exolab.jmscts.core.service;

import org.apache.log4j.Logger;

import org.exolab.core.service.Service;
import org.exolab.core.service.ServiceException;
import org.exolab.core.service.ServiceState;

import org.exolab.jmscts.core.AsyncExecutor;
import org.exolab.jmscts.core.ExecutionListener;


/**
 * This class enables messaging operations to be performed in a separate
 * JVM
 * <p>
 * This service is dependent on the
 * {@link org.exolab.core.util.RmiRegistryService} and
 * {@link ExecutionMonitorService}. These must be initialised prior to
 * starting this service
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:20 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class MessengerService extends Service implements ExecutionListener {

    /** TODO */
    private static final long serialVersionUID = 1L;

    /**
     * The service name
     */
    public static final String NAME = "MessengerService";

    /**
     * The RMI registry port
     */
    private int _port = 0;

    /**
     * The executor
     */
    private AsyncExecutor _executor = null;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(MessengerService.class);


    /**
     * Construct a new <code>MessengerService</code>
     *
     * @param port the RMI registry port
     */
    public MessengerService(int port) {
        super(NAME);
        _port = port;
    }

    /**
     * Start the service
     *
     * @throws ServiceException if the service can't be started
     */
    @Override
    public synchronized void start() throws ServiceException {
        if (!getState().isRunning()) {
            try {
                String args = "-port " + _port;
                _executor = new AsyncExecutor(MessengerServer.class, args,
                                              _port);
                _executor.addListener(this);
                _executor.run();
            } catch (Exception exception) {
                throw new ServiceException(exception.getMessage());
            }
        }
        super.start();
    }

    /**
     * Stop the service
     *
     * @throws ServiceException if the service can't be stopped
     */
    @Override
    public synchronized void stop() throws ServiceException {
        super.stop();
        try {
            _executor.stop();
        } catch (Exception exception) {
            throw new ServiceException(exception.getMessage());
        }
    }

    /**
     * Event handler invoked if the application cannot be started
     *
     * @param throwable the exception thrown by the application
     */
    @Override
    public void failed(Throwable throwable) {
        log.error("MessengerServer failed with error", throwable);
        setState(ServiceState.STOPPED);
        _executor = null;
    }

    /**
     * Event handler invoked when the application starts
     */
    @Override
    public void started() {
        log.debug("MessengerServer started");
    }

    /**
     * Event handler invoked when the application stops
     *
     * @param status the exit status of the application
     */
    @Override
    public void stopped(int status) {
        log.debug("MessengerServer stopped");
        setState(ServiceState.STOPPED);
        _executor = null;
    }

    /**
     * Event handler invoked when the application throws an exception just
     * prior to termination
     *
     * @param throwable the exception thrown by the application
     */
    @Override
    public void error(Throwable throwable) {
        log.error("MessengerServer failed with error", throwable);
    }

}
