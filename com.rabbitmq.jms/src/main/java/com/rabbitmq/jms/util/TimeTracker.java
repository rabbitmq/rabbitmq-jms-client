package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;

/**
 * Simple class to track elapsed time.  Initialised with any time units, returns remaining time (in nanoseconds) on request.
 */
public class TimeTracker {
    private final long timeoutNanos;
    private final long startNanos;
    private volatile boolean timeout; /* becomes true and then sticks there */

    /**
     * Initialise tracker with duration supplied.
     * @param timeout - duration of tracker
     * @param unit - units that <code>timeout</code> is in, e.g. <code>TimeUnit.MILLISECONDS</code>.
     */
    public TimeTracker(long timeout, TimeUnit unit) {
        this.timeoutNanos = unit.toNanos(timeout);
        this.startNanos = System.nanoTime();
        this.timeout = (this.timeoutNanos <= 0);
    }

    /**
     * Initialise tracker with maximum duration -- effectively an infinite time.
     */
    public TimeTracker() {
        this(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    /**
     * Return the remaining time to go in internal units, which are nanoseconds, or zero if there is no more.
     * @return remaining time (in nanoseconds) - 0 means time has run out
     */
    private long internalRemaining() {
        if (this.timeout) return 0;
        long rem = this.timeoutNanos - (System.nanoTime() - this.startNanos);
        if (rem<=0) {
            this.timeout = true;
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
        return (this.timeout || this.internalRemaining()==0);
    }
}
