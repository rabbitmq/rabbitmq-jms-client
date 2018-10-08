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
