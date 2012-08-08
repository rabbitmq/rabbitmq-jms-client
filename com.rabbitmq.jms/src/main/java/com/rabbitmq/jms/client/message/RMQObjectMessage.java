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
    
    @Override
    public void setObject(Serializable object) throws JMSException {
        this.data = object;
    }

    @Override
    public Serializable getObject() throws JMSException {
        return data;
    }

    @Override
    public void clearBody() throws JMSException {
        data = null;
    }

    @Override
    public void writeBody(ObjectOutput out) throws IOException {
        out.writeBoolean(data==null);
        if (data!=null) {
            out.writeObject(data);
        }
    }

    @Override
    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean isnull = in.readBoolean();
        if (!isnull) {
            data = (Serializable)in.readObject();
        }
    }

}
