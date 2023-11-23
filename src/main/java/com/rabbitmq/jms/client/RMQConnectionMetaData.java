// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import java.util.Enumeration;

import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;

/**
 * Meta data for {@link RMQConnection}
 */
public class RMQConnectionMetaData implements ConnectionMetaData {

    JMSMetaData jmsMetaData;

    public RMQConnectionMetaData() {
        this.jmsMetaData = new JMSMetaData();
    }

    private static final GenericVersion RABBITMQ_VERSION_OBJECT =
            new GenericVersion(com.rabbitmq.client.impl.Version.class.getPackage().getImplementationVersion());
    private static final String RABBITMQ_VERSION = RABBITMQ_VERSION_OBJECT.toString();
    private static final int RABBITMQ_MAJOR_VERSION = RABBITMQ_VERSION_OBJECT.getMajor();
    private static final int RABBITMQ_MINOR_VERSION = RABBITMQ_VERSION_OBJECT.getMinor();


    /**
     * These two are currently not used, they are needed for a JMSCTS test
     * We need to make sure these properties get into the messages
     */
    public static final String JMSX_GROUP_ID_LABEL = "JMSXGroupID"; // value should be VMW
    public static final String JMSX_GROUP_SEQ_LABEL = "JMSXGroupSeq";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJMSVersion() throws JMSException {
        return jmsMetaData.getJMSVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMSMajorVersion() throws JMSException {
        return jmsMetaData.getJMSMajorVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMSMinorVersion() throws JMSException {
        return jmsMetaData.getJMSMinorVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJMSProviderName() throws JMSException {
        return jmsMetaData.getJMSProviderName();
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
        return new JMSMetaData.JmsXEnumerator();
    }

}
