// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import org.junit.jupiter.api.Test;

import com.rabbitmq.jms.util.Shell;

/* ShellIT tests that Shell.java correctly issues commands */
public class ShellIT extends AbstractITQueue {

    @Test
    public void issueSimpleCommand() throws Exception {
        String result = Shell.executeSimpleCommand("pwd");
        System.out.println("Result of pwd is '"+result+"'");
    }

}
