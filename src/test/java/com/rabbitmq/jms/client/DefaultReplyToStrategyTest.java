// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.jms.JMSException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabbitmq.jms.admin.RMQDestination;

public class DefaultReplyToStrategyTest {

    DefaultReplyToStrategy strategy;

    RMQMessage message;

    RMQDestination destination;

    @BeforeEach
    void setUp() {
        strategy = new DefaultReplyToStrategy();
        message = mock(RMQMessage.class);
        destination = mock(RMQDestination.class);
    }


    @Test
    void noReplyTo() throws JMSException {

        strategy.handleReplyTo(destination, message, null);

        verifyNoInteractions(destination, message);
    }

    @Test
    void emptyReplyTo() throws JMSException {

        strategy.handleReplyTo(destination, message, "");

        verifyNoInteractions(destination, message);
    }

    @Test
    void nonDirctReplyTo() throws JMSException {

        strategy.handleReplyTo(destination, message, "non direct reply to");

        verifyNoInteractions(destination, message);
    }

    @Test
    void directReplyTo() throws JMSException {

        strategy.handleReplyTo(destination, message, ReplyToStrategy.DIRECT_REPLY_TO);

        RMQDestination d = new RMQDestination(ReplyToStrategy.DIRECT_REPLY_TO, "", ReplyToStrategy.DIRECT_REPLY_TO, ReplyToStrategy.DIRECT_REPLY_TO);

        verify(message).setJMSReplyTo(d);
        verifyNoMoreInteractions(message);
        verifyNoInteractions(destination);
    }

    @Test
    void directReplyToqueue() throws JMSException {

        strategy.handleReplyTo(destination, message, ReplyToStrategy.DIRECT_REPLY_TO + "-123456678");

        RMQDestination d = new RMQDestination(ReplyToStrategy.DIRECT_REPLY_TO, "", ReplyToStrategy.DIRECT_REPLY_TO  + "-123456678", ReplyToStrategy.DIRECT_REPLY_TO + "-123456678");

        verify(message).setJMSReplyTo(d);
        verifyNoMoreInteractions(message);
        verifyNoInteractions(destination);
    }
}
