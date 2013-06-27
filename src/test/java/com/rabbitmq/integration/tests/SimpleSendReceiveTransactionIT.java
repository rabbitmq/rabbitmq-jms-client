/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import javax.jms.DeliveryMode;
import javax.jms.Queue;
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
public class SimpleSendReceiveTransactionIT extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue." + SimpleSendReceiveTransactionIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + SimpleQueueMessageIT.class.getName();

    @Test
    public void testQueueSendAndRollback() throws Exception {
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
            reconnect();
        }
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receiveNoWait();
        Assert.assertNull(message);
    }

    @Test
    public void testSendAndCommitAndReceiveMessage() throws Exception {
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
            reconnect();
        }
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive();
        Assert.assertEquals(MESSAGE, message.getText());
    }
}
