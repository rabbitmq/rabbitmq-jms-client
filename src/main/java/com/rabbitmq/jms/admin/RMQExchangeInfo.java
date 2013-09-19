/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.io.Serializable;

/**
 * RabbitMQ exchange information required by a JMS {@link com.rabbitmq.jms.admin.RMQDestination RMQDestination} object.
 */
public class RMQExchangeInfo implements Serializable {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RMQExchangeInfo other = (RMQExchangeInfo) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    public static final String RABBITMQ_AMQ_TOPIC_EXCHANGE_NAME = "amq.topic";
    public static final String JMS_DURABLE_TOPIC_EXCHANGE_NAME = "jms.durable.topic";
    public static final String JMS_TEMP_TOPIC_EXCHANGE_NAME = "jms.temp.topic";
    public static final String JMS_TOPIC_SELECTOR_EXCHANGE_TYPE = "x-jms-topic";

    public static final String RABBITMQ_AMQ_DIRECT_EXCHANGE_NAME = "amq.direct";
    public static final String JMS_DURABLE_QUEUE_EXCHANGE_NAME = "jms.durable.queues";
    public static final String JMS_TEMP_QUEUE_EXCHANGE_NAME = "jms.temp.queues";
    public static final String JMS_SELECTOR_QUEUE_EXCHANGE_NAME = "jms.selector.queues";
    public static final String JMS_QUEUE_SELECTOR_EXCHANGE_TYPE = "x-jms-topic";

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
