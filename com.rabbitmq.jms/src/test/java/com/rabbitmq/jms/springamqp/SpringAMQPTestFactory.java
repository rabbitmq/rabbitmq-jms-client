package com.rabbitmq.jms.springamqp;

import com.rabbitmq.jms.TestConnectionFactory;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.connection.SingleConnectionFactory;

public class SpringAMQPTestFactory extends TestConnectionFactory {

    @Override
    public javax.jms.ConnectionFactory getConnectionFactory() {
        return new RMQConnectionFactory(new SingleConnectionFactory());
    }

}
