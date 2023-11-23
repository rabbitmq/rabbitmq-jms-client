// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class TestTimeTracker {

    private static final long QUANTUM_TIME_NANOS = 1000000L; // one millisecond
    private static final long SHORT_WAIT_MILLIS = 100; // hundred milliseconds

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
        assertFalse(tt.timedOut(), "TimeTracker "+description+" timed out!");
        assertFalse(0L >= tt.remainingMillis(), "TimeTracker "+description+" run out!");
    }

    private void assertZero(TimeTracker tt, String description) throws Exception {
        assertEquals(0L, tt.timeoutNanos(), "TimeTracker "+description+" not a 0L timeout.");
        assertTimedOut(tt, description);
    }

    private void assertTimedOut(TimeTracker tt, String description) throws Exception {
        assertTimedOutOnce(tt, description);
        Thread.sleep(SHORT_WAIT_MILLIS);
        assertTimedOutOnce(tt, description); // ensure idempotent
    }

    private void assertTimedOutOnce(TimeTracker tt, String description) {
        assertTrue(tt.timedOut(), "TimeTracker "+description+" not timed out!");
        assertEquals(0l, tt.remainingMillis(), "TimeTracker "+description+" not zero remaining Millis!");
        assertEquals(0l, tt.remainingNanos(), "TimeTracker "+description+" not zero remaining Nanos!");
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
        assertTrue(intervalNanos<QUANTUM_TIME_NANOS, "TimeTracker "+description+" waited too long ("+intervalNanos+" nanos)!");
    }
}
