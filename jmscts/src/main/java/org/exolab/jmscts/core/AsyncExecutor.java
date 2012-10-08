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
 * $Id: AsyncExecutor.java,v 1.4 2004/02/03 21:52:06 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.io.OutputStream;

import org.apache.log4j.Logger;

import org.exolab.jmscts.core.service.ExecutionMonitorService;


/**
 * This class enables java applications to be executed asynchronously,
 * with the output being captured, or echoed to System.out and System.err
 * Clients can register a listener to be notified when the application
 * starts and stops.
 *
 * @version     $Revision: 1.4 $ $Date: 2004/02/03 21:52:06 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         Executor
 * @see         ExecutionListener
 */
public class AsyncExecutor {

    /**
     * The command to execute
     */
    private String _command = null;

    /**
     * The command executor
     */
    private Executor _executor = null;

    /**
     * The identifier for this executor
     */
    private String _id = null;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(AsyncExecutor.class.getName());


    /**
     * Constructor to execute a command, with output going to System.out and
     * System.err
     *
     * @param application the class of the application to execute
     * @param arguments any arguments to pass to the application
     * @param port the RMI registry port
     */
    public AsyncExecutor(Class<?> application, String arguments, int port) {
        this(application, arguments, System.out, System.err, port);
    }

    /**
     * Constructor to execute a command, with all output going to a stream
     *
     * @param application the class of the application to execute
     * @param arguments any arguments to pass to the application
     * @param out the stream to log output to
     * @param port the RMI registry port
     */
    public AsyncExecutor(Class<?> application, String arguments,
                         OutputStream out, int port) {
        this(application, arguments, out, out, port);
    }

    /**
     * Constructor to execute a command with standard output and error output
     * going to two separate streams
     *
     * @param application the class of the application to execute
     * @param arguments any arguments to pass to the application
     * @param out the stream to direct standard output to
     * @param err the stream to direct standard error to
     * @param port the RMI registry port
     */
    public AsyncExecutor(Class<?> application, String arguments, OutputStream out,
                         OutputStream err, int port) {
        if (application == null) {
            throw new IllegalArgumentException("Argument application is null");
        }
        if (out == null) {
            throw new IllegalArgumentException("Argument out is null");
        }
        if (err == null) {
            throw new IllegalArgumentException("Argument err is null");
        }
        _id = ExecutionMonitorService.instance().allocateId();

        StringBuffer command = new StringBuffer();
        command.append(System.getProperty("java.home"));
        command.append("/bin/java ");
        String policy = System.getProperty("java.security.policy");
        if (policy != null) {
            command.append(" -Djava.security.policy=");
            command.append(policy);
            command.append(" ");
        }
        command.append(" -classpath ");
        command.append(System.getProperty("java.class.path"));
        command.append(" ");
        command.append(ApplicationRunner.class.getName());
        command.append(" ");
        command.append(_id);
        command.append(" ");
        command.append(port);
        command.append(" ");
        command.append(application.getName());
        if (arguments != null) {
            command.append(" ");
            command.append(arguments);
        }
        _command = command.toString();
    }

    /**
     * Add a listener to monitor for execution events
     *
     * @param listener the listener to add
     */
    public void addListener(ExecutionListener listener) {
        ExecutionMonitorService.instance().addListener(_id, listener);
    }

    /**
     * Remove a listener
     *
     * @param listener the listener to remove
     */
    public void removeListener(ExecutionListener listener) {
        ExecutionMonitorService.instance().removeListener(_id, listener);
    }

    /**
     * Execute the command in a separate thread. This method returns
     * immediately
     */
    public synchronized void run() {
        if (_executor == null) {
            _executor = new Executor(_command);
        }
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    int status = _executor.run();
                    ExecutionMonitorService.instance().stopped(_id, status);
                } catch (Exception exception) {
                    ExecutionMonitorService.instance().failed(_id, exception);
                }
            }
        };
        log.debug("running command=" + _command);
        thread.start();

    }

    /**
     * Stop the process
     */
    public void stop() {
        if (_executor != null) {
            _executor.stop();
        }
    }

}
