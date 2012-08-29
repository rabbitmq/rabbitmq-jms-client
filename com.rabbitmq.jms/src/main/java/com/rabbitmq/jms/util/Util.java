package com.rabbitmq.jms.util;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

public class Util {
    private static final Util util = new Util();

    /**
     * Returns the singleton of this class
     * @return
     */
    public static Util util() {
        return util;
    }

    /**
     * Wraps an exception and re throws a {@link JMSException}
     * This method will always throw a {@link JMSException}
     * and is used to encapsulate Rabbit API exceptions (all declared as
     * {@link IOException}
     * @param x the exceptio to wrap
     * @param message the message for the {@link JMSException#JMSException(String)} constructor. If null {@link Exception#getMessage()} will be used
     * @return does not return anything, always throws a {@link JMSException}
     * @throws JMSException always on every invocation
     */
    public JMSException handleException(Exception x, String message) throws JMSException {
        JMSException jx = new JMSException(message == null ? x.getMessage() : message);
        jx.initCause(x);
        throw jx;
    }

    /**
     * @see {@link #handleException(Exception, String)}
     * @param x
     * @return
     * @throws JMSException
     */
    public JMSException handleException(Exception x) throws JMSException {
        return this.handleException(x, x.getMessage());
    }

    public JMSSecurityException handleSecurityException(Exception x) throws JMSException {
        JMSSecurityException jx = new JMSSecurityException(x.getMessage());
        jx.initCause(x);
        throw jx;
    }
    /**
     * Throws an exception with the supplied message, or "Closed" if the msg parameter is null,
     * if the bool parameter is true
     * @param bool throws a JMSException if this parameter is set to true
     * @param msg
     * @throws JMSException
     */
    public void checkTrue(boolean bool, String msg) throws JMSException {
        if (bool)
            throw new JMSException(msg != null ? msg : "Closed");
    }

    /**
     * Generates a random UUID string
     * @return a random UUID string
     */
    public String generateUUIDTag() {
        return UUID.randomUUID().toString();
    }
}
