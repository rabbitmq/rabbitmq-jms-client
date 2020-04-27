/* Copyright (c) 2014-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.util.Shell;
import org.junit.jupiter.api.Test;

/* ShellIT tests that Shell.java correctly issues commands */
public class ShellIT extends AbstractITQueue {

    @Test
    public void issueSimpleCommand() throws Exception {
        String result = Shell.executeSimpleCommand("pwd");
        System.out.println("Result of pwd is '"+result+"'");
    }

}
