/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

/**
 * Classes whose instances can be aborted, stopped and started (from other threads) implement this interface.
 */
public interface Abortable {
    /**
     * Cause any other implementing threads to terminate fairly quickly, signalling abnormal termination
     * to its instigator, if necessary.
     * <p>
     * Implementations of this method must be thread-safe.
     * </p>
     */
    void abort() throws Exception;

    /**
     * Cause any other implementing threads to stop temporarily.
     * <p>
     * Implementations of this method must be thread-safe.
     * </p>
     */
    void stop() throws Exception;

    /**
     * Cause any other stopped threads to start.
     * <p>
     * Implementations of this method must be thread-safe.
     * </p>
     */
    void start() throws Exception;
}
