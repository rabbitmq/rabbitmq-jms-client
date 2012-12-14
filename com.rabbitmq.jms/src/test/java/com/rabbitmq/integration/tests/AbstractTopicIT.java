package com.rabbitmq.integration.tests;

import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractTopicIT {
    protected TopicConnectionFactory connFactory;
    protected TopicConnection topicConn;
    
    @Before
    public void beforeTests() throws Exception {
        this.connFactory =
                (TopicConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                                                                      .getConnectionFactory();
        this.topicConn = connFactory.createTopicConnection();
    }
    
    @After
    public void afterTests() throws Exception {
        topicConn.close();
    }

}
