// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import javax.jms.Message;

/**
 * Context when receiving message.
 *
 * @see com.rabbitmq.jms.admin.RMQConnectionFactory#setReceivingContextConsumer(ReceivingContextConsumer)
 * @see ReceivingContextConsumer
 * @since 1.11.0
 */
public class ReceivingContext {

    private final Message message;

    public ReceivingContext(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
