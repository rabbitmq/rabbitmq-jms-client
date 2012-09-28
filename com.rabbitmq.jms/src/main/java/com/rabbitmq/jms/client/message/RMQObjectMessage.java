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
import com.rabbitmq.jms.util.Util;

public class RMQObjectMessage extends RMQMessage implements ObjectMessage {

    private volatile byte[] buf = null;
    /**
     * {@inheritDoc}
     */
    @Override
    public void setObject(Serializable object) throws JMSException {
        Util.checkTrue(isReadonlyBody(), "Message not writeable", MessageNotWriteableException.class);
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
            throw Util.handleException(x);
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
                throw Util.handleException(x);
            }catch (IOException x) {
                throw Util.handleException(x);
            }
        }
    }

    public void clearBodyInternal() throws JMSException {
        this.buf = null;
    }

    public void writeBody(ObjectOutput out) throws IOException {
        out.writeBoolean(this.buf == null);
        if (this.buf != null) {
            out.writeInt(buf.length);
            out.write(this.buf);
        }
    }

    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // the body here is just a byte[] and we delay creating the object
        // until getObject() is called so that
        // we have access to the Thread Context Classloader
        // to deserialize the object
        boolean isnull = in.readBoolean();
        if (!isnull) {
            int len = in.readInt();
            buf = new byte[len];
            in.read(buf);
        }
    }

}
