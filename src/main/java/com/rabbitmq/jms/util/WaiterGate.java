/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;


/**
 * Hand-crafted gate for pausing multiple threads on entry to a region. Allows waiting threads to be aborted (return
 * with {@link AbortedException}) without using interrupts.
 * <dl>
 * <dt>Description</dt>
 * <dd>
 * <p>
 * The {@link GateState} is either {@link GateState.OPENED OPENED}, {@link GateState.CLOSED CLOSED} or {@link GateState.ABORTED ABORTED}, and the main
 * operations are {@link #open()}, {@link #close()}, {@link #waitForOpen(TimeTracker) waitForOpen()} and
 * {@link #abort()}. The abstract methods {@link #onEntry()} and {@link #onAbort()}
 * must be defined by an implementing class and are called (under the gate {@link GateState.lock lock} at the appropriate transition points.
 * </p>
 * <p>
 * If the gate is {@link GateState.CLOSED CLOSED} and {@link #waitForOpen(TimeTracker) waitForOpen()} is called, the caller blocks until the
 * gate is {@link GateState.OPENED OPENED}, the timeout expires, or the gate is {@link GateState.ABORTED ABORTED}. If the gate is
 * {@link GateState.OPENED OPENED}, either already or at a later time, however briefly, the call returns <code>true</code>
 * after the {@link #onEntry()} method is called.
 * </p>
 * <p>
 * A gate may be {@link GateState.OPENED OPENED} and {@link GateState.CLOSED CLOSED} multiple times
 * ({@link WaiterGate.generation generation} counts these openings, starting at 0)
 * but once {@link GateState.ABORTED ABORTED} cannot be opened again. If a thread waits for a gate to open, and the
 * gate is opened, then the thread will enter the gate eventually, even if the gate is opened and closed rapidly
 * before the thread gets control again. This is the reason for the {@link WaiterGate.generation generation} state.
 * </p>
 * </dd>
 * </dl>
 */
abstract class WaiterGate {

    /** possible states of the gate */
    private enum GateState { OPENED, CLOSED, ABORTED };

    private Object lock = new Object();
      private GateState state; // @GuardedBy("lock")
      private long generation; // @GuardedBy("lock")

    /**
     * Create a gate, either {@link GateState.OPENED OPENED} or {@link GateState.CLOSED CLOSED}. It cannot be created {@link GateState.ABORTED ABORTED}.
     * @param opened - <code>true</code> if gate is initially {@link GateState.OPENED OPENED}; <code>false</code> if initially
     *            {@link GateState.CLOSED CLOSED}
     */
    public WaiterGate(boolean opened) {
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
     * Close the gate, provided it is {@link GateState.OPENED OPENED}.
     * @return <code>true</code> if gate {@link GateState.CLOSED CLOSED} by this call, <code>false</code> otherwise
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
     * Open the gate. Can be done only if gate is {@link GateState.CLOSED CLOSED}.
     * @return <code>true</code> if gate {@link GateState.OPENED OPENED} by this call, <code>false</code> otherwise
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
     * Wait (and queue) if gate is {@link GateState.CLOSED CLOSED}; or return <code>true</code> if gate is
     * {@link GateState.OPENED OPENED} soon enough.
     * <p>Calls {@link #onEntry()} when gate is {@link GateState.OPENED OPENED} now or later and
     * {@link #onAbort()} when the gate is {@link GateState.ABORTED ABORTED} now or later.
     * </p>
     * @param tracker - timeout tracker; tracks time until gate is {@link GateState.OPENED OPENED}.
     * @return <code>true</code> if gate is {@link GateState.OPENED OPENED} now or within time limit, <code>false</code> if time
     *         limit expires while waiting
     * @throws InterruptedException if waiting thread is interrupted.
     * @throws AbortedException if gate is {@link GateState.ABORTED ABORTED} now or within time limit.
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
     * Abort (cause to abandon wait and throw {@link AbortedException}) all threads waiting on {@link GateState.CLOSED CLOSED} gate.
     * @return <code>true</code> if gate is {@link GateState.ABORTED ABORTED} by this call; <code>false</code> otherwise
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