/* Copyright (c) 2019-2020 VMware, Inc. or its affiliates. All rights reserved. */

package com.rabbitmq;

import java.time.Duration;

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

}
