/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;

/**
 * Hand-crafted gate for pausing multiple threads on entry to a region. Allows waiting threads to be aborted (return
 * with {@link AbortedException}) without using interrupts.
 * <dl>
 * <dt>Description</dt>
 * <dd>
 * <p>
 * The <code>GateWaiter</code> is either <code>OPENED</code>, <code>CLOSED</code> or <code>ABORTED</code>, and the main
 * operations are <code>open()</code>, <code>close()</code>, <code>waitForOpen(<i>timeout</i>)</code> and
 * <code>abort()</code>. The abstract methods <code>onEntry()</code> and <code>onAbort()</code>
 * must be defined by an implementing class and are called (under the gate lock) at the appropriate transition points.
 * </p>
 * </dd>
 * <dd>
 * <p>
 * If the gate is <code>CLOSED</code> and <code>waitForOpen(<i>timeout</i>)</code> is called, the caller blocks until the
 * gate is <code>OPENED</code>, the timeout expires, or the gate is <code>ABORTED</code>. If the gate is
 * <code>OPENED</code>, either already or at a later time, the call returns after the <code>onEntry()</code> method is
 * called.
 * </p>
 * </dd>
 * </dl>
 */
abstract class GateWaiter {

    /** possible states of the gate */
    private enum GateState { OPENED, CLOSED, ABORTED };

    private Object lock = new Object();
      private GateState state; // @GuardedBy("lock")
      private long generation; // @GuardedBy("lock")

    /**
     * Create a gate, either <code>OPENED</code> or <code>CLOSED</code>. It cannot be created <code>ABORTED</code>.
     * @param opened - <code>true</code> if gate is initially <code>OPENED</code>; <code>false</code> if initially
     *            <code>CLOSED</code>
     */
    public GateWaiter(boolean opened) {
        this.state = (opened ? GateState.OPENED : GateState.CLOSED);
        this.generation = 0;
    }

    /**
     * @return <code>true</code> if gate is open, <code>false</code> otherwise
     */
    public final boolean isOpen() {
        synchronized(this.lock){
            return this.state == GateState.OPENED;
        }
    }

    /**
     * Close the gate, provided it is <code>OPENED</code>.
     * @return <code>true</code> if gate <code>CLOSED</code> by this call, <code>false</code> otherwise
     */
    public final boolean close() {
        synchronized(this.lock) {
            if (this.state == GateState.OPENED) {
                this.state = GateState.CLOSED;
                this.generation++;
                return true; // no need to notify queued threads.
            }
        }
        return false;
    }

    /**
     * Open the gate. Can be done only if gate is <code>CLOSED</code>.
     * @return <code>true</code> if gate <code>OPENED</code> by this call, <code>false</code> otherwise
     */
    public final boolean open() {
        synchronized(this.lock) {
            if (this.state == GateState.CLOSED) {
                this.state = GateState.OPENED;
                this.lock.notifyAll(); // allow current queued threads to pass.
                return true;
            }
        }
        return false;
    }

    /**
     * Called atomically when thread passes through open gate.
     */
    public abstract void onEntry();

    /**
     * Called atomically when thread is aborted while waiting for open.
     */
    public abstract void onAbort();

    /**
     * Wait (and queue) if gate is <code>CLOSED</code>; or return <code>true</code> if gate is
     * <code>OPENED</code> soon enough.
     * <p>Calls <code>onEntry()</code> when gate is <code>OPENED</code> now or later and
     * <code>onAbort()</code> when the gate is <code>ABORTED</code> now or later.
     * </p>
     * @param timeout - time to wait if gate is <code>CLOSED</code>, must be ≥0.
     * @param unit - units that <code>timeout</code> is expressed in.
     * @return <code>true</code> if gate is <code>OPENED</code> now or within time limit, <code>false</code> if time
     *         limit expires while waiting
     * @throws InterruptedException if waiting thread is interrupted.
     * @throws AbortedException if gate is <code>ABORTED</code> now or within time limit.
     */
    public final boolean waitForOpen(long timeout, TimeUnit unit) throws InterruptedException, AbortedException {
        return waitForOpen(new TimeTracker(timeout, unit));
    }

    /**
     * Wait (and queue) if gate is <code>CLOSED</code>; or return <code>true</code> if gate is
     * <code>OPENED</code> soon enough.
     * <p>Calls <code>onEntry()</code> when gate is <code>OPENED</code> now or later and
     * <code>onAbort()</code> when the gate is <code>ABORTED</code> now or later.
     * </p>
     * @param tracker - timeout tracker; tracks time until gate is <code>OPENED</code>.
     * @return <code>true</code> if gate is <code>OPENED</code> now or within time limit, <code>false</code> if time
     *         limit expires while waiting
     * @throws InterruptedException if waiting thread is interrupted.
     * @throws AbortedException if gate is <code>ABORTED</code> now or within time limit.
     */
    public final boolean waitForOpen(TimeTracker tracker) throws InterruptedException, AbortedException {
        synchronized(this.lock) {
            long arrivalGeneration = this.generation;
            while ((this.state == GateState.CLOSED) && (arrivalGeneration == this.generation) && (!tracker.timedOut())) {
                tracker.timedWait(this.lock);
            }
            // this.state == OPENED | ABORTED OR arrivalGeneration != generation OR timeout()
            GateState derivedState = this.state;
            if (derivedState == GateState.ABORTED) {
                this.onAbort();
                throw new AbortedException();
            } else if (derivedState == GateState.OPENED || arrivalGeneration != this.generation) {
                this.onEntry();
                return true;
            } else
                return false;  // we timed out
        }
    }

    /**
     * Abort (cause to abandon wait and throw {@link AbortedException}) all threads waiting on <code>CLOSED</code> gate.
     * @return <code>true</code> if gate is <code>ABORTED</code> by this call; <code>false</code> otherwise
     */
    public final boolean abort() {
        synchronized(this.lock){
            if (this.state != GateState.CLOSED) return false;
            this.state = GateState.ABORTED;
            this.lock.notifyAll(); // allow current queued threads to see abort.
        }
        return true;
    }
}