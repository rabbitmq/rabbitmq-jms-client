package com.rabbitmq.jms.admin;

import java.io.Serializable;

/**
 * RabbitMQ exchange information required by a JMS {@link com.rabbitmq.jms.admin.RMQDestination RMQDestination} object.
 */
public class RMQExchangeInfo implements Serializable {
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
