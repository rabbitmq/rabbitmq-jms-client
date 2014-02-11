/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.browsing;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.client.RMQSession;
import com.rabbitmq.jms.util.RMQJMSException;

/**
 * Implementation class suitable for storing message information for browsing.
 */
public class BrowsingMessageQueue implements QueueBrowser {

    private final String queueName;
    private final RMQDestination dest; // only needed for getQueue();

    private final MessageEnumeration msgQueue;

    public BrowsingMessageQueue(RMQSession session, RMQDestination dest, Channel channel) throws JMSException {
        this.dest = dest;
        this.queueName = dest.getQueueName();
        this.msgQueue  = new MessageEnumeration(session, dest, channel, queueName);
    }

    @Override
    public javax.jms.Queue getQueue() throws JMSException {
        return this.dest;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        // The selector is always null in this implementation
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getEnumeration() throws JMSException {
        return this.msgQueue;
    }

    @Override
    public void close() throws JMSException {
        // discard all messages from enumeration
        this.msgQueue.clearQueue();
        return;
    }

    private static class MessageEnumeration implements Enumeration<RMQMessage> {

        private static final int BROWSING_CONSUMER_TIMEOUT = 1000; // ms
        private final java.util.Queue<GetResponse> msgQueue;
        private final RMQSession session;
        private final RMQDestination dest;

        public MessageEnumeration(RMQSession session, RMQDestination dest, Channel channel, String queueName) throws JMSException {
            this.session = session;
            this.dest = dest;
            this.msgQueue = new ConcurrentLinkedQueue<GetResponse>();
            populateQueue(this.msgQueue, channel, queueName);
        }

        private static void populateQueue(final java.util.Queue<GetResponse> msgQueue, Channel channel, String queueName) throws JMSException {
            try {
                Queue.DeclareOk qdec = channel.queueDeclarePassive(queueName);
                if (qdec.getMessageCount() > 0) {
                    int messagesExpected = qdec.getMessageCount();
                    BrowsingConsumer bc = new BrowsingConsumer(channel, messagesExpected, msgQueue);
                    String consumerTag = channel.basicConsume(queueName, bc);
                    if (bc.finishesInTime(BROWSING_CONSUMER_TIMEOUT))
                        return;
                    else
                        channel.basicCancel(consumerTag);
                }
            } catch(IOException ioe) {
                throw new RMQJMSException("Failed to browse queue '"+queueName+"'", ioe);
            }
        }

        @Override public boolean hasMoreElements() { return !this.msgQueue.isEmpty(); }
        @Override public RMQMessage nextElement() {
            GetResponse resp = this.msgQueue.poll();
            if (null==resp) throw new NoSuchElementException();
            try {
                return RMQMessage.convertMessage(this.session, this.dest, resp);
            } catch (JMSException e) {
                return null;
            }
        }

        void clearQueue() {
            this.msgQueue.clear();
        }
    }

    private static class BrowsingConsumer extends DefaultConsumer {

        private final CountDownLatch latch = new CountDownLatch(1);

        private int messagesExpected;
        private final java.util.Queue<GetResponse> msgQueue;

        public BrowsingConsumer(Channel channel, int messagesExpected, java.util.Queue<GetResponse> msgQueue) {
            super(channel);
            this.messagesExpected = messagesExpected;
            this.msgQueue = msgQueue;
        }

        public boolean finishesInTime(int browsingConsumerTimeout) {
            try {
                return this.latch.await(browsingConsumerTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // reset interrupted status
                return false;
            }
        }

        @Override
        public void handleCancelOk(String consumerTag) {
            this.latch.countDown();
        }

        @Override
        public void handleDelivery(String consumerTag,
                                   Envelope envelope,
                                   AMQP.BasicProperties properties,
                                   byte[] body)
        throws IOException {
            if (this.messagesExpected == 0) {
                this.getChannel().basicCancel(consumerTag);
            }
            else {
                this.msgQueue.add(new GetResponse(envelope, properties, body, --this.messagesExpected));
            }
        }
    }
}
