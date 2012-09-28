package com.rabbitmq.jms.client.message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.TextMessage;

import com.rabbitmq.jms.client.RMQMessage;
import com.rabbitmq.jms.util.Util;

public class RMQTextMessage extends RMQMessage implements TextMessage {

    private volatile String text;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(String string) throws JMSException {
        Util.checkTrue(isReadonlyBody(), "Message has been received and is read only.", MessageNotWriteableException.class);
        this.text = string;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() throws JMSException {
        return this.text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearBodyInternal() throws JMSException {
        this.text = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeBody(ObjectOutput out) throws IOException {
        String text = this.text;
        out.writeBoolean(text == null);
        if (text!=null) out.writeUTF(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readBody(ObjectInput in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        boolean isnull = in.readBoolean();
        if (!isnull) {
            this.text = in.readUTF();
        }
    }

}
