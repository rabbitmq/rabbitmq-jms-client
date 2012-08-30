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
 * $Id: Main.java,v 1.2 2004/02/03 21:52:12 tanderson Exp $
 */
package org.exolab.jmscts.tools.receive;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.exolab.jmscts.core.ReceiptType;
import org.exolab.jmscts.tools.MessagingCommand;
import org.exolab.jmscts.tools.MessagingTool;


/**
 * Invokes the <code>Receive</code> tool
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 21:52:12 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class Main extends MessagingCommand {

    /**
     * The short name for the receipt type argument
     */
    protected static final String RECEIPT = "r";

    /**
     * The long name for the receipt type argument
     */
    protected static final String RECEIPT_LONG = "receipt";

    /**
     * The short name for the selector argument
     */
    protected static final String SELECTOR = "s";

    /**
     * The long name for the selector argument
     */
    protected static final String SELECTOR_LONG = "selector";


    /**
     * Creates the messaging tool
     *
     * @return the messaging tool
     */
    @Override
    protected MessagingTool create() {
        return new Receive();
    }

    /**
     * Returns the command line options
     *
     * @return the command line options
     */
    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        OptionBuilder.withArgName("type");
        OptionBuilder
            .withLongOpt(RECEIPT_LONG);
        OptionBuilder
            .withDescription("Receipt type. One of sync or async. "
                             + "Defaults to sync.");
        Option receipt = OptionBuilder
            .create(RECEIPT);

        OptionBuilder.withArgName("selector");
        OptionBuilder
            .hasArg();
        OptionBuilder
            .withLongOpt(SELECTOR_LONG);
        OptionBuilder
            .withDescription("Specifies the selector. Defaults to no selector");
        Option selector = OptionBuilder
            .create(SELECTOR);

        options.addOption(receipt);
        options.addOption(selector);

        return options;
    }

    /**
     * Returns the command usage
     *
     * @return the command usage
     */
    @Override
    protected String getUsage() {
        return "usage: receive [-config <path>] [-f <name>] -d <name> "
            + "[-receipt <type>] -c <number> [-s <selector>] [-v]";
    }

    /**
     * Parse the command line, populating the tool
     *
     * @param tool the messaging tool
     * @param commands the command line
     * @throws ParseException if the command line cannot be parsed
     */
    @Override
    protected void parse(MessagingTool tool, CommandLine commands)
        throws ParseException {
        super.parse(tool, commands);

        Receive receive = (Receive) tool;
        receive.setReceiptType(getReceiptType(commands));
        receive.setSelector(commands.getOptionValue(SELECTOR));
    }

    /**
     * Parses the receipt type from the command line arguments
     *
     * @param commands the command line arguments
     * @return the receipt type
     * @throws ParseException if the receipt type is invalid
     */
    private static ReceiptType getReceiptType(CommandLine commands)
        throws ParseException {
        ReceiptType receipt = null;
        String type = commands.getOptionValue(RECEIPT, "sync");
        if (type.equals("sync")) {
            receipt = ReceiptType.SYNCHRONOUS;
        } else if (type.equals("async")) {
            receipt = ReceiptType.ASYNCHRONOUS;
        } else {
            throw new ParseException("Invalid receipt type: " + type);
        }
        return receipt;
    }

    /**
     * Main line
     *
     * @param args command line arguments
     * @throws Exception for any error
     */
    public static void main(String[] args) throws Exception {
        Main command = new Main();
        command.invoke(args);
    }
}
