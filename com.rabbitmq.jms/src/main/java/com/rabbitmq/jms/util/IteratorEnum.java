package com.rabbitmq.jms.util;

import java.util.Enumeration;
import java.util.Iterator;
/**
 * A class that implements the {@link Enumeration} interface but iterates over an {@link Iterator}
 */
public class IteratorEnum<E> implements Enumeration<E> {

    final Iterator<E> it;

    public IteratorEnum(Iterator<E> it) {
        this.it = it;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMoreElements() {
        return this.it.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E nextElement() {
        return this.it.next();
    }

}
