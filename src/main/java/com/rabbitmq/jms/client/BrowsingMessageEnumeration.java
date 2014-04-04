/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.Queue;
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
            Queue.DeclareOk qdec = channel.queueDeclarePassive(destQueueName);
            if (qdec.getMessageCount() > 0) {
                // re-fetch messageCount, in case it is changing
                qdec = redeclareCheck(channel, destQueueName, qdec);
                int messagesExpected = (readMax<=0) ? qdec.getMessageCount() : Math.min(readMax, qdec.getMessageCount());
                BrowsingConsumer bc = new BrowsingConsumer(channel, session, dest, messagesExpected, msgQueue, evaluator);
                String consumerTag = channel.basicConsume(destQueueName, bc);
                if (bc.finishesInTime(BROWSING_CONSUMER_TIMEOUT))
                    return;
                else
                    channel.basicCancel(consumerTag);
            }
        } catch(IOException e) {
//          System.out.println(String.format(">> ERROR >> Failed to browse queue named [%s], exception [%s]", queueName, e));
//          e.printStackTrace();
//          Just return an empty enumeration: it is not an error to try to browse a non-existent queue
        } catch(JMSException e) {
//      System.out.println(String.format(">> ERROR >> Failed to browse queue named [%s], exception [%s]", queueName, e));
//      e.printStackTrace();
//      Just return an empty enumeration: it is not an error to try to browse a non-existent queue
        }
    }

    private Queue.DeclareOk
            redeclareCheck(Channel channel, String destQueueName, Queue.DeclareOk qdec) throws IOException, JMSException {
        Queue.DeclareOk qdec2 = channel.queueDeclarePassive(destQueueName);
        if (qdec.getMessageCount() != qdec2.getMessageCount()) {
            // Issue warning and take the second count
            this.logger.warn( "QueueBrowser detected changing count of messages in queue {}; first count={}, second count={}."
                            , destQueueName, qdec.getMessageCount(), qdec2.getMessageCount()
                            );
            // TEST CATCH FAILURE
            throw new RuntimeException( String.format( "TEST: QueueBrowser changing message contents queue=%s, firsrt count=%s, second count=%s"
                                                    , destQueueName, qdec.getMessageCount(), qdec2.getMessageCount()
                                                    )
                                     , new IOException("")
                                     );
//            return qdec2;
        }
        return qdec;
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
