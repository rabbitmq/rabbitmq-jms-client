package com.rabbitmq.jms.rabbitapi;

import javax.jms.ConnectionFactory;

import com.rabbitmq.jms.AbstractTestConnectionFactory;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

/**
 * Connection factory for use in integration tests.
 */
public class RabbitAPIConnectionFactory extends AbstractTestConnectionFactory {

    @Override
    public ConnectionFactory getConnectionFactory() {
        return new RMQConnectionFactory();
    }

}
