// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.TestUtils.SkipIfDelayedMessageExchangePluginNotActivated;
import com.rabbitmq.jms.admin.RMQDestination;
import javax.jms.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Integration test
 */
@SkipIfDelayedMessageExchangePluginNotActivated
public class DelayedAMQPQueueMessageIT extends AbstractAmqpITQueue {

    String queueName = "DelayedAMQPQueueMessageIT";
    RMQDestination destination;

    @BeforeEach
    void init() {
        destination = new RMQDestination(queueName, true, false);
    }

    @AfterEach
    void dispose() throws Exception {
        this.channel.queueDelete(queueName);
    }

    @Test
    void sendMessageToAMQPQueue() {

        try (JMSContext context = this.connFactory.createContext()) {
            JMSProducer producer = context.createProducer();
            producer.setDeliveryDelay(4000L);
            producer.send(destination, "test message");
        }

        try (JMSContext context = this.connFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(destination);

            assertNull(consumer.receive(2000L));
            assertNotNull(consumer.receive(3000L));
        }

    }

}
