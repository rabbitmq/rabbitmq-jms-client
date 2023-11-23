/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An implementation of {@link Enumeration} that uses an {@link Iterator}.
 *
 * @param <E> type of elements enumerated
 */
public class IteratorEnum<E> implements Enumeration<E> {

    final Iterator<E> it;

    /**
     * Create an enumeration based upon the supplied iterator. The iterator is not reset.
     *
     * @param it iterator to use for enumeration
     */
    public IteratorEnum(Iterator<E> it) {
        this.it = it;
    }

    public boolean hasMoreElements() {
        return this.it.hasNext();
    }

    public E nextElement() {
        return this.it.next();
    }

}
