// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2022 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

import static com.rabbitmq.jms.client.Utils.wrap;
import static java.lang.String.format;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import jakarta.jms.BytesMessage;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmqJmsContext implements JMSContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(RmqJmsContext.class);

  private final RMQConnection connection;
  private final int sessionMode;
  private final Lock sessionLock = new ReentrantLock();
  private volatile RMQSession session; // must be always accessed with session() method
  private volatile boolean autoStart = true;

  public RmqJmsContext(RMQConnection connection, int sessionMode) {
    if (!RMQSession.validateSessionMode(sessionMode)) {
      throw new JMSRuntimeException(
          format("cannot create session with acknowledgement mode = %d.", sessionMode));
    }
    this.connection = connection;
    this.sessionMode = sessionMode;
    this.session = null;
  }

  @Override
  public JMSContext createContext(int contextSessionMode) {
    return wrap(() -> new RmqJmsContext(this.connection, contextSessionMode));
  }

  @Override
  public JMSProducer createProducer() {
    return new RmqJmsProducer(this.session(), wrap(() -> this.session().createProducer(null)));
  }

  @Override
  public String getClientID() {
    return wrap(() -> this.connection.getClientID());
  }

  @Override
  public void setClientID(String clientID) {
    wrap(() -> this.connection.setClientID(clientID));
  }

  @Override
  public ConnectionMetaData getMetaData() {
    return wrap(this.connection::getMetaData);
  }

  @Override
  public ExceptionListener getExceptionListener() {
    return wrap(this.connection::getExceptionListener);
  }

  @Override
  public void setExceptionListener(ExceptionListener listener) {
    wrap(() -> this.connection.setExceptionListener(listener));
  }

  @Override
  public void start() {
    wrap(this.connection::start);
  }

  @Override
  public void stop() {
    wrap(this.connection::stop);
  }

  @Override
  public boolean getAutoStart() {
    return this.autoStart;
  }

  @Override
  public void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }

  @Override
  public void close() {
    try {
      wrap(this.session()::close);
    } catch (Exception e) {
      LOGGER.warn("Error while closing context session: {}", e.getMessage());
    }
    if (!this.connection.hasSessions()) {
      wrap(this.connection::close);
    }
  }

  @Override
  public BytesMessage createBytesMessage() {
    return wrap(this.session()::createBytesMessage);
  }

  @Override
  public MapMessage createMapMessage() {
    return wrap(this.session()::createMapMessage);
  }

  @Override
  public Message createMessage() {
    return wrap(this.session()::createMessage);
  }

  @Override
  public ObjectMessage createObjectMessage() {
    return wrap(() -> this.session().createObjectMessage());
  }

  @Override
  public ObjectMessage createObjectMessage(Serializable object) {
    return wrap(() -> this.session().createObjectMessage(object));
  }

  @Override
  public StreamMessage createStreamMessage() {
    return wrap(this.session()::createStreamMessage);
  }

  @Override
  public TextMessage createTextMessage() {
    return wrap(() -> this.session().createTextMessage());
  }

  @Override
  public TextMessage createTextMessage(String text) {
    return wrap(() -> this.session().createTextMessage(text));
  }

  @Override
  public boolean getTransacted() {
    return wrap(this.session()::getTransacted);
  }

  @Override
  public int getSessionMode() {
    return wrap(this.session()::getAcknowledgeMode);
  }

  @Override
  public void commit() {
    wrap(this.session()::commit);
  }

  @Override
  public void rollback() {
    wrap(this.session()::rollback);
  }

  @Override
  public void recover() {
    wrap(this.session()::recover);
  }

  @Override
  public JMSConsumer createConsumer(Destination destination) {
    return this.createConsumer(destination, null);
  }

  @Override
  public JMSConsumer createConsumer(Destination destination, String messageSelector) {
    return this.createConsumer(destination, messageSelector, false);
  }

  @Override
  public JMSConsumer createConsumer(Destination destination, String messageSelector,
      boolean noLocal) {
    maybeAutoStart();
    return new RmqJmsConsumer(this.session(),
        wrap(() -> this.session().createConsumer(destination, messageSelector, noLocal)));
  }

  private void maybeAutoStart() {
    if (autoStart) {
      this.start();
    }
  }

  @Override
  public Queue createQueue(String queueName) {
    return wrap(() -> this.session().createQueue(queueName));
  }

  @Override
  public Topic createTopic(String topicName) {
    return wrap(() -> this.session().createTopic(topicName));
  }

  @Override
  public JMSConsumer createDurableConsumer(Topic topic, String name) {
    return createDurableConsumer(topic, name, null, false);
  }

  @Override
  public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector,
      boolean noLocal) {
    maybeAutoStart();
    return new RmqJmsConsumer(this.session(),
        wrap(() -> this.session().createDurableConsumer(topic, name, messageSelector, noLocal)));
  }

  @Override
  public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
    return this.createSharedDurableConsumer(topic, name, null);
  }

  @Override
  public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
    maybeAutoStart();
    return new RmqJmsConsumer(this.session(),
        wrap(() -> this.session().createSharedDurableConsumer(topic, name, messageSelector)));
  }

  @Override
  public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
    return createSharedConsumer(topic, sharedSubscriptionName, null);
  }

  @Override
  public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName,
      String messageSelector) {
    maybeAutoStart();
    return new RmqJmsConsumer(this.session(), wrap(
        () -> this.session().createSharedConsumer(topic, sharedSubscriptionName, messageSelector)));
  }

  @Override
  public QueueBrowser createBrowser(Queue queue) {
    return wrap(() -> this.session().createBrowser(queue));
  }

  @Override
  public QueueBrowser createBrowser(Queue queue, String messageSelector) {
    return wrap(() -> this.session().createBrowser(queue, messageSelector));
  }

  @Override
  public TemporaryQueue createTemporaryQueue() {
    return wrap(this.session()::createTemporaryQueue);
  }

  @Override
  public TemporaryTopic createTemporaryTopic() {
    return wrap(this.session()::createTemporaryTopic);
  }

  @Override
  public void unsubscribe(String name) {
    wrap(() -> this.session().unsubscribe(name));
  }

  @Override
  public void acknowledge() {
    wrap(this.session()::acknowledgeMessages);
  }

  private RMQSession session() {
    try {
      if (sessionLock.tryLock(1, TimeUnit.MILLISECONDS)) {
        if (this.session == null) {
          this.session = wrap(() -> (RMQSession) this.connection.createSession(this.sessionMode));
        }
      } else {
        throw new JMSRuntimeException("Impossible to access context session");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new JMSRuntimeException("Thread interrupted while trying to access context session", "",
          e);
    }
    return this.session;
  }

}
