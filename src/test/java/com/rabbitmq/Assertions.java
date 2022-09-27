// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2022 VMware, Inc. or its affiliates. All rights reserved.


package com.rabbitmq;

import javax.jms.JMSException;
import javax.jms.Message;
import org.assertj.core.api.AbstractObjectAssert;

public abstract class Assertions {

  public static JmsMessageAssert assertThat(Message message) {
    return new JmsMessageAssert(message);
  }

  public static class JmsMessageAssert extends AbstractObjectAssert<JmsMessageAssert, Message> {

    private static final String JMS_X_DELIVERY_COUNT = "JMSXDeliveryCount";

    public JmsMessageAssert isRedelivered() throws JMSException {
      isNotNull();
      if (!actual.getJMSRedelivered()) {
        failWithMessage("Message is expected to be redelivered");
      }
      return this;
    }

    public JmsMessageAssert isNotRedelivered() throws JMSException {
      isNotNull();
      if (actual.getJMSRedelivered()) {
        failWithMessage("Message is not expected to be redelivered");
      }
      return this;
    }

    public JmsMessageAssert hasDeliveryCount(int expectedDeliveryCount) throws JMSException {
      isNotNull();
      int actualDeliveryCount = this.actual.getIntProperty(JMS_X_DELIVERY_COUNT);
      if (actualDeliveryCount != expectedDeliveryCount) {
        failWithMessage("Delivery count is expected to be %d but is %d", expectedDeliveryCount, actualDeliveryCount);
      }
      return this;
    }

    public JmsMessageAssert(Message message) {
      super(message, JmsMessageAssert.class);
    }

    public static JmsMessageAssert assertThat(Message message) {
      return new JmsMessageAssert(message);
    }

  }

}
