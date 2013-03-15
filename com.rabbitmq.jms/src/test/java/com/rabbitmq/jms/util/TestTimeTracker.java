/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestTimeTracker {

    private static final long QUANTUM_TIME_NANOS = 1000000L; //one millisecond
    private static final long SHORT_WAIT_MILLIS = 10; // ten milliseconds

    /**
     * Test TimeTracker constructors.
     */
    @Test
    public void testTimeTrackerZeros() throws Exception {
        assertZero(TimeTracker.ZERO, "ZERO");
        assertZero(new TimeTracker(0, TimeUnit.SECONDS), "(0s)");
        assertZero(new TimeTracker(0, TimeUnit.MILLISECONDS), "(0ms)");
        assertZero(new TimeTracker(0, TimeUnit.NANOSECONDS), "(0nanos)");
    }

    /**
     * Test TimeTracker constructors.
     */
    @Test
    public void testTimeTrackerSmallTimers() throws Exception {
        TimeTracker tt = new TimeTracker(SHORT_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        assertNotTimedOut(tt, "SHORT_WAIT");
        Thread.sleep(SHORT_WAIT_MILLIS); // we wait for this time, and we should have timed out by now.
        assertTimedOut(tt, "SHORT_WAIT");
    }

    private void assertNotTimedOut(TimeTracker tt, String description) {
        assertFalse("TimeTracker "+description+" timed out!", tt.timedOut());
        assertFalse("TimeTracker "+description+" run out!", 0L >= tt.remainingMillis());
    }

    private void assertZero(TimeTracker tt, String description) throws Exception {
        assertEquals("TimeTracker "+description+" not a 0L timeout.", 0L, tt.timeoutNanos());
        assertTimedOut(tt, description);
    }

    private void assertTimedOut(TimeTracker tt, String description) throws Exception {
        assertTimedOutOnce(tt, description);
        Thread.sleep(SHORT_WAIT_MILLIS);
        assertTimedOutOnce(tt, description); // ensure idempotent
    }

    private void assertTimedOutOnce(TimeTracker tt, String description) {
        assertTrue("TimeTracker "+description+" not timed out!", tt.timedOut());
        assertEquals("TimeTracker "+description+" not zero remaining Millis!", 0l, tt.remainingMillis());
        assertEquals("TimeTracker "+description+" not zero remaining Nanos!", 0l, tt.remainingNanos());
        Object lock = new Object();
        long startNanos = System.nanoTime();
        synchronized(lock) {
            try {
                tt.timedWait(lock);
            } catch (InterruptedException ie) {
                //do nothing
            }
        }
        long intervalNanos = System.nanoTime() - startNanos;
        assertTrue("TimeTracker "+description+" waited too long ("+intervalNanos+" nanos)!", intervalNanos<QUANTUM_TIME_NANOS);
    }
}
