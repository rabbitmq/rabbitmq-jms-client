// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.admin;

import org.junit.jupiter.api.Test;

import javax.naming.Reference;

import static org.assertj.core.api.Assertions.assertThat;

public class RMQDestinationTest {

    RMQObjectFactory rmqObjectFactory = new RMQObjectFactory();

    @Test
    void queueRegeneration() throws Exception {
        RMQDestination queue = new RMQDestination("queue", true, false);
        Reference reference = queue.getReference();
        RMQDestination newQueue = (RMQDestination) rmqObjectFactory.getObjectInstance(reference, null, null, null);
        assertThat(newQueue.isQueue()).isTrue();
        assertThat(newQueue.getDestinationName()).isEqualTo("queue");
        assertThat(newQueue.isAmqp()).isFalse();
    }

    @Test
    void topicRegeneration() throws Exception {
        RMQDestination topic = new RMQDestination("topic", false, false);
        Reference reference = topic.getReference();
        RMQDestination newTopic = (RMQDestination) rmqObjectFactory.getObjectInstance(reference, null, null, null);
        assertThat(newTopic.isQueue()).isFalse();
        assertThat(newTopic.getDestinationName()).isEqualTo("topic");
        assertThat(newTopic.isAmqp()).isFalse();
    }

    @Test
    void amqpDestinationRegeneration() throws Exception {
        RMQDestination destination = new RMQDestination(
                "destination", "exchange", "routing-key", "queue"
        );
        Reference reference = destination.getReference();
        RMQDestination newReference = (RMQDestination) rmqObjectFactory.getObjectInstance(reference, null, null, null);
        assertThat(newReference.isQueue()).isTrue();
        assertThat(newReference.getDestinationName()).isEqualTo("destination");
        assertThat(newReference.isAmqp()).isTrue();
        assertThat(newReference.getAmqpExchangeName()).isEqualTo("exchange");
        assertThat(newReference.getAmqpRoutingKey()).isEqualTo("routing-key");
        assertThat(newReference.getAmqpQueueName()).isEqualTo("queue");
    }

    @Test
    void amqpDestinationExchangeRoutingKeyOnlyRegeneration() throws Exception {
        RMQDestination destination = new RMQDestination(
                "destination", "exchange", "routing-key", null
        );
        Reference reference = destination.getReference();
        RMQDestination newReference = (RMQDestination) rmqObjectFactory.getObjectInstance(reference, null, null, null);
        assertThat(newReference.isQueue()).isTrue();
        assertThat(newReference.getDestinationName()).isEqualTo("destination");
        assertThat(newReference.isAmqp()).isTrue();
        assertThat(newReference.getAmqpExchangeName()).isEqualTo("exchange");
        assertThat(newReference.getAmqpRoutingKey()).isEqualTo("routing-key");
        assertThat(newReference.getAmqpQueueName()).isNull();
    }

    @Test
    void amqpDestinationQueueOnlyRegeneration() throws Exception {
        RMQDestination destination = new RMQDestination(
                "destination", null, null, "queue"
        );
        Reference reference = destination.getReference();
        RMQDestination newReference = (RMQDestination) rmqObjectFactory.getObjectInstance(reference, null, null, null);
        assertThat(newReference.isQueue()).isTrue();
        assertThat(newReference.getDestinationName()).isEqualTo("destination");
        assertThat(newReference.isAmqp()).isTrue();
        assertThat(newReference.getAmqpExchangeName()).isNull();
        assertThat(newReference.getAmqpRoutingKey()).isNull();
        assertThat(newReference.getAmqpQueueName()).isEqualTo("queue");
    }

}
