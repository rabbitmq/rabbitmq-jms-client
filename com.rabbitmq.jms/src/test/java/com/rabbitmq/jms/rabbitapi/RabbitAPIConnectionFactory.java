package com.rabbitmq.jms.rabbitapi;

import javax.jms.ConnectionFactory;

import com.rabbitmq.jms.TestConnectionFactory;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

/**
 * Connection factory for use in integration tests.
 */
public class RabbitAPIConnectionFactory extends TestConnectionFactory {

    @Override
    public ConnectionFactory getConnectionFactory() {
        return new RMQConnectionFactory();
    }

}
