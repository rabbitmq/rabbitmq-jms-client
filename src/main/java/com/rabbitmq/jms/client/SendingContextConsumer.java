package com.rabbitmq.jms.client;

import javax.jms.JMSException;

/**
 * Callback before sending a message.
 *
 * @see com.rabbitmq.jms.admin.RMQConnectionFactory#setSendingContextConsumer(SendingContextConsumer)
 * @see SendingContext
 * @since 1.11.0
 */
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

}
