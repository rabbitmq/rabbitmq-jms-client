// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.integration.tests;

import static com.rabbitmq.Assertions.assertThat;
import static com.rabbitmq.TestUtils.onCompletion;
import static java.lang.String.valueOf;

import com.rabbitmq.TestUtils;
import com.rabbitmq.jms.admin.RMQDestination;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class RmqJmsContextIT extends AbstractAmqpITQueue {

  String queueName;
  RMQDestination destination;

  @BeforeEach
  void init(TestInfo info) throws Exception {
    queueName = TestUtils.queueName(info);
    destination = new RMQDestination(queueName, true, false);
  }

  @AfterEach
  void dispose() throws Exception {
    this.channel.queueDelete(queueName);
  }

  @Test
  void sendReceiveAcknowledge() throws InterruptedException, IOException {
    int messageCount = 10;
    try (JMSContext context = this.connFactory.createContext()) {
      CountDownLatch latch = new CountDownLatch(messageCount);
      JMSProducer producer = context.createProducer();
      producer.setAsync(onCompletion(t -> latch.countDown()));
      IntStream.range(0, messageCount).forEach(i -> producer.send(destination, valueOf(i)));
      assertThat(latch).completes();
    }

    try (JMSContext context = this.connFactory.createContext(JMSContext.CLIENT_ACKNOWLEDGE)) {
      JMSConsumer consumer = context.createConsumer(destination);
      CountDownLatch latch = new CountDownLatch(messageCount);
      consumer.setMessageListener(message -> latch.countDown());
      assertThat(latch).completes();
      // acknowledge all the messages
      context.acknowledge();
    }
    // there should be no messages left in the queue
    org.assertj.core.api.Assertions.assertThat(
        TestUtils.waitUntil(() -> channel.queueDeclarePassive(queueName).getMessageCount() == 0));
  }

  @Test
  void setClientIdShouldFailAfterSessionCreation() {
    try (JMSContext context = this.connFactory.createContext()) {
      context.createProducer();
      Assertions.assertThatThrownBy(() -> context.setClientID("foo"));
      Assertions.assertThat(context.getClientID()).isNull();
    }
  }

  @Test
  void setClientIdCanBeSetOnlyOnce() {
    try (JMSContext context = this.connFactory.createContext()) {
      context.setClientID("foo");
      Assertions.assertThat(context.getClientID()).isEqualTo("foo");
      Assertions.assertThatThrownBy(() -> context.setClientID("foo")).isInstanceOf(
          JMSRuntimeException.class);
      Assertions.assertThat(context.getClientID()).isEqualTo("foo");
    }
  }

}
