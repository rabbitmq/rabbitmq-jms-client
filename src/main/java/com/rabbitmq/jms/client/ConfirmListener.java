// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import javax.jms.CompletionListener;
import javax.jms.Destination;
import javax.jms.Message;

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
 * <p>
 * This interface is deprecated since the library supports JMS 2.0 Asynchronous Send
 * (<code>CompletionListener</code> API). It will be removed in RabbitMQ JMS Client 3.0.
 * <p>
 * Do not use async send methods and the {@link ConfirmListener} API
 * at the same time, the behavior when they are both in use is not determined.
 *
 * @see <a href="https://www.rabbitmq.com/confirms.html#publisher-confirms">Publisher Confirms</a>
 * @see <a href="https://www.rabbitmq.com/publishers.html#data-safety">Publisher Guide</a>
 * @see com.rabbitmq.jms.admin.RMQConnectionFactory#setConfirmListener(ConfirmListener)
 * @see javax.jms.MessageProducer#send(Message, CompletionListener)
 * @see javax.jms.MessageProducer#send(Destination, Message, CompletionListener)
 * @see javax.jms.MessageProducer#send(Message, int, int, long, CompletionListener)
 * @see javax.jms.MessageProducer#send(Destination, Message, int, int, long, CompletionListener)
 * @since 1.13.0
 * @deprecated Use the {@link javax.jms.MessageProducer} <code>send</code> methods with a {@link javax.jms.CompletionListener}
 */
@Deprecated
@FunctionalInterface
public interface ConfirmListener {

    /**
     * Callback invoked when an outbound message is confirmed.
     *
     * @param context information about the confirmed message
     */
    void handle(PublisherConfirmContext context);

}
