package com.rabbitmq.jms.client.message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import com.rabbitmq.jms.client.RMQMessage;

public class RMQTextMessage extends RMQMessage implements TextMessage {

    private String text;

    @Override
    public void setText(String string) throws JMSException {
        this.text = string;
    }

    @Override
    public String getText() throws JMSException {
        return this.text;
    }

    @Override
    public void clearBody() throws JMSException {
        this.text = null;
    }

    @Override
    public void writeBody(ObjectOutput out) throws IOException {
        out.writeBoolean(this.text == null);
        out.writeUTF(this.text);
    }

    @Override
    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean isnull = in.readBoolean();
        if (!isnull) {
            this.text = in.readUTF();
        }
    }

}
