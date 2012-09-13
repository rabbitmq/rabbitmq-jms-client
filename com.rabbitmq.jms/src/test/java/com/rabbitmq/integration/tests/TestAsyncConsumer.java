package com.rabbitmq.integration.tests;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rabbitmq.jms.TestConnectionFactory;

@Category(IntegrationTest.class)
public class TestAsyncConsumer {
    static final String QUEUE_NAME = "test.queue." + TestAsyncConsumer.class.getCanonicalName();
    static final String MESSAGE = "Hello " + TestAsyncConsumer.class.getName();

    @Test
    public void testSendAndReceiveTextMessage() throws Exception {

        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
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
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            CountDownLatch latch = new CountDownLatch(1);
            MessageListener listener = new MessageListener(latch);
            queueReceiver.setMessageListener(listener);
            latch.await(1000, TimeUnit.MILLISECONDS);
            TextMessage message = (TextMessage) listener.getLastMessage();
            assertEquals(MESSAGE, message.getText());
            assertEquals(1, listener.getMessageCount());
            assertTrue(listener.isSuccess());
        } finally {
            queueConn.close();
        }

    }
    
    @Test
    public void testSendAndReceiveTextMessageNoLocal() throws Exception {
        System.out.println("This test will fail");
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
            /*
             * NoLocal set to true - we should not receive a message
             */
            QueueReceiver queueReceiver = (QueueReceiver)queueSession.createConsumer(queue, null, true);
            CountDownLatch latch = new CountDownLatch(1);
            MessageListener listener = new MessageListener(latch);
            queueReceiver.setMessageListener(listener);
            latch.await(1000, TimeUnit.MILLISECONDS);
            message = (TextMessage) listener.getLastMessage();
            assertNull(message);
            assertFalse(listener.isSuccess());
            
            /*
             * NoLocal set to false - we should receive a message
             */
            queueReceiver = (QueueReceiver)queueSession.createConsumer(queue, null, false);
            latch = new CountDownLatch(1);
            listener = new MessageListener(latch);
            queueReceiver.setMessageListener(listener);
            latch.await(1000, TimeUnit.MILLISECONDS);
            message = (TextMessage) listener.getLastMessage();
            assertEquals(MESSAGE, message.getText());
            assertEquals(1, listener.getMessageCount());
            assertTrue(listener.isSuccess());

        
        } finally {
            queueConn.close();
        }
    }

    private static class MessageListener implements javax.jms.MessageListener {

        private volatile boolean success = false;
        private volatile Message lastMessage;
        private AtomicInteger messageCount = new AtomicInteger(0);
        private final CountDownLatch latch;

        public MessageListener(CountDownLatch latch) {
            super();
            this.latch = latch;
        }

        @Override
        public void onMessage(Message message) {
            success = (messageCount.incrementAndGet() == 1);
            lastMessage = message;
            latch.countDown();
        }

        public boolean isSuccess() {
            return success;
        }

        public Message getLastMessage() {
            return lastMessage;
        }

        public int getMessageCount() {
            return messageCount.get();
        }

    }
}
