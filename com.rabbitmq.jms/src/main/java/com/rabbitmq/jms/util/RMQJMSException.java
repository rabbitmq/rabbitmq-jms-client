package com.rabbitmq.jms.util;

import javax.jms.JMSException;

/**
 * Wraps an exception as a {@link JMSException}.
 */
public class RMQJMSException extends JMSException {
    /** TODO */
    private static final long serialVersionUID = 1L;

    public RMQJMSException(String msg, Exception x) {
        this(msg, null, x);
    }

    public RMQJMSException(Exception x) {
        this(x.getMessage(), x);
    }

    private RMQJMSException(String msg, String errorCode, Exception x) {
        super(msg, errorCode);
        this.initCause(x);
    }

}
