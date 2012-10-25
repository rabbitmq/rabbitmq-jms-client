package com.rabbitmq.integration.tests;

public abstract class AbstractTestConnectionFactory {
    public abstract javax.jms.ConnectionFactory getConnectionFactory();

    public static AbstractTestConnectionFactory getTestConnectionFactory() throws Exception {
        return new RabbitAPIConnectionFactory();
    }
}
