/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;

/**
 * Implementation class suitable for storing message information for browsing.
 */
class BrowsingMessageQueue implements QueueBrowser {

    private final String queueName;
    private final RMQDestination dest; // only needed for getQueue();

    private final BrowsingMessageEnumeration msgQueue;

    public BrowsingMessageQueue(RMQSession session, RMQDestination dest) throws JMSException {
        this.dest = dest;
        this.queueName = dest.getQueueName();
        Channel chan = session.getBrowsingChannel();
        this.msgQueue  = new BrowsingMessageEnumeration(session, dest, chan, queueName);
        session.closeBrowsingChannel(chan); // this should requeue all the messages browsed
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
}
