/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.util.RJMSLogger;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Receive messages from RMQ Queue with delay (timer).
 * <p>
 * The blocking method <code>get()</code> only returns with <code>null</code> when either the Receiver is closed,
 * or the timeout expires.
 * </p>
 */
class DelayedReceiver {

    private static final RJMSLogger LOGGER = new RJMSLogger("DelayedReceiver");

    @SuppressWarnings("unused")
    private final int batchingSize;
    private final RMQMessageConsumer rmqMessageConsumer;

    private final Object responseLock = new Object();
    private boolean aborted = false; // @GuardedBy(responseLock)

    /**
     * @param batchingSize - the intended limit of messages that can be pre-fetched.
     * @param rmqMessageConsumer - the JMS MessageConsumer we are serving.
     */
    public DelayedReceiver(int batchingSize, RMQMessageConsumer rmqMessageConsumer) {
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

    private void abort() {
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