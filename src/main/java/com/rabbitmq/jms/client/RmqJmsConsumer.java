// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import static com.rabbitmq.jms.client.Utils.wrap;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageFormatRuntimeException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.StreamMessage;

class RmqJmsConsumer implements JMSConsumer {

  private final Session session;
  private final MessageConsumer consumer;
  private final Queue<Message> failedBodyConversionMessages = new ConcurrentLinkedQueue<>();

  RmqJmsConsumer(Session session, MessageConsumer consumer) {
    this.session = session;
    this.consumer = consumer;
  }

  @Override
  public String getMessageSelector() {
    return wrap(() -> this.consumer.getMessageSelector());
  }

  @Override
  public MessageListener getMessageListener() throws JMSRuntimeException {
    return wrap(() -> this.consumer.getMessageListener());
  }

  @Override
  public void setMessageListener(MessageListener listener) throws JMSRuntimeException {
    wrap(() -> this.consumer.setMessageListener(listener));
  }

  @Override
  public Message receive() {
    return wrap(() -> this.consumer.receive());
  }

  @Override
  public Message receive(long timeout) {
    return wrap(() -> this.consumer.receive(timeout));
  }

  @Override
  public Message receiveNoWait() {
    return wrap(() -> this.consumer.receiveNoWait());
  }

  @Override
  public void close() {
    wrap(() -> this.consumer.close());
  }

  @Override
  public <T> T receiveBody(Class<T> c) {
    Message message = failedBodyConversionMessages.poll();
    if (message == null) {
      message = this.receive();
    }
    return handleBodyConversion(message, c);
  }

  @Override
  public <T> T receiveBody(Class<T> c, long timeout) {
    Message message = failedBodyConversionMessages.poll();
    if (message == null) {
      message = this.receive(timeout);
    }
    return handleBodyConversion(message, c);
  }

  @Override
  public <T> T receiveBodyNoWait(Class<T> c) {
    Message message = failedBodyConversionMessages.poll();
    if (message == null) {
      message = this.receiveNoWait();
    }
    return handleBodyConversion(message, c);
  }

  private <T> T handleBodyConversion(Message message, Class<T> c) {
    return wrap(() -> {
      if (message == null) {
        return null;
      } else if (message instanceof StreamMessage) {
        if (isAutoAckOrDupsOk()) {
          failedBodyConversionMessages.offer(message);
        }
        throw new MessageFormatRuntimeException(
            "Not possible to call receiveBody on a StreamMessage");
      } else {
        try {
          return message.getBody(c);
        } catch (MessageFormatException e) {
          if (isAutoAckOrDupsOk()) {
            failedBodyConversionMessages.offer(message);
          }
          throw new MessageFormatRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
      }
    });
  }

  private boolean isAutoAckOrDupsOk() throws JMSException {
    return this.session.getAcknowledgeMode() == Session.AUTO_ACKNOWLEDGE
        || this.session.getAcknowledgeMode() == Session.DUPS_OK_ACKNOWLEDGE;
  }
}
