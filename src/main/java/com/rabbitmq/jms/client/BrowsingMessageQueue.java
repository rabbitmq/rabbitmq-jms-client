// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import static com.rabbitmq.jms.client.Subscription.JMS_TYPE_IDENTS;

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
    private final int queueBrowserReadMax;
    private final ReceivingContextConsumer receivingContextConsumer;

    public BrowsingMessageQueue(RMQSession session, RMQDestination dest, String selector,
            int queueBrowserReadMax, ReceivingContextConsumer receivingContextConsumer) throws JMSException {
        this.dest = dest;
        this.selector = selector;
        this.session = session;
        this.evaluator = setEvaluator(selector);
        this.queueBrowserReadMax = queueBrowserReadMax;
        this.receivingContextConsumer = receivingContextConsumer;
    }

    private static final SqlEvaluator setEvaluator(String selector) throws JMSException {
        if (selector==null || selector.trim().isEmpty()) return null;
        SqlEvaluator evaluator = new SqlEvaluator(new SqlParser(new SqlTokenStream(selector)), JMS_TYPE_IDENTS);
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
        Enumeration e = new BrowsingMessageEnumeration(this.session, this.dest, chan, this.evaluator,
            this.queueBrowserReadMax, this.receivingContextConsumer);
        session.closeBrowsingChannel(chan); // this should requeue all the messages browsed
        return e;
    }

    @Override
    public void close() throws JMSException {
        //no-op
        return;
    }
}
