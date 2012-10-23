package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;

/**
 * Hand-crafted lockable gate for pausing multiple threads on entry to a region.
 * Allows waiting to be aborted without using interrupts.
 */
abstract class GateWaiter {
    private Object lock = new Object();
    /** Generation number of queued threads for a close. */
    /*@GuardedBy("lock")*/ private long generation;
    /*@GuardedBy("lock")*/ private long openGeneration;
    /*@GuardedBy("lock")*/ private long abortGeneration;
    private volatile boolean fixedOpen = false;
    /**
     * Create a gate.
     * @param open - whether gate is initially open or not (closed).
     */
    public GateWaiter(boolean open) {
        this.generation = 0;
        this.openGeneration = open ? 0 : -1;
        this.abortGeneration = -1;
    }
    /**
     * @return <code>true</code> if gate is open, <code>false</code> otherwise
     */
    public final boolean isOpen() {
        synchronized(this.lock){
            return this.openGeneration >= this.generation;
        }
    }

    /**
     * Close the gate for next generation of entrants, unless already closed.
     * @return <code>true</code> if gate closed, <code>false</code> otherwise
     */
    public final boolean close() {
        if (this.fixedOpen) return false;
        synchronized(this.lock) {
            if (this.isOpen()) {
                ++this.generation;
            }
        }
        return true;
    }

    /**
     * Open the gate. Can be done at any time.  Will not stop aborting if it is in-progress.
     */
    public final void open() {
        synchronized(this.lock) {
            this.openGeneration = this.generation;
            this.lock.notifyAll(); // allow current generation of queued threads to pass.
        }
    }

    /**
     * Called when thread waits for the gate to open.
     */
    public abstract void onWait();

    /**
     * Called when thread passes through open gate.
     */
    public abstract void onEntry();

    /**
     * Wait (and queue) if gate is closed; or register and return <code>true</code> if gate is opened soon enough.
     * @param timeoutNanos - time to wait if gate is closed, must be >= 0
     * @return <code>true</code> if gate is open or opened within time limit, <code>false</code> if timed out
     * @throws InterruptedException if waiting thread is interrupted.
     * @throws AbortedException is thrown if gate is aborted.
     */
    public final boolean waitForOpen(long timeoutNanos) throws InterruptedException, AbortedException {
        if (!this.isOpen())
            this.onWait();
        synchronized(this.lock) {
            if (!this.isOpen()) { // gate closed -- we queue
                long gen = this.generation;  // generation we arrived in
                long rem = timeoutNanos;
                long startTime = System.nanoTime();
                while ((this.abortGeneration < gen) && (this.openGeneration < gen) && (rem > 0)) {
                    TimeUnit.NANOSECONDS.timedWait(this.lock, rem);
                    rem = timeoutNanos - (System.nanoTime() - startTime);
                }
                // this.abortGeneration >= gen  OR  this.openGeneration >= gen  OR  rem <= 0
                if (this.abortGeneration == gen) // we are aborted
                    throw new AbortedException();
                if (this.openGeneration < gen)  // we should have been opened by now
                    return false;  // we timed out
                // fall through when open
            }
        }
        this.onEntry();
        return true;
    }

    /**
     * Abort (cause to abandon wait and throw AbortedException) all threads waiting on closed gate.
     * @return <code>true</code> if waiters are aborted; <code>false</code> if gate is open
     */
    public final boolean abortWaiters() {
        synchronized(this.lock){
            if (this.isOpen()) return false;
            this.abortGeneration = this.generation; // catch current generation
            ++this.generation;                      // let new ones wait
            this.lock.notifyAll();                  // wake up current waiters
            return true;
        }
    }
}