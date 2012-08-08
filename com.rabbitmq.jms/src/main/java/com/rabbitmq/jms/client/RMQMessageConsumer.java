package com.rabbitmq.jms.client;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.Util;

public class RMQMessageConsumer implements MessageConsumer, QueueReceiver, TopicSubscriber {

    private final RMQDestination destination;
    private final RMQSession session;
    private final String consumerTag;
    
    public RMQMessageConsumer(RMQSession session, RMQDestination destination, String consumerTag) {
        this.session = session;
        this.destination = destination;
        this.consumerTag = consumerTag;
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
        try {
            GetResponse resp = null;
            if (destination.isQueue()) {
                resp = getSession().getChannel().basicGet(destination.getQueueName(), !getSession().getTransacted());
            } else {
                resp = getSession().getChannel().basicGet(getConsumerTag(), !getSession().getTransacted());
            }
            
            if (resp == null)
                return null;
            RMQMessage message = RMQMessage.fromMessage(resp.getBody());
            return message;
        } catch (IOException x) {
            Util.util().handleException(x);
        } catch (ClassNotFoundException x) {
            Util.util().handleException(x);
        } catch (IllegalAccessException x) {
            Util.util().handleException(x);
        } catch (InstantiationException x) {
            Util.util().handleException(x);
        }
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
    
    public String getConsumerTag() {
        return consumerTag;
    }

    @Override
    public Topic getTopic() throws JMSException {
        return (Topic)getDestination();
    }

    @Override
    public boolean getNoLocal() throws JMSException {
        return false;
    }
    
    

}
