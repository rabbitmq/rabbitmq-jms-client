package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Similar implementation like the count down latch, except this latch can be counted up 
 * and counted down. Once it has reached zero, it will allow for a release, but can then be counted up or down again.
 * @see {link java.util.concurrent.CountDownLatch}
 */

public class CountUpAndDownLatch {

    private static final class CountSync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -1;

        /**
         * Constructor with the initial value.
         * Anything but a 0 will make threads lock on the {@link  CountUpAndDownLatch#awaitZero(long, TimeUnit)}
         * method
         * @param count the initial value of the latch
         */
        CountSync(int count) {
            setState(count);
        }

        /**
         * Returns the current value
         * @return
         */
        int getCount() {
            return getState();
        }

        /**
         * {@inheritDoc}
         */
        public int tryAcquireShared(int acquires) {
            return getState() == 0? 1 : -1;
        }

        /**
         * {@inheritDoc}
         */
        public boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                int nextc = c + releases;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    /**
     * synchronization object for threads to wait on
     */
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
