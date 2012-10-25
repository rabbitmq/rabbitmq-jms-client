package com.rabbitmq.integration.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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

import org.junit.Test;


/**
 * Asynchronous Consumer integration test.
 */
public class AsyncConsumerIT {
    private static final String QUEUE_NAME = "test.queue." + AsyncConsumerIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + AsyncConsumerIT.class.getName();

    /**
     * Basic send and receive with a Consumer. Uses serial send followed by async receive.
     * @throws Exception if test error.
     */
    @Test
    public void testSendAndAsyncReceiveTextMessage() throws Exception {

        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
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
            QueueConnectionFactory connFactory = (QueueConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
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

    private static class MessageListener implements javax.jms.MessageListener {

        private volatile boolean success = false;
        private volatile Message lastMessage;
        private AtomicInteger messageCount = new AtomicInteger(0);
        private final CountDownLatch latch;

        public MessageListener(CountDownLatch latch) {
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
