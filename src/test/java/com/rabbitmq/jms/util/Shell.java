// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
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

    private static final String DOCKER_PREFIX = "DOCKER:";

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
        executeCommand(rabbitmqctlCommand() + rabbitmqctlNodenameArgument() + " stop_app");
        executeCommand(rabbitmqctlCommand() + rabbitmqctlNodenameArgument() + " start_app");
    }

    public static List<Binding> listBindings(boolean includeDefaults) throws IOException {
        List<Binding> bindings = new ArrayList<>();
        ProcessState process = executeCommand(rabbitmqctlCommand() + rabbitmqctlNodenameArgument() + " list_bindings source_name destination_name routing_key --quiet");
        String output = process.output();
        String[] lines = output.split("\n");
        if (lines.length > 0) {
            for (int i = 1; i < lines.length; i++) {
                String [] fields = lines[i].split("\t");
                Binding binding = new Binding(fields[0], fields[1], fields.length > 2 ? fields[2] : "");
                if (includeDefaults || !binding.isDefault()) {
                    bindings.add(binding);
                }
            }
        }
        return bindings;
    }

    public static ProcessState executeCommand(String command) throws IOException {
        Process pr = executeCommandProcess(command);
        InputStreamPumpState inputState = new InputStreamPumpState(pr.getInputStream());
        InputStreamPumpState errorState = new InputStreamPumpState(pr.getErrorStream());

        int ev = waitForExitValue(pr, inputState, errorState);
        inputState.pump();
        errorState.pump();
        if (ev != 0) {
            throw new IOException("unexpected command exit value: " + ev +
                "\ncommand: " + command + "\n" +
                "\nstdout:\n" + inputState.buffer.toString() +
                "\nstderr:\n" + errorState.buffer.toString() + "\n");
        }
        return new ProcessState(pr, inputState, errorState);
    }

    public static class ProcessState {

        private final Process process;
        private final InputStreamPumpState inputState;
        private final InputStreamPumpState errorState;

        ProcessState(Process process, InputStreamPumpState inputState,
            InputStreamPumpState errorState) {
            this.process = process;
            this.inputState = inputState;
            this.errorState = errorState;
        }

        public String output() {
            return inputState.buffer.toString();
        }

    }

    private static class InputStreamPumpState {

        private final BufferedReader reader;
        private final StringBuilder buffer;

        private InputStreamPumpState(InputStream in) {
            this.reader = new BufferedReader(new InputStreamReader(in));
            this.buffer = new StringBuilder();
        }

        void pump() throws IOException {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        }

    }

    private static int waitForExitValue(Process pr, InputStreamPumpState inputState,
        InputStreamPumpState errorState) throws IOException {
        while(true) {
            try {
                inputState.pump();
                errorState.pump();
                pr.waitFor();
                break;
            } catch (InterruptedException ignored) {}
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

    private static String rabbitmqctlNodenameArgument() {
        return isRabbitmqctlOnDocker() ? "" : " -n \'" + nodenameA() + "\'";
    }
    public static boolean isRabbitmqctlOnDocker() {
        String rabbitmqCtl = System.getProperty("rabbitmqctl.bin");
        if (rabbitmqCtl == null) {
            throw new IllegalStateException("Please define the rabbitmqctl.bin system property");
        }
        return rabbitmqCtl.startsWith(DOCKER_PREFIX);
    }
    public static ProcessState rabbitmqctl(String command) throws IOException {
        return executeCommand(rabbitmqctlCommand() + rabbitmqctlNodenameArgument() + " " + command);
    }
    public static String rabbitmqctlCommand() {
        String rabbitmqCtl = System.getProperty("rabbitmqctl.bin");
        if (rabbitmqCtl == null) {
            throw new IllegalStateException("Please define the rabbitmqctl.bin system property");
        }
        if (rabbitmqCtl.startsWith("DOCKER:")) {
            String containerId = rabbitmqCtl.split(":")[1];
            return "docker exec " + containerId + " rabbitmqctl";
        } else {
            return rabbitmqCtl;
        }
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
