// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.jms.client.RMQConnection;
import com.rabbitmq.jms.util.Shell;
import com.rabbitmq.jms.util.Shell.ProcessState;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.jms.CompletionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestUtils {

  public static final Duration TEN_SECONDS = Duration.ofSeconds(10);

  private static final Duration POLLING_INTERVAL = Duration.ofMillis(100);

  public static boolean waitUntil(ExceptionBooleanSupplier condition) {
    return waitUntil(TEN_SECONDS, condition);
  }
  public static boolean waitUntil(Duration duration, ExceptionBooleanSupplier condition) {
    Boolean result = waitUntilNotNull(duration,
        () -> condition.getAsBoolean() ? Boolean.TRUE : null);
    return result != null;
  }

  public static <T> T waitUntilNotNull(Callable<T> operation) {
    return waitUntilNotNull(TEN_SECONDS, operation);
  }

  public static <T> T waitUntilNotNull(Duration duration, Callable<T> operation) {
    long elapsed = 0;
    try {
      T result = operation.call();
      while (result == null && elapsed <= duration.toNanos()) {
        long start = System.nanoTime();
        System.out.println(result + " " + elapsed + " " + duration.toNanos());
        try {
          Thread.sleep(POLLING_INTERVAL.toMillis());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        }
        elapsed += POLLING_INTERVAL.toMillis();
        result = operation.call();
        elapsed += (System.nanoTime() - start);
      }
      if (result == null) {
        Assertions.fail("Condition did not return non-null result");
      }
      return result == null ? operation.call() : result;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  public static CompletionListener onCompletion(CallableConsumer<Message> onCompletionListener) {
    return new CompletionListener() {
      @Override
      public void onCompletion(Message message) {
        try {
          onCompletionListener.accept(message);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onException(Message message, Exception exception) {

      }
    };
  }

  private static boolean tlsAvailable() {
    if (Shell.rabbitmqctlCommand() == null) {
      throw new IllegalStateException(
          "rabbitmqctl.bin system property not set, cannot check if TLS is enabled");
    } else {
      try {
        ProcessState process = Shell.rabbitmqctl("status");
        String output = process.output();
        return output.contains("amqp/ssl");
      } catch (Exception e) {
        throw new RuntimeException("Error while trying to detect TLS: " + e.getMessage());
      }
    }
  }
  private static boolean isPluginActivated(String plugin) {
    if (Shell.rabbitmqctlCommand() == null) {
      throw new IllegalStateException(
              "rabbitmqctl.bin system property not set, cannot check if TLS is enabled");
    } else {
      try {
        ProcessState process = Shell.rabbitmqctl("status");
        String output = process.output();
        return output.contains(plugin);
      } catch (Exception e) {
        throw new RuntimeException("Error while detecting if plugin " + plugin + " is enabled: " + e.getMessage());
      }
    }
  }

  public static String name(TestInfo info) {
    return name(info.getTestClass().get(), info.getTestMethod().get());
  }

  private static String name(Class<?> testClass, Method testMethod) {
    String uuid = UUID.randomUUID().toString();
    return String.format(
        "%s_%s%s",
        testClass.getSimpleName(), testMethod.getName(), uuid.substring(uuid.length() / 2));
  }

  public static String text(Message message) {
    try {
      return message.getBody(String.class);
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }

  public interface ExceptionBooleanSupplier {

    boolean getAsBoolean() throws Exception;

  }

  public static Connection amqpConnection(jakarta.jms.Connection connection) {
    try {
      Field connectionField = RMQConnection.class.getDeclaredField("rabbitConnection");
      connectionField.setAccessible(true);
      return (Connection) connectionField.get(connection);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @FunctionalInterface
  public interface CallableConsumer<T> {

    void accept(T t) throws Exception;
  }

  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @ExtendWith(SkipIfTlsNotActivatedCondition.class)
  public @interface SkipIfTlsNotActivated {

  }

  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @ExtendWith(SkipIfDelayedMessageExchangePluginNotActivatedCondition.class)
  public @interface SkipIfDelayedMessageExchangePluginNotActivated {

  }

  private static class SkipIfTlsNotActivatedCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
      if (tlsAvailable()) {
        return ConditionEvaluationResult.enabled("TLS is available");
      } else {
        return ConditionEvaluationResult.disabled("TLS is not available");
      }
    }
  }
  private static class SkipIfDelayedMessageExchangePluginNotActivatedCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
      String plugin = "rabbitmq_delayed_message_exchange";
      try {
        boolean activated = isPluginActivated(plugin);
        return activated ? ConditionEvaluationResult.enabled(plugin + " plugin is activated") :
            ConditionEvaluationResult.disabled(plugin + " plugin is not activated");
      } catch(Exception e) {
        return ConditionEvaluationResult.disabled(plugin + " plugin is not activated");
      }
    }
  }


}
