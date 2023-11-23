// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.


package com.rabbitmq;

import com.rabbitmq.client.GetResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.assertj.core.api.MapAssert;
import org.assertj.core.internal.ByteArrays;

public abstract class Assertions {

  private Assertions() {

  }

  public static JmsMessageAssert assertThat(Message message) {
    return new JmsMessageAssert(message);
  }

  public static AmqpMessageAssert assertThat(GetResponse response) {
    return new AmqpMessageAssert(response);
  }

  public static CountDownLatchAssert assertThat(CountDownLatch latch) {
    return new CountDownLatchAssert(latch);
  }

  public static class JmsMessageAssert extends AbstractObjectAssert<JmsMessageAssert, Message> {

    private static final String JMS_X_DELIVERY_COUNT = "JMSXDeliveryCount";

    public JmsMessageAssert(Message message) {
      super(message, JmsMessageAssert.class);
    }

    public static JmsMessageAssert assertThat(Message message) {
      return new JmsMessageAssert(message);
    }

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
        failWithMessage("Delivery count is expected to be %d but is %d", expectedDeliveryCount,
            actualDeliveryCount);
      }
      return this;
    }

    public JmsMessageAssert isTextMessage() {
      isNotNull();
      if (!(this.actual instanceof TextMessage)) {
        failWithMessage("Expected a text message, but is %s", this.actual.getClass().getName());
      }
      return this;
    }

    public JmsMessageAssert hasText(String expected) throws JMSException {
      isNotNull();
      isTextMessage();
      if (!expected.equals(this.actual.getBody(String.class))) {
        failWithMessage("Expected %s but got %s", expected, this.actual.getBody(String.class));
      }
      return this;
    }

    public JmsMessageAssert hasType(String expected) throws JMSException {
      isNotNull();
      if (!expected.equals(this.actual.getJMSType())) {
        failWithMessage("Expected %s JMSType but got %s", expected, this.actual.getJMSType());
      }
      return this;
    }

    public JmsMessageAssert hasProperty(String key, Object expectedValue) throws JMSException {
      isNotNull();
      Object value = actual.getObjectProperty(key);
      if (value == null) {
        failWithMessage("Expected %s property but is not present", key);
      }
      if (!expectedValue.equals(value)) {
        failWithMessage("Expected %s for property %s value but got %s", expectedValue, key, value);
      }
      return this;
    }


  }

  public static class AmqpMessageAssert extends
      AbstractObjectAssert<AmqpMessageAssert, GetResponse> {

    public AmqpMessageAssert(GetResponse response) {
      super(response, AmqpMessageAssert.class);
    }

    public static AmqpMessageAssert assertThat(GetResponse response) {
      return new AmqpMessageAssert(response);
    }

    public AmqpMessageAssert hasBody(byte[] expected) {
      isNotNull();
      ByteArrays.instance()
          .assertNotEmpty(getWritableAssertionInfo(), actual.getBody());
      ByteArrays.instance()
          .assertContainsExactly(getWritableAssertionInfo(), actual.getBody(), expected);
      return this;
    }

    public AmqpMessageAssert hasBody(String expected) {
      isNotNull();
      String body = new String(actual.getBody(), StandardCharsets.UTF_8);
      if (!expected.equals(body)) {
        failWithMessage("Expected %s but got %s", expected, body);
      }
      return this;
    }

    public MapAssert<String, Object> headers() {
      return AssertionsForInterfaceTypes.assertThat(actual.getProps().getHeaders());
    }


  }

  public static class CountDownLatchAssert extends
      AbstractObjectAssert<CountDownLatchAssert, CountDownLatch> {

    public CountDownLatchAssert(CountDownLatch latch) {
      super(latch, CountDownLatchAssert.class);
    }

    public static CountDownLatchAssert assertThat(CountDownLatch latch) {
      return new CountDownLatchAssert(latch);
    }

    public CountDownLatchAssert completes() throws InterruptedException {
      return completes(Duration.ofSeconds(10));
    }

    public CountDownLatchAssert completes(Duration timeout) throws InterruptedException {
      isNotNull();
      boolean completed = actual.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (!completed) {
        failWithMessage("Expected to complete in %s but did not", timeout.toString());
      }
      return this;
    }
  }
}
