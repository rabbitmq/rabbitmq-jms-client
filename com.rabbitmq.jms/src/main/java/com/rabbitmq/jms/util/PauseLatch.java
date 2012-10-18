package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Latch which can pause and resume multiple participating threads.
 * <p>
 * When latch is open, participating threads will not block on the latch. When latch is closed, threads will block,
 * until the latch is opened (by another thread).
 * </p>
 * <p>{@link #pause()} will close the latch,
 * <br/>{@link #resume()} will open the latch, and unblock all waiting threads,
 * <br/>{@link #await await(...)} will block the calling thread if (and only if) the latch is closed,
 * <br/>{@link #finalResume()} will open the latch and nail it open (cannot be closed again).
 * </p>
 * <p>A <code>PauseLatch</code> may be constructed open or closed.
 */
public class PauseLatch {

    private static final int CLOSED = 1;
    private static final int OPEN = 0;

    private volatile boolean finalResume = false;
    /**
     * AQS implementation of synchronisation primitive to pause (block) and resume multiple threads.
     * <p>State is single integer with two values: 0 meaning OPEN, 1 meaning CLOSED.</p>
     * <dl>
     * <dt>acquire</dt>
     * <dd>fails if in CLOSED state (and blocks thread), and succeeds if in OPEN state</dd>
     * <dt>release</dt>
     * <dd>succeeds if in CLOSED state, and fails if in OPEN state (</dd>
     * </dl>
     */
    private static class PauseSync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1715200786237741115L;

        /**
         * @param closed initial state of the latch
         */
        PauseSync(boolean closed) {
            setState(closed?CLOSED:OPEN);
        }

        /**
         * Returns true if the latch is closed
         * @return
         */
        boolean isClosed() {
            return getState() == CLOSED;
        }

        /**
         * {@inheritDoc}
         * @return fail (return -1) if CLOSED; succeed (return +1) if OPEN
         * <p/>{@inheritDoc}
         */
        @Override
        public int tryAcquireShared(int ignored) {
            return isClosed() ? -1 : 1;
        }

        /**
         * {@inheritDoc}
         * @param nextState the state to set the <code>PauseLatch</code> to.
         * <p/>{@inheritDoc}
         * @return <code>true</code> if set to <code>OPEN</code>; <code>false</code> if set to <code>CLOSED</code>.
         * <p/>{@inheritDoc}
         */
        @Override
        public boolean tryReleaseShared(int nextState) {
            while (!compareAndSetState(getState(), nextState)) {}
            return (nextState == OPEN);
        }
    }

    private final PauseSync sync;

    /**
     * Create a <code>PauseLatch</code>, with initial state.
     * @param closed initially blocking threads if <code>true</code>, open if <code>false</code>.
     */
    public PauseLatch(boolean closed) {
        sync = new PauseSync(closed);
    }

    /**
     * @return <code>true</code> if latch is closed and calls to <code>await(long, TimeUnit)</code> will block,
     *         <code>false</code> otherwise.
     */
    public boolean isPaused() {
        return sync.isClosed();
    }

    /**
     * Close the latch, so subsequent <code>await()</code>ing threads will block.
     * @return <code>true</code> if the latch is closed after this call, <code>false</code> if {@link #finalResume()}
     *         has been called or the latch is open after this call.
     */
    public boolean pause() {
        return !finalResume && !sync.releaseShared(CLOSED);
    }

    /**
     * Opens the latch and wakes up all waiting threads. Does not block.
     * @return <code>true</code> in all cases.
     */
    public boolean resume() {
        return sync.releaseShared(OPEN);
    }

    /**
     * Wakes up all waiting threads.
     * After this call, all subsequent calls to <code>pause()</code> will be ignored
     * @return <code>true</code> if the latch is open after this call
     */
    public boolean finalResume() {
        finalResume = true;
        return resume();
    }

    /**
     * Returns true immediately if the latch is open.
     * Otherwise the latch is closed and the thread blocks until one of the following occurs:
     * <dl>
     * <dt>latch is opened (by another thread)</dt>
     * <dd>thread unblocked and returns <code>true</code></dd>
     * <dt>timeout expires before latch is opened</dt>
     * <dd>thread unblocked and returns <code>false</code></dd>
     * </dl>
     * @param timeout the time to wait for the latch to open.
     * @param unit the time unit of the timeout argument.
     * @return <code>false</code> if timeout was reached before latch opens; <code>true</code> if latch is open or opens while we are waiting.
     * @throws InterruptedException if the calling thread was interrupted while waiting.
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(0, unit.toNanos(timeout)); // first parameter is ignored
    }
}
