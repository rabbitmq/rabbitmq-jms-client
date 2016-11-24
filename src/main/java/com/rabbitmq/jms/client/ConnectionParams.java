/* Copyright (c) 2016 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import com.rabbitmq.client.Connection;

/**
 * Holder for {@link RMQConnection} constructor arguments.
 */
public class ConnectionParams {

    /** The TCP connection wrapper to the RabbitMQ broker */
    private com.rabbitmq.client.Connection rabbitConnection;

    /** Maximum time (in ms) to wait for close() to complete */
    private long terminationTimeout;

    /** Max number of messages to read from a browsed queue */
    private int queueBrowserReadMax;

    /** How long to wait for onMessage to return, in milliseconds */
    private int onMessageTimeoutMs;

    public Connection getRabbitConnection() {
        return rabbitConnection;
    }

    public ConnectionParams setRabbitConnection(Connection rabbitConnection) {
        this.rabbitConnection = rabbitConnection;
        return this;
    }

    public long getTerminationTimeout() {
        return terminationTimeout;
    }

    public ConnectionParams setTerminationTimeout(long terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
        return this;
    }

    public int getQueueBrowserReadMax() {
        return queueBrowserReadMax;
    }

    public ConnectionParams setQueueBrowserReadMax(int queueBrowserReadMax) {
        this.queueBrowserReadMax = queueBrowserReadMax;
        return this;
    }

    public int getOnMessageTimeoutMs() {
        return onMessageTimeoutMs;
    }

    public ConnectionParams setOnMessageTimeoutMs(int onMessageTimeoutMs) {
        this.onMessageTimeoutMs = onMessageTimeoutMs;
        return this;
    }
}
