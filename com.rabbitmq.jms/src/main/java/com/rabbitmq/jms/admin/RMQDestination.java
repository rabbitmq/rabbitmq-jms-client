package com.rabbitmq.jms.admin;

import java.io.IOException;
import java.util.HashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import com.rabbitmq.jms.client.RMQMessageDestination;
import com.rabbitmq.jms.client.RMQSession;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ implementation of JMS {@link Destination}
 */
@SuppressWarnings("serial")
public class RMQDestination extends RMQMessageDestination {

    private final RMQSession session;

    public RMQDestination(RMQSession session, String name, boolean queue, boolean durable, boolean temporary) throws JMSException {
        super();
        this.session = session;
        setName(name);
        setQueue(queue);
        setExchangeName("");
        setRoutingKey(name);
        setConsumerTag(name + "." + System.identityHashCode(this));
        try {
            if (queue) {
                session.getChannel().queueDeclare(name, durable, temporary, !durable, new HashMap<String,Object>());
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }
    

    public RMQSession getSession() {
        return session;
    }
    
    

}
