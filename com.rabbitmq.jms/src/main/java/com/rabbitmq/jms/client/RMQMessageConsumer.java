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
        return destination;
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
        return receive(Long.MAX_VALUE);
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        if (timeout==0) {
            timeout = Long.MAX_VALUE;
        }
        String name = null;
        if (this.destination.isQueue()) {
            name = this.destination.getQueueName();
        } else {
            name = this.getConsumerTag();
        }

        try {
            SynchronousConsumer sc = new SynchronousConsumer(this.session.getChannel(), timeout);
            getSession().getChannel().basicConsume(name, sc);
            GetResponse response = sc.receive();
            if (response == null)
                return null;
            this.session.messageReceived(response);
            RMQMessage message = RMQMessage.fromMessage(response.getBody());
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
    public Message receiveNoWait() throws JMSException {
        try {
            GetResponse response = null;
            if (this.destination.isQueue()) {
                response = this.getSession().getChannel().basicGet(this.destination.getQueueName(), !this.getSession().getTransacted());
            } else {
                response = this.getSession().getChannel().basicGet(this.getConsumerTag(), !this.getSession().getTransacted());
            }

            if (response == null)
                return null;
            this.session.messageReceived(response);
            RMQMessage message = RMQMessage.fromMessage(response.getBody());
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
    public void close() throws JMSException {
        // TODO Auto-generated method stub

    }

    public RMQDestination getDestination() {
        return this.destination;
    }

    public RMQSession getSession() {
        return this.session;
    }

    public String getConsumerTag() {
        return this.consumerTag;
    }

    @Override
    public Topic getTopic() throws JMSException {
        return (Topic) this.getDestination();
    }

    @Override
    public boolean getNoLocal() throws JMSException {
        return false;
    }

}
