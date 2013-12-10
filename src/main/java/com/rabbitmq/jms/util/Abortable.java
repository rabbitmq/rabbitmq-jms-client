/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

/**
 * Classes whose instances can be aborted, stopped and started (from other threads) implement this interface.
 */
public interface Abortable {
    /**
     * Cause any other implementing threads to terminate fairly quickly, signalling abnormal termination
     * to its instigator, if necessary.
     * <p>
     * Implementations of this method must be thread-safe, and throw no exceptions, except possibly {@link RuntimeException}s.
     * </p>
     */
    void abort();

    /**
     * Cause any other implementing threads to halt temporarily.
     * <p>
     * Implementations of this method must be thread-safe, and throw no exceptions, except possibly {@link RuntimeException}s.
     * </p>
     */
    void stop();

    /**
     * Cause any other halted threads to start.
     * <p>
     * Implementations of this method must be thread-safe, and throw no exceptions, except possibly {@link RuntimeException}s.
     * </p>
     */
    void start();
}
