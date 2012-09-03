package com.rabbitmq.jms.client.message;

import javax.jms.JMSException;
import javax.jms.StreamMessage;

// TODO For now we don't handle direct TCP streaming
// TODO this should write to disk, and when we send the message
// TODO we have disassemble and reassemble multiple messages
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void writeObject(Object value) throws JMSException {
        writeObject(value,true);
    }
    

}
