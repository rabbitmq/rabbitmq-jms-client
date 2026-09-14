// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.TestUtils;
import jakarta.jms.QueueConnection;
import jakarta.jms.QueueConnectionFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.TestInfo;

public abstract class AbstractAmqpITQueue {
    protected QueueConnectionFactory connFactory;
    protected QueueConnection queueConn;

    private ConnectionFactory rabbitConnFactory = new ConnectionFactory();
    protected Connection rabbitConn;
    protected Channel channel;
    protected String q;

    @BeforeEach
    public void setUp(TestInfo info) throws Exception {
        this.q = TestUtils.name(info);
        this.connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        customise((RMQConnectionFactory) connFactory);
        this.queueConn = connFactory.createQueueConnection();

        this.rabbitConn = rabbitConnFactory.newConnection();
        this.channel = rabbitConn.createChannel();
    }

    protected void customise(RMQConnectionFactory connectionFactory) {

    }

    @AfterEach
    public void tearDown() throws Exception {
        if (this.queueConn != null) this.queueConn.close();
        if (this.channel != null) this.channel.close();
        if (this.rabbitConn != null) this.rabbitConn.close();
    }

}
