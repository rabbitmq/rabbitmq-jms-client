package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertThat;
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

import org.junit.Test;

import com.rabbitmq.jms.TestConnectionFactory;

import junit.framework.Assert;


public class TestRabbitMQRedeliverOnNack {
    static final String QUEUE_NAME = "test.queue."+TestRabbitMQRedeliverOnNack.class.getCanonicalName();
    static final String MESSAGE = "Hello " + TestRabbitMQRedeliverOnNack.class.getName();
    
    
    @Test
    public void test() throws Exception {
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
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
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
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
