/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.util;

import javax.jms.JMSSecurityException;

/**
 * Wraps an exception as a {@link JMSSecurityException}.
 */
public class RMQJMSSecurityException extends JMSSecurityException {
    /** TODO */
    private static final long serialVersionUID = 1L;

    public RMQJMSSecurityException(String msg, Exception x) {
        this(msg, null, x);
    }

    public RMQJMSSecurityException(Exception x) {
        this(x.getMessage(), x);
    }

    private RMQJMSSecurityException(String msg, String errorCode, Exception x) {
        super(msg, errorCode);
        this.initCause(x);
    }
}
