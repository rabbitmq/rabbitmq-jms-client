package com.rabbitmq.jms.util;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.UUID;

import javax.jms.IllegalStateException;
import javax.jms.InvalidClientIDException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.MessageEOFException;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotReadableException;
import javax.jms.MessageNotWriteableException;

/**
 * Utility class which variously wraps exceptions, writes primitive/serializable types, checks state
 * information. and generates unique ids.
 */
public class Util {
    private static final Util util = new Util();

    /**
     * @return the singleton of this class
     */
    public static Util util() {
        return util;
    }

    /**
     * Wraps an exception as a {@link JMSException}
     * This method will return a {@link JMSException}
     * and is used to encapsulate Rabbit API exceptions (all declared as {@link IOException}).
     * @param x the exception to wrap
     * @param message the message for the {@link JMSException#JMSException(String)} constructor.
     * @return wrapped exception for caller to throw
     */
    public JMSException handleException(Exception x, String message) {
        JMSException jx = new JMSException(message);
        jx.initCause(x);
        return jx;
    }

    /**
     * @see #handleException(Exception, String)
     * @param x exception to wrap
     * @return wrapped exception
     */
    public JMSException handleException(Exception x) {
        return this.handleException(x, x.getMessage());
    }

    /**
     * Wraps exception as a {@link JMSSecurityException}.
     * @see #handleException(Exception, String)
     * @param x exception to wrap
     * @return wrapped exception
     */
    public JMSException handleSecurityException(Exception x) {
        JMSSecurityException jx = new JMSSecurityException(x.getMessage());
        jx.initCause(x);
        return jx;
    }

    /**
     * Wraps exception as a {@link MessageFormatException}.
     * @see #handleException(Exception, String)
     * @param x exception to wrap
     * @return wrapped exception
     */
    public JMSException handleMessageFormatException(Exception x) {
        MessageFormatException jx = new MessageFormatException(x.getMessage());
        jx.initCause(x);
        return jx;
    }

    /**
     * Throws an exception of the supplied type if the first parameter is <b>true</b>.
     * @param bool whether to construct and throw an exception
     * @param msg the message string to pass the exception constructor
     * @param clazz the type of exception to be thrown
     * @throws JMSException sub-type if <code>bool</code> is <code>true</code>
     */
    public void checkTrue(boolean bool, String msg, Class<? extends JMSException> clazz) throws JMSException {
        if (bool) {
            if (IllegalStateException.class.equals(clazz)) {
                throw new IllegalStateException(msg);
            } else if (InvalidClientIDException.class.equals(clazz)) {
                throw new InvalidClientIDException(msg);
            } else if (IllegalStateException.class.equals(clazz)) {
                throw new IllegalStateException(msg);
            } else if (InvalidDestinationException.class.equals(clazz)) {
                throw new InvalidDestinationException(msg);
            } else if (JMSException.class.equals(clazz)) {
                throw new JMSException(msg);
            } else if (JMSSecurityException.class.equals(clazz)) {
                throw new JMSSecurityException(msg);
            } else if (MessageEOFException.class.equals(clazz)) {
                throw new MessageEOFException(msg);
            } else if (MessageFormatException.class.equals(clazz)) {
                throw new MessageFormatException(msg);
            } else if (MessageNotReadableException.class.equals(clazz)) {
                throw new MessageNotReadableException(msg);
            } else if (MessageNotWriteableException.class.equals(clazz)) {
                throw new MessageNotWriteableException(msg);
            } else {
                throw new JMSException(msg);
            }
        }
    }

    /**
     * Generates a random UUID string
     * @return a random UUID string
     */
    public String generateUUIDTag() {
        return UUID.randomUUID().toString();
    }

    /**
     * Utility method to write an object as a primitive or as an object
     * @param s the object to write
     * @param out the output (normally a stream) to write it to
     * @param allowSerializable true if we allow {@link Serializable} objects
     * @throws IOException from write primitives
     * @throws MessageFormatException if s is not a recognised type for writing
     * @throws NullPointerException if s is null
     */
    public void writePrimitiveData(Object s, ObjectOutput out, boolean allowSerializable) throws IOException, MessageFormatException {
        if(s==null) {
            throw new NullPointerException();
        } else if (s instanceof Boolean) {
            out.writeBoolean(((Boolean) s).booleanValue());
        } else if (s instanceof Byte) {
            out.writeByte(((Byte) s).byteValue());
        } else if (s instanceof Short) {
            out.writeShort((((Short) s).shortValue()));
        } else if (s instanceof Integer) {
            out.writeInt(((Integer) s).intValue());
        } else if (s instanceof Long) {
            out.writeLong(((Long) s).longValue());
        } else if (s instanceof Float) {
            out.writeFloat(((Float) s).floatValue());
        } else if (s instanceof Double) {
            out.writeDouble(((Double) s).doubleValue());
        } else if (s instanceof String) {
            out.writeUTF((String) s);
        } else if (s instanceof Character) {
            out.writeChar(((Character) s).charValue());
        } else if (s instanceof Character) {
            out.writeChar(((Character) s).charValue());
        } else if (allowSerializable && s instanceof Serializable) {
            out.writeObject(s);
        } else if (s instanceof byte[]) {
            out.write((byte[])s);
        } else
            throw new MessageFormatException(s + " is not a recognized writable type.");


    }
}
