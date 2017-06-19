/* Copyright (c) 2017 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import javax.jms.JMSException;

/**
 * Wraps an execution exception as a {@link JMSException}.
 */
public class RMQMessageListenerExecutionJMSException extends JMSException {

    /** Default version ID */
    private static final long serialVersionUID = 1L;

    public RMQMessageListenerExecutionJMSException(String msg, Throwable x) {
        this(msg, null, x);
    }

    public RMQMessageListenerExecutionJMSException(Throwable x) {
        this(x.getMessage(), x);
    }

    private RMQMessageListenerExecutionJMSException(String msg, String errorCode, Throwable x) {
        super(msg, errorCode);
        this.initCause(x);
    }

}
