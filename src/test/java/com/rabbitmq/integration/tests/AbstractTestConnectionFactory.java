/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.integration.tests;

public abstract class AbstractTestConnectionFactory {
    public abstract javax.jms.ConnectionFactory getConnectionFactory();

    public static AbstractTestConnectionFactory getTestConnectionFactory() throws Exception {
        return getTestConnectionFactory(false, 0);
    }

    public static AbstractTestConnectionFactory getTestConnectionFactory(boolean isSsl, int qbrMax) throws Exception {
        return new RabbitAPIConnectionFactory(isSsl, qbrMax);
    }
}
