// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static com.rabbitmq.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.jms.CompletionListener;
import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for asynchronous send in JMS 2.0
 */
public class AsynchronousSendIT {

  private static final String QUEUE_NAME =
      "test.queue." + AsynchronousSendIT.class.getCanonicalName();

  Connection connection;

  @BeforeEach
  public void init() throws Exception {
    RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
        .getConnectionFactory();
    connection = connectionFactory.createConnection();
    connection.start();
  }

  @AfterEach
  public void tearDown() throws Exception {
    if (connection != null) {
      connection.close();
    }
    com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
    try (com.rabbitmq.client.Connection c = cf.newConnection()) {
      c.createChannel().queueDelete(QUEUE_NAME);
    }
  }

  @Test
  public void confirmListenerShouldBeCalledWhenConfirmListenerIsEnabled() throws Exception {
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Queue queue = session.createQueue(QUEUE_NAME);
    MessageConsumer consumer = session.createConsumer(queue);
    BlockingQueue<Message> receivedMessages = new LinkedBlockingQueue<>();
    consumer.setMessageListener(msg -> receivedMessages.add(msg));

    MessageProducer producer = session.createProducer(queue);

    // no publisher confirm
    producer.send(session.createTextMessage("hello1"));
    assertThat(receivedMessages.poll(5, TimeUnit.SECONDS)).isTextMessage().hasText("hello1");

    AtomicReference<CountDownLatch> confirmationLatch = new AtomicReference<>(
        new CountDownLatch(1));
    AtomicReference<Message> confirmedMessage = new AtomicReference<>();

    // with publisher confirm
    CompletionListenerAdapter completionListener = new CompletionListenerAdapter() {
      @Override
      public void onCompletion(Message message) {
        confirmedMessage.set(message);
        confirmationLatch.get().countDown();
      }
    };
    producer.send(session.createTextMessage("hello2"), completionListener);
    assertThat(confirmationLatch.get().await(5, TimeUnit.SECONDS)).isTrue();
    assertThat(confirmedMessage.get()).isTextMessage().hasText("hello2");
    assertThat(receivedMessages.poll(5, TimeUnit.SECONDS)).isTextMessage().hasText("hello2");

    // publisher confirm are now enabled, but "sync send" still works
    producer.send(session.createTextMessage("hello3"));
    assertThat(receivedMessages.poll(5, TimeUnit.SECONDS)).isTextMessage().hasText("hello3");

    // with publisher confirm again
    confirmationLatch.set(new CountDownLatch(1));
    producer.send(session.createTextMessage("hello4"), completionListener);
    assertThat(confirmationLatch.get().await(5, TimeUnit.SECONDS)).isTrue();
    assertThat(confirmedMessage.get()).isTextMessage().hasText("hello4");
    assertThat(receivedMessages.poll(5, TimeUnit.SECONDS)).isTextMessage().hasText("hello4");
  }

  private static abstract class CompletionListenerAdapter implements CompletionListener {

    @Override
    public void onCompletion(Message message) {

    }

    @Override
    public void onException(Message message, Exception exception) {

    }
  }

}
