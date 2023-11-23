// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.jms.JMSException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabbitmq.jms.admin.RMQDestination;

public class HandleAnyReplyToStrategyTest {

    HandleAnyReplyToStrategy strategy;

    RMQMessage message;

    @BeforeEach
    void setUp() {
        strategy = new HandleAnyReplyToStrategy();
        message = mock(RMQMessage.class);
    }


    @Test
    void noReplyTo() throws JMSException {
        strategy.handleReplyTo(message, null);

        verifyNoInteractions(message);
    }

    @Test
    void emptyReplyTo() throws JMSException {
        strategy.handleReplyTo(message, "");

        verifyNoInteractions(message);
    }

    @Test
    void nonDirectReplyTo() throws JMSException {
        strategy.handleReplyTo(message, "non direct reply to");

        RMQDestination d = new RMQDestination("non direct reply to", "", "non direct reply to", "non direct reply to");

        verify(message).setJMSReplyTo(d);
        verifyNoMoreInteractions(message);
    }

    @Test
    void directReplyTo() throws JMSException {

        strategy.handleReplyTo(message, ReplyToStrategy.DIRECT_REPLY_TO);

        RMQDestination d = new RMQDestination(ReplyToStrategy.DIRECT_REPLY_TO, "", ReplyToStrategy.DIRECT_REPLY_TO, ReplyToStrategy.DIRECT_REPLY_TO);

        verify(message).setJMSReplyTo(d);
        verifyNoMoreInteractions(message);
    }

    @Test
    void directReplyToqueue() throws JMSException {
        strategy.handleReplyTo(message, ReplyToStrategy.DIRECT_REPLY_TO + "-123456678");

        RMQDestination d = new RMQDestination(ReplyToStrategy.DIRECT_REPLY_TO, "", ReplyToStrategy.DIRECT_REPLY_TO  + "-123456678", ReplyToStrategy.DIRECT_REPLY_TO + "-123456678");

        verify(message).setJMSReplyTo(d);
        verifyNoMoreInteractions(message);
    }
}
