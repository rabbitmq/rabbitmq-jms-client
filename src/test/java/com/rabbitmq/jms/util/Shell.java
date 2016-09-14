/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

import java.io.*;

/**
 * Shell is a test utility class for issuing shell commands from integration tests.
 */
public class Shell {

    private static final String CTLCMD = "rabbitmqctl ";

    public static String executeSimpleCommand(String command) {
        StringBuffer outputSb = new StringBuffer();

        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                outputSb.append(line + "\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputSb.toString();
    }

    /*
    public static String executeControl(String cmd) {
        return executeCommand(CTLCMD + cmd);
    }
    */


    public static String capture(InputStream is)
        throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder buff = new StringBuilder();
        while ((line = br.readLine()) != null) {
            buff.append(line).append("\n");
        }
        return buff.toString();
    }

    public static Process invokeMakeTarget(String command) throws IOException {
        File rabbitmqctl = new File(rabbitmqctlCommand());
        return executeCommand(makeCommand() +
            " -C \'" + rabbitmqDir() + "\'" +
            " RABBITMQCTL=\'" + rabbitmqctl.getAbsolutePath() + "\'" +
            " RABBITMQ_NODENAME=\'" + nodenameA() + "\'" +
            " RABBITMQ_NODE_PORT=" + node_portA() +
            " RABBITMQ_CONFIG_FILE=\'" + config_fileA() + "\'" +
            " " + command);
    }

    public static Process executeCommand(String command) throws IOException
    {
        Process pr = executeCommandProcess(command);

        int ev = waitForExitValue(pr);
        if (ev != 0) {
            String stdout = capture(pr.getInputStream());
            String stderr = capture(pr.getErrorStream());
            throw new IOException("unexpected command exit value: " + ev +
                "\ncommand: " + command + "\n" +
                "\nstdout:\n" + stdout +
                "\nstderr:\n" + stderr + "\n");
        }
        return pr;
    }

    private static int waitForExitValue(Process pr) {
        while(true) {
            try {
                pr.waitFor();
                break;
            } catch (InterruptedException ignored) {}
        }
        return pr.exitValue();
    }

    private static Process executeCommandProcess(String command) throws IOException
    {
        String[] finalCommand;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            finalCommand = new String[4];
            finalCommand[0] = "C:\\winnt\\system32\\cmd.exe";
            finalCommand[1] = "/y";
            finalCommand[2] = "/c";
            finalCommand[3] = command;
        } else {
            finalCommand = new String[3];
            finalCommand[0] = "/bin/sh";
            finalCommand[1] = "-c";
            finalCommand[2] = command;
        }
        return Runtime.getRuntime().exec(finalCommand);
    }

    public static String makeCommand()
    {
        return System.getProperty("make.bin", "make");
    }

    public static String nodenameA()
    {
        return System.getProperty("test-broker.A.nodename");
    }

    public static String node_portA()
    {
        return System.getProperty("test-broker.A.node_port");
    }

    public static String config_fileA()
    {
        return System.getProperty("test-broker.A.config_file");
    }

    public static String rabbitmqctlCommand()
    {
        return System.getProperty("rabbitmqctl.bin");
    }

    public static String rabbitmqDir()
    {
        return System.getProperty("rabbitmq.dir");
    }
}
