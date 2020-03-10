/* Copyright (c) 2019-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.client;

import com.rabbitmq.client.Channel;

import javax.jms.Message;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Utility class to handle publisher confirms.
 *
 * @since 1.13.0
 */
class PublisherConfirmsUtils {

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
        final Map<Long, Message> outstandingConfirms = new ConcurrentHashMap<>();
        final AtomicLong multipleLowerBound = new AtomicLong(1);
        PublishingListener publishingListener = (message, sequenceNumber) -> {
            outstandingConfirms.put(sequenceNumber, message);
        };
        channel.addConfirmListener(new com.rabbitmq.client.ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) {
                cleanPublisherConfirmsCorrelation(
                        outstandingConfirms, multipleLowerBound,
                        deliveryTag, multiple, message -> confirmListener.handle(new PublisherConfirmContext(message, true))
                );
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) {
                cleanPublisherConfirmsCorrelation(
                        outstandingConfirms, multipleLowerBound,
                        deliveryTag, multiple, message -> confirmListener.handle(new PublisherConfirmContext(message, false))
                );
            }
        });
        return publishingListener;
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
    private static void cleanPublisherConfirmsCorrelation(Map<Long, Message> outstandingConfirms, AtomicLong multipleLowerBound,
                                                          long deliveryTag, boolean multiple, Consumer<Message> messageConsumer) {
        Long lowerBound = multipleLowerBound.get();
        if (multiple) {
            for (long i = lowerBound; i <= deliveryTag; i++) {
                Message message = outstandingConfirms.remove(i);
                if (message != null) {
                    messageConsumer.accept(message);
                }
            }
        } else {
            Message message = outstandingConfirms.remove(deliveryTag);
            if (message != null) {
                messageConsumer.accept(message);
            }
            if (deliveryTag == lowerBound + 1) {
                multipleLowerBound.compareAndSet(lowerBound, deliveryTag);
            }
        }
    }

}
