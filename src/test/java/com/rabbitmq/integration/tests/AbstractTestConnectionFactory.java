/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

public abstract class AbstractTestConnectionFactory {
    public abstract javax.jms.ConnectionFactory getConnectionFactory();

    public static AbstractTestConnectionFactory getTestConnectionFactory() throws Exception {
        return new RabbitAPIConnectionFactory();
    }

    public static AbstractTestConnectionFactory getSslTestConnectionFactory() throws Exception {
        return new RabbitAPIConnectionFactory(true);
    }
}
