/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.util;

import jakarta.jms.JMSException;

/**
 * Wraps an exception as a {@link JMSException}.
 */
public class RMQJMSException extends JMSException {
    /** Default version ID */
    private static final long serialVersionUID = 1L;

    public RMQJMSException(String msg, Throwable x) {
        this(msg, null, x);
    }

    public RMQJMSException(Throwable x) {
        this(x.getMessage(), x);
    }

    private RMQJMSException(String msg, String errorCode, Throwable x) {
        super(msg, errorCode);
        this.initCause(x);
    }

}
