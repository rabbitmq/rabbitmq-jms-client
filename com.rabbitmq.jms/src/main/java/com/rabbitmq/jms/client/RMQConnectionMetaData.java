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

    /**
     * These two are currently not used, they are needed for a JMSCTS test
     * We need to make sure these properties get into the messages
     */
    public static final String JMSX_GROUP_ID_LABEL = "JMSXGroupID"; // value
                                                                    // should be
                                                                    // VMW
    public static final String JMSX_GROUP_SEQ_LABEL = "JMSXGroupSeq";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJMSVersion() throws JMSException {
        return JMS_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMSMajorVersion() throws JMSException {
        return JMS_MAJOR_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMSMinorVersion() throws JMSException {
        return JMS_MINOR_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJMSProviderName() throws JMSException {
        return JMS_PROVIDER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderVersion() throws JMSException {
        return RABBITMQ_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProviderMajorVersion() throws JMSException {
        return RABBITMQ_MAJOR_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProviderMinorVersion() throws JMSException {
        return RABBITMQ_MINOR_VERSION;
    }

    /**
     * {@inheritDoc}
     * This method currently returns an empty enumeration
     */
    @Override
    public Enumeration<String> getJMSXPropertyNames() throws JMSException {
        return new JmsXEnumerator();
    }

    public class JmsXEnumerator implements Enumeration<String> {
        int idx = 0;
        @Override
        public boolean hasMoreElements() {
            return idx < 2;
        }

        @Override
        public String nextElement() {
            switch (idx++) {
            case 0 : return JMSX_GROUP_ID_LABEL;
            case 1 : return JMSX_GROUP_SEQ_LABEL;
            default: return null;
            }
        }

    }

}
