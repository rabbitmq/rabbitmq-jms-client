// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.Channel;

import javax.jms.CompletionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle publisher confirms.
 *
 * @since 1.13.0
 */
class PublisherConfirmsUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherConfirmsUtils.class);

    /**
     * Enables publisher confirms support.
     * <p>
     * Adds a {@link com.rabbitmq.client.ConfirmListener} to the AMQP {@link Channel}
     * and notifies the user-provided {@link ConfirmListener} when confirms notified
     * arrive.
     * <p>
     * Returns a {@link PublishingListener} that must be called whenever a message is published.
     *
     * @param channel
     * @param confirmListener
     * @return
     */
    static PublishingListener configurePublisherConfirmsSupport(Channel channel, ConfirmListener confirmListener) {
        final Map<Long, OutboundMessageContext> outstandingConfirms = new ConcurrentHashMap<>();
        final AtomicLong multipleLowerBound = new AtomicLong(1);
        PublishingListener publishingListener = (message, completionListener, sequenceNumber) -> {
            outstandingConfirms.put(sequenceNumber, new OutboundMessageContext(message, completionListener));
        };
        channel.addConfirmListener(new com.rabbitmq.client.ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) {
                cleanPublisherConfirmsCorrelation(
                        outstandingConfirms, multipleLowerBound,
                        deliveryTag, multiple, context -> {
                            executeSafely(
                                () -> context.completionListener.onCompletion(context.message),
                                "CompletionListener"
                            );
                            executeSafely(
                                () -> confirmListener.handle(new PublisherConfirmContext(context.message, true)),
                                "ConfirmListener"
                            );
                        }
                );
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) {
                cleanPublisherConfirmsCorrelation(
                        outstandingConfirms, multipleLowerBound,
                        deliveryTag, multiple, context -> {
                            executeSafely(
                                () -> context.completionListener.onException(context.message,
                                    new JMSException("Outbound message was negatively acknowledged")),
                                "CompletionListener"
                            );
                            executeSafely(
                                () -> confirmListener.handle(
                                    new PublisherConfirmContext(context.message, false)),
                                "ConfirmListener"
                            );
                        }
                );
            }
        });
        return publishingListener;
    }

    private static void executeSafely(VoidCallable callable, String object) {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.warn("Error while executing {}: {}", object, e.getMessage());
        }
    }

    /**
     * Cleans the data structure used to correlate publishing sequence numbers to messages when a confirm comes in.
     * <p>
     * Invoke a provided callback for each message confirmed/nack-ed.
     *
     * @param outstandingConfirms
     * @param multipleLowerBound
     * @param deliveryTag
     * @param multiple
     * @param messageConsumer
     */
    private static void cleanPublisherConfirmsCorrelation(Map<Long, OutboundMessageContext> outstandingConfirms, AtomicLong multipleLowerBound,
                                                          long deliveryTag, boolean multiple, Consumer<OutboundMessageContext> messageConsumer) {
        Long lowerBound = multipleLowerBound.get();
        if (multiple) {
            for (long i = lowerBound; i <= deliveryTag; i++) {
                OutboundMessageContext context = outstandingConfirms.remove(i);
                if (context != null) {
                    messageConsumer.accept(context);
                }
            }
        } else {
            OutboundMessageContext context = outstandingConfirms.remove(deliveryTag);
            if (context != null) {
                messageConsumer.accept(context);
            }
            if (deliveryTag == lowerBound + 1) {
                multipleLowerBound.compareAndSet(lowerBound, deliveryTag);
            }
        }
    }

    private static class OutboundMessageContext {

        private final Message message;
        private final CompletionListener completionListener;


        private OutboundMessageContext(Message message, CompletionListener completionListener) {
            this.message = message;
            this.completionListener = completionListener;
        }
    }

    @FunctionalInterface
    private interface VoidCallable {

        void call() throws Exception;

    }

}
