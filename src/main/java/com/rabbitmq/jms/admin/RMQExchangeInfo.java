/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.io.Serializable;

/**
 * RabbitMQ exchange information required by a JMS {@link com.rabbitmq.jms.admin.RMQDestination RMQDestination} object.
 */
public class RMQExchangeInfo implements Serializable {

    public static final String RABBITMQ_AMQ_TOPIC_EXCHANGE_NAME = "amq.topic";
    public static final String JMS_DURABLE_TOPIC_EXCHANGE_NAME = "jms.durable.topic";
    public static final String JMS_TEMP_TOPIC_EXCHANGE_NAME = "jms.temp.topic";
    public static final String JMS_TOPIC_SELECTOR_EXCHANGE_TYPE = "x-jms-topic";

    /** TODO */
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String type;

    /**
     * Only required for serializability.
     */
    public RMQExchangeInfo() {
        this.name = null;
        this.type = null;
    }

    public RMQExchangeInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String name() { return this.name; }
    public String type() { return this.type;}
}
