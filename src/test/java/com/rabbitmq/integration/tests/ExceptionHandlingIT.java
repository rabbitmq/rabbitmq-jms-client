/* Copyright (c) 2018 Pivotal Software, Inc. All rights reserved. */

package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.Session;

/**
 * To check an exception is thrown depending on the corresponding setting.
 *
 * https://github.com/rabbitmq/rabbitmq-jms-client/issues/53
 */
public class ExceptionHandlingIT {

    RMQConnectionFactory connFactory;
    QueueConnection queueConn;

    @Before
    public void beforeTests() throws Exception {
        this.connFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
            .getConnectionFactory();
    }

    @After
    public void afterTests() throws Exception {
        if (queueConn != null)
            queueConn.close();
    }

    @Test
    public void shouldNotThrowExceptionForNonExistingDestinationByDefault() throws Exception {
        listenOnNonExistingDestination();
    }

    @Test(expected = JMSException.class)
    public void shouldThrowExceptionForNonExistingDestinationWhenFlagIsSet() throws Exception {
        connFactory.setThrowExceptionOnConsumerStartFailure(true);
        listenOnNonExistingDestination();
    }

    private void listenOnNonExistingDestination() throws JMSException {
        queueConn = connFactory.createQueueConnection();
        queueConn.start();
        String queueName = ExceptionHandlingIT.class.getSimpleName() + " " + System.currentTimeMillis();
        RMQDestination queue = new RMQDestination(queueName, null, null, queueName);
        Session session = queueConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);

        messageConsumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message msg) {
            }
        });
    }
}
