package com.rabbitmq.jms.admin;

/**
 * RabbitMQ exchange information required by a JMS {@link com.rabbitmq.jms.admin.RMQDestination RMQDestination} object.
 */
public class RMQExchangeInfo {
    private final String name;
    private final String type;

    public RMQExchangeInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String name() { return this.name; }
    public String type() { return this.type;}
}
