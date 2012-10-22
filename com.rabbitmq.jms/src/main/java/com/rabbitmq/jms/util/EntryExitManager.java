package com.rabbitmq.jms.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import com.rabbitmq.jms.client.Completion;

/**
 * Manages threads entering and exiting a notional region. Entry is controlled by a gate, and exit is signalled by
 * {@link Completion}. Can prevent threads entering, and wait for threads which have entered to exit.
 * <p>
 * When the gate is open, threads are not prevented from entering. When the gate is closed, threads will block on
 * {@link #enter enter(...)}, until the gate is opened (by some other thread).
 * </p>
 * <p>
 * Threads which enter the region must leave it by calling {@link #exit()}. This will signal the exit of the thread. The
 * manager offers a method {@link #waitToClear waitToClear(...)} which will block
 * until all the currently entered threads have exited the region. Threads which enter during {@link #waitToClear} are not
 * detected.
 * </p>
 * <dl>
 * <dt>{@link #closeGate()}</dt>
 * <dd>will close the gate to all threads,</dd>
 * <dt>{@link #openGate()}</dt>
 * <dd>will open the gate, unblocking all waiting threads,</dd>
 * <dt>{@link #finalOpenGate()}</dt>
 * <dd>will open the gate permanently (cannot be closed again),</dd>
 * <dt>{@link #enter enter(...)}</dt>
 * <dd>will allow the calling thread to enter the region, or block if the gate is closed,</dd>
 * <dt>{@link #exit()}</dt>
 * <dd>will signal the calling thread to exit the region,</dd>
 * <dt>{@link #waitToClear waitToClear(...)}</dt>
 * <dd>will block until all the threads currently in the region have exited.</dd>
 * </dl>
 */
public class EntryExitManager {

    private static final int GATE_CLOSED = 1;
    private static final int GATE_OPEN = 0;
    private ThreadLocal<Completion> completion = new ThreadLocal<Completion>();

    private final GateSync sync;
    private final Queue<Completion> entered = new ConcurrentLinkedQueue<Completion>();

    private volatile boolean gateFixedOpen = false;

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
    private class GateSync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1715200786237741115L;

        /**
         * @param closed initial state of the gate
         */
        GateSync(boolean closed) {
            setState(closed?GATE_CLOSED:GATE_OPEN);
        }

        /**
         * Returns true if the gate is closed
         * @return
         */
        boolean isClosed() {
            return getState() == GATE_CLOSED;
        }

        /**
         * {@inheritDoc}
         * @return fail (return -1) if CLOSED; succeed (return +1) if OPEN
         * <p/>{@inheritDoc}
         */
        @Override
        public int tryAcquireShared(int ignored) {
            if (isClosed())
                return -1;
            else {
                registerEntry();
                return 1;
            }
        }

        /**
         * {@inheritDoc}
         * @param nextState the state to set the <code>GateSync</code> to.
         * <p/>{@inheritDoc}
         * @return <code>true</code> if set to <code>OPEN</code>; <code>false</code> if set to <code>CLOSED</code>.
         * <p/>{@inheritDoc}
         */
        @Override
        public boolean tryReleaseShared(int nextState) {
            while (!compareAndSetState(getState(), nextState)) {}
            return (nextState == GATE_OPEN);
        }
    }

    private void registerEntry() {
        Completion comp = new Completion();
        this.completion.set(comp); // thread-local
        this.entered.add(comp);
    }

    /**
     * Create an <code>EntryExitManager</code>, with initial state.
     * @param closed initially with gate closed if <code>true</code>, open if <code>false</code>.
     */
    public EntryExitManager(boolean closed) {
        sync = new GateSync(closed);
    }

    /**
     * Is the gate closed.
     * @return <code>true</code> if the gate is closed, <code>false</code> otherwise.
     */
    public boolean isClosed() {
        return sync.isClosed();
    }

    /**
     * Close the gate, so subsequent <code>enter()</code>ing threads will block.
     * @return <code>true</code> if the gate is closed after this call, <code>false</code> if {@link #finalOpenGate()}
     *         has been called or the gate is open after this call.
     */
    public boolean closeGate() {
        return !gateFixedOpen && !sync.releaseShared(GATE_CLOSED);
    }

    /**
     * Opens the gate and wakes up all waiting threads. Does not block.
     * @return <code>true</code> in all cases.
     */
    public boolean openGate() {
        return sync.releaseShared(GATE_OPEN);
    }

    /**
     * Opens the gate and wakes up all waiting threads. Does not block.
     * After this call, all subsequent calls to <code>closeGate()</code> will be ignored.
     * @return <code>true</code> if the gate is open after this call
     */
    public boolean finalOpenGate() {
        gateFixedOpen = true;
        return openGate();
    }

    /**
     * Returns <code>true</code> immediately if the gate is open.
     * Otherwise if the gate is closed the thread blocks until one of the following occurs:
     * <dl>
     * <dt>gate is opened (by another thread);</dt>
     * <dt>timeout expires before gate is opened.</dt>
     * </dl>
     * @param timeout the time to wait for the gate to open.
     * @param unit the time unit of the timeout argument.
     * @return <code>false</code> if timeout was reached before gate opens; <code>true</code> if gate is open or opens while we are waiting.
     * @throws InterruptedException if the callers thread is interrupted while waiting.
     */
    public boolean enter(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(0, unit.toNanos(timeout)); // first parameter is ignored
    }

    /**
     * This thread is exiting the region. Must be called eventually by the thread that entered the region.
     */
    public void exit() {
        Completion comp = this.completion.get(); // this thread's completion object
        if (comp != null) {
            comp.setComplete();
            this.entered.remove(comp);
        }
    }

    /**
     * Wait for current threads to exit region.
     * @param timeout max time to wait in <code>unit</code>s.
     * @param unit of time measurement for <code>timeout</code>.
     * @return <code>true</code> if they all exited in time, <code>false</code> otherwise.
     * @throws InterruptedException if thread is interrupted while waiting.
     */
    public boolean waitToClear(long timeout, TimeUnit unit) throws InterruptedException {
        List<Completion> comps = new LinkedList<Completion>(this.entered);
        if (comps.isEmpty()) return true; // nothing to wait for

        long remainingTime = unit.toNanos(timeout);
        long startTime = System.nanoTime();
        for (Completion c : comps) {
            try {
                c.waitUntilComplete(remainingTime, TimeUnit.NANOSECONDS);
                long newStartTime = System.nanoTime();
                remainingTime -= (newStartTime - startTime);
                startTime = newStartTime;
            } catch (TimeoutException unused) {
                return false; // we ran out of time
            }
        }
        return true;
    }
}