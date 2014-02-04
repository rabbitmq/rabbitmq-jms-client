/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.browsing;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;

import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.client.RMQSession;

/**
 * Implementation class suitable for storing message information for browsing.
 */
public class InternalMessageQueue implements QueueBrowser {

    private final RMQSession session;
    private final RMQDestination dest;

    private final Enumeration<RMQMessage> msgQueue;

    public InternalMessageQueue(RMQSession session, RMQDestination dest) {
        this.session = session;
        this.dest = dest;
        this.msgQueue = new Enumeration<RMQMessage>()
            {   @Override public boolean hasMoreElements() { return false; }
                @Override public RMQMessage  nextElement() { throw new NoSuchElementException(); }
            };
    }

    @Override
    public Queue getQueue() throws JMSException {
        return dest;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        // The selector is always null in this implementation
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getEnumeration() throws JMSException {
        return this.msgQueue;
    }

    @Override
    public void close() throws JMSException {
        // discard all messages from enumeration
        while (this.msgQueue.hasMoreElements()) this.msgQueue.nextElement();
        return;
    }

}
