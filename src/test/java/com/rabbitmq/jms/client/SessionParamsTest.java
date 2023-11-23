// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2016-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class SessionParamsTest {

    @Test
    void  createNoExplicitReplyToStrategy() {
        SessionParams params = new SessionParams();

        assertSame(DefaultReplyToStrategy.INSTANCE, params.getReplyToStrategy());
    }

    @Test
    void  createNullReplyToStrategy() {
        SessionParams params = new SessionParams();
        params.setReplyToStrategy(null);

        assertNull(params.getReplyToStrategy());
    }

    @Test
    void  createExplicitlySetReplyToStrategy() {
        SessionParams params = new SessionParams();
        params.setReplyToStrategy(HandleAnyReplyToStrategy.INSTANCE);

        assertSame(HandleAnyReplyToStrategy.INSTANCE, params.getReplyToStrategy());
    }
}


