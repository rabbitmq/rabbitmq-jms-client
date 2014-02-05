/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.browsing;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;

import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.client.RMQSession;

/**
 * Implementation class suitable for storing message information for browsing.
 */
public class BrowsingMessageQueue implements QueueBrowser {

    private final RMQDestination dest;

    private final Enumeration<RMQMessage> msgQueue;

    public BrowsingMessageQueue(RMQSession session, RMQDestination dest) {
        this.dest = dest;
        this.msgQueue = new MessageEnumeration(session, dest);
    }

    @Override
    public Queue getQueue() throws JMSException {
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
        while (this.msgQueue.hasMoreElements()) this.msgQueue.nextElement();
        return;
    }

    private static class MessageEnumeration implements Enumeration<RMQMessage> {

        private java.util.Queue<RMQMessage> msgQueue;

        public MessageEnumeration(RMQSession session, RMQDestination dest){
            this.msgQueue = new ConcurrentLinkedQueue<RMQMessage>();
            populateQueue(this.msgQueue, session, dest);
        }

        private static void populateQueue(final java.util.Queue<RMQMessage> msgQueue, RMQSession session, RMQDestination dest) {
            // tbd
        }

        @Override public boolean hasMoreElements() { return !this.msgQueue.isEmpty(); }
        @Override public RMQMessage nextElement() {
            RMQMessage msg = this.msgQueue.poll();
            if (null==msg) throw new NoSuchElementException();
            return msg;
        }
    }
}
