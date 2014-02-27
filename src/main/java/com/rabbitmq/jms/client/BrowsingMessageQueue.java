/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.parse.sql.SqlEvaluator;
import com.rabbitmq.jms.parse.sql.SqlParser;
import com.rabbitmq.jms.parse.sql.SqlTokenStream;
import com.rabbitmq.jms.util.RMQJMSSelectorException;

/**
 * Implementation class suitable for storing message information for browsing.
 */
class BrowsingMessageQueue implements QueueBrowser {

    private final String selector;
    private final RMQDestination dest; // only needed for getQueue();

    private final BrowsingMessageEnumeration msgQueue;

    public BrowsingMessageQueue(RMQSession session, RMQDestination dest, String selector) throws JMSException {
        this.dest = dest;
        this.selector = selector;
        Channel chan = session.getBrowsingChannel();
        SqlEvaluator evaluator = setEvaluator(selector);
        this.msgQueue  = new BrowsingMessageEnumeration(session, dest, chan, evaluator);
    }

    private static final SqlEvaluator setEvaluator(String selector) throws JMSException {
        if (selector==null || selector.trim().isEmpty()) return null;
        SqlEvaluator evaluator = new SqlEvaluator(new SqlParser(new SqlTokenStream(selector)), RMQSession.JMS_TYPE_IDENTS);
        if (!evaluator.evaluatorOk())
            throw new RMQJMSSelectorException(evaluator.getErrorMessage());
        return evaluator;
    }
    @Override
    public javax.jms.Queue getQueue() throws JMSException {
        return this.dest;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        return this.selector;
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
