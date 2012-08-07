package com.rabbitmq.jms;

import com.rabbitmq.jms.rabbitapi.RabbitAPIConnectionFactory;

public abstract class TestConnectionFactory {
    public abstract javax.jms.ConnectionFactory getConnectionFactory();
    
    public static TestConnectionFactory getTestConnectionFactory() throws Exception {
        return new RabbitAPIConnectionFactory();
    }
}
