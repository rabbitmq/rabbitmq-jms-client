// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import com.rabbitmq.TestUtils;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.Shell;
import jakarta.jms.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static com.rabbitmq.Assertions.assertThat;
import static com.rabbitmq.TestUtils.onCompletion;
import static java.lang.String.valueOf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test
 */
public class DelayedAMQPQueueMessageIT extends AbstractAmqpITQueue {

    String queueName;
    RMQDestination destination;

    @BeforeEach
    void init() throws Exception {
        queueName = "DelayedAMQPQueueMessageIT";
        destination = new RMQDestination(queueName, true, false);
    }

    @BeforeEach
    public void beforeMethod() throws IOException {
        try {
            Shell.rabbitmqPlugins("is_enabled rabbitmq_delayed_message_exchange");
        }catch(Exception e) {
            System.out.println("Skipped DelayedMessageIT. Plugin rabbitmq_delayed_message_exchange is not enabled");
            Assumptions.assumeTrue(false);
        }
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
