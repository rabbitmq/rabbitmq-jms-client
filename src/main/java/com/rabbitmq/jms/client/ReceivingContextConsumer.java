package com.rabbitmq.jms.client;

import javax.jms.JMSException;

/**
 * Callback before receiving a message.
 *
 * @see com.rabbitmq.jms.admin.RMQConnectionFactory#setSendingContextConsumer(SendingContextConsumer)
 * @see SendingContext
 * @since 1.11.0
 */
public interface ReceivingContextConsumer {

    ReceivingContextConsumer NO_OP = new ReceivingContextConsumer() {

        @Override
        public void accept(ReceivingContext ctx) {
        }
    };

    /**
     * Called before receiving a message.
     * <p>
     * Can be used to customize the message
     * before it is dispatched to application code.
     *
     * @param receivingContext
     * @throws JMSException
     */
    void accept(ReceivingContext receivingContext) throws JMSException;
}
