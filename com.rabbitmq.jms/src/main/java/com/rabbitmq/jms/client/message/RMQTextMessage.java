package com.rabbitmq.jms.client.message;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import com.rabbitmq.jms.client.RMQMessage;

public class RMQTextMessage extends RMQMessage implements TextMessage {
    @Override
    public void setText(String string) throws JMSException {
        setBody(string.getBytes(getCharset()));
    }

    @Override
    public String getText() throws JMSException {
        return new String(getBody(), getCharset());
    }

}
