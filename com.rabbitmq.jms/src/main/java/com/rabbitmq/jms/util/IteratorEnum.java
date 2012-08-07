package com.rabbitmq.jms.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnum<E> implements Enumeration<E> {

    final Iterator<E> it;
    public IteratorEnum(Iterator<E> it) {
        this.it = it;
    }
    
    @Override
    public boolean hasMoreElements() {
        return it.hasNext();
    }

    @Override
    public E nextElement() {
        return it.next();
    }

    
}
