/* Copyright (c) 2016-2017 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import javax.jms.Message;
import javax.jms.MessageProducer;
import java.util.Map;

/**
 * Holder for {@link RMQSession} constructor arguments.
 */
public class SessionParams {

    /** The connection that creates the session */
    private RMQConnection connection;

    /** Whether the session should be transacted or not */
    private boolean transacted;

    /** How long to wait for onMessage to return, in milliseconds */
    private int onMessageTimeoutMs;

    /** Acknowledgement mode for the session */
    int mode;

    /** List of durable subscriptions */
    private Map<String, RMQMessageConsumer> subscriptions;

    /**
     * Whether {@link MessageProducer} properties (delivery mode,
     * priority, TTL) take precedence over respective {@link Message}
     * properties or not.
     * Default is true.
     */
    private boolean preferProducerMessageProperty = true;

    public RMQConnection getConnection() {
        return connection;
    }

    public SessionParams setConnection(RMQConnection connection) {
        this.connection = connection;
        return this;
    }

    public boolean isTransacted() {
        return transacted;
    }

    public SessionParams setTransacted(boolean transacted) {
        this.transacted = transacted;
        return this;
    }

    public int getOnMessageTimeoutMs() {
        return onMessageTimeoutMs;
    }

    public SessionParams setOnMessageTimeoutMs(int onMessageTimeoutMs) {
        this.onMessageTimeoutMs = onMessageTimeoutMs;
        return this;
    }

    public int getMode() {
        return mode;
    }

    public SessionParams setMode(int mode) {
        this.mode = mode;
        return this;
    }

    public Map<String, RMQMessageConsumer> getSubscriptions() {
        return subscriptions;
    }

    public SessionParams setSubscriptions(Map<String, RMQMessageConsumer> subscriptions) {
        this.subscriptions = subscriptions;
        return this;
    }

    public boolean willPreferProducerMessageProperty() {
        return preferProducerMessageProperty;
    }

    public SessionParams setPreferProducerMessageProperty(boolean preferProducerMessageProperty) {
        this.preferProducerMessageProperty = preferProducerMessageProperty;
        return this;
    }
}
