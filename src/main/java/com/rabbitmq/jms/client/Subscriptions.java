// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2022 VMware, Inc. or its affiliates. All rights reserved.

package com.rabbitmq.jms.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Subscriptions {

  private final Map<String, Subscription> nonDurableSubscriptions = new ConcurrentHashMap();
  private final Map<String, Subscription> durableSubscriptions = new ConcurrentHashMap();

  Subscription register(String name, String queue, boolean durable, boolean shared, String selector,
      boolean noLocal) {
    if (durable) {
      Subscription subscription = durableSubscriptions.computeIfAbsent(name,
          n -> new Subscription(name, queue, durable, shared, selector, noLocal));
      return subscription;
    } else {
      Subscription subscription = nonDurableSubscriptions.computeIfAbsent(name,
          n -> new Subscription(name, queue, durable, shared, selector, noLocal));
      return subscription;
    }
  }

  Subscription getDurable(String name) {
    return durableSubscriptions.get(name);
  }

  Subscription removeDurable(String name) {
    return durableSubscriptions.remove(name);
  }

  Subscription getNonDurable(String name) {
    return nonDurableSubscriptions.get(name);
  }
}
