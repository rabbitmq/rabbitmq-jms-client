package com.rabbitmq.jms.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;

import com.rabbitmq.jms.admin.RMQDestination;


public class RMQMessageConsumer implements MessageConsumer, QueueReceiver {

    private final RMQDestination destination;
    private final RMQSession session;

    public RMQMessageConsumer(RMQSession session, RMQDestination destination) {
        this.session = session;
        this.destination = destination;
    }

    @Override
    public Queue getQueue() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public Message receive() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws JMSException {
        // TODO Auto-generated method stub

    }

    public RMQDestination getDestination() {
        return destination;
    }

    public RMQSession getSession() {
        return session;
    }

}
