/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.util.RJMSLogger;
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

    private static final RJMSLogger LOGGER = new RJMSLogger("ReceiveBuffer");

    @SuppressWarnings("unused")
    private final int batchingSize;
    private final RMQMessageConsumer rmqMessageConsumer;

    private final Object responseLock = new Object();
    private boolean aborted = false; // @GuardedBy(responseLock)

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
        LOGGER.log("get", tt);

        try {
            synchronized (this.responseLock) {
                GetResponse resp = null;
                while (!this.aborted && !tt.timedOut()) {
                    resp = this.rmqMessageConsumer.getFromRabbitQueue();
                    if (resp != null)
                        break;
                    new TimeTracker(100, TimeUnit.MILLISECONDS).timedWait(this.responseLock);
                }
                return resp;
            }

        } catch (InterruptedException e) {
            LOGGER.log("get", e, "interrupted while buffer.poll-ing");
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void abort() {
        synchronized(this.responseLock) {
            this.aborted = true;
            this.responseLock.notifyAll();
        }
    }

    public void close() {
        LOGGER.log("close");
        this.abort();
    }

}