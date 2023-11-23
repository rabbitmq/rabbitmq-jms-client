// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractITTopic {
    protected TopicConnectionFactory connFactory;
    protected TopicConnection topicConn;

    @BeforeEach
    public void beforeTests() throws Exception {
        this.connFactory =
                (TopicConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                                                                      .getConnectionFactory();
        customise((RMQConnectionFactory) connFactory);
        this.topicConn = connFactory.createTopicConnection();
    }

    protected void customise(RMQConnectionFactory connectionFactory) {

    }

    @AfterEach
    public void afterTests() throws Exception {
        if (this.topicConn != null)
            this.topicConn.close();
    }

    protected void reconnect() throws Exception {
        if (this.topicConn != null)
            this.topicConn.close();
        this.topicConn = connFactory.createTopicConnection();
    }

}
