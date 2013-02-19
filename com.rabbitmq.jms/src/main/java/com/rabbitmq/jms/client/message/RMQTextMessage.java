package com.rabbitmq.jms.client.message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.jms.JMSException;
import javax.jms.MessageNotWriteableException;
import javax.jms.TextMessage;

import com.rabbitmq.jms.client.RMQMessage;

/**
 * Implements {@link TextMessage} interface.
 */
public class RMQTextMessage extends RMQMessage implements TextMessage {

    private volatile String text;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(String string) throws JMSException {
        if (isReadonlyBody()) throw new MessageNotWriteableException("Message has been received and is read only.");
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
    public void readBody(ObjectInput inputStream) throws IOException, ClassNotFoundException {
        boolean isnull = inputStream.readBoolean();
        if (!isnull) {
            this.text = inputStream.readUTF();
        }
    }

}
