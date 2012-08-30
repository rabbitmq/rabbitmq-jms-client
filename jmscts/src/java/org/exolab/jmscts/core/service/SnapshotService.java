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
 * $Id: SnapshotService.java,v 1.3 2004/02/02 03:49:20 tanderson Exp $
 */
package org.exolab.jmscts.core.service;

import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;

import org.exolab.core.service.Service;
import org.exolab.core.service.ServiceException;

import org.exolab.jmscts.core.AbstractTestRunner;


/**
 * This service enables the state of a running compliance suite to be captured.
 * <p>
 * This service is dependent on the
 * {@link org.exolab.core.util.RmiRegistryService}. This must be initialised
 * prior to starting this service
 *
 * @version     $Revision: 1.3 $ $Date: 2004/02/02 03:49:20 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class SnapshotService extends Service {

    /** TODO */
    private static final long serialVersionUID = 1L;

    /**
     * The service name
     */
    public static final String NAME = "SnapshotService";

    /**
     * The test runner
     */
    private AbstractTestRunner _runner = null;

    /**
     * The name of the server bound in the RMI registry
     */
    private String _name = null;

    /**
     * The server to handle snapshot requests
     */
    private SnapshotServer _server = null;


    /**
     * Construct a new <code>SnapshotService</code>
     *
     * @param runner the test runner
     * @param port the RMI registry port
     */
    public SnapshotService(AbstractTestRunner runner, int port) {
        super(NAME);
        if (runner == null) {
            throw new IllegalArgumentException("Argument runner is null");
        }
        _runner = runner;
        _name = "//localhost:" + port + "/Snapshot";
    }

    /**
     * Snapshot the current test suite coverage
     *
     * @param path the directory path to write the coverage report
     * @throws Exception if the report cannot be generated
     */
    public void snapshot(String path) throws Exception {
        _runner.snapshot(path);
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
                _server = new SnapshotServer(this);
                Naming.bind(_name, _server);
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
            Naming.unbind(_name);
            UnicastRemoteObject.unexportObject(_server, true);
        } catch (Exception exception) {
            throw new ServiceException(exception.getMessage());
        }
    }

}
