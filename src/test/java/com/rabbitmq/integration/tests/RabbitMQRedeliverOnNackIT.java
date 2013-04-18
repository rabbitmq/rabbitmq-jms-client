/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Test;


/**
 * Integration test for re-deliver on rollback.
 */
public class RabbitMQRedeliverOnNackIT  extends AbstractITQueue {
    private static final String QUEUE_NAME = "test.queue."+RabbitMQRedeliverOnNackIT.class.getCanonicalName();
    private static final String MESSAGE = "Hello " + RabbitMQRedeliverOnNackIT.class.getName();

    /**
     * Test that Rabbit re-delivers a received message which has been rolled-back.
     * @throws Exception on test failure
     */
    @Test
    public void testRMQRedeliverOnNack() throws Exception {
        try {
            queueConn.start();
            QueueSession queueSession = queueConn.createQueueSession( false /*not transacted*/
                                                                    , Session.CLIENT_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue(QUEUE_NAME);
            QueueSender queueSender = queueSession.createSender(queue);
            queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage message = queueSession.createTextMessage(MESSAGE);
            queueSender.send(message);
        } finally {
            reconnect();
        }

        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession( true /*transacted*/
                                                                , Session.DUPS_OK_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(QUEUE_NAME);
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);
        TextMessage message = (TextMessage) queueReceiver.receive();
        assertNotNull("No message delivered initially", message);
        assertEquals("Wrong message delivered initially", MESSAGE, message.getText());

        queueSession.rollback();

        message = (TextMessage) queueReceiver.receive();

        assertNotNull("No message delivered after rollback", message);
        assertEquals("Wrong message redelivered", MESSAGE, message.getText());
        assertTrue("Message not marked 'redelivered'", message.getJMSRedelivered());

        queueSession.commit();
    }
}
