// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import javax.jms.JMSException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Callback before sending a message.
 *
 * @see com.rabbitmq.jms.admin.RMQConnectionFactory#setSendingContextConsumer(SendingContextConsumer)
 * @see SendingContext
 * @since 1.11.0
 */
@FunctionalInterface
public interface SendingContextConsumer {

    /**
     * Called before sending a message.
     * <p>
     * Can be used to customize the message or the destination
     * before the message is actually sent.
     *
     * @param sendingContext
     * @throws JMSException
     */
    void accept(SendingContext sendingContext) throws JMSException;

    /**
     * Same semantics as {@link Consumer#andThen(Consumer)}.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code SendingContextConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default SendingContextConsumer andThen(SendingContextConsumer after) {
        Objects.requireNonNull(after);
        return (SendingContext ctx) -> {
            accept(ctx);
            after.accept(ctx);
        };
    }
}
