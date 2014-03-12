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
    private final SqlEvaluator evaluator;
    private final RMQSession session;

    public BrowsingMessageQueue(RMQSession session, RMQDestination dest, String selector) throws JMSException {
        this.dest = dest;
        this.selector = selector;
        this.session = session;
        this.evaluator = setEvaluator(selector);
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
        Channel chan = this.session.getBrowsingChannel();
        return new BrowsingMessageEnumeration(this.session, this.dest, chan, this.evaluator);
    }

    @Override
    public void close() throws JMSException {
        //no-op
        return;
    }
}
