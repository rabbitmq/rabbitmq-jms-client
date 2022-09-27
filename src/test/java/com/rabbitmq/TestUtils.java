// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2022 VMware, Inc. or its affiliates. All rights reserved.

package com.rabbitmq;

import com.rabbitmq.jms.util.Shell;
import com.rabbitmq.jms.util.Shell.ProcessState;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestUtils {

    private static final long POLLING_INTERVAL = 100;

    public static boolean waitUntil(Duration duration, ExceptionBooleanSupplier condition) {
        long elapsed = 0;
        try {
            while (!condition.getAsBoolean() && elapsed <= duration.toMillis()) {

                try {
                    Thread.sleep(POLLING_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                elapsed += POLLING_INTERVAL;
            }
            return condition.getAsBoolean();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public interface ExceptionBooleanSupplier {

        boolean getAsBoolean() throws Exception;

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

    private static class DisabledIfTlsNotEnabledCondition implements ExecutionCondition {

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            if (tlsAvailable()) {
                return ConditionEvaluationResult.enabled("TLS is enabled");
            } else {
                return ConditionEvaluationResult.disabled("TLS is disabled");
            }
        }
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ExtendWith(DisabledIfTlsNotEnabledCondition.class)
    public @interface DisabledIfTlsNotEnabled {}

    public static String queueName(TestInfo info) {
        return queueName(info.getTestClass().get(), info.getTestMethod().get());
    }

    private static String queueName(Class<?> testClass, Method testMethod) {
        String uuid = UUID.randomUUID().toString();
        return String.format(
            "%s_%s%s",
            testClass.getSimpleName(), testMethod.getName(), uuid.substring(uuid.length() / 2));
    }
}
