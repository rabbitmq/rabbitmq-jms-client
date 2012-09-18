package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Latch which can pause and resume.
 */
public class PauseLatch {

    private volatile boolean finalResume = false;
    private static class PauseSync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1715200786237741115L;

        /**
         * 0 = not paused
         * 1 = paused
         * @param paused - intial state of the latch
         */
        PauseSync(boolean paused) {
            setState(paused?1:0);
        }

        /**
         * Returns true if the state is paused
         * @return
         */
        boolean isPaused() {
            return getState() == 1;
        }

        /**
         * {@inheritDoc}
         */
        public int tryAcquireShared(int acquires) {
            return isPaused() ? -1 : 1;
        }

        /**
         * {@inheritDoc}
         */
        public boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                if (compareAndSetState(c, releases)) {
                    if (c == releases && c == 0 ) {
                        //not paused - resume called
                        return true;
                    } else if (c == releases && c == 1 ) {
                        //already paused - pause called
                        return false;
                    } else {
                        //we've changed state
                        //return true if we are no
                        //longer paused
                        return releases == 0;
                    }
                }

            }
        }
    }

    private final PauseSync sync;

    /**
     * Create a <code>PauseLatch</code>, with initial paused state.
     * @param paused initially paused if <code>true</code>, not paused if <code>false</code>.
     */
    public PauseLatch(boolean paused) {
        sync = new PauseSync(paused);
    }

    /**
     * @return true if latch is paused and calls to <code>await(long, TimeUnit)</code> will block
     */
    public boolean isPaused() {
        return sync.isPaused();
    }

    /**
     * Set the latch in pause state.
     * @return true if the latch is in a paused state after this call
     */
    public boolean pause() {
        if (finalResume) {
            return false;
        } else {
            return !sync.releaseShared(1);
        }
    }

    /**
     * Wakes up all waiting threads.
     * @return true if the latch is not in paused state after this call
     */
    public boolean resume() {
        return sync.releaseShared(0);
    }

    /**
     * wakes up all waiting threads
     * after this call, all subsequent calls to pause will be ignored
     * @return true if the latch is not in paused state after this call
     */
    public boolean finalResume() {
        finalResume = true;
        return sync.releaseShared(0);
    }

    /**
     * Causes the thread to wait if the latch is in a pause state.
     * Otherwise this call returns immediately with value of true
     * @param timeout the time to wait
     * @param unit the time unit of the timeout argument
     * @return false if timeout was reached
     * @throws InterruptedException if the calling thread was interrupted while waiting
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }
}
