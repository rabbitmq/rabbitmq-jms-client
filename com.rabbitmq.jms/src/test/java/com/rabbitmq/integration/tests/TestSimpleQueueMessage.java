package com.rabbitmq.integration.tests;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.MapMessage;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import junit.framework.Assert;

import org.junit.Test;

import com.rabbitmq.jms.TestConnectionFactory;
import com.rabbitmq.jms.message.TestMessages;

public class TestSimpleQueueMessage {

    @Test
    public void testSendAndReceiveTextMessage() throws Exception {
        final String MESSAGE2 = "2. Hello " + TestSimpleQueueMessage.class.getName();
        final String QUEUE_NAME = "test.queue";
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE2);
            queueSender.send(message);
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

    @Test
    public void testSendAndReceiveBytesMessage() throws Exception {
        final String QUEUE_NAME = "test.queue";
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            BytesMessage message = queueSession.createBytesMessage();

            TestMessages.writeBytesMessage(message);
            queueSender.send(message);
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
            BytesMessage message = (BytesMessage) queueReceiver.receive();
            TestMessages.readBytesMessage(message);
        } finally {
            queueConn.close();
        }

    }

    @Test
    public void testSendAndReceiveMapMessage() throws Exception {
        final String QUEUE_NAME = "test.queue";
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            MapMessage message = queueSession.createMapMessage();

            TestMessages.writeMapMessage(message);
            queueSender.send(message);
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
            MapMessage message = (MapMessage) queueReceiver.receive();
            TestMessages.readMapMessage(message);
        } finally {
            queueConn.close();
        }

    }

    @Test
    public void testSendAndReceiveStreamMessage() throws Exception {
        final String QUEUE_NAME = "test.queue";
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            StreamMessage message = queueSession.createStreamMessage();
            TestMessages.writeStreamMessage(message);
            queueSender.send(message);
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
            StreamMessage message = (StreamMessage) queueReceiver.receive();
            TestMessages.readStreamMessage(message);
        } finally {
            queueConn.close();
        }

    }

    @Test
    public void testSendAndReceiveObjectMessage() throws Exception {
        final String QUEUE_NAME = "test.queue";
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            ObjectMessage message = queueSession.createObjectMessage();
            TestMessages.writeObjectMessage(message);
            queueSender.send(message);
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
            ObjectMessage message = (ObjectMessage) queueReceiver.receive();
            TestMessages.readObjectMessage(message);
        } finally {
            queueConn.close();
        }

    }

}
