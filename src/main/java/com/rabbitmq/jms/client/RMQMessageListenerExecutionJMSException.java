// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2017-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
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
