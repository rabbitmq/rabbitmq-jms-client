/* Copyright (c) 2016-2017 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import com.rabbitmq.client.Connection;

import javax.jms.Message;
import javax.jms.MessageProducer;

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

    /**
     * QoS setting for channels
     *
     * @see com.rabbitmq.client.Channel#basicQos(int)
     */
    private int channelsQos = RMQConnection.NO_CHANNEL_QOS;

    /**
     * Whether {@link MessageProducer} properties (delivery mode,
     * priority, TTL) take precedence over respective {@link Message}
     * properties or not.
     * Default is true.
     */
    private boolean preferProducerMessageProperty = true;

    /**
     * Whether requeue message on {@link RuntimeException} in the
     * {@link javax.jms.MessageListener} or not.
     * Default is false.
     */
    private boolean requeueOnMessageListenerException = false;

    /**
     * Whether using auto-delete for server-named queues for non-durable topics.
     * If set to true, those queues will be deleted when the session is closed.
     * If set to false, queues will be deleted when the owning connection is closed.
     * Default is false.
     * @since 1.8.0
     */
    private boolean cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose = false;

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

    public int getChannelsQos() {
        return channelsQos;
    }

    public ConnectionParams setChannelsQos(int channelsQos) {
        this.channelsQos = channelsQos;
        return this;
    }

    public boolean willPreferProducerMessageProperty() {
        return preferProducerMessageProperty;
    }

    public ConnectionParams setPreferProducerMessageProperty(boolean preferProducerMessageProperty) {
        this.preferProducerMessageProperty = preferProducerMessageProperty;
        return this;
    }

    public boolean willRequeueOnMessageListenerException() {
        return requeueOnMessageListenerException;
    }

    public ConnectionParams setRequeueOnMessageListenerException(boolean requeueOnMessageListenerException) {
        this.requeueOnMessageListenerException = requeueOnMessageListenerException;
        return this;
    }

    public boolean isCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose() {
        return this.cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose;
    }

    public ConnectionParams setCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose(boolean cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose) {
        this.cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose = cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose;
        return this;
    }
}
