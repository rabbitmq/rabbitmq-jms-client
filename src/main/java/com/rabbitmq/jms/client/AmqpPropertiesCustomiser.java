/* Copyright (c) 2018-2020 VMware, Inc. or its affiliates. All rights reserved. */

package com.rabbitmq.jms.client;

import com.rabbitmq.client.AMQP;

import javax.jms.Message;

/**
 * Callback to customise properties of outbound AMQP messages.
 *
 * @since 1.9.0
 */
public interface AmqpPropertiesCustomiser {

    /**
     * Customise AMQP message properties.
     *
     * @param builder    the AMQP properties builder
     * @param jmsMessage the outbound JMS message
     * @return the customised or a new AMQP properties builder
     */
    AMQP.BasicProperties.Builder customise(AMQP.BasicProperties.Builder builder, Message jmsMessage);
}
