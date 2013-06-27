/* Copyright (c) 2013 GoPivotal, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JNDI Factory to create resources in containers such as <a href="http://tomcat.apache.org">Tomcat</a>.
 * <p>
 * An example Tomcat configuration for a {@link ConnectionFactory} would look like:
 * </p>
 * <pre>
 * &lt;Resource name=&quot;jms/ConnectionFactory&quot; type=&quot;javax.jms.ConnectionFactory&quot;
 *           factory=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot;
 *           username=&quot;guest&quot;
 *           password=&quot;guest&quot;
 *           virtualHost=&quot;/&quot;
 *           host=&quot;localhost&quot;
 *           threadsPerConnection=&quot;2&quot;/&gt;
 * </pre>
 * <p>
 * the type attribute can be {@link javax.jms.ConnectionFactory}, {@link javax.jms.QueueConnectionFactory},
 * {@link javax.jms.TopicConnectionFactory} or the actual classname of the implementation,
 * {@link com.rabbitmq.jms.admin.RMQConnectionFactory}.
 * </p>
 * <p>
 * A destination, {@link Queue} or {@link Topic}, can be created using the following configuration ({@link Queue} first):
 * </p>
 * <pre>
 * &lt;Resource name=&quot;jms/Queue&quot; type=&quot;javax.jms.Queue&quot;
           factory=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot;
           destinationName=&quot;queueName&quot;/&gt;
 * </pre>
 * <p>
 * and a {@link Topic} would be created thus:
 * </p>
 * <pre>
 * &lt;Resource name=&quot;jms/Topic&quot; type=&quot;javax.jms.Topic&quot;
 *           factory=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot;
 *           destinationName=&quot;topicName&quot;/&gt;
 * </pre>
 * <p>
 * Valid types are:
 * </p>
 * <pre>
 * javax.jms.ConnectionFactory
 * javax.jms.QueueConnectionFactory
 * javax.jms.TopicConnectionFactory
 * javax.jms.Topic
 * javax.jms.Queue
 * </pre>
 * <p>
 * Properties for a {@link ConnectionFactory} are:
 * </p>
 * <ul>
 * <li>username</li>
 * <li>password</li>
 * <li>virtualHost</li>
 * <li>host</li>
 * <li>port</li>
 * <li>threadsPerConnection</li>
 * <li>threadPrefix</li>
 * <li>terminationTimeout</li>
 * </ul>
 * <p>
 * Properties for a {@link Topic} or a {@link Queue} are:
 * </p>
 * <ul>
 * <li>destinationName</li>
 * </ul>
 * TODO Implement SSL and socket options.
 */
public class RMQObjectFactory implements ObjectFactory {

    private final Logger logger = LoggerFactory.getLogger(RMQObjectFactory.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context ctx, Hashtable<?, ?> environment) throws Exception {
        // We only know how to deal with <code>javax.naming.Reference</code>s
        if ((obj == null) || !(obj instanceof Reference)) {
            return null;
        }
        Reference ref = (Reference) obj;

        String className = ref.getClassName();
        if (className == null || className.trim().length() == 0) {
            throw new NamingException("Unable to instantiate object, type has not been specified");
        }

        /*
         * Valid class names are:
         * javax.jms.ConnectionFactory
         * javax.jms.QueueConnectionFactory
         * javax.jms.TopicConnectionFactory
         * javax.jms.Topic
         * javax.jms.Queue
         *
         */
        boolean topic = false;
        if (QueueConnectionFactory.class.getName().equals(className)) {
            className = RMQConnectionFactory.class.getName();
        } else if (TopicConnectionFactory.class.getName().equals(className)) {
            className = RMQConnectionFactory.class.getName();
            topic = true;
        } else if (ConnectionFactory.class.getName().equals(className)) {
            className = RMQConnectionFactory.class.getName();
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

    /**
     * Creates a RMQConnectionFactory from a Reference
     * @param ref - the reference containing all properties
     * @param name - the name of the object
     * @return a {@link RMQConnectionFactory} object configured
     * @throws NamingException if a required property is missing
     */
    public Object createConnectionFactory(Reference ref, Name name) throws NamingException {
        this.logger.trace("Creating connection factory ref '{}', name '{}'.", ref, name);
        RMQConnectionFactory f = new RMQConnectionFactory();

        String username = getStringProperty(ref, "username", true, "guest");
        String password = getStringProperty(ref, "password", true, "guest");
        String virtualHost = getStringProperty(ref, "virtualHost", true, "/");
        String host = getStringProperty(ref, "host", true, "127.0.0.1");

        int port = getIntProperty(ref, "port", true, 5672);
        int threadsPerConnection = getIntProperty(ref, "threadPerConnection", true, 2);
        String threadPrefix = getStringProperty(ref, "threadPrefix", true, "Rabbit JMS Thread #");
        int terminationTimeout = getIntProperty(ref, "terminationTimeout", true, 15000);

        f.setUsername(username);
        f.setPassword(password);
        f.setVirtualHost(virtualHost);
        f.setHost(host);
        f.setPort(port);
        f.setThreadsPerConnection(threadsPerConnection);
        f.setThreadPrefix(threadPrefix);
        f.setTerminationTimeout(terminationTimeout);

        return f;
    }

    /**
     * Creates a {@link RMQDestination} from a reference
     * @param ref the reference containing the required properties
     * @param name the name
     * @param topic true if this is a topic, false if it is a queue
     * @return a {@link RMQDestination} object with the destinationName configured
     * @throws NamingException if the <code>destinationName</code> property is missing
     */
    public Object createDestination(Reference ref, Name name, boolean topic) throws NamingException {
        this.logger.trace("Creating destination ref '{}', name '{}' (topic={}).", ref, name, topic);
        String dname = getStringProperty(ref, "destinationName", false, null);
        RMQDestination d = new RMQDestination(dname, !topic, false);
        return d;
    }

    /**
     * Returns the value of a set property in a reference
     * @param ref the reference containing the value
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing or contain a null value, in this case <code>defaultValue</code> will be returned
     * @param defaultValue the defaultValue to return if the property is null and <code>mayBeNull==true</code>
     * @return the String value for the property
     * @throws NamingException if the property is missing and <code>mayBeNull==false</code>
     */
    private String getStringProperty(Reference ref,
                                     String propertyName,
                                     boolean mayBeNull,
                                     String defaultValue) throws NamingException {
        RefAddr ra = ref.get(propertyName);
        if (!mayBeNull && (ra == null || ra.getContent()==null)) {
            throw new NamingException("Property [" + propertyName + "] may not be null.");
        }
        String content = ra == null ? null : ra.getContent() == null ? null : ra.getContent().toString();
        if (!mayBeNull && (content == null)) {
            throw new NamingException("Property [" + propertyName + "] is present but is lacking a value.");
        }

        if (content == null && mayBeNull) {
            content = defaultValue;
        }
        return content;
    }

    /**
     * Reads a property from the reference and returns the int value it represents
     * @param ref the reference
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing, in which case <code>defaultValue</code> will be returned
     * @param defaultValue the default value to return if <code>mayBeNull</code> is set to true
     * @return the int value representing the property value
     * @throws NamingException if the property is missing while mayBeNull is set to false, or a number format exception happened
     */
    private int getIntProperty(Reference ref,
                               String propertyName,
                               boolean mayBeNull,
                               int defaultValue) throws NamingException {
        RefAddr ra = ref.get(propertyName);
        if (!mayBeNull && ra == null) {
            throw new NamingException("Property [" + propertyName + "] may not be null.");
        }
        String content = ra == null ? null : ra.getContent() == null ? null : ra.getContent().toString();
        if (content == null && !mayBeNull) {
            throw new NamingException("Property [" + propertyName + "] is present but is lacking a value.");
        }
        int result = defaultValue;
        try {
            result = Integer.parseInt(content);
        } catch (Exception x) {
            if (!mayBeNull) {
                NamingException nx = new NamingException("Property [" + propertyName + "] is present but is not an integer value[" + content + "]");
                nx.setRootCause(x);
                throw nx;
            }
        }

        return result;
    }
}
