package com.rabbitmq.jms.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.jms.JMSException;
import javax.jms.Message;


class RMQNullMessage extends RMQMessage {

    // there is no body

    @Override
    protected void clearBodyInternal() throws JMSException {
        // no-op
    }

    @Override
    protected void writeBody(ObjectOutput out, ByteArrayOutputStream bout) throws IOException {
        // no-op
    }

    @Override
    protected void writeAmqpBody(ByteArrayOutputStream out) throws IOException {
        // no-op
    }

    @Override
    protected void readBody(ObjectInput inputStream, ByteArrayInputStream bin) throws IOException,
                                                                              ClassNotFoundException {
        // no-op
    }

    @Override
    protected void readAmqpBody(byte[] barr) {
        // no-op
    }

    public static final RMQMessage recreate(Message msg) throws JMSException {
        RMQNullMessage rmqNMsg = new RMQNullMessage();
        RMQMessage.copyAttributes(rmqNMsg, msg);
        return rmqNMsg;
    }
}
