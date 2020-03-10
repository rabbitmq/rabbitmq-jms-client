/* Copyright (c) 2013-2020 VMware, Inc. or its affiliates. All rights reserved. */
package com.rabbitmq.jms.util;

import javax.jms.MessageFormatException;

/**
 * Wraps an exception as a JMS {@link MessageFormatException}.
 */
public class RMQMessageFormatException extends MessageFormatException {
    /** TODO */
    private static final long serialVersionUID = 1L;

    public RMQMessageFormatException(String msg, Exception x) {
        this(msg, null, x);
    }

    public RMQMessageFormatException(Exception x) {
        this(x.getMessage(), x);
    }

    private RMQMessageFormatException(String msg, String errorCode, Exception x) {
        super(msg, errorCode);
        this.initCause(x);
    }
}
