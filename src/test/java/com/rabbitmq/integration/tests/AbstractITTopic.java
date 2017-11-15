/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

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
        this.topicConn = connFactory.createTopicConnection();
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
