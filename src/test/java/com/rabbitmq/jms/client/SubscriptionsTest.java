// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.jms.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.RMQMessageConsumer.ClosedListener;
import com.rabbitmq.jms.client.Subscription.Context;
import com.rabbitmq.jms.client.Subscription.PostAction;
import javax.jms.JMSException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class SubscriptionsTest {

  private final boolean noLocal = false;
  private final String selector = null;
  Subscriptions subscriptions;
  String name = "subscription-name";
  @Mock
  RMQSession session;
  @Mock
  RMQMessageConsumer consumer1;
  @Mock
  RMQMessageConsumer consumer2;
  @Mock
  RMQDestination topic;
  AutoCloseable mocks;
  private boolean shared, durable;

  @BeforeEach
  void init() {
    subscriptions = new Subscriptions();
    mocks = openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  void sharedNonDurable() throws Exception {
    shared = true;
    durable = false;
    Subscription subscription = subscriptions.register(name, name, durable, shared, selector,
        noLocal);
    PostAction postAction = subscription.validateNewConsumer(topic, durable, shared, selector,
        noLocal);
    assertThat(postAction).isSameAs(PostAction.NO_OP);
    assertThat(subscription).isSameAs(subscriptions.get(durable, name));
    subscription.add(consumer1);
    assertThat(subscription.consumerCount()).isEqualTo(1);

    ArgumentCaptor<ClosedListener> closedListenerCaptor1 = ArgumentCaptor.forClass(
        ClosedListener.class);
    verify(consumer1).addClosedListener(closedListenerCaptor1.capture());

    // not creating a new subscription
    assertThat(subscriptions.register(name, name, durable, shared, selector, noLocal)).isSameAs(
        subscription);
    // registering a new consumer
    when(consumer1.isClosed()).thenReturn(false);
    when(consumer1.getDestination()).thenReturn(topic);
    postAction = subscription.validateNewConsumer(topic, durable, shared, selector, noLocal);
    assertThat(postAction).isSameAs(PostAction.NO_OP);
    assertThat(subscription).isSameAs(subscriptions.get(durable, name));
    subscription.add(consumer2);
    assertThat(subscription.consumerCount()).isEqualTo(2);

    ArgumentCaptor<ClosedListener> closedListenerCaptor2 = ArgumentCaptor.forClass(
        ClosedListener.class);
    verify(consumer2).addClosedListener(closedListenerCaptor2.capture());

    when(consumer2.isClosed()).thenReturn(false);
    // a consumer with a different message selector
    assertThatThrownBy(
        () -> subscription.validateNewConsumer(topic, durable, shared, "foo-selector", noLocal))
        .isInstanceOf(JMSException.class)
        .hasMessageContaining("has at least 1 active consumer");
    assertThat(subscription.consumerCount()).isEqualTo(2);

    // simulate the closing of the second consumer
    closedListenerCaptor2.getValue().closed(consumer2);
    assertThat(subscription.consumerCount()).isEqualTo(1);
    // subscription still exists
    assertThat(subscription).isSameAs(subscriptions.get(durable, name));

    // simulate the closing of the first consumer
    closedListenerCaptor1.getValue().closed(consumer1);
    assertThat(subscription.consumerCount()).isZero();
    // the subscription is gone
    assertThat(subscriptions.get(durable, name)).isNull();
  }

  @Test
  void sharedDurable() throws Exception {
    shared = true;
    durable = true;
    Subscription subscription = subscriptions.register(name, name, durable, shared, selector,
        noLocal);
    PostAction postAction = subscription.validateNewConsumer(topic, durable, shared, selector,
        noLocal);
    assertThat(postAction).isSameAs(PostAction.NO_OP);
    assertThat(subscription).isSameAs(subscriptions.get(durable, name));
    subscription.add(consumer1);
    assertThat(subscription.consumerCount()).isEqualTo(1);

    // not creating a new subscription
    assertThat(subscriptions.register(name, name, durable, shared, selector, noLocal)).isSameAs(
        subscription);
    // registering a new consumer
    when(consumer1.isClosed()).thenReturn(false);
    when(consumer1.getDestination()).thenReturn(topic);
    postAction = subscription.validateNewConsumer(topic, durable, shared, selector, noLocal);
    assertThat(postAction).isSameAs(PostAction.NO_OP);
    assertThat(subscription).isSameAs(subscriptions.get(durable, name));
    subscription.add(consumer2);
    assertThat(subscription.consumerCount()).isEqualTo(2);

    when(consumer2.isClosed()).thenReturn(false);
    // a consumer with a different message selector
    assertThatThrownBy(
        () -> subscription.validateNewConsumer(topic, durable, shared, "foo-selector", noLocal))
        .isInstanceOf(JMSException.class)
        .hasMessageContaining("has at least 1 active consumer");
    assertThat(subscription.consumerCount()).isEqualTo(2);

    // one consumer is closed, the validation of a new consumer should remove closed consumers
    when(consumer2.isClosed()).thenReturn(true);
    postAction = subscription.validateNewConsumer(topic, durable, shared, selector, noLocal);
    assertThat(postAction).isSameAs(PostAction.NO_OP);
    assertThat(subscription.consumerCount()).isEqualTo(1);

    // the consumer is now closed
    when(consumer1.isClosed()).thenReturn(true);
    // the consumer still returns it is closed, we try to register a new consumer, but
    // with a different selector.
    // we should get a new subscription
    subscriptions.register(name, name, durable, shared, selector, true);
    postAction = subscription.validateNewConsumer(topic, durable, shared, "foo-selector", true);
    assertThat(postAction).isNotSameAs(PostAction.NO_OP);
    postAction.run(new Context(session, subscriptions));
    Subscription sub = subscriptions.get(true, name);
    assertThat(sub).isNotSameAs(subscription);
    sub.add(consumer1);
    assertThat(sub.consumerCount()).isEqualTo(1);
  }

  @Test
  void unsharedDurable() throws Exception {
    shared = false;
    durable = true;
    Subscription subscription = subscriptions.register(name, name, durable, shared, selector,
        noLocal);
    PostAction postAction = subscription.validateNewConsumer(topic, durable, shared, selector,
        noLocal);
    assertThat(postAction).isSameAs(PostAction.NO_OP);
    assertThat(subscription).isSameAs(subscriptions.get(durable, name));
    subscription.add(consumer1);
    assertThat(subscription.consumerCount()).isEqualTo(1);

    // not creating a new subscription
    assertThat(subscriptions.register(name, name, durable, shared, selector, noLocal)).isSameAs(
        subscription);

    when(consumer1.isClosed()).thenReturn(false);
    // trying to add a shared consumer
    assertThatThrownBy(
        () -> subscription.validateNewConsumer(topic, durable, !shared, selector, noLocal))
        .isInstanceOf(JMSException.class)
        .hasMessageContaining("already exists and is unshared");

    assertThatThrownBy(
        () -> subscription.validateNewConsumer(topic, durable, shared, selector, noLocal))
        .isInstanceOf(JMSException.class)
        .hasMessageContaining("already 1 active consumer");

    when(consumer1.isClosed()).thenReturn(true);
    when(consumer1.getDestination()).thenReturn(topic);
    // the consumer is closed, adding a new one, nothing special, it's like the first one
    subscriptions.register(name, name, durable, shared, null, true);
    postAction = subscription.validateNewConsumer(topic, durable, shared, selector, noLocal);
    assertThat(postAction).isSameAs(PostAction.NO_OP);
    assertThat(subscription).isSameAs(subscriptions.get(durable, name));
    subscription.add(consumer1);
    assertThat(subscription.consumerCount()).isEqualTo(1);

    // the consumer still returns it is closed, we try to register a new consumer, but
    // with a different selector.
    // we should get a new subscription
    subscriptions.register(name, name, durable, shared, selector, true);
    postAction = subscription.validateNewConsumer(topic, durable, shared, "foo-selector", true);
    assertThat(postAction).isNotSameAs(PostAction.NO_OP);
    postAction.run(new Context(session, subscriptions));
    Subscription sub = subscriptions.get(durable, name);
    assertThat(sub).isNotSameAs(subscription);
    sub.add(consumer1);
    assertThat(sub.consumerCount()).isEqualTo(1);
  }

}
