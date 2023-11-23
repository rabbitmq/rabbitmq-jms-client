// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.integration.tests;

import static com.rabbitmq.Assertions.assertThat;
import static com.rabbitmq.TestUtils.onCompletion;
import static com.rabbitmq.TestUtils.waitUntilNotNull;
import static com.rabbitmq.client.impl.LongStringHelper.asLongString;
import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.TestUtils;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class RmqJmsProducerIT extends AbstractAmqpITQueue {

  private static final String USER_STRING_PROPERTY_NAME = "UserHdr";
  private static final byte[] BYTE_ARRAY = {(byte) -2, (byte) -3, 4, 5, 34, (byte) -1};
  private static final String STRING_PROP_VALUE = "the string property value";

  String queueName;
  Queue destination;

  @BeforeEach
  void init(TestInfo info) throws Exception {
    queueName = TestUtils.queueName(info);
    channel.queueDeclare(queueName,
        false, // durable
        true,  // exclusive
        true,  // autoDelete
        null   // options
    );
    destination = new RMQDestination(queueName, "", queueName,
        null);  // write-only AMQP-mapped queue
  }

  @AfterEach
  void dispose() throws Exception {
    this.channel.queueDelete(queueName);
  }

  @Test
  void send() throws Exception {
    try (JMSContext context = this.connFactory.createContext()) {

      JMSProducer producer = context.createProducer();

      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      producer.setPriority(9);
      producer.setProperty(USER_STRING_PROPERTY_NAME, STRING_PROP_VALUE);
      producer.send(destination, BYTE_ARRAY);

      GetResponse response = waitUntilNotNull(() -> channel.basicGet(queueName, false));
      assertThat(response)
          .isNotNull()
          .hasBody(BYTE_ARRAY)
          .headers().containsOnlyKeys(
              "JMSMessageID", "JMSDeliveryMode", "JMSPriority", "JMSTimestamp",
              USER_STRING_PROPERTY_NAME)
          .containsEntry("JMSPriority", 9)
          .containsEntry("JMSDeliveryMode", asLongString("NON_PERSISTENT"))
          .containsEntry(USER_STRING_PROPERTY_NAME, asLongString(STRING_PROP_VALUE));
    }
  }

  @Test
  void sendAsync() throws Exception {
    try (JMSContext context = this.connFactory.createContext()) {
      JMSProducer producer = context.createProducer();
      producer.send(destination, "message1");

      GetResponse response = waitUntilNotNull(() -> channel.basicGet(queueName, false));
      assertThat(response).isNotNull().hasBody("message1");

      CountDownLatch latch = new CountDownLatch(1);
      AtomicInteger completionCount = new AtomicInteger(0);
      producer.setAsync(onCompletion(msg -> {
        completionCount.incrementAndGet();
        latch.countDown();
      }));

      producer.send(destination, "message2");
      assertThat(latch).completes();
      assertThat(completionCount).hasValue(1);
      response = waitUntilNotNull(() -> channel.basicGet(queueName, false));
      assertThat(response).isNotNull().hasBody("message2");

      producer.setAsync(null);
      producer.send(destination, "message3");
      response = waitUntilNotNull(() -> channel.basicGet(queueName, false));
      assertThat(response).isNotNull().hasBody("message3");
      assertThat(completionCount).hasValue(1);
    }
  }

}
