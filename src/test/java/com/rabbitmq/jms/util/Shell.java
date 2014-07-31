package com.rabbitmq.jms.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Shell is a test utility class for issuing shell commands from integration tests.
 */
public class Shell {
    public String executeCommand(String command) {
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
}
