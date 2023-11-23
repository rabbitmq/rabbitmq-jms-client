// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * Integration Test to validate that RabbitMQ server is running and working
 */
public class SimpleServerConnectionIT {

    private volatile String username = "guest";
    private volatile String password = "guest";
    private volatile String virtualHost = "/";
    private volatile String host = "localhost";
    private volatile int port = 5672;
    private volatile Connection connection = null;

    @BeforeEach
    public void setUp() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(this.getUsername());
        factory.setPassword(this.getPassword());
        factory.setVirtualHost(this.getVirtualHost());
        factory.setHost(this.getHost());
        factory.setPort(this.getPort());
        this.connection = factory.newConnection();
    }

    @AfterEach
    public void tearDown() throws Exception {
        Connection conn = this.getConnection();
        this.connection = null;
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    public void testRabbitConnection() throws Exception {
        final Channel channel = this.getConnection().createChannel();
        try {
            channel.exchangeDeclare("exchangeName", "direct", true);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, "exchangeName", "routingKey");
            byte[] messageBodyBytes = "Hello, world!".getBytes();
            channel.basicPublish("exchangeName", "routingKey", null, messageBodyBytes);
            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, "myConsumerTag", new DefaultConsumer(channel) {
                @Override
                public void
                handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    // (process the message components here
                    // ...)
                    System.out.println("Received Message:" + new String(body));
                    channel.basicAck(deliveryTag, false);
                }
            });
        Thread.sleep(1000);
        } finally {
            channel.exchangeDelete("exchangeName");
            channel.close();
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualHost() {
        return this.virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
