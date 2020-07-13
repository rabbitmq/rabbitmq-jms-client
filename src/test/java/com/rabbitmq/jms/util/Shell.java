// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Shell is a test utility class for issuing shell commands from integration tests.
 */
public class Shell {

    public static String executeSimpleCommand(String command) {
        StringBuffer outputSb = new StringBuffer();

        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                outputSb.append(line + "\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputSb.toString();
    }

    public static String capture(InputStream is)
        throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder buff = new StringBuilder();
        while ((line = br.readLine()) != null) {
            buff.append(line).append("\n");
        }
        return buff.toString();
    }

    public static void restartNode() throws IOException {
        executeCommand(rabbitmqctlCommand() + " -n " + nodenameA() + " stop_app");
        executeCommand(rabbitmqctlCommand() + " -n " + nodenameA() + " start_app");
    }

    public static List<Binding> listBindings(boolean includeDefaults) throws IOException {
        List<Binding> bindings = new ArrayList<>();
        Process process = executeCommand(rabbitmqctlCommand() + " -n " + nodenameA() + " list_bindings source_name destination_name routing_key --quiet");
        String output = capture(process.getInputStream());
        String[] lines = output.split("\n");
        if (lines.length > 0) {
            for (int i = 1; i < lines.length; i++) {
                String [] fields = lines[i].split("\t");
                Binding binding = new Binding(fields[0], fields[1], fields[2]);
                if (includeDefaults || !binding.isDefault()) {
                    bindings.add(binding);
                }
            }
        }
        return bindings;
    }

    public static Process executeCommand(String command) throws IOException {
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
        while (true) {
            try {
                pr.waitFor();
                break;
            } catch (InterruptedException ignored) {
            }
        }
        return pr.exitValue();
    }

    private static Process executeCommandProcess(String command) throws IOException {
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

    public static String nodenameA() {
        return System.getProperty("test-broker.A.nodename");
    }

    public static String rabbitmqctlCommand() {
        return System.getProperty("rabbitmqctl.bin");
    }

    public static class Binding {

        private final String source, destination, routingKey;

        public Binding(String source, String destination, String routingKey) {
            this.source = source;
            this.destination = destination;
            this.routingKey = routingKey;
        }

        public String getSource() {
            return source;
        }

        public String getDestination() {
            return destination;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public boolean isDefault() {
            return "".equals(source);
        }

        @Override
        public String toString() {
            return "Binding{" +
                    "source='" + source + '\'' +
                    ", destination='" + destination + '\'' +
                    ", routingKey='" + routingKey + '\'' +
                    '}';
        }
    }
}
