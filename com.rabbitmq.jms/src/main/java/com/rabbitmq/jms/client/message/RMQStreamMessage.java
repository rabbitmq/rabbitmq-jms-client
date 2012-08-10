package com.rabbitmq.jms.client.message;

import javax.jms.JMSException;
import javax.jms.StreamMessage;

public class RMQStreamMessage extends RMQBytesMessage implements StreamMessage {

    /**
     * {@inheritDoc}
     */
    @Override
    public String readString() throws JMSException {
        return super.readUTF();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeString(String value) throws JMSException {
        super.writeUTF(value);
    }

}
