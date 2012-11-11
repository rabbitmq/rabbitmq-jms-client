package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertTrue;

import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import junit.framework.Assert;

import org.junit.Test;


/**
 * Integration test
 */
public class RabbitMQRedeliverOnNackIT {
    static final String QUEUE_NAME = "test.queue."+RabbitMQRedeliverOnNackIT.class.getCanonicalName();
    static final String MESSAGE = "Hello " + RabbitMQRedeliverOnNackIT.class.getName();


    /**
     * Test that Rabbit re-delivers a message which has been explicitly NACKed.
     * @throws Exception on test failure
     */
    @Test
    public void testRMQRedeliverOnNack() throws Exception {
        QueueConnectionFactory connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        QueueConnection queueConn = null;
        queueConn = connFactory.createQueueConnection();
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
        } finally {
            queueConn.close();
        }
        queueConn = connFactory.createQueueConnection();
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(true, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            TextMessage message = (TextMessage) queueReceiver.receive();
            Assert.assertEquals(MESSAGE, message.getText());
            queueSession.rollback();
            message = (TextMessage) queueReceiver.receive();
            Assert.assertEquals(MESSAGE, message.getText());
            assertTrue(message.getJMSRedelivered());
            queueSession.commit();
        } finally {
            queueConn.close();
        }

    }
}
