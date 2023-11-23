// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.jms.client;

import static com.rabbitmq.jms.client.RMQSession.RJMS_CLIENT_VERSION;
import static java.lang.String.format;

import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.parse.sql.SqlCompiler;
import com.rabbitmq.jms.parse.sql.SqlEvaluator;
import com.rabbitmq.jms.parse.sql.SqlExpressionType;
import com.rabbitmq.jms.parse.sql.SqlParser;
import com.rabbitmq.jms.parse.sql.SqlTokenStream;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.RMQJMSSelectorException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidSelectorException;
import javax.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Subscription {

  static final Map<String, SqlExpressionType> JMS_TYPE_IDENTS = generateJMSTypeIdents();
  private static final Logger LOGGER = LoggerFactory.getLogger(Subscription.class);
  /**
   * Selector exchange arg key for erlang selector expression
   */
  private static final String RJMS_COMPILED_SELECTOR_ARG = "rjms_erlang_selector";
  /**
   * Selector exchange arg key for client version
   */
  private static final String RJMS_VERSION_ARG = "rjms_version";
  private final List<RMQMessageConsumer> consumers = new CopyOnWriteArrayList<>();
  private final Subscriptions subscriptions;

  public final String name, queue;

  public final boolean durable, shared;

  private final String selector;
  private final boolean noLocal;

  Subscription(String name, String queue, boolean durable, boolean shared, String selector,
      boolean noLocal) {
    this(null, name, queue, durable, shared, selector, noLocal);
  }

  Subscription(Subscriptions subscriptions, String name, String queue, boolean durable,
      boolean shared, String selector,
      boolean noLocal) {
    this.subscriptions = subscriptions;
    this.name = name;
    this.queue = queue;
    this.durable = durable;
    this.shared = shared;
    this.selector = selector;
    this.noLocal = noLocal;
  }

  private static boolean messageSelectorEquals(String selector1, String selector2) {
    if (selector1 == null && selector2 == null) {
      return true;
    } else if (selector1 == null && selector2 != null) {
      return false;
    } else if (selector1 != null && selector2 == null) {
      return false;
    } else {
      return selector1.equals(selector2);
    }
  }

  private static boolean nullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  private static Map<String, SqlExpressionType> generateJMSTypeIdents() {
    Map<String, SqlExpressionType> map = new HashMap<String, SqlExpressionType>(
        6);  // six elements only
    map.put("JMSDeliveryMode", SqlExpressionType.STRING);
    map.put("JMSPriority", SqlExpressionType.ARITH);
    map.put("JMSMessageID", SqlExpressionType.STRING);
    map.put("JMSTimestamp", SqlExpressionType.ARITH);
    map.put("JMSCorrelationID", SqlExpressionType.STRING);
    map.put(RMQMessage.JMS_TYPE_HEADER, SqlExpressionType.STRING);
    return Collections.unmodifiableMap(map);
  }

  PostAction validateNewConsumer(Destination topic, boolean durable, boolean shared,
      String selector,
      boolean noLocal)
      throws JMSException {
    if (this.shared != shared) {
      throw new JMSException(format("The '%s' subscription already exists and is %s",
          this.name,
          this.shared ? "shared" : "unshared"));
    }
    if (this.durable != durable) {
      throw new IllegalArgumentException(
          format("Cannot validate a %s consumer in a %s subscription",
              durable ? "durable" : "non-durable", this.durable ? "durable" : "non-durable"));
    }
    PostAction postAction;
    if (this.durable) {
      if (this.shared) {
        postAction = validateSharedDurable(topic, selector);
      } else {
        postAction = validateUnsharedDurable(topic, selector, noLocal);
      }
    } else {
      postAction = validateSharedNonDurable(topic, selector);
    }
    return postAction;
  }

  private PostAction validateSharedDurable(Destination topic, String selector) throws JMSException {
    // * shared durable
    // ** if active (not closed) consumer, new consumer, same ID, different topic or selector
    // => JMSException or JMSRuntimeException
    // ** if no active (not closed) consumers, new consumer, same ID, different topic or selector
    // => unsubscribe old one, create a new one
    // ** same namespace as unshared durable, JMSException or JMSRuntimeException in case of collision
    PostAction postAction = PostAction.NO_OP;
    if (!this.consumers.isEmpty()) {
      boolean atLeastOneActive = this.consumers.stream().filter(c -> !c.isClosed())
          .count() > 0;
      if (atLeastOneActive) {
        RMQDestination previousTopic = this.consumers.get(0).getDestination();
        if (!topic.equals(previousTopic) || !messageSelectorEquals(selector, this.selector)) {
          throw new JMSException(
              format("The '%s' subscription has at least 1 active consumer, "
                      + "a new consumer must have the same destination and the same message selector",
                  this.name));
        }
      } else {
        RMQDestination previousTopic = this.consumers.get(0).getDestination();
        if (!topic.equals(previousTopic) || !messageSelectorEquals(selector, this.selector)) {
          // we unsubscribe and create a new subscription instance to replace this one
          postAction = c -> {
            c.session.unsubscribe(this.name);
            c.subscriptions.remove(this.durable, this.name);
            c.subscriptions.register(this.name, this.queue, this.durable, this.shared, selector,
                this.noLocal);
          };
          // the current subscription will go away, so let's clear the consumer list
          this.consumers.clear();
        }
      }
      // remove the closed consumers
      List<RMQMessageConsumer> closedConsumers = this.consumers.stream()
          .filter(c -> c.isClosed()).collect(
              Collectors.toList());
      if (!closedConsumers.isEmpty()) {
        this.consumers.removeAll(closedConsumers);
      }
    }
    return postAction;
  }

  private PostAction validateUnsharedDurable(Destination topic, String selector, boolean noLocal)
      throws JMSException {
    // * unshared durable
    // ** if consumer active (not closed), new consumer, same ID => JMSException or JMSRuntimeException
    // ** if no consumer active (not closed), new consumer, same ID, different topic, selector or noLocal
    // => unsubscribe old one, create new one
    // ** same namespace as shared durable, JMSException or JMSRuntimeException in case of collision
    PostAction postAction = PostAction.NO_OP;
    if (this.consumers.size() > 1) {
      throw new IllegalStateException(
          format("Subscription '%s' is unshared, it should not have %d consumers", this.name,
              this.consumers.size()));
    } else if (this.consumers.size() == 1) {
      RMQMessageConsumer consumer = this.consumers.get(0);
      if (consumer.isClosed()) {
        RMQDestination previousTopic = consumer.getDestination();
        if (!topic.equals(previousTopic) || !messageSelectorEquals(selector, this.selector)
            || noLocal != this.noLocal) {
          // we unsubscribe and create a new subscription instance to replace this one
          postAction = c -> {
            c.session.unsubscribe(this.name);
            c.subscriptions.remove(this.durable, this.name);
            c.subscriptions.register(this.name, this.queue, this.durable, this.shared, selector,
                noLocal);
          };
          this.consumers.remove(consumer);
        } else {
          // the existing consumer is closed, we remove it from the consumer list
          // we keep the same subscription though, it's still the same object
          this.consumers.remove(consumer);
        }
      } else {
        throw new JMSException(
            format("The '%s' subscription has already 1 active consumer", this.name));
      }
    }
    return postAction;
  }

  private PostAction validateSharedNonDurable(Destination topic, String selector)
      throws JMSException {
    // * shared non-durable
    // ** new consumer, same ID, different topic or selector => JMSException or JMSRuntimeException
    if (!this.shared) {
      throw new IllegalStateException(
          "Unshared non-durable subscriptions are not supported by this module");
    }
    if (!this.consumers.isEmpty()) {
      boolean atLeastOneActive = this.consumers.stream().filter(c -> !c.isClosed())
          .count() > 0;
      if (atLeastOneActive) {
        RMQDestination previousTopic = this.consumers.get(0).getDestination();
        if (!topic.equals(previousTopic) || !messageSelectorEquals(selector, this.selector)) {
          throw new JMSException(
              format("The '%s' subscription has at least 1 active consumer, "
                      + "a new consumer must have the same destination and the same message selector",
                  this.name));
        }
      }
    }
    return PostAction.NO_OP;
  }

  void add(RMQMessageConsumer consumer) {
    this.consumers.add(consumer);
    if (this.shared && !this.durable) {
      // shared non-durable subscriptions go away they no longer have consumers
      // adding a listener to enforce this
      consumer.addClosedListener(c -> {
        synchronized (this.subscriptions) {
          this.consumers.remove(c);
          if (this.consumers.isEmpty()) {
            this.subscriptions.remove(durable, this.name);
          }
        }
      });
    }
  }

  int consumerCount() {
    return this.consumers.size();
  }

  void createTopology(RMQDestination topic, RMQSession session, Channel channel)
      throws JMSException {
    if (this.consumers.isEmpty()) {
      try {
        session.declareRMQQueue(topic, this.queue, this.durable, false);
        if (nullOrEmpty(this.selector)) {
          // bind the queue to the exchange with the correct routing key
          channel.queueBind(this.queue, topic.getAmqpExchangeName(), topic.getAmqpRoutingKey());
        } else {
          // get this session's topic selector exchange (name)
          String selectionExchange = session.getSelectionExchange(this.durable);
          // bind it to the topic exchange with the topic routing key
          channel.exchangeBind(selectionExchange, topic.getAmqpExchangeName(),
              topic.getAmqpRoutingKey());
          this.bindSelectorQueue(channel, topic, selector, this.queue, selectionExchange);
        }
      } catch (IOException x) {
        LOGGER.error("consumer with tag '{}' could not be created", this.name, x);
        throw new RMQJMSException("RabbitMQ Exception creating Consumer", x);
      }
    }
  }

  private void bindSelectorQueue(Channel channel, RMQDestination dest, String jmsSelector,
      String queueName,
      String selectionExchange)
      throws InvalidSelectorException, IOException {
    SqlCompiler compiler = new SqlCompiler(
        new SqlEvaluator(new SqlParser(new SqlTokenStream(jmsSelector)), JMS_TYPE_IDENTS));
    if (compiler.compileOk()) {
      Map<String, Object> args = new HashMap<>(5);
      args.put(RJMS_COMPILED_SELECTOR_ARG, compiler.compile());
      args.put(RJMS_VERSION_ARG, RJMS_CLIENT_VERSION);
      // bind the queue to the topic selector exchange with the jmsSelector expression as argument
      channel.queueBind(queueName, selectionExchange, dest.getAmqpRoutingKey(), args);
    } else {
      throw new RMQJMSSelectorException(
          String.format("Selector expression failure: \"%s\".", jmsSelector));
    }
  }

  String queue() {
    return this.queue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Subscription that = (Subscription) o;
    return durable == that.durable && shared == that.shared && noLocal == that.noLocal
        && name.equals(
        that.name) && Objects.equals(selector, that.selector);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, durable, shared, selector, noLocal);
  }

  @FunctionalInterface
  interface PostAction {

    PostAction NO_OP = c -> {
    };

    void run(Context context) throws JMSException;

  }

  static class Context {

    private final RMQSession session;
    private final Subscriptions subscriptions;

    Context(RMQSession session, Subscriptions subscriptions) {
      this.session = session;
      this.subscriptions = subscriptions;
    }
  }
}
