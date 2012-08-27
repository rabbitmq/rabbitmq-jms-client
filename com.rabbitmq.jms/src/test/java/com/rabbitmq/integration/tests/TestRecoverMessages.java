package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
            queueSession.recover();
            TextMessage tmsg2 = (TextMessage)queueReceiver.receive();
            assertEquals(tmsg1, tmsg2);
            tmsg2.acknowledge();
            TextMessage tmsg3 = (TextMessage)queueReceiver.receiveNoWait();
            assertNull(tmsg3);
        } finally {
            if (queueConn!=null) queueConn.close();
        }
    }

   

}
