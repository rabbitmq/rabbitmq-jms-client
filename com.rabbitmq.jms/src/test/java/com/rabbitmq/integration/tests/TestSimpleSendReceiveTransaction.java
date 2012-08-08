package com.rabbitmq.integration.tests;

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

import com.rabbitmq.jms.TestConnectionFactory;

public class TestSimpleSendReceiveTransaction {
    @Test
    public void testQueueSendAndRollback() throws Exception {
        final String MESSAGE2 = "2. Hello " + TestSimpleQueueMessage.class.getName();
        final String QUEUE_NAME = "test.queue";
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(true, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE2);
            queueSender.send(message);
            queueSession.rollback();
        } finally {
            queueConn.close();
        }
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            TextMessage message = (TextMessage) queueReceiver.receive();
            Assert.assertNull(message);
        } finally {
            queueConn.close();
        }
    }

    @Test
    public void testSendAndCommitAndReceiveMessage() throws Exception {
        final String MESSAGE2 = "2. Hello " + TestSimpleQueueMessage.class.getName();
        final String QUEUE_NAME = "test.queue";
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(true, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE2);
            queueSender.send(message);
            queueSession.commit();
        } finally {
            queueConn.close();
        }
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            TextMessage message = (TextMessage) queueReceiver.receive();
            Assert.assertEquals(MESSAGE2, message.getText());
        } finally {
            queueConn.close();
        }

    }

}
