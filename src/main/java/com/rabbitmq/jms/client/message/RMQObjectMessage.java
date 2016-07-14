/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.ObjectMessage;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.WhiteListObjectInputStream;

/**
 * Implements {@link ObjectMessage} interface.
 */
public class RMQObjectMessage extends RMQMessage implements ObjectMessage {

    private final List<String> trustedPackages;
    /** Buffer to hold serialised object */
    private volatile byte[] buf = null;

    public RMQObjectMessage() {
        this(WhiteListObjectInputStream.DEFAULT_TRUSTED_PACKAGES);
    }

    public RMQObjectMessage(List<String> trustedPackages) {
        this.trustedPackages = trustedPackages;
    }

    @Override
    public void setObject(Serializable object) throws JMSException {
        if (isReadonlyBody()) throw new MessageNotWriteableException("Message not writeable");
        try {
            if (object==null) {
                buf = null;
            } else {
                /*
                 * We have to serialise the object now
                 */
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bout);
                out.writeObject(object);
                out.flush();
                buf = bout.toByteArray();
            }
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }

    }

    @Override
    public Serializable getObject() throws JMSException {
        return this.getObject(this.trustedPackages);
    }

    public Serializable getObject(List<String> trustedPackages) throws JMSException {
        if (buf == null) {
            return null;
        } else {
            this.loggerDebugByteArray("Deserialising object from buffer {} for {}", this.buf, "RMQObjectMessage");
            ByteArrayInputStream bin = new ByteArrayInputStream(buf);
            try {
                WhiteListObjectInputStream in = new WhiteListObjectInputStream(bin, trustedPackages);
                return (Serializable)in.readObject();
            } catch (ClassNotFoundException x) {
                throw new RMQJMSException(x);
            } catch (IOException x) {
                throw new RMQJMSException(x);
            }
        }
    }

    @Override
    public void clearBodyInternal() throws JMSException {
        this.buf = null;
    }

    @Override
    protected void writeBody(ObjectOutput out, ByteArrayOutputStream bout) throws IOException {
        out.writeBoolean(this.buf == null);
        if (this.buf != null) {
            out.writeInt(buf.length);
            out.write(this.buf);
        }
    }

    @Override
    protected void readBody(ObjectInput inputStream, ByteArrayInputStream bin) throws IOException, ClassNotFoundException {
        // the body here is just a byte[] and we delay deserialising the object
        // until getObject() is called so that we have access to the Thread Context Classloader
        boolean isnull = inputStream.readBoolean();
        if (!isnull) {
            readWholeBuffer(inputStream.readInt(), inputStream);
        }
    }

    @Override
    protected void readAmqpBody(byte[] barr) {
        throw new UnsupportedOperationException();
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

    @Override
    protected void writeAmqpBody(ByteArrayOutputStream out) throws IOException {
        throw new UnsupportedOperationException();
    }

    public static RMQMessage recreate(ObjectMessage msg) throws JMSException {
        RMQObjectMessage rmqOMsg = new RMQObjectMessage();
        RMQMessage.copyAttributes(rmqOMsg, msg);

        // note: ObjectMessage here comes from the outside and may
        //       be an implementation not provided by us, so we cannot
        //       enforce class name validation.
        rmqOMsg.setObject(msg.getObject());

        return rmqOMsg;
    }

    public static RMQMessage recreate(RMQObjectMessage msg, List<String> patterns) throws JMSException {
        RMQObjectMessage rmqOMsg = new RMQObjectMessage(patterns);
        RMQMessage.copyAttributes(rmqOMsg, msg);

        rmqOMsg.setObject(msg.getObject(patterns));

        return rmqOMsg;
    }
}
