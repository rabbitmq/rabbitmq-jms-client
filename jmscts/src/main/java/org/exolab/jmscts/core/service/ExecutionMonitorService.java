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
 * $Id: ExecutionMonitorService.java,v 1.2 2004/02/02 03:49:20 tanderson Exp $
 */
package org.exolab.jmscts.core.service;

import java.rmi.Naming;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import org.exolab.core.service.Service;
import org.exolab.core.service.ServiceException;

import org.exolab.jmscts.core.ExecutionListener;


/**
 * This service enables the state of a running compliance suite to be captured.
 * <p>
 * This service is dependent on the
 * {@link org.exolab.core.util.RmiRegistryService}. This must be initialised
 * prior to starting this service
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:20 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class ExecutionMonitorService extends Service {

    /** TODO */
    private static final long serialVersionUID = 1L;

    /**
     * The service name
     */
    public static final String NAME = "ExecutionMonitorService";

    /**
     * The singleton instance
     */
    private static ExecutionMonitorService _instance = null;

    /**
     * A map of application identifiers and their corresponding listeners
     */
    private HashMap<String, ArrayList<ExecutionListener>> _listeners = new HashMap<String, ArrayList<ExecutionListener>>();

    /**
     * The server to handle application notification
     */
    private ExecutionMonitorServer _server = null;

    /**
     * The name of the server bound in the RMI registry
     */
    private String _name = null;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ExecutionMonitorService.class);


    /**
     * Construct a new <code>ExecutionMonitorService</code>
     *
     * @param port the RMI registry port
     */
    protected ExecutionMonitorService(int port) {
        super(NAME);
        _name = "//localhost:" + port + "/ExecutionListener";
    }

    /**
     * Initialise the ExecutionMonitorService
     *
     * @param port the RMI registry port
     * @return the singleton instance of the service
     */
    public static synchronized ExecutionMonitorService initialise(int port)  {
        if (_instance != null) {
            throw new IllegalStateException(
                "ExecutionMonitorService has already been initialised");
        }
        _instance = new ExecutionMonitorService(port);
        return _instance;
    }

    /**
     * Returns the singleton instance of the ExecutionMonitorService
     *
     * @return the singleton instance of the ExecutionMonitorService, or null,
     * if it hasn't been initialised
     */
    public static synchronized ExecutionMonitorService instance() {
        return _instance;
    }

    /**
     * Add a listener for an application
     *
     * @param id the application id
     * @param listener the listener
     */
    public void addListener(String id, ExecutionListener listener) {
        if (id == null) {
            throw new IllegalArgumentException("Argument 'id' is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Argument 'listener' is null");
        }
        synchronized (_listeners) {
            ArrayList<ExecutionListener> listeners = _listeners.get(id);
            if (listeners == null) {
                listeners = new ArrayList<ExecutionListener>();
                _listeners.put(id, listeners);
            }
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener for an application
     *
     * @param id the application id
     * @param listener the listener
     */
    public void removeListener(String id, ExecutionListener listener) {
        if (id == null) {
            throw new IllegalArgumentException("Argument 'id' is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Argument 'listener' is null");
        }
        synchronized (_listeners) {
            ArrayList<?> listeners = _listeners.get(id);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    _listeners.remove(id);
                }
            }
        }
    }

    /**
     * Notifies all registered listeners for an application that the
     * application failed to start
     *
     * @param id the application id
     * @param throwable the reason for the failure
     */
    public void failed(String id, Throwable throwable) {
        ExecutionListener[] listeners = getListeners(id);
        if (listeners != null) {
            for (int i = 0; i < listeners.length; ++i) {
                listeners[i].failed(throwable);
            }
        } else {
            log.debug("Application=" + id + " failed to start but no "
                      + "listeners are registered", throwable);
        }
    }

    /**
     * Notifies all registered listeners for an application that the
     * application has started

     * @param id the application id
     */
    public void started(String id) {
        ExecutionListener[] listeners = getListeners(id);
        if (listeners != null) {
            for (int i = 0; i < listeners.length; ++i) {
                listeners[i].started();
            }
        } else {
            log.debug("Application=" + id + " started but no "
                      + "listeners are registered");
        }
    }

    /**
     * Notifies all registered listeners for an application that the
     * application has stopped
     *
     * @param id the application id
     * @param status the exit status of the application
     */
    public void stopped(String id, int status) {
        ExecutionListener[] listeners = getListeners(id);
        if (listeners != null) {
            for (int i = 0; i < listeners.length; ++i) {
                listeners[i].stopped(status);
            }
        } else {
            log.debug("Application=" + id + " stopped with status=" + status
                      + " but no listeners are registered");
        }
    }

    /**
     * Notifies all registered listeners for an application that the
     * application has thrown an exception just prior to termination
     *
     * @param id the application id
     * @param throwable the exception thrown by the application
     */
    public void error(String id, Throwable throwable) {
        ExecutionListener[] listeners = getListeners(id);
        if (listeners != null) {
            for (int i = 0; i < listeners.length; ++i) {
                listeners[i].failed(throwable);
            }
        } else {
            log.debug("Application=" + id + " threw exception "
                      + " but no listeners are registered");
        }
    }

    /**
     * Allocate a unique identifier to an application
     *
     * @return a unique identifier for an application
     */
    public String allocateId() {
        return new UID().toString();
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
                _server = new ExecutionMonitorServer();
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
        _listeners.clear();
        try {
            Naming.unbind(_name);
            UnicastRemoteObject.unexportObject(_server, true);

        } catch (Exception exception) {
            throw new ServiceException(exception.getMessage());
        }
    }

    /**
     * Returns all listsners for the specified id
     *
     * @param id the application id
     * @return the listsners, or <code>null</code> if none are registered
     */
    private ExecutionListener[] getListeners(String id) {
        ExecutionListener[] result = null;
        synchronized (_listeners) {
            ArrayList<?> listeners = _listeners.get(id);
            if (listeners != null) {
                result = listeners.toArray(
                    new ExecutionListener[]{});
            }
        }
        return result;
    }

}
