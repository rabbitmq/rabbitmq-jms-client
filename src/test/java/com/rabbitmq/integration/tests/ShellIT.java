/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import org.junit.Test;

import com.rabbitmq.jms.util.Shell;

/* ShellIT tests that Shell.java correctly issues commands */
public class ShellIT extends AbstractITQueue {

    @Test
    public void issueSimpleCommand() throws Exception {
        String result = Shell.executeSimpleCommand("pwd");
        System.out.println("Result of pwd is '"+result+"'");
    }

}
