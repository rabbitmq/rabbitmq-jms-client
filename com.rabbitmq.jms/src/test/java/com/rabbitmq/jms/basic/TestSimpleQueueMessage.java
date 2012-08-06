package com.rabbitmq.jms.basic;

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
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.connection.SingleConnectionFactory;

public class TestSimpleQueueMessage {

    private final String QUEUE_NAME = "test.queue";
    private final String MESSAGE1 = "1. Hello "+TestSimpleQueueMessage.class.getName();
    private final String MESSAGE2 = "2. Hello "+TestSimpleQueueMessage.class.getName();

    /**
     * Sends a text message through a a test queue {@link #QUEUE_NAME}
     * @throws Exception
     */
    @Test
    public void test01Send() throws Exception {
        // get the initial context
        QueueConnectionFactory connFactory = (QueueConnectionFactory)TestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
        Queue queue = new RMQDestination((RMQConnectionFactory) connFactory, QUEUE_NAME, true);
        QueueConnection queueConn = connFactory.createQueueConnection();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE1);
        queueSender.send(message);
        queueConn.close();
    }

    /**
     * Receives a text message through a a test queue {@link #QUEUE_NAME}
     * @throws Exception
     */
    @Test
    public void test02Receive() throws Exception {
        QueueConnectionFactory connFactory = (QueueConnectionFactory)TestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
        Queue queue = new RMQDestination((RMQConnectionFactory) connFactory, QUEUE_NAME, true);
        QueueConnection queueConn = connFactory.createQueueConnection();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive();
        queueConn.close();
        Assert.assertEquals(MESSAGE1, message.getText());
    }

    /**
     * Sends a text message through a a test queue {@link #QUEUE_NAME} but creates the queue using the JMS API
     * @throws Exception
     */
    @Test
    public void test03Send() throws Exception {
        // get the initial context
        QueueConnectionFactory connFactory = (QueueConnectionFactory)TestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
        QueueConnection queueConn = connFactory.createQueueConnection();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE2);
        queueSender.send(message);
        queueConn.close();
    }

    /**
     * Receives a text message through a a test queue {@link #QUEUE_NAME} sent in
     * {@link TestSimpleQueueMessage#test03Send()}
     * @throws Exception
     */
    @Test
    public void test04Receive() throws Exception {
        QueueConnectionFactory connFactory = (QueueConnectionFactory)TestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
        Queue queue = new RMQDestination((RMQConnectionFactory) connFactory, QUEUE_NAME, true);
        QueueConnection queueConn = connFactory.createQueueConnection();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive();
        queueConn.close();
        Assert.assertEquals(MESSAGE2, message.getText());
    }

}
