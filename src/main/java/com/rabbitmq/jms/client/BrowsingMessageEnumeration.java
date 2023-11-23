// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.parse.sql.SqlEvaluator;

class BrowsingMessageEnumeration implements Enumeration<RMQMessage> {

    private static final int BROWSING_CONSUMER_TIMEOUT = 10000; // ms
    private final java.util.Queue<RMQMessage> msgQueue;

    public BrowsingMessageEnumeration(RMQSession session, RMQDestination dest, Channel channel, SqlEvaluator evaluator, int readMax,
            ReceivingContextConsumer receivingContextConsumer) {
        java.util.Queue<RMQMessage> msgQ = new ConcurrentLinkedQueue<RMQMessage>();
        populateQueue(msgQ, channel, session, dest, evaluator, readMax, receivingContextConsumer);
        this.msgQueue = msgQ;
    }

    private static void populateQueue(java.util.Queue<RMQMessage> msgQueue, Channel channel, RMQSession session, RMQDestination dest, SqlEvaluator evaluator,
            int readMax, ReceivingContextConsumer receivingContextConsumer) {
        try {
            String destQueueName = dest.getQueueName();
            int qCount = getNumberOfMessages(channel, destQueueName);
            if (qCount > 0) { // we need to read them
                int messagesExpected = (readMax<=0) ? qCount : Math.min(readMax, qCount);
                channel.basicQos(messagesExpected); // limit subsequent consumers to the expected number of messages unacknowledged
                BrowsingConsumer bc = new BrowsingConsumer(channel, session, dest, messagesExpected, msgQueue,
                    evaluator, receivingContextConsumer);
                String consumerTag = channel.basicConsume(destQueueName, bc);
                if (bc.finishesInTime(BROWSING_CONSUMER_TIMEOUT))
                    return;
                else
                    channel.basicCancel(consumerTag);
            }
        } catch(Exception e) {
            // Ignore any errors;
        }
    }

    private static int getNumberOfMessages(Channel channel, String destQueueName) {
        try {
            // It turns out that the messages take an indeterminate time to get to their queues,
            // so this passive declare may not give a result that agrees with our expectations
            // (even though it is *accurate* in some rabbitmq-server defined sense).

            // Heuristics, like issuing the passive declare twice in a row, with or without a pause,
            // do not always work---there appear to be some erratic delays occasionally---and so
            // the decision has been taken to *not* try to circumvent this. There is, after all,
            // nothing in the JMS spec that makes any guarantees about what a QueueBrowser will see.
            // Our integration tests have to be less dogmatic, therefore.

            // In the code below, there are commented out sections used in test to explore the behaviour.
//            int mc1 = channel.queueDeclarePassive(destQueueName).getMessageCount();
//            Thread.sleep(100);
            int mc2 = channel.queueDeclarePassive(destQueueName).getMessageCount();
//            if (mc1!=mc2) System.out.println(String.format("q='%s', msgcount=%s/%s", destQueueName, mc1, mc2));
//            System.out.println(String.format("q='%s', msgcount=%s", destQueueName, mc2));
            return mc2;
        } catch (Exception e) { // ignore errors---we assume no messages in the queue in this case.
        }
        return 0; // default drop-through value
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
