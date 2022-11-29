// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.util.Shell;
import jakarta.jms.*;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.rabbitmq.jms.util.Shell.rabbitmqPlugins;
import static com.rabbitmq.jms.util.Shell.rabbitmqctlCommand;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test
 */
public class DelayedMessageIT extends AbstractITQueue {

    private static final String MESSAGE = "Hello " + DelayedMessageIT.class.getName();

    @BeforeEach
    public void beforeMethod() throws IOException {
        try {
            Shell.rabbitmqPlugins("is_enabled rabbitmq_delayed_message_exchange");
        }catch(Exception e) {
            System.out.println("Skipped DelayedMessageIT. Plugin rabbitmq_delayed_message_exchange is not enabled");
            org.junit.Assume.assumeTrue(false);
        }
    }


    @Test
    public void testJMSQueue() throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createTemporaryQueue();
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryDelay(1000L);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        queueSender.send(message);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        message = (TextMessage) queueReceiver.receive(2000L);
        assertNotNull(message);
        assertEquals(MESSAGE, message.getText());
    }


}
