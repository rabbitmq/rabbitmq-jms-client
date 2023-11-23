// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import javax.jms.JMSException;
import javax.jms.Message;

import com.rabbitmq.jms.admin.RMQDestination;

/**
 * Default implementation of the reply-to strategy.
 * <b>
 * This will ensure that any reply to queues using the
 * amq.rabbitmq.reply-to.<id> are correctly created.
 *
 * @since 2.9.0
 */
public class DefaultReplyToStrategy implements ReplyToStrategy {

    private static final long serialVersionUID = -496756742546656456L;

    /** An instance of the strategy to avoid having to create multiple copies of the object. */
    public static final DefaultReplyToStrategy INSTANCE = new DefaultReplyToStrategy();

    /**
     * Handles the reply to on a received message.
     *
     * @param message  The RMQMessage that has been received.
     * @param replyTo  The reply to queue value received.
     * @throws JMSException  if there's an issue updating the RMQMessage
     */
    @Override
    public void handleReplyTo(
            final Message message,
            final String replyTo) throws JMSException {

        if (replyTo != null && replyTo.startsWith(DIRECT_REPLY_TO)) {
            message.setJMSReplyTo(new RMQDestination(DIRECT_REPLY_TO, "", replyTo, replyTo));
        }
    }
}
