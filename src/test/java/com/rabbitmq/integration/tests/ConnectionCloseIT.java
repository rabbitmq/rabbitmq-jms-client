/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import org.junit.Test;

import com.rabbitmq.jms.client.Completion;

/**
 * Integration test of close on receive on a Topic destination.
 */
public class ConnectionCloseIT extends AbstractITTopic {
    private static final String TOPIC_NAME = "test.topic." + ConnectionCloseIT.class.getCanonicalName();

    private static final long FIVE_SECONDS = 5000; // ms
    private static final long ONE_SECOND = 1000; // ms
    private static final long ZERO_SECONDS = 0; // ms

    @Test
    public void testSendAndReceiveTextMessage() throws Exception {
        topicConn.start();

        TopicSession topicSession = topicConn.createTopicSession(true, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topicDestination = topicSession.createTopic(TOPIC_NAME);
        MessageConsumer messageConsumer = topicSession.createConsumer(topicDestination);

        Completion receiveCompletion = new Completion();
        Thread receiver = new DelayedReceive(ZERO_SECONDS, messageConsumer, receiveCompletion);
        Thread closer = new DelayedClose(ONE_SECOND, topicConn);

        receiver.start();
        closer.start();

        try {
            receiveCompletion.waitUntilComplete(FIVE_SECONDS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            fail("Timeout before receive returns!");
        }
    }

    private static class DelayedClose extends Thread {
        private final long timeoutMillis;
        private final TopicConnection conn;

        public DelayedClose(long timeoutMillis, TopicConnection conn) {
            this.timeoutMillis = timeoutMillis;
            this.conn = conn;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(this.timeoutMillis);
                this.conn.close();
            } catch (InterruptedException e) {
                e.printStackTrace(); // for debugging
            } catch (JMSException e) {
                e.printStackTrace(); // for debugging
            }
        }
    }

    private static class DelayedReceive extends Thread {
        private final long timeoutMillis;
        private final MessageConsumer subscr;
        private final Completion completion;
        private final AtomicReference<Message> result;

        public DelayedReceive(long timeoutMillis, MessageConsumer subscr, Completion completion) {
            this.timeoutMillis = timeoutMillis;
            this.subscr = subscr;
            this.result = new AtomicReference<Message>(null);
            this.completion = completion;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(this.timeoutMillis);
                this.result.set(this.subscr.receive());
                this.completion.setComplete();
            } catch (InterruptedException e) {
                e.printStackTrace(); // for debugging
            } catch (JMSException e) {
                e.printStackTrace(); // for debugging
            }
        }
    }
}
