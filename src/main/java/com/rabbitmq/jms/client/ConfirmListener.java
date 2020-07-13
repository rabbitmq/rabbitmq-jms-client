// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

/**
 * Listener to be notified of publisher confirms.
 * <p>
 * Publisher Confirms are a RabbitMQ extension to the AMQP protocol.
 * In the context of the JMS client, they allow to be asynchronously
 * notified when an outbound message has been confirmed by the broker
 * (meaning the message made it safely to the broker).
 * <p>
 * Note publisher confirms are enabled at the {@link com.rabbitmq.jms.admin.RMQConnectionFactory} level,
 * by setting a {@link ConfirmListener} instance with
 * {@link com.rabbitmq.jms.admin.RMQConnectionFactory#setConfirmListener(ConfirmListener)}. This
 * means all the AMQP {@link com.rabbitmq.client.Channel} created from the {@link com.rabbitmq.jms.admin.RMQConnectionFactory}
 * instance will have publisher confirms enabled.
 *
 * @see <a href="https://www.rabbitmq.com/confirms.html#publisher-confirms">Publisher Confirms</a>
 * @see <a href="https://www.rabbitmq.com/publishers.html#data-safety">Publisher Guide</a>
 * @see com.rabbitmq.jms.admin.RMQConnectionFactory#setConfirmListener(ConfirmListener)
 * @since 1.13.0
 */
@FunctionalInterface
public interface ConfirmListener {

    /**
     * Callback invoked when an outbound message is confirmed.
     *
     * @param context information about the confirmed message
     */
    void handle(PublisherConfirmContext context);

}
