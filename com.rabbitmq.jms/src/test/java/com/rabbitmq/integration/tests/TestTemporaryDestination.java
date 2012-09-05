package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rabbitmq.integration.IntegrationTest;
import com.rabbitmq.jms.TestConnectionFactory;

@Category(IntegrationTest.class)
public class TestTemporaryDestination {

    static final String MESSAGE = "Hello " + TestTemporaryDestination.class.getName();
    
    @Test
    public void testQueueSendAndReceiveSingleSession() throws Exception {
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createTemporaryQueue();
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            message = (TextMessage) queueReceiver.receive(1000);
            assertNotNull(message);
            assertEquals(MESSAGE, message.getText());

        } finally {
            queueConn.close();
        }
    }
    
    @Test
    public void testQueueSendAndReceiveTwoSessions() throws Exception {
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            queueConn.start();
            QueueSession queueSession1 = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            QueueSession queueSession2 = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession1.createTemporaryQueue();
            QueueSender queueSender = queueSession1.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession1.createTextMessage(MESSAGE);
            queueSender.send(message);
            QueueReceiver queueReceiver = queueSession2.createReceiver(queue);
            message = (TextMessage) queueReceiver.receive(1000);
            assertNotNull(message);
            assertEquals(MESSAGE, message.getText());

        } finally {
            queueConn.close();
        }        
    }

    @Test
    public void testTopicSendAndReceiveSingleSession() throws Exception {
        TopicConnection topicConn = null;
        try {
            TopicConnectionFactory connFactory = (TopicConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            topicConn = connFactory.createTopicConnection();
            topicConn.start();
            TopicSession topicSession = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession.createTemporaryTopic();
            TopicPublisher topicSender = topicSession.createPublisher(topic);
            topicSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            TopicSubscriber topicReceiver1 = topicSession.createSubscriber(topic);
            TopicSubscriber topicReceiver2 = topicSession.createSubscriber(topic);
            
            TextMessage message = topicSession.createTextMessage(MESSAGE);
            topicSender.send(message);
            
            TextMessage message1 = (TextMessage) topicReceiver1.receive(1000);
            TextMessage message2 = (TextMessage) topicReceiver2.receive(1000);
            assertNotNull(message1);
            assertNotNull(message2);
            assertEquals(MESSAGE, message1.getText());
            assertEquals(MESSAGE, message2.getText());

        } finally {
            topicConn.close();
        }
    }
    
    @Test
    public void testTopicSendAndReceiveTwoSessions() throws Exception {
        TopicConnection topicConn = null;
        try {
            TopicConnectionFactory connFactory = (TopicConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            topicConn = connFactory.createTopicConnection();
            topicConn.start();
            TopicSession topicSession1 = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            TopicSession topicSession2 = topicConn.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Topic topic = topicSession1.createTemporaryTopic();
            TopicPublisher topicSender = topicSession1.createPublisher(topic);
            topicSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            TopicSubscriber topicReceiver1 = topicSession1.createSubscriber(topic);
            TopicSubscriber topicReceiver2 = topicSession2.createSubscriber(topic);
            
            TextMessage message = topicSession1.createTextMessage(MESSAGE);
            topicSender.send(message);
            
            TextMessage message1 = (TextMessage) topicReceiver1.receive(1000);
            TextMessage message2 = (TextMessage) topicReceiver2.receive(1000);
            assertNotNull(message1);
            assertNotNull(message2);
            assertEquals(MESSAGE, message1.getText());
            assertEquals(MESSAGE, message2.getText());

        } finally {
            topicConn.close();
        }
    }

}
