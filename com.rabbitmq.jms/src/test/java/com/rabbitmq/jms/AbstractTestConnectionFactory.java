package com.rabbitmq.jms;

import com.rabbitmq.jms.rabbitapi.RabbitAPIConnectionFactory;

public abstract class AbstractTestConnectionFactory {
    public abstract javax.jms.ConnectionFactory getConnectionFactory();

    public static AbstractTestConnectionFactory getTestConnectionFactory() throws Exception {
        return new RabbitAPIConnectionFactory();
    }
}
