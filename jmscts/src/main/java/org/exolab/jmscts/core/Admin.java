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
 * $Id: Admin.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.rmi.Naming;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.GnuParser;

import org.exolab.jmscts.core.service.RemoteService;
import org.exolab.jmscts.core.service.Snapshot;


/**
 * This class enables the state of a running compliance suite to be captured,
 * and the suite to be terminated.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class Admin {

    /**
     * The snapshot file argument
     */
    private static final String SNAPSHOT = "snapshot";

    /**
     * The port argument
     */
    private static final String PORT = "port";

    /**
     * The stop option
     */
    private static final String STOP = "stop";

    /**
     * The abort option
     */
    private static final String ABORT = "abort";


    /**
     * Prevent construction of utility class
     */
    private Admin() {
    }

    /**
     * Main line for the admin tool
     *
     * @param args command line arguments
     * @throws Exception for any error
     */
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(SNAPSHOT, true, "snapshot the current test state");
        options.addOption(STOP, false, "stop a running compliance test");
        options.addOption(ABORT, false, "abort a running compliance test");
        options.addOption(
            PORT, true, "use port to connect to the compliance test");

        CommandLineParser parser = new GnuParser();
        CommandLine commands = parser.parse(options, args);

        String path = commands.getOptionValue(SNAPSHOT);
        String port = commands.getOptionValue(
            PORT, AbstractTestRunner.DEFAULT_PORT);

        if (path != null) {
            String name = getName(port, "Snapshot");
            Snapshot server = (Snapshot) Naming.lookup(name);
            server.snapshot(path);
        } else if (commands.hasOption(STOP)) {
            String name = getName(port, "Terminator");
            RemoteService server = (RemoteService) Naming.lookup(name);
            server.stop();
        } else if (commands.hasOption(ABORT)) {
            String name = getName(port, "Terminator");
            RemoteService server = (RemoteService) Naming.lookup(name);
            server.abort();
        } else {
            usage();
        }
    }

    /**
     * Prints usage for the tool
     */
    private static void usage() {
        System.err.println(
            "usage: " + Admin.class.getName() + " <arguments> [options]\n"
            + "arguments:\n"
            + "  -snapshot <path> snapshot the current test state\n"
            + "  -stop            stop a running compliance test\n"
            + "  -abort           abort a running compliance test\n"
            + "options:\n"
            + "  -port <port>     use port to connect to the compliance test"
            + "\n");
    }

    /**
     * Helper to return a service URL
     *
     * @param port the port the service is running on
     * @param name the name of the service
     * @return the service URL
     */
    private static String getName(String port, String name) {
        return "//localhost:" + port + "/" + name;
    }

}
