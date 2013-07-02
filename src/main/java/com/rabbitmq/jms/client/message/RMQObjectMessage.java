/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.client.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.ObjectMessage;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.RMQJMSException;

/**
 * Implements {@link ObjectMessage} interface.
 */
public class RMQObjectMessage extends RMQMessage implements ObjectMessage {

    private volatile byte[] buf = null;
    /**
     * {@inheritDoc}
     */
    @Override
    public void setObject(Serializable object) throws JMSException {
        if (isReadonlyBody()) throw new MessageNotWriteableException("Message not writeable");
        try {
            if (object==null) {
                buf = null;
            } else {
                /*
                 * We have to take a copy of the object at this point in time
                 * according to the spec
                 */
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bout);
                out.writeObject(object);
                out.flush();
                buf = bout.toByteArray();
            }
        }catch (IOException x) {
            throw new RMQJMSException(x);
        }

    }

    public Serializable getObject() throws JMSException {
        if (buf==null) {
            return null;
        } else {
            ByteArrayInputStream bin = new ByteArrayInputStream(buf);
            try {
                ObjectInputStream in = new ObjectInputStream(bin);
                return (Serializable)in.readObject();
            }catch (ClassNotFoundException x) {
                throw new RMQJMSException(x);
            }catch (IOException x) {
                throw new RMQJMSException(x);
            }
        }
    }

    public void clearBodyInternal() throws JMSException {
        this.buf = null;
    }

    protected void writeBody(ObjectOutput out) throws IOException {
        out.writeBoolean(this.buf == null);
        if (this.buf != null) {
            out.writeInt(buf.length);
            out.write(this.buf);
        }
    }

    protected void readBody(ObjectInput inputStream) throws IOException, ClassNotFoundException {
        // the body here is just a byte[] and we delay creating the object
        // until getObject() is called so that
        // we have access to the Thread Context Classloader
        // to deserialize the object
        boolean isnull = inputStream.readBoolean();
        if (!isnull) {
            readWholeBuffer(inputStream.readInt(), inputStream);
        }
    }

    private void readWholeBuffer(int len, ObjectInput inputStream) throws IOException {
        buf = new byte[len];
        int totalBytesRead = inputStream.read(buf, 0, len);
        if (totalBytesRead == -1) throw new IOException("ObjectMessage body too small!");
        while (totalBytesRead < len) {
            int bytesRead = inputStream.read(buf, totalBytesRead, len-totalBytesRead);
            if (bytesRead == -1) throw new IOException("ObjectMessage body too small!");
            totalBytesRead += bytesRead;
        }
    }

}
