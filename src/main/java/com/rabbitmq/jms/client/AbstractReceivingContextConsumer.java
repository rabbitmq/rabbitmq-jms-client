package com.rabbitmq.jms.client;

import javax.jms.JMSException;

/**
 * Composeable {@link ReceivingContextConsumer}.
 *
 * @see ReceivingContextConsumer
 * @since 1.11.0
 */
public abstract class AbstractReceivingContextConsumer implements ReceivingContextConsumer {

    /**
     * Returns a composed {@code AbstractReceivingContextConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code ReceivingContextConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    public AbstractReceivingContextConsumer andThen(final ReceivingContextConsumer after) {
        if (after == null) {
            throw new NullPointerException();
        }
        return new AbstractReceivingContextConsumer() {

            @Override
            public void accept(ReceivingContext ctx) throws JMSException {
                AbstractReceivingContextConsumer.this.accept(ctx);
                after.accept(ctx);
            }
        };
    }
}
