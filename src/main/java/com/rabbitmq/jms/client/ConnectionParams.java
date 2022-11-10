// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2016-2022 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.jms.util.WhiteListObjectInputStream;

import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import java.util.function.BiFunction;
import java.util.List;

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
     * {@link jakarta.jms.MessageListener} or not.
     * Default is false.
     *
     * @since 1.7.0
     * @see ConnectionParams#requeueOnTimeout
     */
    private boolean requeueOnMessageListenerException = false;

    /**
     * Whether to requeue a message that timed out or not.
     *
     * Only taken into account if requeueOnMessageListenerException is true.
     * Default is false.
     *
     * @since 2.3.0
     * @see ConnectionParams#requeueOnMessageListenerException
     */
    private boolean requeueOnTimeout = false;

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
    private boolean cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose = false;

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

    private boolean keepTextMessageType = false;

    private List<String> trustedPackages = WhiteListObjectInputStream.DEFAULT_TRUSTED_PACKAGES;

    private boolean validateSubscriptionNames = false;

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

    public boolean willNackOnRollback() {
        return nackOnRollback;
    }

    public ConnectionParams setNackOnRollback(boolean nackOnRollback) {
        this.nackOnRollback = nackOnRollback;
        return this;
    }

    public boolean isCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose() {
        return this.cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose;
    }

    public ConnectionParams setCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose(boolean cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose) {
        this.cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose = cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose;
        return this;
    }

    public BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> getAmqpPropertiesCustomiser() {
        return amqpPropertiesCustomiser;
    }

    public ConnectionParams setAmqpPropertiesCustomiser(BiFunction<AMQP.BasicProperties.Builder, Message, AMQP.BasicProperties.Builder> amqpPropertiesCustomiser) {
        this.amqpPropertiesCustomiser = amqpPropertiesCustomiser;
        return this;
    }

    public SendingContextConsumer getSendingContextConsumer() {
        return sendingContextConsumer;
    }

    public ConnectionParams setSendingContextConsumer(SendingContextConsumer sendingContextConsumer) {
        this.sendingContextConsumer = sendingContextConsumer;
        return this;
    }

    public ReceivingContextConsumer getReceivingContextConsumer() {
        return receivingContextConsumer;
    }

    public ConnectionParams setReceivingContextConsumer(ReceivingContextConsumer receivingContextConsumer) {
        this.receivingContextConsumer = receivingContextConsumer;
        return this;
    }

    public ConnectionParams setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
        return this;
    }

    public ConfirmListener getConfirmListener() {
        return confirmListener;
    }

    public ConnectionParams setKeepTextMessageType(boolean keepTextMessageType) {
        this.keepTextMessageType = keepTextMessageType;
        return this;
    }

    public boolean isKeepTextMessageType() {
        return keepTextMessageType;
    }

    public ConnectionParams setTrustedPackages(List<String> trustedPackages) {
        this.trustedPackages = trustedPackages;
        return this;
    }

    public List<String> getTrustedPackages() {
        return trustedPackages;
    }

    public ConnectionParams setRequeueOnTimeout(boolean requeueOnTimeout) {
        this.requeueOnTimeout = requeueOnTimeout;
        return this;
    }

    public boolean willRequeueOnTimeout() {
        return requeueOnTimeout;
    }

    public ConnectionParams setValidateSubscriptionNames(boolean validateSubscriptionNames) {
        this.validateSubscriptionNames = validateSubscriptionNames;
        return this;
    }

    public boolean isValidateSubscriptionNames() {
        return validateSubscriptionNames;
    }
}
