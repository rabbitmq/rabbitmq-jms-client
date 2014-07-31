/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.jms.util.TimeTracker;

/**
 * Used to signal completion of an asynchronous operation.
 */
public class Completion {

    private final FutureBoolean fb = new FutureBoolean();

    /**
     * Signal completion.
     */
    public void setComplete() {
        this.fb.setComplete();
    }

    /**
     * Non-blocking snapshot test for completion.
     */
    public boolean isComplete() {
        return this.fb.isComplete();
    }

    /**
     * Wait (forever) until completion is signalled.
     *
     * @throws InterruptedException if thread is interrupted while waiting.
     */
    public void waitUntilComplete() throws InterruptedException {
        this.fb.get();
    }

    /**
     * Wait for a time limit until completion is signalled. Returns normally if completion is signalled before timeout
     * or interruption.
     *
     * @param timeout time to wait (in units).
     * @param unit units of time for timeout.
     * @throws TimeoutException if timed out before completion is signalled.
     * @throws InterruptedException if thread is interrupted while waiting.
     */
    public void waitUntilComplete(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        this.fb.get(new TimeTracker(timeout, unit));
    }

    /**
     * Wait for a time limit until completion is signalled. Returns normally if completion is signalled before timeout
     * or interruption.
     *
     * @param tt time tracker.
     * @throws TimeoutException if timed out before completion is signalled.
     * @throws InterruptedException if thread is interrupted while waiting.
     */
    public void waitUntilComplete(TimeTracker tt) throws TimeoutException, InterruptedException {
        this.fb.get(tt);
    }

    private class FutureBoolean {
        private final Object lock = new Object();
        private boolean completed = false; // guardedBy(lock)

        public boolean get() throws InterruptedException {
            try {
                return get(new TimeTracker());
            } catch (TimeoutException e) {
                throw new IllegalStateException("Impossible timeout.", e);
            }
        }

        public boolean get(TimeTracker tt) throws InterruptedException, TimeoutException {
            synchronized (this.lock) {
                while (!this.completed && !tt.timedOut()) {
                    tt.timedWait(this.lock);
                }
                if (this.completed)
                    return true;
                else {
                    throw new TimeoutException();
                }
            }
        }

        void setComplete() {
            synchronized (this.lock) {
                this.completed = true;
                this.lock.notifyAll();
            }
        }

        boolean isComplete() {
            synchronized (this.lock) {
                return this.completed;
            }
        }
    }
}
