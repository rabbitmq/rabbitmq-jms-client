/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse;

public interface Multiples {
    static class Pair<L, R> {
        private final L l;
        private final R r;
        public Pair(L l, R r) { this.l = l   ; this.r = r; }
        public L left()       { return this.l; }
        public R right()      { return this.r; }
    }
    static class Triple<F, S, T> {
        private final Pair<F, Pair<S, T>> p;
        public Triple(F f, S s, T t) { this.p=new Pair<F, Pair<S, T>>(f, new Pair<S, T>(s, t)); }
        public F first()             { return this.p.left();          }
        public S second()            { return this.p.right().left();  }
        public T third()             { return this.p.right().right(); }
    }
    static class List<E> {
        private final Pair<E, List<E>> n;
        public List()               { this.n = null; }
        public List(E e, List<E> t) { this.n=new Pair<E, List<E>>(e, t); }
        public boolean isEmpty()    { return (this.n == null); }
        public E head()             { return this.n.left();  }
        public List<E> tail()       { return this.n.right(); }
    }
}
