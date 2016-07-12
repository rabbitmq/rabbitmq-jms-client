/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractITTopicSSL {
    protected TopicConnectionFactory connFactory;
    protected TopicConnection topicConn;

    @Before
    public void beforeTests() throws Exception {
        org.junit.Assume.assumeTrue(Boolean.getBoolean("com.rabbitmq.jms.TLSTests"));
        this.connFactory =
                (TopicConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory(true, 0)
                                                                      .getConnectionFactory();
        this.topicConn = connFactory.createTopicConnection();
    }

    @After
    public void afterTests() throws Exception {
        if (topicConn != null)
            topicConn.close();
    }

}
