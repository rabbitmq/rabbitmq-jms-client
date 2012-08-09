//
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License
// at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
// the License for the specific language governing rights and
// limitations under the License.
//
// The Original Code is RabbitMQ.
//
// The Initial Developer of the Original Code is VMware, Inc.
// Copyright (c) 2012 VMware, Inc. All rights reserved.
//

package com.rabbitmq.integration.tests;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.integration.IntegrationTest;

/**
 * TODO: Document the purpose of this test.
 */
@Category(IntegrationTest.class)
public class TestSimpleServerConnection {
    // TODO: Refactor into integration test.
    // TODO: Remove unneeded publics.
    // TODO: Remove getters and setters?

    private volatile String username = "guest";
    private volatile String password = "guest";
    private volatile String virtualHost = "/";
    private volatile String host = "localhost";
    private volatile int port = 5672;
    private volatile Connection conn = null;

    @Before
    public void setUp() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(this.getUsername());
        factory.setPassword(this.getPassword());
        factory.setVirtualHost(this.getVirtualHost());
        factory.setHost(this.getHost());
        factory.setPort(this.getPort());
        this.conn = factory.newConnection();
    }

    @After
    public void tearDown() throws Exception {
        Connection conn = this.getConnection();
        this.conn = null;
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    public void test() throws Exception {
        final Channel channel = this.getConnection().createChannel();
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
        channel.close();
    }

    public Connection getConnection() {
        return this.conn;
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
