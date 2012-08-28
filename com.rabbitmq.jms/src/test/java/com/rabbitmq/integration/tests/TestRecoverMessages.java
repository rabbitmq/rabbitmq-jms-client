package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rabbitmq.integration.IntegrationTest;
import com.rabbitmq.jms.TestConnectionFactory;

@Category(IntegrationTest.class)
public class TestRecoverMessages {

    static final String QUEUE_NAME = "test.queue."+TestRecoverMessages.class.getCanonicalName();
    static final String MESSAGE = "Hello " + TestRecoverMessages.class.getName();
    
    @Test
    public void testRecoverTextMessageSync() throws Exception {
        QueueConnection queueConn = null;
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            TextMessage tmsg1 = (TextMessage)queueReceiver.receive();
            assertFalse(tmsg1.getJMSRedelivered());
            queueSession.recover();
            TextMessage tmsg2 = (TextMessage)queueReceiver.receive();
            assertEquals(tmsg1, tmsg2);
            assertTrue(tmsg2.getJMSRedelivered());
            tmsg2.acknowledge();
            TextMessage tmsg3 = (TextMessage)queueReceiver.receiveNoWait();
            assertNull(tmsg3);
        } finally {
            if (queueConn!=null) queueConn.close();
        }
    }

    @Test
    public void testRecoverTextMessageAsyncSync() throws Exception {
        QueueConnection queueConn = null;
        final ArrayList<Message> messages = new ArrayList<Message>();
        try {
            QueueConnectionFactory connFactory = (QueueConnectionFactory) TestConnectionFactory.getTestConnectionFactory()
                                                                                               .getConnectionFactory();
            queueConn = connFactory.createQueueConnection();
            QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
            QueueReceiver queueReceiver = queueSession.createReceiver(queue);
            
            queueReceiver.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    messages.add(message);
                }
            });
            //allow subscription to take place
            Thread.sleep(100);
            //we should have received one message
            assertEquals(1, messages.size());
            TextMessage tmsg1 = (TextMessage)messages.get(0);
            assertFalse(tmsg1.getJMSRedelivered());
            
            queueSession.recover();
            //we should have received two messages
            assertEquals(2, messages.size());
            TextMessage tmsg2 = (TextMessage)messages.get(1);
            assertEquals(tmsg1, tmsg2);
            assertTrue(tmsg2.getJMSRedelivered());

            tmsg2.acknowledge();
            queueReceiver.setMessageListener(null);
            TextMessage tmsg3 = (TextMessage)queueReceiver.receiveNoWait();
            assertNull(tmsg3);
        } finally {
            if (queueConn!=null) queueConn.close();
        }
    }

}
