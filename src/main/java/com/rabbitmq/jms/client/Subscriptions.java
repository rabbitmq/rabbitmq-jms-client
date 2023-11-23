// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.jms.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Subscriptions {

  private final Map<String, Subscription> nonDurableSubscriptions = new ConcurrentHashMap<>();
  private final Map<String, Subscription> durableSubscriptions = new ConcurrentHashMap<>();

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
