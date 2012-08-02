package com.rabbitmq.test.java;

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

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.connection.SingleConnectionFactory;

public class TestSimpleQueueMessage {

    private final String QUEUE_NAME = "test.queue";

    @Test
    public void send() throws Exception {
        // get the initial context
        QueueConnectionFactory connFactory = new RMQConnectionFactory(new SingleConnectionFactory());
        Queue queue = new RMQDestination((RMQConnectionFactory) connFactory, QUEUE_NAME, true);
        QueueConnection queueConn = connFactory.createQueueConnection();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage("Hello");
        queueSender.send(message);
        System.out.println("sent: " + message.getText());
        queueConn.close();
    }

    @Test
    public void receive() throws Exception {
        QueueConnectionFactory connFactory = new RMQConnectionFactory(new SingleConnectionFactory());
        Queue queue = new RMQDestination((RMQConnectionFactory) connFactory, QUEUE_NAME, true);
        QueueConnection queueConn = connFactory.createQueueConnection();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive();
        System.out.println("receiver: " + message.getText());
        queueConn.close();
    }

}
