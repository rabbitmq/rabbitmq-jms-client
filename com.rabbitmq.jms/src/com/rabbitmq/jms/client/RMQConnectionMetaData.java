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
package com.rabbitmq.jms.client;

import java.util.Enumeration;

import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;

/**
 * Meta data for {@link RMQConnection}
 */
public class RMQConnectionMetaData implements ConnectionMetaData {

    private static final String JMS_PROVIDER_NAME      = "RabbitMQ";
    private static final String RABBITMQ_VERSION       = "2.8.3";
    private static final int    RABBITMQ_MINOR_VERSION = 8;
    private static final int    RABBITMQ_MAJOR_VERSION = 2;
    private static final String JMS_VERSION            = "1.1";
    private static final int    JMS_MAJOR_VERSION      = 1;
    private static final int    JMS_MINOR_VERSION      = 1;

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
