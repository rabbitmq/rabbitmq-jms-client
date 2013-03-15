/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.util;

import javax.jms.Connection;

/**
 * Internal exception to indicate that waiting API call has been cancelled by other API action -- e.g. {@link Connection#close()}.
 */
public class AbortedException extends Exception {

    private static final long serialVersionUID = 1012573992638419310L;

    /**
     * Constructs a new exception with no message.
     */
    public AbortedException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public AbortedException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified <code>Throwable</code> as cause.
     * @param cause the cause of this exception.
     */
    public AbortedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and <code>Throwable</code> as cause.
     * @param message the detail message.
     * @param cause the cause of this exception.
     */
    public AbortedException(String message, Throwable cause) {
        super(message, cause);
    }

}
