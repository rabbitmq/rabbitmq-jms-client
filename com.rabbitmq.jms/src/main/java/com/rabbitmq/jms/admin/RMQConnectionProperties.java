package com.rabbitmq.jms.admin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RMQConnectionProperties {
    public static final String HOST = "rabbitmq.jms.host";
    public static final String USER = "rabbitmq.jms.user";
    public static final String PASSWORD = "rabbitmq.jms.password";
    public static final String VHOST = "rabbitmq.jms.vhost";
    public static final String CHANNEL_MAX = "rabbitmq.jms.channel-max";
    public static final String FRAME_MAX = "rabbitmq.jms.frame-max";
    public static final String HEARTBEAT = "rabbitmq.jms.heartbeat";
    public static final String PORT = "rabbitmq.jms.port";
    public static final String SSL_PORT = "rabbitmq.jms.ssl.port";
    public static final String SSL_KEYSTORE = "rabbitmq.jms.ssl.keystore";
    public static final String SSL_KEYSTORE_ALIAS = "rabbitmq.jms.ssl.keystore-alias";
    public static final String SSL_KEYSTORE_PASSWORD = "rabbitmq.jms.ssl.keystore-password";
    public static final String CONNECTION_TIMEOUT = "rabbitmq.jms.connection.timeout";

    public static final String DEFAULT_USER = com.rabbitmq.client.ConnectionFactory.DEFAULT_USER;
    public static final String DEFAULT_PASS = com.rabbitmq.client.ConnectionFactory.DEFAULT_PASS;
    public static final String DEFAULT_VHOST = com.rabbitmq.client.ConnectionFactory.DEFAULT_VHOST;
    public static final int DEFAULT_CHANNEL_MAX = com.rabbitmq.client.ConnectionFactory.DEFAULT_CHANNEL_MAX;
    public static final int DEFAULT_FRAME_MAX = com.rabbitmq.client.ConnectionFactory.DEFAULT_FRAME_MAX;
    public static final int DEFAULT_HEARTBEAT = com.rabbitmq.client.ConnectionFactory.DEFAULT_HEARTBEAT;
    public static final String DEFAULT_HOST = com.rabbitmq.client.ConnectionFactory.DEFAULT_HOST;
    public static final int DEFAULT_AMQP_PORT = com.rabbitmq.client.ConnectionFactory.DEFAULT_AMQP_PORT;
    public static final int DEFAULT_AMQP_OVER_SSL_PORT = com.rabbitmq.client.ConnectionFactory.DEFAULT_AMQP_OVER_SSL_PORT;
    public static final int DEFAULT_CONNECTION_TIMEOUT = com.rabbitmq.client.ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT;

    private final Map<String, Object> properties = new HashMap<String, Object>();

    public RMQConnectionProperties() {
        properties.put(HOST, DEFAULT_HOST);
        properties.put(USER, DEFAULT_USER);
        properties.put(PASSWORD, DEFAULT_PASS);
        properties.put(VHOST, DEFAULT_VHOST);
        properties.put(CHANNEL_MAX, DEFAULT_CHANNEL_MAX);
        properties.put(FRAME_MAX, DEFAULT_FRAME_MAX);
        properties.put(HEARTBEAT, DEFAULT_HEARTBEAT);
        properties.put(PORT, DEFAULT_AMQP_PORT);
        properties.put(CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getUsername() {
        return (String) properties.get(USER);
    }

    public String getPassword() {
        return (String) properties.get(PASSWORD);
    }

}
