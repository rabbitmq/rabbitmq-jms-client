package com.rabbitmq.jms.client.message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import com.rabbitmq.jms.client.RMQMessage;

public class RMQObjectMessage extends RMQMessage implements ObjectMessage {

    private Serializable data;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObject(Serializable object) throws JMSException {
        this.data = object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable getObject() throws JMSException {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearBody() throws JMSException {
        this.data = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBody(ObjectOutput out) throws IOException {
        out.writeBoolean(this.data == null);
        if (this.data != null) {
            out.writeObject(this.data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // TODO it may be a good idea to delay
        // this call until getObject() is called so that
        // we have access to the Thread Context Classloader
        // to deserialize the object
        boolean isnull = in.readBoolean();
        if (!isnull) {
            this.data = (Serializable) in.readObject();
        }
    }

}
