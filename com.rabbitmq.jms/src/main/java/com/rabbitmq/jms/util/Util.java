package com.rabbitmq.jms.util;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.UUID;

import javax.jms.MessageFormatException;

/**
 * Utility class which variously wraps exceptions, writes primitive/serializable types, checks state
 * information. and generates unique ids.
 */
public final class Util {
    /**
     * Generates a random UUID string
     * @return a random UUID string
     */
    public final static String generateUUIDTag() {
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
    public final static void writePrimitiveData(Object s, ObjectOutput out, boolean allowSerializable) throws IOException, MessageFormatException {
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
