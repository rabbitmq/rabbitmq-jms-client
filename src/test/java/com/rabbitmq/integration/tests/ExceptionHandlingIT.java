/* Copyright (c) 2018-2020 VMware, Inc. or its affiliates. All rights reserved. */

package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.Session;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * To check an exception is thrown depending on the corresponding setting.
 *
 * https://github.com/rabbitmq/rabbitmq-jms-client/issues/53
 */
public class ExceptionHandlingIT {

    RMQConnectionFactory connFactory;
    QueueConnection queueConn;

    @BeforeEach
    public void beforeTests() throws Exception {
        this.connFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
            .getConnectionFactory();
    }

    @AfterEach
    public void afterTests() throws Exception {
        if (queueConn != null)
            queueConn.close();
    }

    @Test
    public void shouldNotThrowExceptionForNonExistingDestinationByDefault() throws Exception {
        listenOnNonExistingDestination();
    }

    @Test
    public void shouldThrowExceptionForNonExistingDestinationWhenFlagIsSet() throws Exception {
        connFactory.setThrowExceptionOnConsumerStartFailure(true);
        assertThrows(JMSException.class, () -> listenOnNonExistingDestination());
    }

    private void listenOnNonExistingDestination() throws JMSException {
        queueConn = connFactory.createQueueConnection();
        queueConn.start();
        String queueName = ExceptionHandlingIT.class.getSimpleName() + " " + System.currentTimeMillis();
        RMQDestination queue = new RMQDestination(queueName, null, null, queueName);
        Session session = queueConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);

        messageConsumer.setMessageListener(msg -> {
        });
    }
}
