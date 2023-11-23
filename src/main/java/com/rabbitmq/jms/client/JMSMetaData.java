// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import java.util.Enumeration;

import javax.jms.JMSException;

/**
 * Meta data for {@link JMSMetaData}
 */
class JMSMetaData {
    private static final String JMS_PROVIDER_NAME = "RabbitMQ";
    private static final String JMS_VERSION = "1.1";
    private static final int JMS_MAJOR_VERSION = 1;
    private static final int JMS_MINOR_VERSION = 1;

    /**
     * These two are currently not used, they are needed for a JMSCTS test
     * We need to make sure these properties get into the messages
     */
    public static final String JMSX_GROUP_ID_LABEL = "JMSXGroupID"; // value should be VMW
    public static final String JMSX_GROUP_SEQ_LABEL = "JMSXGroupSeq";

    public String getJMSVersion() throws JMSException {
        return JMS_VERSION;
    }

    public int getJMSMajorVersion() throws JMSException {
        return JMS_MAJOR_VERSION;
    }

    public int getJMSMinorVersion() throws JMSException {
        return JMS_MINOR_VERSION;
    }

    public String getJMSProviderName() throws JMSException {
        return JMS_PROVIDER_NAME;
    }

    static class JmsXEnumerator implements Enumeration<String> {
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
