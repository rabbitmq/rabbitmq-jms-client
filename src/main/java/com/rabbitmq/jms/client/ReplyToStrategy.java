// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2016-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;

import com.rabbitmq.jms.admin.RMQDestination;

/**
 * Interface to provide a pluggable mechanism for dealing with messages
 * received with a reply-to queue specified.
 * <b>
 * Implementations of this interface should update the message's JMSReplyTo
 * property directly.
 *
 * @since 2.9.0
 */
public interface ReplyToStrategy extends Serializable {

    String DIRECT_REPLY_TO = "amq.rabbitmq.reply-to";

  /**
   * Handles the reply to on a received message.
   *
   * @param message  The RMQMessage that has been received.
   * @param replyTo  The reply to queue value received.
   * @throws JMSException  if there's an issue updating the RMQMessage
   */
  void handleReplyTo(Message message, String replyTo) throws JMSException;
}
