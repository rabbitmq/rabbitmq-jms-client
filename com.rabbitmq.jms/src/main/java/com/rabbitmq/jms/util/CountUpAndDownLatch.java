package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Similar implementation like the count down latch, except this latch can be counted up 
 * and counted down. Once it has reached zero, it will allow for a release, but can then be counted up again.
 * @see {link java.util.concurrent.CountDownLatch}
 */

public class CountUpAndDownLatch {

    private static final class CountSync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -1;

        CountSync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        public int tryAcquireShared(int acquires) {
            return getState() == 0? 1 : -1;
        }

        public boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                int nextc = c + releases;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final CountSync countSync;

    /**
     * Create a latch with the initial value.
     * @param count
     */
    public CountUpAndDownLatch(int count) {
        this.countSync = new CountSync(count);
    }

    /**
     * Blocks and waits until the latch reaches zero or the timeout period has passed
     * @param timeout - the time to block on the latch
     * @param unit - the timeunit to be used
     * @return true if the latch reached zero, false if the timeout period passed
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public boolean awaitZero(long timeout, TimeUnit unit) throws InterruptedException {
        return countSync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * Counts down the latch, if it reaches zero then it will release the waiting threads
     */
    public void countDown() {
        countSync.releaseShared(-1);
    }
    
    /**
     * Counts up the latch, if it reaches zero then it will release the waiting threads
     */
    public void countUp() {
        countSync.releaseShared(1);
    }

    /**
     * Returns the current count value
     * @return the count value of this latch
     */
    public long getCount() {
        return countSync.getCount();
    }

}
