// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2022 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

import static com.rabbitmq.jms.client.Utils.wrap;

import java.io.Serializable;
import javax.jms.BytesMessage;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmqJmsContext implements JMSContext {

  private static final Logger LOGGER = LoggerFactory.getLogger(RmqJmsContext.class);

  private final RMQConnection connection;
  private final RMQSession session;
  private volatile boolean autoStart = true;

  public RmqJmsContext(RMQConnection connection, RMQSession session) {
    this.connection = connection;
    this.session = session;
  }

  @Override
  public JMSContext createContext(int sessionMode) {
    return wrap(() -> {
      RMQSession s = (RMQSession) this.connection.createSession(sessionMode);
      return new RmqJmsContext(this.connection, s);
    });
  }

  @Override
  public JMSProducer createProducer() {
    return new RmqJmsProducer(session, wrap(() -> session.createProducer(null)));
  }

  @Override
  public String getClientID() {
    // we don't use the client ID anywhere anyway
    // dealing with it make the use of immutable properties difficult
    // FIXME support client ID if possible
    throw new UnsupportedOperationException();
  }

  @Override
  public void setClientID(String clientID) {
    // we don't use the client ID anywhere anyway
    // dealing with it make the use of immutable properties difficult
    // FIXME support client ID if possible
    throw new UnsupportedOperationException();
  }

  @Override
  public ConnectionMetaData getMetaData() {
    return wrap(this.connection::getMetaData);
  }

  @Override
  public ExceptionListener getExceptionListener() {
    return wrap(() -> this.connection.getExceptionListener());
  }

  @Override
  public void setExceptionListener(ExceptionListener listener) {
    wrap(() -> this.connection.setExceptionListener(listener));
  }

  @Override
  public void start() {
    wrap(() -> this.connection.start());
  }

  @Override
  public void stop() {
    wrap(() -> this.connection.stop());
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
      wrap(() -> this.session.close());
    } catch (Exception e) {
      LOGGER.warn("Error while closing context session: {}", e.getMessage());
    }
    if (!this.connection.hasSessions()) {
      wrap(() -> this.connection.close());
    }
  }

  @Override
  public BytesMessage createBytesMessage() {
    return wrap(() -> session.createBytesMessage());
  }

  @Override
  public MapMessage createMapMessage() {
    return wrap(() -> session.createMapMessage());
  }

  @Override
  public Message createMessage() {
    return wrap(() -> session.createMessage());
  }

  @Override
  public ObjectMessage createObjectMessage() {
    return wrap(() -> session.createObjectMessage());
  }

  @Override
  public ObjectMessage createObjectMessage(Serializable object) {
    return wrap(() -> session.createObjectMessage(object));
  }

  @Override
  public StreamMessage createStreamMessage() {
    return wrap(() -> session.createStreamMessage());
  }

  @Override
  public TextMessage createTextMessage() {
    return wrap(() -> session.createTextMessage());
  }

  @Override
  public TextMessage createTextMessage(String text) {
    return wrap(() -> session.createTextMessage(text));
  }

  @Override
  public boolean getTransacted() {
    return wrap(() -> this.session.getTransacted());
  }

  @Override
  public int getSessionMode() {
    return wrap(() -> this.session.getAcknowledgeMode());
  }

  @Override
  public void commit() {
    wrap(() -> this.session.commit());
  }

  @Override
  public void rollback() {
    wrap(() -> this.session.rollback());
  }

  @Override
  public void recover() {
    wrap(() -> this.session.recover());
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
    return new RmqJmsConsumer(this.session,
        wrap(() -> this.session.createConsumer(destination, messageSelector, noLocal)));
  }

  private void maybeAutoStart() {
    if (autoStart) {
      this.start();
    }
  }

  @Override
  public Queue createQueue(String queueName) {
    return wrap(() -> this.session.createQueue(queueName));
  }

  @Override
  public Topic createTopic(String topicName) {
    return wrap(() -> this.session.createTopic(topicName));
  }

  @Override
  public JMSConsumer createDurableConsumer(Topic topic, String name) {
    return createDurableConsumer(topic, name, null, false);
  }

  @Override
  public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector,
      boolean noLocal) {
    maybeAutoStart();
    return new RmqJmsConsumer(this.session,
        wrap(() -> this.session.createDurableConsumer(topic, name, messageSelector, noLocal)));
  }

  @Override
  public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
    return this.createSharedDurableConsumer(topic, name, null);
  }

  @Override
  public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
    maybeAutoStart();
    return new RmqJmsConsumer(this.session,
        wrap(() -> this.session.createSharedDurableConsumer(topic, name, messageSelector)));
  }

  @Override
  public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
    return createSharedConsumer(topic, sharedSubscriptionName, null);
  }

  @Override
  public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName,
      String messageSelector) {
    maybeAutoStart();
    return new RmqJmsConsumer(this.session, wrap(
        () -> this.session.createSharedConsumer(topic, sharedSubscriptionName, messageSelector)));
  }

  @Override
  public QueueBrowser createBrowser(Queue queue) {
    return wrap(() -> this.session.createBrowser(queue));
  }

  @Override
  public QueueBrowser createBrowser(Queue queue, String messageSelector) {
    return wrap(() -> this.session.createBrowser(queue, messageSelector));
  }

  @Override
  public TemporaryQueue createTemporaryQueue() {
    return wrap(() -> this.session.createTemporaryQueue());
  }

  @Override
  public TemporaryTopic createTemporaryTopic() {
    return wrap(() -> this.session.createTemporaryTopic());
  }

  @Override
  public void unsubscribe(String name) {
    wrap(() -> this.session.unsubscribe(name));
  }

  @Override
  public void acknowledge() {
    // TODO write a test for this method
    wrap(() -> this.session.acknowledgeMessages());
  }

}
