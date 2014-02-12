package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.JMSException;

import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.RMQJMSException;

class BrowsingMessageEnumeration implements Enumeration<RMQMessage> {

    private static final int BROWSING_CONSUMER_TIMEOUT = 1000; // ms
    private final java.util.Queue<GetResponse> msgQueue;
    private final RMQSession session;
    private final RMQDestination dest;

    public BrowsingMessageEnumeration(RMQSession session, RMQDestination dest, Channel channel, String queueName) throws JMSException {
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
