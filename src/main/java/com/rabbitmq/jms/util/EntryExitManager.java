/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.jms.client.Completion;

/**
 * Manages threads entering and exiting a notional region. Entry is controlled by a gate, and exit is signalled by
 * {@link Completion}. Can block threads entering, abort waiting threads and wait for threads which have entered to exit.
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
 * <dt>{@link #enter enter(...)}</dt>
 * <dd>will allow the calling thread to enter the region, or block if the gate is closed,</dd>
 * <dt>{@link #exit()}</dt>
 * <dd>will signal the calling thread to exit the region,</dd>
 * <dt>{@link #waitToClear waitToClear(...)}</dt>
 * <dd>will block until all the threads currently in the region have exited,</dd>
 * <dt>{@link #abortWaiters()}</dt>
 * <dd>will reject all waiting threads with an <code>AbortedException</code>.</dd>
 * </dl>
 */
public class EntryExitManager {

    private final WaiterGate gate;
    private final Queue<Completion> entered = new ConcurrentLinkedQueue<Completion>();
    private ThreadLocal<Completion> threadCompletion = new ThreadLocal<Completion>();

    private void registerEntry() {
        Completion comp = new Completion();
        this.threadCompletion.set(comp); // thread-local
        this.entered.add(comp);
    }

    /**
     * Create an <code>EntryExitManager</code>, initially closed.
     */
    public EntryExitManager() {
        this.gate = new WaiterGate(false) {
            @Override public void onEntry() { /*register on entry*/ EntryExitManager.this.registerEntry();}
            @Override public void onAbort() { /*noop*/ }
            };
    }

    /**
     * Is the gate closed?
     * @return <code>true</code> if the gate is closed, <code>false</code> otherwise.
     */
    public boolean isClosed() {
        return !gate.isOpen();
    }

    /**
     * Close the gate, if allowed, so subsequent <code>enter()</code>ing threads will block.
     * @return <code>true</code> in all cases.
     */
    public boolean closeGate() {
        gate.close();
        return true;
    }

    /**
     * Opens the gate and wakes up all waiting threads. Does not block.
     * @return <code>true</code> in all cases.
     */
    public boolean openGate() {
        gate.open();
        return true;
    }

    /**
     * Returns <code>true</code> immediately if the gate is open.
     * Otherwise if the gate is closed the thread blocks until one of the following occurs:
     * <dl>
     * <dt>gate is opened (by another thread);</dt>
     * <dt>timeout expires before gate is opened.</dt>
     * </dl>
     * @param timeout the time to wait for the gate to open.
     * @param unit the time unit of the <code>timeout</code> argument.
     * @return <code>false</code> if timeout was reached before gate opens; <code>true</code> if gate is open or opens while we are waiting.
     * @throws InterruptedException if the callers thread is interrupted while waiting.
     * @throws AbortedException if this thread is aborted by a <code>stop()</code> or <code>close()</code> while waiting.
     */
    public boolean enter(long timeout, TimeUnit unit) throws InterruptedException, AbortedException {
        return enter(new TimeTracker(timeout, unit));
    }

    /**
     * Returns <code>true</code> immediately if the gate is open.
     * Otherwise if the gate is closed the thread blocks until one of the following occurs:
     * <dl>
     * <dt>gate is opened (by another thread);</dt>
     * <dt>timeout expires before gate is opened.</dt>
     * </dl>
     * @param tt the time tracker used to wait for the gate to open.
     * @return <code>false</code> if timeout was reached before gate opens; <code>true</code> if gate is open or opens while we are waiting.
     * @throws InterruptedException if the callers thread is interrupted while waiting.
     * @throws AbortedException if this thread is aborted by a <code>stop()</code> or <code>close()</code> while waiting.
     */
    public boolean enter(TimeTracker tt) throws InterruptedException, AbortedException {
        return gate.waitForOpen(tt);
    }

    /**
     * This thread is exiting the region. Must be called eventually by the thread that entered the region.
     */
    public void exit() {
        Completion comp = this.threadCompletion.get(); // this thread's completion object
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
     * @throws InterruptedException if thread is interrupted and is waiting.
     */
    public boolean waitToClear(long timeout, TimeUnit unit) throws InterruptedException {
        return waitToClear(new TimeTracker(timeout, unit));
    }

    /**
     * Wait for current threads to exit region.
     * @param tt timeout tracker.
     * @return <code>true</code> if they all exited in time, <code>false</code> otherwise.
     * @throws InterruptedException if thread is interrupted and is waiting.
     */
    public boolean waitToClear(TimeTracker tt) throws InterruptedException {
        List<Completion> comps = new LinkedList<Completion>(this.entered);
        if (comps.isEmpty()) return true; // nothing to wait for
        for (Completion c : comps) {
            try {
                if (tt.timedOut()) return false;
                c.waitUntilComplete(tt);
            } catch (TimeoutException unused) {
                return false; // we ran out of time
            }
        }
        return true;
    }

    /**
     * Abort all threads waiting to enter with an <code>AbortedException</code>.
     */
    public void abortWaiters() {
        gate.abort();
    }
}