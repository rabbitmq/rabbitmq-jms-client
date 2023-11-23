// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test the {@link AbortableHolder} collection of {@link Abortable}s.
 */
public class TestAbortableHolder {

    private class CountingAbortable implements Abortable {
        private final Counts cs;
        public CountingAbortable(Counts cs) { this.cs = cs; }
        public void abort() { cs.incAborts(); }
        public void stop() { cs.incStops(); }
        public void start() { cs.incStarts(); }
    };

    private static class PartialCounts extends Counts {
        private final Counts localCounts = new Counts();
        private final Counts parent;
        public PartialCounts(Counts parent) { this.parent = parent; }
        public void incAborts() { this.parent.incAborts(); this.localCounts.incAborts(); }
        public void incStops() { this.parent.incStops(); this.localCounts.incStops(); }
        public void incStarts() { this.parent.incStarts(); this.localCounts.incStarts(); }
        public int getAborts() { return this.localCounts.aborts; }
        public int getStops() { return this.localCounts.stops; }
        public int getStarts() { return this.localCounts.starts; }
    }

    private static class Counts {
        private int aborts = 0;
        private int stops = 0;
        private int starts = 0;
        public void incAborts() { this.aborts++; }
        public void incStops() { this.stops++; }
        public void incStarts() { this.starts++; }
        public int getAborts() { return this.aborts; }
        public int getStops() { return this.stops; }
        public int getStarts() { return this.starts; }
    }

    /**
     * Check empty to start with, and stays empty when {@link Abortable} added and removed.
     *
     * @throws Exception test
     */
    @Test
    public void emptyAbortableHolder() throws Exception {
        AbortableHolder ah = new AbortableHolder();
        Counts cs = new Counts();
        driveAndAssertCounts("Not initially Empty", ah, 0, 0, 0, cs);

        CountingAbortable ca = new CountingAbortable(cs);
        ah.add(ca);
        ah.remove(ca);

        driveAndAssertCounts("Not Empty after add and remove", ah, 0, 0, 0, cs);
    }

    /**
     * Check single element.
     *
     * @throws Exception test
     */
    @Test
    public void singletonAbortableHolder() throws Exception {
        AbortableHolder ah = new AbortableHolder();
        Counts cs = new Counts();
        CountingAbortable ca = new CountingAbortable(cs);
        ah.add(ca);
        driveAndAssertCounts("Singleton", ah, 1, 1, 1, cs);
        driveAndAssertCounts("Singleton driven twice", ah, 2, 2, 2, cs);
    }

    /**
     * Check recursion prevention.
     *
     * @throws Exception test
     */
    @Test
    public void recursiveAbortableHolder() throws Exception {
        AbortableHolder ah = new AbortableHolder();
        Counts cs = new Counts();
        CountingAbortable ca = new CountingAbortable(cs);
        ah.add(ca);
        ah.add(ah);
        driveAndAssertCounts("Recursive", ah, 1, 1, 1, cs);
        driveAndAssertCounts("Recursive driven twice", ah, 2, 2, 2, cs);
    }

    /**
     * Check multiple elements.
     *
     * @throws Exception test
     */
    @Test
    public void doubledAbortableHolder() throws Exception {
        AbortableHolder ah = new AbortableHolder();
        Counts cs = new Counts();
        CountingAbortable ca = new CountingAbortable(cs);
        ah.add(ca);
        ah.add(ca);
        driveAndAssertCounts("Double driven once", ah, 2, 2, 2, cs);
        driveAndAssertCounts("Double driven twice", ah, 4, 4, 4, cs);
    }

    /**
     * Check multiple elements and removed.
     *
     * @throws Exception test
     */
    @Test
    public void multipleRemovedAbortableHolder() throws Exception {
        AbortableHolder ah = new AbortableHolder();
        Counts cs = new Counts();
        CountingAbortable ca = new CountingAbortable(cs);
        ah.add(ca);
        ah.add(ca);
        ah.remove(ca);
        driveAndAssertCounts("Should be one", ah, 1, 1, 1, cs);
    }

    /**
     * Check multiple disparate elements.
     *
     * @throws Exception test
     */
    @Test
    public void multipleAbortableHolder() throws Exception {
        AbortableHolder ah = new AbortableHolder();
        Counts cs = new Counts();
        Counts cs1 = new PartialCounts(cs);
        Counts cs2 = new PartialCounts(cs);
        CountingAbortable ca1 = new CountingAbortable(cs1);
        CountingAbortable ca2 = new CountingAbortable(cs2);
        ah.add(ca1);
        ah.add(ca2);
        ah.add(ca2);
        ah.add(ca1);
        ah.remove(ca2);
        ah.remove(ca1);
        driveAndAssertCounts("Should be one time", ah, 2, 2, 2, cs);
        assertCounts("First", 1, 1, 1, cs1);
        assertCounts("Second", 1, 1, 1, cs2);
    }

    private void driveAndAssertCounts(String descr, AbortableHolder ah, int exAbort, int exStop, int exStart, Counts cs) throws Exception {
        int aborts = cs.getAborts();
        int stops = cs.getStops();
        int starts = cs.getStops();
        assertCounts(descr, aborts, stops, starts, cs);
        ah.abort();
        assertCounts(descr, exAbort, stops, starts, cs);
        ah.stop();
        assertCounts(descr, exAbort, exStop, starts, cs);
        ah.start();
        assertCounts(descr, exAbort, exStop, exStart, cs);
    }

    private void assertCounts(String descr, int exAbort, int exStop, int exStart, Counts cs) throws Exception {
        assertEquals(exAbort, cs.getAborts(), descr+"(abort)");
        assertEquals(exStop, cs.getStops(), descr+"(stop)");
        assertEquals(exStart, cs.getStarts(), descr+"(start)");
    }
}
