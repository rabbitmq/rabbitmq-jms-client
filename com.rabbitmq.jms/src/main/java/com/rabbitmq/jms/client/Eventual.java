package com.rabbitmq.jms.client;

import java.util.concurrent.TimeoutException;

import net.jcip.annotations.GuardedBy;

import com.rabbitmq.jms.util.TimeTracker;

/**
 * Used to hold a value that may be set in the future, with optional timeout on <code>get()</code>. An exception may be
 * set instead, in which case this is thrown instead of returning the value on get().
 *
 * @param <V> - type of value which may eventually be set
 * @param <E> - extension of {@link Exception} that may be set instead of value
 */
public class Eventual<E extends Exception, V> {

    private final Object lock = new Object();
    @GuardedBy("lock")    private boolean completed = false;
    @GuardedBy("lock")    private V value;
    @GuardedBy("lock")    private E exception = null;

    /**
     * Wait (forever) until value or exception is set. Returns normally if value or exception is set before
     * interruption.
     *
     * @throws InterruptedException if thread is interrupted while waiting.
     */
    public void waitUntilSet() throws InterruptedException {
        this.waitUntilSet(new TimeTracker());
    }

    /**
     * Wait for a time limit until the value or the exception is set. Returns true if value or exception is set before
     * timeout or interruption, returns false if timeout expires before value or exception is set.
     *
     * @param tt - time tracker
     * @throws InterruptedException if thread is interrupted while waiting.
     */
    public boolean waitUntilSet(TimeTracker tt) throws InterruptedException {
        synchronized (this.lock) {
            while (!this.completed && !tt.timedOut()) {
                tt.timedWait(this.lock);
            }
            return this.completed;
        }
    }

    /**
     * Wait (forever) until value or exception is set. Returns value if it is set before interruption.
     *
     * @return value if it is set before interruption
     * @throws InterruptedException if thread is interrupted while waiting.
     * @throws E if exception is set instead of value.
     */
    public V get() throws InterruptedException, E {
        try {
            return get(new TimeTracker());
        } catch (TimeoutException e) {
            throw new IllegalStateException("Impossible timeout.", e);
        }
    }

    /**
     * Wait for a time limit until the value or exception is set. Returns value or throws exception if set before
     * timeout or interruption.
     *
     * @param tt - time tracker
     * @return value if set in time and before interruption
     * @throws TimeoutException if timed out before completion is signalled.
     * @throws InterruptedException if thread is interrupted while waiting.
     * @throws E if exception is set instead of value.
     */
    public V get(TimeTracker tt) throws InterruptedException, TimeoutException, E {
        synchronized (this.lock) {
            if (!this.waitUntilSet(tt))
                throw new TimeoutException();
            // this.completed == true
            if (this.exception == null)
                return this.value;
            throw this.exception;
        }
    }

    /**
     * Set value and signal completion to all waiting threads. Does not block.
     *
     * @param value - value to set
     * @throws IllegalStateException if already set.
     */
    public void setValue(V value) {
        synchronized (this.lock) {
            if (this.completed)
                throw new IllegalStateException("Eventual already set. Cannot set value to " + value + ".");
            this.completed = true;
            this.value = value;
            this.lock.notifyAll();
        }
    }

    /**
     * Set exception and signal completion to all waiting threads. Does not block.
     *
     * @param exception - exception to be set (and thrown to getters) -- must not be <code>null</code>
     * @throws IllegalStateException if already set.
     */
    public void setException(E exception) {
        if (exception == null)
            throw new IllegalArgumentException("Cannot set exception to null");
        synchronized (this.lock) {
            if (this.completed)
                throw new IllegalStateException("Eventual already set. Cannot set exception to " + exception + ".");
            this.completed = true;
            this.exception = exception;
            this.lock.notifyAll();
        }
    }
}
