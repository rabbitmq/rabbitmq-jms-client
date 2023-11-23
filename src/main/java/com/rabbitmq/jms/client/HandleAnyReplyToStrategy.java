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
 * Implementation of the reply to strategy that deals with any reply-to
 * value received and will use the default, i.e. "", exchange.
 * <b>
 * If the reply-to starts with "amq.rabbitmq.reply-to", this will
 * correctly handle these types of temporary queues.
 *
 * @since 2.9.0
 */
public class HandleAnyReplyToStrategy implements ReplyToStrategy {

    private static final long serialVersionUID = -496756742546656456L;

    /** An instance of the strategy to avoid having to create multiple copies of the object. */
    public static final HandleAnyReplyToStrategy INSTANCE = new HandleAnyReplyToStrategy();

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

        if (replyTo != null && !"".equals(replyTo)) {
            if (replyTo.startsWith(DIRECT_REPLY_TO)) {
                message.setJMSReplyTo(new RMQDestination(DIRECT_REPLY_TO, "", replyTo, replyTo));
            } else {
                message.setJMSReplyTo(new RMQDestination(replyTo, "", replyTo, replyTo));
            }
        }
    }
}
