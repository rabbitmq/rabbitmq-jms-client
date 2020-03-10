/* Copyright (c) 2016-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.client;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.jms.util.WhiteListObjectInputStream;

import javax.jms.Message;
import javax.jms.MessageProducer;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

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

    /**
     * Whether requeue message on {@link RuntimeException} in the
     * {@link javax.jms.MessageListener} or not.
     * Default is false.
     */
    private boolean requeueOnMessageListenerException = false;

    /**
     * Whether to commit nack on rollback or not.
     * Default is false.
     *
     * @since 1.14.0
     */
    private boolean nackOnRollback = false;

    /**
     * Whether using auto-delete for server-named queues for non-durable topics.
     * If set to true, those queues will be deleted when the session is closed.
     * If set to false, queues will be deleted when the owning connection is closed.
     * Default is false.
     *
     * @since 1.8.0
     */
    private boolean cleanUpServerNamedQueuesForNonDurableTopics = false;

    /**
     * Callback to customise properties of outbound AMQP messages.
     *
     * @since 1.9.0
     */
    private BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser;

    /**
     * Callback before sending a message.
     *
     * @since 1.11.0
     */
    private SendingContextConsumer sendingContextConsumer;

    /**
     * Callback before receiving a message.
     *
     * @since 1.11.0
     */
    private ReceivingContextConsumer receivingContextConsumer;

    /**
     * Callback for publisher confirms.
     *
     * @since 1.13.0
     */
    private ConfirmListener confirmListener;

    private List<String> trustedPackages = WhiteListObjectInputStream.DEFAULT_TRUSTED_PACKAGES;

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

    public boolean willRequeueOnMessageListenerException() {
        return requeueOnMessageListenerException;
    }

    public SessionParams setRequeueOnMessageListenerException(boolean requeueOnMessageListenerException) {
        this.requeueOnMessageListenerException = requeueOnMessageListenerException;
        return this;
    }

    public boolean willNackOnRollback() {
        return nackOnRollback;
    }

    public SessionParams setNackOnRollback(boolean nackOnRollback) {
        this.nackOnRollback = nackOnRollback;
        return this;
    }

    public boolean isCleanUpServerNamedQueuesForNonDurableTopics() {
        return cleanUpServerNamedQueuesForNonDurableTopics;
    }

    public SessionParams setCleanUpServerNamedQueuesForNonDurableTopics(boolean cleanUpServerNamedQueuesForNonDurableTopics) {
        this.cleanUpServerNamedQueuesForNonDurableTopics = cleanUpServerNamedQueuesForNonDurableTopics;
        return this;
    }

    public BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> getAmqpPropertiesCustomiser() {
        return amqpPropertiesCustomiser;
    }

    public SessionParams setAmqpPropertiesCustomiser(BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser) {
        this.amqpPropertiesCustomiser = amqpPropertiesCustomiser;
        return this;
    }

    public SessionParams setSendingContextConsumer(SendingContextConsumer sendingContextConsumer) {
        this.sendingContextConsumer = sendingContextConsumer;
        return this;
    }

    public SendingContextConsumer getSendingContextConsumer() {
        return sendingContextConsumer;
    }

    public SessionParams setReceivingContextConsumer(ReceivingContextConsumer receivingContextConsumer) {
        this.receivingContextConsumer = receivingContextConsumer;
        return this;
    }

    public ReceivingContextConsumer getReceivingContextConsumer() {
        return receivingContextConsumer;
    }

    public SessionParams setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
        return this;
    }

    public ConfirmListener getConfirmListener() {
        return confirmListener;
    }

    public SessionParams setTrustedPackages(List<String> trustedPackages) {
        this.trustedPackages = trustedPackages;
        return this;
    }

    public List<String> getTrustedPackages() {
        return trustedPackages;
    }
}
