// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import static com.rabbitmq.jms.client.RMQMessage.JMS_MESSAGE_CORR_ID;
import static com.rabbitmq.jms.client.RMQMessage.JMS_MESSAGE_TYPE;
import static com.rabbitmq.jms.client.RMQMessage.getCharset;
import static com.rabbitmq.jms.client.Utils.wrap;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.jms.BytesMessage;
import javax.jms.CompletionListener;
import javax.jms.Destination;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

class RmqJmsProducer implements JMSProducer {

  private final Session session;
  private final MessageProducer producer;
  private final Map<String, Serializable> properties = new ConcurrentHashMap<>();
  private final Map<String, Serializable> headers = new ConcurrentHashMap<>();
  private volatile CompletionListener completionListener = null;

  RmqJmsProducer(Session session, MessageProducer producer) {
    this.session = session;
    this.producer = producer;
  }

  @Override
  public JMSProducer send(Destination destination, Message message) {
    properties.entrySet()
        .forEach(entry -> wrap(() -> message.setObjectProperty(entry.getKey(), entry.getValue())));
    headers.entrySet()
        .forEach(entry -> wrap(() -> message.setObjectProperty(entry.getKey(), entry.getValue())));
    if (completionListener == null) {
      wrap(() -> this.producer.send(destination, message));
    } else {
      wrap(() -> this.producer.send(destination, message, this.completionListener));
    }
    return this;
  }

  @Override
  public JMSProducer send(Destination destination, String body) {
    return this.send(destination, wrap(() -> session.createTextMessage(body)));
  }

  @Override
  public JMSProducer send(Destination destination, Map<String, Object> body) {
    Message message = wrap(() -> {
      MapMessage m = session.createMapMessage();
      if (body != null) {
        for (Entry<String, Object> entry : body.entrySet()) {
          m.setObject(entry.getKey(), entry.getValue());
        }
      }
      return m;
    });
    return this.send(destination, message);
  }

  @Override
  public JMSProducer send(Destination destination, byte[] body) {
    Message message = wrap(() -> {
      BytesMessage m = session.createBytesMessage();
      m.writeBytes(body);
      return m;
    });
    return this.send(destination, message);
  }

  @Override
  public JMSProducer send(Destination destination, Serializable body) {
    return this.send(destination, wrap(() -> session.createObjectMessage(body)));
  }

  @Override
  public JMSProducer setDisableMessageID(boolean value) {
    wrap(() -> this.producer.setDisableMessageID(value));
    return this;
  }

  @Override
  public boolean getDisableMessageID() {
    return wrap(() -> this.producer.getDisableMessageID());
  }

  @Override
  public JMSProducer setDisableMessageTimestamp(boolean value) {
    wrap(() -> this.producer.setDisableMessageTimestamp(value));
    return this;
  }

  @Override
  public boolean getDisableMessageTimestamp() {
    return wrap(() -> this.producer.getDisableMessageTimestamp());
  }

  @Override
  public JMSProducer setDeliveryMode(int deliveryMode) {
    wrap(() -> this.producer.setDeliveryMode(deliveryMode));
    return this;
  }

  @Override
  public int getDeliveryMode() {
    return wrap(() -> this.producer.getDeliveryMode());
  }

  @Override
  public JMSProducer setPriority(int priority) {
    wrap(() -> this.producer.setPriority(priority));
    return this;
  }

  @Override
  public int getPriority() {
    return wrap(() -> this.producer.getPriority());
  }

  @Override
  public JMSProducer setTimeToLive(long timeToLive) {
    wrap(() -> this.producer.setTimeToLive(timeToLive));
    return this;
  }

  @Override
  public long getTimeToLive() {
    return wrap(() -> this.producer.getTimeToLive());
  }

  @Override
  public JMSProducer setDeliveryDelay(long deliveryDelay) {
    wrap(() -> this.producer.setDeliveryDelay(deliveryDelay));
    return this;
  }

  @Override
  public long getDeliveryDelay() {
    return wrap(() -> this.producer.getDeliveryDelay());
  }

  @Override
  public JMSProducer setAsync(CompletionListener completionListener) {
    if (completionListener == null && this.completionListener != null) {
      this.completionListener = RMQMessageProducer.NO_OP_COMPLETION_LISTENER;
    } else {
      this.completionListener = completionListener;
    }
    return this;
  }

  @Override
  public CompletionListener getAsync() {
    return this.completionListener;
  }

  @Override
  public JMSProducer setProperty(String name, boolean value) {
    return setPropertyInternal(name, value);
  }

  @Override
  public JMSProducer setProperty(String name, byte value) {
    return setPropertyInternal(name, value);
  }

  @Override
  public JMSProducer setProperty(String name, short value) {
    return setPropertyInternal(name, value);
  }

  @Override
  public JMSProducer setProperty(String name, int value) {
    return setPropertyInternal(name, value);
  }

  @Override
  public JMSProducer setProperty(String name, long value) {
    return setPropertyInternal(name, value);
  }

  @Override
  public JMSProducer setProperty(String name, float value) {
    return setPropertyInternal(name, value);
  }

  @Override
  public JMSProducer setProperty(String name, double value) {
    return setPropertyInternal(name, value);
  }

  @Override
  public JMSProducer setProperty(String name, String value) {
    return setPropertyInternal(name, value);
  }

  @Override
  public JMSProducer setProperty(String name, Object value) {
    return setPropertyInternal(name, (Serializable) value);
  }

  private JMSProducer setPropertyInternal(String name, Serializable value) {
    this.properties.put(name, value);
    return this;
  }

  @Override
  public JMSProducer clearProperties() {
    this.properties.clear();
    return this;
  }

  @Override
  public boolean propertyExists(String name) {
    return this.properties.containsKey(name);
  }

  @Override
  public boolean getBooleanProperty(String name) {
    return wrap(() -> Utils.getBooleanProperty(this.properties, name));
  }

  @Override
  public byte getByteProperty(String name) {
    return wrap(() -> Utils.getByteProperty(this.properties, name));
  }

  @Override
  public short getShortProperty(String name) {
    return wrap(() -> Utils.getShortProperty(this.properties, name));
  }

  @Override
  public int getIntProperty(String name) {
    return wrap(() -> Utils.getIntProperty(this.properties, name));
  }

  @Override
  public long getLongProperty(String name) {
    return wrap(() -> Utils.getLongProperty(this.properties, name));
  }

  @Override
  public float getFloatProperty(String name) {
    return wrap(() -> Utils.getFloatProperty(this.properties, name));
  }

  @Override
  public double getDoubleProperty(String name) {
    return wrap(() -> Utils.getDoubleProperty(this.properties, name));
  }

  @Override
  public String getStringProperty(String name) {
    return wrap(() -> Utils.getStringProperty(this.properties, name));
  }

  @Override
  public Object getObjectProperty(String name) {
    return this.properties.get(name);
  }

  @Override
  public Set<String> getPropertyNames() {
    return Collections.unmodifiableSet(this.properties.keySet());
  }

  @Override
  public JMSProducer setJMSCorrelationIDAsBytes(byte[] correlationID) {
    String id = correlationID != null ? new String(correlationID, getCharset()) : null;
    return this.setHeaderInternal(JMS_MESSAGE_CORR_ID, id);
  }

  @Override
  public byte[] getJMSCorrelationIDAsBytes() {
    String id = Utils.getStringProperty(this.headers, JMS_MESSAGE_CORR_ID);
    return id == null ? null : id.getBytes(getCharset());
  }

  @Override
  public JMSProducer setJMSCorrelationID(String correlationID) {
    return this.setHeaderInternal(JMS_MESSAGE_CORR_ID, correlationID);
  }

  @Override
  public String getJMSCorrelationID() {
    return Utils.getStringProperty(this.headers, JMS_MESSAGE_CORR_ID);
  }

  @Override
  public JMSProducer setJMSType(String type) {
    return this.setHeaderInternal(RMQMessage.JMS_MESSAGE_TYPE, type);
  }

  @Override
  public String getJMSType() {
    return Utils.getStringProperty(this.headers, JMS_MESSAGE_TYPE);
  }

  @Override
  public JMSProducer setJMSReplyTo(Destination replyTo) {
    return this.setHeaderInternal(RMQMessage.JMS_MESSAGE_REPLY_TO, (Serializable) replyTo);
  }

  @Override
  public Destination getJMSReplyTo() {
    return (Destination) this.headers.get(RMQMessage.JMS_MESSAGE_REPLY_TO);
  }

  private JMSProducer setHeaderInternal(String name, Serializable value) {
    this.headers.put(name, value);
    return this;
  }
}
