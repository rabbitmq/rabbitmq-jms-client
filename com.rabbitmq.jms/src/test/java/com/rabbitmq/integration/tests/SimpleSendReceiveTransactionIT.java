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


/**
 * Integration test
 */
public class SimpleSendReceiveTransactionIT {
    static final String QUEUE_NAME = "test.queue." + SimpleSendReceiveTransactionIT.class.getCanonicalName();
    static final String MESSAGE = "Hello " + SimpleQueueMessageIT.class.getName();

    @Test
    public void testQueueSendAndRollback() throws Exception {

        QueueConnectionFactory connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        QueueConnection queueConn = null;
        queueConn = connFactory.createQueueConnection();
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(true, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
            queueSession.rollback();
        } finally {
            queueConn.close();
        }
        queueConn = connFactory.createQueueConnection();
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            TextMessage message = (TextMessage) queueReceiver.receiveNoWait();
            Assert.assertNull(message);
        } finally {
            queueConn.close();
        }
    }

    @Test
    public void testSendAndCommitAndReceiveMessage() throws Exception {
        QueueConnectionFactory connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
                .getConnectionFactory();
        QueueConnection queueConn = null;
        queueConn = connFactory.createQueueConnection();
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(true, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
            queueSession.commit();
        } finally {
            queueConn.close();
        }
        queueConn = connFactory.createQueueConnection();
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            TextMessage message = (TextMessage) queueReceiver.receive();
            Assert.assertEquals(MESSAGE, message.getText());
        } finally {
            queueConn.close();
        }

    }

}
