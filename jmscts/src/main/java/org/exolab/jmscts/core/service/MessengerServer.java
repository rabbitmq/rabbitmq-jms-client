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
 * $Id: MessengerServer.java,v 1.2 2004/02/02 03:49:20 tanderson Exp $
 */
package org.exolab.jmscts.core.service;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.jms.JMSException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.exolab.jmscts.core.AbstractTestRunner;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageVerifier;
import org.exolab.jmscts.core.Messenger;


/**
 * This class enables messaging operations to be performed in a separate
 * JVM
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:49:20 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class MessengerServer extends UnicastRemoteObject implements Messenger {

    /** TODO */
    private static final long serialVersionUID = 1L;

    /**
     * The registry port argument
     */
    private static final String PORT = "port";

    /**
     * The name of the server bound in the RMI registry
     */
    private String _name = null;


    /**
     * Construct a new instance
     *
     * @param port the RMI registry port
     * @throws Exception for any error
     */
    protected MessengerServer(int port) throws Exception {
        _name = "//localhost:" + port + "/Messenger";
        Naming.rebind(_name, this);
    }

    /**
     * Send messages to a destination
     *
     * @param message the type of the message to send
     * @param destination the name of the destination
     * @param count the number of messages to send
     * @param populator the message populator (may be null)
     * @throws Exception if the populator fails or remote call fails
     * @throws JMSException if the messages can't be sent
     */
    @Override
    public void send(Class<?> message, String destination, int count,
                     MessagePopulator populator)
        throws Exception, JMSException {
    }

    /**
     * Receive messages from a destination
     *
     * @param destination the name of the destination
     * @param count the expected number of messages to receive
     * @param timeout the maximum time to wait for each
     * @param verifier the message verifier (may be null)
     * @return the actual number of messages received
     * @throws JMSException if the messages can't be received
     * @throws RemoteException if the remote call fails
     */
    @Override
    public int receive(String destination, int count, long timeout,
                       MessageVerifier verifier)
        throws JMSException, RemoteException {
        return 0;
    }

    /**
     * Main line
     *
     * @param args command line arguments
     * @throws Exception for any error
     */
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(
            PORT, true, "use port to connect to the compliance test");

        CommandLineParser parser = new PosixParser();
        CommandLine commands = parser.parse(options, args);

        String port = commands.getOptionValue(
            PORT, AbstractTestRunner.DEFAULT_PORT);

        new MessengerServer(Integer.parseInt(port));
    }

}
