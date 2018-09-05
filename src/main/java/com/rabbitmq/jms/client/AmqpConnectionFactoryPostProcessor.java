/* Copyright (c) 2018 Pivotal Software, Inc. All rights reserved. */

package com.rabbitmq.jms.client;

import com.rabbitmq.client.ConnectionFactory;

/**
 * Callback to post-process the AMQP {@link com.rabbitmq.client.ConnectionFactory}.
 *
 * <p>
 * This callback is enforced just before the creation of the AMQP connection.
 * It can be used to for any customisation: TLS, threading, etc.
 *
 * @since 1.10.0
 */
public interface AmqpConnectionFactoryPostProcessor {

    /**
     * Customise the {@link ConnectionFactory} before AMQP connection creation.
     *
     * @param connectionFactory
     */
    void postProcess(ConnectionFactory connectionFactory);
}
