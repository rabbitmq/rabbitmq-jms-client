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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: MessagingCommand.java,v 1.2 2004/02/03 21:52:12 tanderson Exp $
 */
package org.exolab.jmscts.tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.xml.DOMConfigurator;


/**
 * Messaging tool command parser
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 21:52:12 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public abstract class MessagingCommand {

    /**
     * The short name for the provider configuration argument
     */
    protected static final String CONFIG = "c";

    /**
     * The long name for the provider configuration argument
     */
    protected static final String CONFIG_LONG = "config";

    /**
     * The short name for the factory argument
     */
    protected static final String FACTORY = "f";

    /**
     * The long name for the factory argument
     */
    protected static final String FACTORY_LONG = "factory";

    /**
     * The short name for the destination argument
     */
    protected static final String DESTINATION = "d";

    /**
     * The long name for the destination argument
     */
    protected static final String DESTINATION_LONG = "destination";

    /**
     * The short name for the count argument
     */
    protected static final String COUNT = "c";

    /**
     * The long name for the count argument
     */
    protected static final String COUNT_LONG = "count";

    /**
     * The short name for the verbose argument
     */
    protected static final String VERBOSE = "v";

    /**
     * The long name for the verbose argument
     */
    protected static final String VERBOSE_LONG = "verbose";


    /**
     * Construct a new <code>MessagingCommand</code>
     */
    public MessagingCommand() {
        // configure the logger
        DOMConfigurator.configure(getHome() + "/config/log4j.xml");
    }

    /**
     * Invoke the command
     *
     * @param args the command line arguments
     * @throws Exception for any error
     */
    public void invoke(String[] args) throws Exception {
        // parse the command line
        Options options = getOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine commands = null;

        try {
            commands = parser.parse(options, args);
        } catch (ParseException exception) {
            usage(options, exception.getMessage());
        }

        MessagingTool tool = create();
        parse(tool, commands);

        tool.invoke();
    }

    /**
     * Create the messaging tool
     *
     * @return the messaging tool
     */
    protected abstract MessagingTool create();

    /**
     * Parse the command line, populating the tool
     *
     * @param tool the messaging tool
     * @param commands the command line
     * @throws ParseException if the command line cannot be parsed
     */
    protected void parse(MessagingTool tool, CommandLine commands)
        throws ParseException {

        String config = commands.getOptionValue(
            "config", getHome() + "/config/providers.xml");

        tool.setConfig(config);
        tool.setConnectionFactory(commands.getOptionValue(FACTORY));
        tool.setDestination(commands.getOptionValue(DESTINATION));
        tool.setCount(Integer.parseInt(commands.getOptionValue(COUNT)));
        if (commands.hasOption(VERBOSE)) {
            tool.setVerbose(true);
        }
    }

    /**
     * Returns the command line arguments
     *
     * @return the command line arguments
     */
    protected Options getOptions() {
        OptionBuilder.withArgName("path");
        OptionBuilder
            .hasArg();
        OptionBuilder
            .withLongOpt(CONFIG_LONG);
        OptionBuilder
            .withDescription("the provider configuration path");
        Option config = OptionBuilder
            .create(CONFIG);

        OptionBuilder.withArgName("name");
        OptionBuilder
            .hasArg();
        OptionBuilder
            .withLongOpt(FACTORY_LONG);
        OptionBuilder
            .withDescription("the connection factory name");
        Option factory = OptionBuilder
            .create(FACTORY);

        OptionBuilder.withArgName("name");
        OptionBuilder
            .hasArg();
        OptionBuilder
            .isRequired();
        OptionBuilder
            .withLongOpt(DESTINATION_LONG);
        OptionBuilder
            .withDescription("the destination name");
        Option destination = OptionBuilder
            .create(DESTINATION);

        OptionBuilder.withArgName("number");
        OptionBuilder
            .hasArg();
        OptionBuilder
            .isRequired();
        OptionBuilder
            .withLongOpt(COUNT_LONG);
        OptionBuilder
            .withDescription("the number of messages to process");
        Option count = OptionBuilder
            .create(COUNT);

        OptionBuilder.withDescription("use verbose logging");
        OptionBuilder
            .withLongOpt(VERBOSE_LONG);
        Option verbose = OptionBuilder
            .create(VERBOSE);

        Options options = new Options();

        options.addOption(config);
        options.addOption(factory);
        options.addOption(destination);
        options.addOption(count);
        options.addOption(verbose);
        return options;
    }

    /**
     * Returns the command usage
     *
     * @return the command usage
     */
    protected abstract String getUsage();

    /**
     * Prints the usage for the command, and exits with an error code
     *
     * @param options the command line options
     * @param message message to display before the usage. May be
     * <code>null</code>
     */
    protected void usage(Options options, String message) {
        if (message != null) {
            System.err.println(message);
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getUsage(), options);
        System.exit(1);
    }

    /**
     * Returns the value of the <code>jmscts.home</code> system property,
     * defaulting to the value of <code>user.dir</code> if its not set
     *
     * @return the value of the <code>jmscts.home</code> system property
     */
    protected String getHome() {
        return System.getProperty("jmscts.home",
                                  System.getProperty("user.dir"));
    }

}

