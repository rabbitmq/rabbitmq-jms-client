//
// The contents of this file are subject to the Mozilla Public License
// Version 1.1 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License
// at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
// the License for the specific language governing rights and
// limitations under the License.
//
// The Original Code is RabbitMQ.
//
// The Initial Developer of the Original Code is VMware, Inc.
// Copyright (c) 2012 VMware, Inc. All rights reserved.
//
package com.rabbitmq.jms.client;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSender;

import org.springframework.util.Assert;

import com.rabbitmq.jms.admin.RMQDestination;

/**
 *
 */
public class RMQMessageProducer implements MessageProducer, QueueSender {

    private final RMQDestination destination;
    private final RMQSession session;

    public RMQMessageProducer(RMQSession session, RMQDestination destination) {
        this.session = session;
        this.destination = destination;
    }

    @Override
    public void setDisableMessageID(boolean value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setDisableMessageTimestamp(boolean value) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getDeliveryMode() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setPriority(int defaultPriority) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getPriority() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setTimeToLive(long timeToLive) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public long getTimeToLive() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Destination getDestination() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(Message message) throws JMSException {
        org.springframework.amqp.core.Message msg = ((RMQMessage) message).buildMessage();
        destination.getTemplate().send(msg);
    }

    @Override
    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void
    send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public Queue getQueue() throws JMSException {
        Assert.notNull(destination);
        Assert.isTrue(destination.isQueue());
        return destination;
    }

    @Override
    public void send(Queue queue, Message message) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(Queue queue, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        // TODO Auto-generated method stub

    }

    public RMQSession getSession() {
        return session;
    }

}
