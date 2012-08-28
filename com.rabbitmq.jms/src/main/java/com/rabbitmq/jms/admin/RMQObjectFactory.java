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
import javax.naming.RefAddr;
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
        if (className == null || className.trim().length() == 0) {
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
            throw new NamingException("Unknown class:" + className);
        }

    }

    public Object createConnectionFactory(Reference ref, Name name) throws Exception {
        RMQConnectionFactory f = new RMQConnectionFactory();

        String username = getStringProperty(ref, "username", true, false, "guest");
        String password = getStringProperty(ref, "password", true, false, "guest");
        String virtualHost = getStringProperty(ref, "virtualHost", true, false, "/");
        String host = getStringProperty(ref, "localhost", true, false, "localhost");

        int port = getIntProperty(ref, "port", true, 5672);
        int threadsPerConnection = getIntProperty(ref, "threadPerConnection", true, 2);
        String threadPrefix = getStringProperty(ref, "threadPrefix", true, true, "Rabbit JMS Thread #");

        f.setUsername(username);
        f.setPassword(password);
        f.setVirtualHost(virtualHost);
        f.setHost(host);
        f.setPort(port);
        f.setThreadsPerConnection(threadsPerConnection);
        f.setThreadPrefix(threadPrefix);
        
        return f;
    }

    public Object createDestination(Reference ref, Name name, boolean topic) throws Exception {
        RMQDestination d = new RMQDestination();

        String dname = getStringProperty(ref, "name", false, false, null);
        String exchName = getStringProperty(ref, "exchangeName", false, false, null);
        String routingKey = getStringProperty(ref, "routingKey", false, false, null);

        d.setName(dname);
        d.setExchangeName(exchName);
        d.setRoutingKey(routingKey);
        d.setQueue(!topic);

        return d;
    }

    private String
            getStringProperty(Reference ref, String name, boolean mayBeNull, boolean mayBeEmpty, String defaultValue) throws NamingException {
        RefAddr ra = ref.get(name);
        if (!mayBeNull && ra == null) {
            throw new NamingException("Property [" + name + "] may not be null.");
        }
        String content = ra.getContent().toString();
        if (!mayBeEmpty && (content == null || content.trim().length() == 0)) {
            throw new NamingException("Property [" + name + "] is present but is lacking a value.");
        }

        if (content == null && mayBeNull) {
            return defaultValue;
        }

        if (content != null && content.trim().length() == 0) {
            if (mayBeEmpty) {
                return content;
            } else {
                return defaultValue;
            }
        }

        return content;
    }

    private int getIntProperty(Reference ref, String name, boolean mayBeNull, int defaultValue) throws NamingException {
        RefAddr ra = ref.get(name);
        if (!mayBeNull && ra == null) {
            throw new NamingException("Property [" + name + "] may not be null.");
        }
        String content = ra.getContent().toString();
        if (content == null && !mayBeNull) {
            throw new NamingException("Property [" + name + "] is present but is lacking a value.");
        }
        int result = defaultValue;
        try {
            result = Integer.parseInt(content);
        } catch (Exception x) {
            if (!mayBeNull) {
                NamingException nx = new NamingException("Property [" + name + "] is present but is not an integer value[" + content + "]");
                nx.setRootCause(x);
                throw nx;
            }
        }

        return result;
    }
}
