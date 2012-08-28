package com.rabbitmq.jms.admin;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

// TODO implement JNDI creation for RMQConnectionFactory and RMQDestination
public class RMQObjectFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(Object obj, Name name, Context ctx, Hashtable<?, ?> environment) throws Exception {
        // We only know how to deal with <code>javax.naming.Reference</code>s
        if ((obj == null) || !(obj instanceof Reference)) {
            return null;
        }
        Reference ref = (Reference) obj;
        
        String className = ref.getClassName();
        if (className == null || className.trim().length()==0) {
            throw new NamingException("Unable to instantiate opbject, type has not been specified");
        }
        
        boolean topic = false;
        if (QueueConnectionFactory.class.getName().equals(className)) {
            className = RMQConnectionFactory.class.getName();
        } else if (TopicConnectionFactory.class.getName().equals(className)) {
            className = RMQConnectionFactory.class.getName();
            topic = true;
        } else if (ConnectionFactory.class.getName().equals(className)) {
            className = RMQConnectionFactory.class.getName();
        } else if (Destination.class.getName().equals(className)) {
            className = RMQDestination.class.getName();
        } else if (Topic.class.getName().equals(className)) {
            className = RMQDestination.class.getName();
            topic = true;
        } else if (Queue.class.getName().equals(className)) {
            className = RMQDestination.class.getName();
        }

        if (className.equals(RMQConnectionFactory.class.getName())) {
            return createConnectionFactory(ref, name);
        } else if (className.equals(RMQDestination.class.getName())) {
            return createDestination(ref, name, topic);
        } else {
            throw new NamingException("Unknown class:"+className);
        }

    }
    
    public Object createConnectionFactory(Reference ref, Name name) throws Exception {
        RMQConnectionFactory f = new RMQConnectionFactory();
        //TODO set properties
        return f;
    }

    public Object createDestination(Reference ref, Name name, boolean topic) throws Exception {
        RMQDestination d = new RMQDestination();
        //TODO set properties
        return d;
    }
}
