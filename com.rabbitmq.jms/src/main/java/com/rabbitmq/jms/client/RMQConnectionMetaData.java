package com.rabbitmq.jms.client;

import java.util.Enumeration;

import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;

/**
 * Meta data for {@link RMQConnection}
 */
public class RMQConnectionMetaData implements ConnectionMetaData {

    private static final String JMS_PROVIDER_NAME = "RabbitMQ";
    private static final String RABBITMQ_VERSION = "2.8.3";
    private static final int RABBITMQ_MINOR_VERSION = 8;
    private static final int RABBITMQ_MAJOR_VERSION = 2;
    private static final String JMS_VERSION = "1.1";
    private static final int JMS_MAJOR_VERSION = 1;
    private static final int JMS_MINOR_VERSION = 1;

    @Override
    public String getJMSVersion() throws JMSException {
        return JMS_VERSION;
    }

    @Override
    public int getJMSMajorVersion() throws JMSException {
        return JMS_MAJOR_VERSION;
    }

    @Override
    public int getJMSMinorVersion() throws JMSException {
        return JMS_MINOR_VERSION;
    }

    @Override
    public String getJMSProviderName() throws JMSException {
        return JMS_PROVIDER_NAME;
    }

    @Override
    public String getProviderVersion() throws JMSException {
        return RABBITMQ_VERSION;
    }

    @Override
    public int getProviderMajorVersion() throws JMSException {
        return RABBITMQ_MAJOR_VERSION;
    }

    @Override
    public int getProviderMinorVersion() throws JMSException {
        return RABBITMQ_MINOR_VERSION;
    }

    @Override
    public Enumeration<String> getJMSXPropertyNames() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

}
