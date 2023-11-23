// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import java.nio.charset.StandardCharsets;
import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JmsTypeHeaderAutomaticallySetIT {

  private static final String QUEUE_NAME =
      "test.queue." + JmsTypeHeaderAutomaticallySetIT.class.getCanonicalName();
  private static final String MESSAGE = "Hello " + JmsTypeHeaderAutomaticallySetIT.class.getName();
  private static final long TEST_RECEIVE_TIMEOUT = 1000; // one second
  protected QueueConnection queueConn;
  protected Connection rabbitConn;
  protected Channel channel;
  private RMQConnectionFactory connFactory;
  private final ConnectionFactory rabbitConnFactory = new ConnectionFactory();

  @BeforeEach
  public void setUp() throws Exception {
    this.rabbitConn = rabbitConnFactory.newConnection();
    this.channel = rabbitConn.createChannel();

    channel.queueDeclare(
        QUEUE_NAME,
        false, // durable
        false, // exclusive
        false, // autoDelete
        null // options
        );
  }

  @AfterEach
  public void tearDown() throws Exception {
    channel.queueDelete(QUEUE_NAME);
    this.queueConn.close();
    this.channel.close();
    this.rabbitConn.close();
  }

  @Test
  void jmsTypeHeaderShouldNotBeSetIfOptionDisabled() throws Exception {
    this.connFactory = connectionFactory();
    this.queueConn = connFactory.createQueueConnection();

    queueConn.start();
    QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
    Queue queue = new RMQDestination(QUEUE_NAME, "", QUEUE_NAME, null);

    QueueSender queueSender = queueSession.createSender(queue);
    queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

    Message message = queueSession.createTextMessage(MESSAGE);

    queueSender.send(message);
    queueSession.close();

    queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
    queue = new RMQDestination(QUEUE_NAME, null, null, QUEUE_NAME);
    QueueReceiver queueReceiver = queueSession.createReceiver(queue);
    message = queueReceiver.receive(TEST_RECEIVE_TIMEOUT);

    assertThat(message).isNotNull().isInstanceOf(BytesMessage.class);

    BytesMessage msg = (BytesMessage) message;
    byte [] body = new byte[(int) msg.getBodyLength()];
    msg.readBytes(body);
    assertThat(body).asString(StandardCharsets.UTF_8).isEqualTo(MESSAGE);
  }

  @Test
  void jmsTypeHeaderShouldBeSetIfOptionEnabled() throws Exception {
    this.connFactory = connectionFactory();
    this.connFactory.setKeepTextMessageType(true);
    this.queueConn = connFactory.createQueueConnection();

    queueConn.start();
    QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
    Queue queue = new RMQDestination(QUEUE_NAME, "", QUEUE_NAME, null);

    QueueSender queueSender = queueSession.createSender(queue);
    queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

    Message message = queueSession.createTextMessage(MESSAGE);

    queueSender.send(message);
    queueSession.close();

    queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
    queue = new RMQDestination(QUEUE_NAME, null, null, QUEUE_NAME);
    QueueReceiver queueReceiver = queueSession.createReceiver(queue);
    message = queueReceiver.receive(TEST_RECEIVE_TIMEOUT);

    assertThat(message).isNotNull().isInstanceOf(TextMessage.class);

    TextMessage msg = (TextMessage) message;
    assertThat(msg.getText()).isEqualTo(MESSAGE);
  }

  private RMQConnectionFactory connectionFactory() throws Exception {
    return (RMQConnectionFactory)
        AbstractTestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
  }
}
