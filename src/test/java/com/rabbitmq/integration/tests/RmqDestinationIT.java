// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.admin.RMQDestinationTest;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.junit.jupiter.api.Test;

public class RmqDestinationIT extends AbstractITQueue {

  private static final String QUEUE_NAME =
      "test.queue." + RMQDestinationTest.class.getCanonicalName();

  @Test
  void createRmqDestinationWithArguments() throws Exception {
    this.queueConn.start();
    QueueSession session = this.queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

    Queue queue =
        new RMQDestination(
            QUEUE_NAME, true, false, Collections.singletonMap("x-queue-type", "quorum"));
    QueueSender sender = session.createSender(queue);
    drainQueue(session, queue);
    String body = String.valueOf(System.currentTimeMillis());
    TextMessage msg = session.createTextMessage(body);
    sender.send(msg);

    CountDownLatch consumerLatch = new CountDownLatch(1);
    AtomicReference<TextMessage> message = new AtomicReference<>();
    MessageConsumer consumer = session.createConsumer(queue);
    consumer.setMessageListener(
        m -> {
          message.set((TextMessage) m);
          consumerLatch.countDown();
        });
    assertThat(consumerLatch.await(10, TimeUnit.SECONDS)).isTrue();
    assertThat(message.get().getText()).isEqualTo(body);
  }
}
