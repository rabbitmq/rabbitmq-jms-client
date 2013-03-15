/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Test;

/**
 * Integration test
 */
public class RecoverMessagesIT extends AbstractITQueue {

    static final String QUEUE_NAME = "test.queue." + RecoverMessagesIT.class.getCanonicalName();
    static final String MESSAGE = "Hello " + RecoverMessagesIT.class.getName();

    @Test
    public void testRecoverTextMessageSync() throws Exception {
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        queueSender.send(message);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage tmsg1 = (TextMessage) queueReceiver.receive();
        assertFalse(tmsg1.getJMSRedelivered());
        queueSession.recover();
        TextMessage tmsg2 = (TextMessage) queueReceiver.receive();
        assertEquals(tmsg1, tmsg2);
        assertTrue(tmsg2.getJMSRedelivered());
        tmsg2.acknowledge();
        TextMessage tmsg3 = (TextMessage) queueReceiver.receiveNoWait();
        assertNull(tmsg3);
    }

    @Test
    public void testRecoverTextMessageAsyncSync() throws Exception {
        final ArrayList<Message> messages = new ArrayList<Message>();

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        TextMessage message = queueSession.createTextMessage(MESSAGE);
        queueSender.send(message);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        final CountDownLatch latch = new CountDownLatch(2);
        queueReceiver.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                messages.add(message);
                latch.countDown();
            }
        });
        // allow subscription to take place
        Thread.sleep(100);
        // we should have received one message
        assertEquals(1, messages.size());
        TextMessage tmsg1 = (TextMessage) messages.get(0);
        assertFalse(tmsg1.getJMSRedelivered());
        queueSession.recover();
        latch.await(1000, TimeUnit.MILLISECONDS);
        // we should have received two messages
        // There is no synchronisation so no guarantee we see the latest messages!
        assertEquals(2, messages.size());
        TextMessage tmsg2 = (TextMessage) messages.get(1);
        assertEquals(tmsg1, tmsg2);
        assertTrue(tmsg2.getJMSRedelivered());

        tmsg2.acknowledge();
        queueReceiver.setMessageListener(null);
        TextMessage tmsg3 = (TextMessage) queueReceiver.receiveNoWait();
        assertNull(tmsg3);
    }
}
