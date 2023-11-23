// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.integration.tests;

import static com.rabbitmq.Assertions.assertThat;
import static com.rabbitmq.TestUtils.TEN_SECONDS;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonMap;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.rabbitmq.TestUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.jms.admin.RMQDestination;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageFormatRuntimeException;
import javax.jms.Queue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class RmqJmsConsumerIT extends AbstractAmqpITQueue {

  private static final long RECEIVE_TIMEOUT = TEN_SECONDS.toMillis();

  String queueName;
  Queue destination;

  private static AMQP.BasicProperties amqpProperties(Map<String, Object> headers) {
    return new AMQP.BasicProperties.Builder()
        .headers(headers).build();
  }

  @BeforeEach
  void init(TestInfo info) throws Exception {
    queueName = TestUtils.queueName(info);
    channel.queueDeclare(queueName,
        false, // durable
        false,  // not exclusive
        true,  // autoDelete
        null   // options
    );
    destination = new RMQDestination(queueName, null, null,
        queueName);  // read-only AMQP-mapped queue
  }

  @AfterEach
  void dispose() throws Exception {
    this.channel.queueDelete(queueName);
  }

  @Test
  void receive() throws Exception {
    Map<String, Object> hdrs = new HashMap<String, Object>();
    hdrs.put("JMSType", "TextMessage");  // To signal a JMS TextMessage
    hdrs.put("foo", "bar");

    channel.basicPublish("", queueName,
        amqpProperties(hdrs),
        "hello".getBytes(StandardCharsets.UTF_8));

    try (JMSContext context = this.connFactory.createContext()) {
      JMSConsumer consumer = context.createConsumer(destination);
      Message message = consumer.receive(RECEIVE_TIMEOUT);
      assertThat(message).isNotNull().isTextMessage()
          .hasType("TextMessage")
          .hasText("hello")
          .hasProperty("foo", "bar");
    }
  }

  @Test
  void messageListener() throws Exception {
    try (JMSContext context = this.connFactory.createContext()) {
      JMSConsumer consumer = context.createConsumer(destination);
      AtomicReference<Message> messageReference = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);
      consumer.setMessageListener(message -> {
        messageReference.set(message);
        latch.countDown();
      });

      channel.basicPublish("", queueName,
          amqpProperties(singletonMap("JMSType", "TextMessage")),
          "hello".getBytes(StandardCharsets.UTF_8));

      assertThat(latch).completes();

      assertThat(messageReference.get()).isNotNull().isTextMessage()
          .hasType("TextMessage")
          .hasText("hello");
    }
  }

  @Test
  void receiveBodyKeepsMessageAfterFormatExceptionForAutoAckAndDupsOk() throws Exception {
    int messageCount = 10;
    for (byte i = 0; i < messageCount; i++) {
      channel.basicPublish("", queueName,
          amqpProperties(singletonMap("JMSType", "TextMessage")),
          valueOf(i).getBytes(StandardCharsets.UTF_8));
    }
    try (JMSContext context = this.connFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
      JMSConsumer consumer = context.createConsumer(destination);
      List<String> bodies = new ArrayList<>();
      bodies.add(consumer.receiveBody(String.class, RECEIVE_TIMEOUT));
      Assertions.assertThat(bodies).hasSize(1).element(0).isEqualTo("0");
      assertThatThrownBy(() -> consumer.receiveBody(Boolean.class, RECEIVE_TIMEOUT))
          .isInstanceOf(MessageFormatRuntimeException.class);
      assertThatThrownBy(() -> consumer.receiveBody(Boolean.class, RECEIVE_TIMEOUT))
          .isInstanceOf(MessageFormatRuntimeException.class);
      bodies.add(consumer.receiveBody(String.class, RECEIVE_TIMEOUT));
      Assertions.assertThat(bodies).hasSize(2).element(1).isEqualTo("1");
      long start = System.nanoTime();
      BooleanSupplier timeoutCondition = () -> (System.nanoTime() - start) < TEN_SECONDS.toNanos();
      while (bodies.size() < messageCount && timeoutCondition.getAsBoolean()) {
        bodies.add(consumer.receiveBody(String.class, RECEIVE_TIMEOUT));
      }
      Assertions.assertThat(bodies).hasSize(messageCount);
      range(0, messageCount).forEach(i -> Assertions.assertThat(bodies).element(i).isEqualTo(
          valueOf(i)));
    }
  }

}
