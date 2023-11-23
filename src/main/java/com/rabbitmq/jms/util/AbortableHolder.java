/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Bag of {@link Abortable}s which is itself an {@link Abortable}.
 * {@link Abortable} actions are propagated to elements.
 */
public class AbortableHolder implements Abortable {
    private final java.util.Queue<Abortable> abortableQueue = new ConcurrentLinkedQueue<Abortable>();
    private final boolean[] flags = new boolean[] { false, false, false }; // to prevent infinite regress

    private enum Action {
        ABORT(0) { @Override void doit(Abortable a) throws Exception { a.abort(); } },
        START(1) { @Override void doit(Abortable a) throws Exception { a.start(); } },
        STOP(2)  { @Override void doit(Abortable a) throws Exception { a.stop();  } };
        private final int ind;

        Action(int ind) { this.ind = ind;  }

        int index()     { return this.ind; }

        abstract void doit(Abortable a) throws Exception;
    };

    /**
     * @param a - abortable to hold
     */
    public void add(Abortable a) {
        this.abortableQueue.add(a);
    }

    /**
     * @param a - abortable to remove (once) if held
     */
    public void remove(Abortable a) {
        this.abortableQueue.remove(a);
    }

    public void abort() throws Exception { act(Action.ABORT); }

    public void start() throws Exception { act(Action.START); }

    public void stop() throws Exception  { act(Action.STOP);  }

    private void act(AbortableHolder.Action action) throws Exception {
        if (this.flags[action.index()]) return; // prevent infinite
        this.flags[action.index()] = true;      // regress

        Abortable[] as = this.abortableQueue.toArray(new Abortable[this.abortableQueue.size()]);
        for (Abortable a : as) { action.doit(a); }
        this.flags[action.index()] = false;     // allow multiple invocations
    }
}