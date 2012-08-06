package com.rabbitmq.jms.basic;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TestSimpleServerConnection {

    private volatile String username = "guest";
    private volatile String password = "guest";
    private volatile String virtualHost = "/";
    private volatile String host = "localhost";
    private volatile int port = 5672;
    private volatile Connection conn = null;

    @Before
    public void setUp() throws Exception {
        AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
        AMQP.BasicProperties minBasic = bob.build();
        AMQP.BasicProperties minPersistentBasic = bob.deliveryMode(2).build();
        AMQP.BasicProperties persistentBasic = bob.priority(0).contentType("application/octet-stream").build();
        AMQP.BasicProperties persistentTextPlain = bob.contentType("text/plain").build();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(getUsername());
        factory.setPassword(getPassword());
        factory.setVirtualHost(getVirtualHost());
        factory.setHost(getHost());
        factory.setPort(getPort());
        conn = factory.newConnection();

    }

    @After
    public void tearDown() throws Exception {
        Connection conn = getConnection();
        this.conn = null;
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    public void test() throws Exception {
        final String message = "Hello, world!";
        final Channel channel = getConnection().createChannel();
        channel.exchangeDeclare("exchangeName", "direct", true);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, "exchangeName", "routingKey");
        byte[] messageBodyBytes = message.getBytes();
        channel.basicPublish("exchangeName", "routingKey", null, messageBodyBytes);
        boolean autoAck = false;
        final StringBuilder response = new StringBuilder();
        channel.basicConsume(queueName, autoAck, "myConsumerTag", new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                long deliveryTag = envelope.getDeliveryTag();
                // (process the message components here ...)
                response.append(new String(body));
                channel.basicAck(deliveryTag, false);
            }
        });
        Thread.sleep(100);
        channel.close();
        Assert.assertEquals("Received message does not match sent message.", message, response.toString());

    }

    public Connection getConnection() {
        return conn;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
