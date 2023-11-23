/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import javax.jms.JMSException;

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
