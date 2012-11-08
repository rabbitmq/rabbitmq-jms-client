package com.rabbitmq.jms.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Intermediate buffer between RabbitMQ queue and JMS client.
 * <p>
 * The buffer offers a blocking get, with timeout, where the buffer is populated by
 * an asynchronous RabbitMQ {@link Consumer}.  The buffer fills with messages from
 * a RabbitMQ queue, up to a certain size, prompted to do so whenever the buffer is empty.
 * A new Consumer is constructed whenever needed to populate the buffer.
 * </p>
 * <p>
 * The buffer is dedicated to a particular queue, and has a fixed 'fill size' when created.
 * If more messages than the 'fill size' try to get put in the buffer the excess messages
 * are NACKed back to RabbitMQ.
 * </p>
 * <p>
 * The blocking method <code>get()</code> only returns with <code>null</code> when either the buffer is closed,
 * or the timeout expires.
 * </p>
 * <p>
 * When the buffer is <code>close()</code>d, the messages remaining in the buffer are NACKed.
 * </p>
 */
class ReceiveBuffer {

    private final BlockingQueue<GetResponse> buffer = new LinkedBlockingQueue<GetResponse>();
    private final int batchingSize;
    private final RMQMessageConsumer rmqMessageConsumer;

    /**
     * @param batchingSize - the intended limit of messages that can remain in the buffer.
     * @param rmqMessageConsumer - the JMS MessageConsumer we are serving.
     */
    public ReceiveBuffer(int batchingSize, RMQMessageConsumer rmqMessageConsumer) {
        this.batchingSize = batchingSize;
        this.rmqMessageConsumer = rmqMessageConsumer;
    }

    /**
     * Get a message if one arrives in the time available.
     * @param tt - keeps track of the time
     * @return message gotten, or <code>null</code> if timeout or connection closed.
     */
    public GetResponse get(TimeTracker tt) {
        GetResponse resp = this.buffer.poll();
        if (ReceiveConsumer.isEOFMessage(resp)) { // we've aborted
            return null;
        }
        if (null!=resp)
            return resp;
        // nothing of import on the queue, let's try to get some more
        ReceiveConsumer rc = this.getSomeMore(tt);
        try {
            resp = this.buffer.poll(tt.remainingNanos(), TimeUnit.NANOSECONDS);
            if (resp==null                          // we timed out
             || ReceiveConsumer.isEOFMessage(resp)) // Consumer ended before we timed out
                return null;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        } finally {
            rc.cancel(); // ensure consumer is removed (eventually; may block).
        }
        // real messages (or interruptions) drop through to here
        return resp;
    }

    private ReceiveConsumer getSomeMore(TimeTracker tt) {
        // TODO: set up a Consumer to put messages in the buffer, and die after timeout or if buffer is filled.
        // TODO: If there is a Consumer already setup, and not cancelling, adjust the timeout.
        // TODO: If the existing Consumer is cancelling, wait for it to finish before creating a new one.
        ReceiveConsumer receiveConsumer = new ReceiveConsumer(this.rmqMessageConsumer, this.buffer, this.batchingSize);
        receiveConsumer.register();
        return receiveConsumer;
    }
}
