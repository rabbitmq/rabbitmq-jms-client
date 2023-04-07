// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2022 VMware, Inc. or its affiliates. All rights reserved.

package com.rabbitmq.jms.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class Subscriptions {

  private final Map<String, Subscription> nonDurableSubscriptions = new ConcurrentHashMap<>();
  private final Map<String, Subscription> durableSubscriptions = new ConcurrentHashMap<>();

  Subscription subscription = new Subscription("Name", "Queue", true, true, "Selector", true);

  private final List<RMQMessageConsumer> consumers = new CopyOnWriteArrayList<>();

  void add(RMQMessageConsumer consumer) {
    this.consumers.add(consumer);
    if (subscription.shared && !subscription.durable) {

      consumer.addClosedListener(c -> {
        synchronized (this) {
          this.consumers.remove(c);
          if (this.consumers.isEmpty()) {
            this.remove(subscription.durable, subscription.name);
          }
        }
      });
    }
  }

  Subscription register(String name, String queue, boolean durable, boolean shared, String selector,
      boolean noLocal) {
    Map<String, Subscription> subscriptions =
        durable ? this.durableSubscriptions : this.nonDurableSubscriptions;
    return subscriptions.computeIfAbsent(name,
        n -> new Subscription(this, name, queue, durable, shared, selector, noLocal));
  }

  Subscription get(boolean durable, String name) {
    return durable ? durableSubscriptions.get(name) : nonDurableSubscriptions.get(name);
  }

  Subscription remove(boolean durable, String name) {
    return durable ? this.durableSubscriptions.remove(name)
        : this.nonDurableSubscriptions.remove(name);
  }
}
