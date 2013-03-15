/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

import javax.jms.InvalidSelectorException;

/**
 * Wraps an exception as a {@link InvalidSelectorException}.
 */
public class RMQJMSSelectorException extends InvalidSelectorException {
    /** TODO */
    private static final long serialVersionUID = 1L;

    public RMQJMSSelectorException(String msg, Exception x) {
        this(msg, null, x);
    }

    public RMQJMSSelectorException(Exception x) {
        this(x.getMessage(), x);
    }

    private RMQJMSSelectorException(String msg, String errorCode, Exception x) {
        super(msg, errorCode);
        this.initCause(x);
    }
}
