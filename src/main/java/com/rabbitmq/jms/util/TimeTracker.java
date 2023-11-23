/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;

/**
 * Simple class to track elapsed time.  Initialised with any time units, returns remaining time (in nanoseconds) on request.
 */
public class TimeTracker {
    private final long timeoutNanos;
    private final long startNanos;
    private volatile boolean timed_out; /* becomes true and then sticks there */

    /** Public tracker that is permanently timed out. */
    public static final TimeTracker ZERO = new TimeTracker(0);

    /**
     * Initialise tracker with duration supplied.
     * @param timeout - duration of tracker
     * @param unit - units that <code>timeout</code> is in, e.g. <code>TimeUnit.MILLISECONDS</code>.
     */
    public TimeTracker(long timeout, TimeUnit unit) {
        this(unit.toNanos(timeout));
    }

    /**
     * Initialise tracker with duration of old tracker (regardless that the old one has timed out).
     * @param timeTracker - a TimeTracker instance
     */
    public TimeTracker(TimeTracker timeTracker) {
        this(timeTracker.timeoutNanos());
    }

    /**
     * Initialise tracker with maximum duration -- effectively an infinite time.
     */
    public TimeTracker() {
        this(Long.MAX_VALUE);
    }

    /**
     * Accessor for resurrecting a TimeTracker
     * @return time originally set in nanoseconds
     */
    long timeoutNanos() {
        return this.timeoutNanos;
    }

    /**
     * Initialise tracker with nanoseconds duration supplied.
     * @param timeoutNanos - duration of tracker in nanoseconds
     */
    private TimeTracker(long timeoutNanos) {
        this.timeoutNanos = timeoutNanos;
        this.startNanos = System.nanoTime();
        this.timed_out = (timeoutNanos <= 0);
    }

    /**
     * Return the remaining time to go in internal units, which are nanoseconds, or zero if there is no more.
     * @return remaining time (in nanoseconds) - 0 means time has run out
     */
    private long internalRemaining() {
        if (this.timed_out) return 0;
        long rem = this.timeoutNanos - (System.nanoTime() - this.startNanos);
        if (rem<=0) {
            this.timed_out = true;
            return 0;
        }
        return rem;
    }

    /**
     * Return the remaining time to go in nanoseconds, or zero if there is no more.
     * @return remaining time (in nanoseconds) - 0 means time has run out
     */
    public long remainingNanos() {
        return this.internalRemaining();
    }

    /**
     * Return the remaining time to go in milliseconds, or zero if there is no more.
     * @return remaining time (in milliseconds) - 0 means time has run out
     */
    public long remainingMillis() {
        return TimeUnit.NANOSECONDS.toMillis(this.internalRemaining());
    }

    /**
     * A {@link TimeUnit#timedWait} utility which uses the <code>TimeTracker</code> state.
     * <p>
     * Used in <code><b>synchronized</b>(<i>lock</i>){}</code> blocks that want to timeout based upon a time tracker object.
     * </p>
     * @param lock - object to lock on
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public void timedWait(Object lock) throws InterruptedException {
        TimeUnit.NANOSECONDS.timedWait(lock, this.internalRemaining());
    }

    /**
     * @return <code>true</code> if time has run out, <code>false</code> otherwise
     */
    public boolean timedOut() {
        return (this.timed_out || this.internalRemaining()==0);
    }

    @Override
    public String toString() {
        long timeoutMillis = TimeUnit.NANOSECONDS.toMillis(timeoutNanos);
        long internalTimeoutMillis = TimeUnit.NANOSECONDS.toMillis(this.internalRemaining());

        return (new StringBuilder("TimeTracker(")
                .append(timeoutMillis)
                .append("ms set, ")
                .append(internalTimeoutMillis).append("ms rem)")
        ).toString();
    }
}
