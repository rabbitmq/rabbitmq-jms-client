/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.parse.sql.SqlEvaluator;

class BrowsingMessageEnumeration implements Enumeration<RMQMessage> {
    private final Logger logger = LoggerFactory.getLogger(BrowsingMessageEnumeration.class);

    private static final int BROWSING_CONSUMER_TIMEOUT = 10000; // ms
    private final java.util.Queue<RMQMessage> msgQueue;

    public BrowsingMessageEnumeration(RMQSession session, RMQDestination dest, Channel channel, SqlEvaluator evaluator, int readMax) throws JMSException {
        this.msgQueue = new ConcurrentLinkedQueue<RMQMessage>();
        this.populateQueue(channel, session, dest, evaluator, readMax);
        session.closeBrowsingChannel(channel); // this should requeue all the messages browsed
    }

    private void populateQueue(Channel channel, RMQSession session, RMQDestination dest, SqlEvaluator evaluator, int readMax) {
        try {
            String destQueueName = dest.getQueueName();
            int qCount = channel.queueDeclarePassive(destQueueName).getMessageCount();
            if (qCount > 0) {
                // re-fetch messageCount, in case it is changing
                qCount = redeclareCheck(channel, destQueueName, qCount);
                int messagesExpected = (readMax<=0) ? qCount : Math.min(readMax, qCount);
                BrowsingConsumer bc = new BrowsingConsumer(channel, session, dest, messagesExpected, msgQueue, evaluator);
                String consumerTag = channel.basicConsume(destQueueName, bc);
                if (bc.finishesInTime(BROWSING_CONSUMER_TIMEOUT))
                    return;
                else
                    channel.basicCancel(consumerTag);
            }
        } catch(Exception e) {
            // Ignore any errors; the redeclareCheck() function will output a warning if necessary
        }
    }

    private int redeclareCheck(Channel channel, String destQueueName, int qCount) throws Exception {
        int qCount2 = channel.queueDeclarePassive(destQueueName).getMessageCount();
        if (qCount != qCount2) {
            // Issue warning
            this.logger.warn( "QueueBrowser detected changing count of messages in queue {}; first count={}, second count={}."
                            , destQueueName, qCount, qCount2
                            );
        }
        return qCount2; // take the second count anyway
    }

    @Override public boolean hasMoreElements() {
        return !this.msgQueue.isEmpty();
        }

    @Override public RMQMessage nextElement() {
        RMQMessage resp = this.msgQueue.poll();
        if (null==resp) throw new NoSuchElementException();
        return resp;
    }

    void clearQueue() {
        this.msgQueue.clear();
    }
}
