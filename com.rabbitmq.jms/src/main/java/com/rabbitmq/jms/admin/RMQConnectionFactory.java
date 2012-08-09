package com.rabbitmq.jms.admin;

import java.io.IOException;
import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

import com.rabbitmq.jms.client.RMQConnection;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ Implementation of JMS {@link ConnectionFactory}
 */
public class RMQConnectionFactory implements ConnectionFactory, Referenceable, Serializable, QueueConnectionFactory, TopicConnectionFactory {

    /** TODO */
    private static final long serialVersionUID = -4953157213762979615L;
    
    private String username = "guest";
    private String password = "guest";
    private String virtualHost = "/";
    private String host = "localhost";
    private int port = 5672;

    @Override
    public Connection createConnection() throws JMSException {
        return this.createConnection(username, password);
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost(getVirtualHost());
        factory.setHost(getHost());
        factory.setPort(getPort());
        com.rabbitmq.client.Connection rabbitConnection = null;
        try {
            rabbitConnection = factory.newConnection();
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        return new RMQConnection(rabbitConnection);
    }

    @Override
    public Reference getReference() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        return (TopicConnection) this.createConnection();
    }

    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        return (TopicConnection) this.createConnection(userName, password);
    }

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        return (QueueConnection) this.createConnection();
    }

    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        return (QueueConnection) this.createConnection(userName, password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
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
