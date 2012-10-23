package com.rabbitmq.jms.util;

public class RuntimeWrapperException extends RuntimeException {

    public RuntimeWrapperException(Throwable cause) {
        super(cause);
    }

    public RuntimeWrapperException(String message, Throwable cause) {
        super(message, cause);
    }

}
