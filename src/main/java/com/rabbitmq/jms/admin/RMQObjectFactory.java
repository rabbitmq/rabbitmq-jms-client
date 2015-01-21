/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
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
 *           host=&quot;localhost&quot;/&gt;
 * </pre>
 * <p>Alternatively, a <a href="http://www.rabbitmq.com/uri-spec.html">AMQP uri</a> can be used:
 * </p>
 * <pre>
 * &lt;Resource name=&quot;jms/ConnectionFactory&quot; type=&quot;javax.jms.ConnectionFactory&quot;
 *           factory=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot;
 *           uri=&quot;amqp://guest:guest@127.0.0.1&quot;
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
 *           factory=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot;
 *           destinationName=&quot;queueName&quot;/&gt;
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
 * Valid properties for a {@link ConnectionFactory} are:
 * </p>
 * <ul>
 * <li>uri</li>
 * <li>host</li>
 * <li>password</li>
 * <li>port</li>
 * <li>queueBrowserReadMax</li>
 * <li>ssl</li>
 * <li>terminationTimeout</li>
 * <li>username</li>
 * <li>virtualHost</li>
 * </ul>
 * and are applied in this order, if they are present. If a property is not present, or is not set by means of the
 * <code>uri</code> attribute, the default value is the same as that obtained by instantiating a
 * {@link RMQConnectionFactory} object with the default constructor.
 * <p>
 * Properties for a {@link Topic} or a {@link Queue} are:
 * </p>
 * <ul>
 * <li>destinationName</li>
 * </ul>
 * TODO Implement socket options.
 */
public class RMQObjectFactory implements ObjectFactory {

    private final Logger logger = LoggerFactory.getLogger(RMQObjectFactory.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context ctx, Hashtable<?, ?> environment) throws Exception {
        // We only know how to deal with <code>javax.naming.Reference</code>s
        if ((obj == null) || !(obj instanceof javax.naming.Reference)) {
            return null;
        }
        Reference ref = (Reference) obj;

        String className = ref.getClassName();
        if (className == null || className.trim().length() == 0) {
            throw new NamingException("Unable to instantiate object: type has not been specified");
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
        boolean connectionFactory = false;
        if (  javax.jms.QueueConnectionFactory.class.getName().equals(className)
           || javax.jms.TopicConnectionFactory.class.getName().equals(className)
           || javax.jms.ConnectionFactory.class.getName().equals(className)
           ) {
            connectionFactory = true;
        } else if (javax.jms.Topic.class.getName().equals(className)) {
            topic = true;
        } else if (javax.jms.Queue.class.getName().equals(className)) {
        } else {
            throw new NamingException(String.format("Unknown class [%s]", className));
        }

        if (connectionFactory) {
            return createConnectionFactory(ref, name);
        } else {
            return createDestination(ref, name, topic);
        }
    }

    /**
     * Creates a RMQConnectionFactory from a Reference
     * @param ref - the reference containing all properties
     * @param name - the name of the object
     * @return a {@link RMQConnectionFactory} object configured
     * @throws NamingException if a required property is missing or invalid
     */
    public Object createConnectionFactory(Reference ref, Name name) throws NamingException {
        this.logger.trace("Creating connection factory ref '{}', name '{}'.", ref, name);
        RMQConnectionFactory f = new RMQConnectionFactory();

        try { // set uri first, which may fail if it doesn't parse
            f.setUri(getStringProperty(ref, "uri", true, f.getUri()));
        } catch (JMSException e) {
            this.logger.warn("Failed to set RMQConnectionFactory properties by URI--defaults taken initially.", e);
        }

        // explicit properties (these override the uri, if set)
        f.setHost               (getStringProperty (ref, "host",                true, f.getHost()               ));
        f.setPassword           (getStringProperty (ref, "password",            true, f.getPassword()           ));
        f.setPort               (getIntProperty    (ref, "port",                true, f.getPort()               ));
        f.setQueueBrowserReadMax(getIntProperty    (ref, "queueBrowserReadMax", true, f.getQueueBrowserReadMax()));
        f.setSsl                (getBooleanProperty(ref, "ssl",                 true, f.isSsl()                 ));
        f.setTerminationTimeout (getLongProperty   (ref, "terminationTimeout",  true, f.getTerminationTimeout() ));
        f.setUsername           (getStringProperty (ref, "username",            true, f.getUsername()           ));
        f.setVirtualHost        (getStringProperty (ref, "virtualHost",         true, f.getVirtualHost()        ));

        return f;
    }

    /**
     * Create a {@link RMQDestination} from a reference
     * @param ref the reference containing the required properties
     * @param name - the name
     * @param topic - true if this is a topic, false if it is a queue (ignored if this is amqp-mapped)
     * @return a {@link RMQDestination} object with the destinationName configured
     * @throws NamingException if the <code>destinationName</code> property is missing
     */
    public Object createDestination(Reference ref, Name name, boolean topic) throws NamingException {
        this.logger.trace("Creating destination ref '{}', name '{}' (topic={}).", ref, name, topic);
        String dname = getStringProperty(ref, "destinationName", false, null);
        boolean amqp = getBooleanProperty(ref, "amqp", true, false);
        if (amqp) {
            String amqpExchangeName = getStringProperty(ref, "amqpExchangeName", false, null);
            String amqpRoutingKey = getStringProperty(ref, "amqpRoutingKey", false, null);
            String amqpQueueName = getStringProperty(ref, "amqpQueueName", false, null);
            return new RMQDestination(dname, amqpExchangeName, amqpRoutingKey, amqpQueueName);
        } else {
            return new RMQDestination(dname, !topic, false);
        }
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
        String content = propertyContent(ref, propertyName, mayBeNull);
        if (content == null) return defaultValue;
        return content;
    }

    /**
     * Reads a property from the reference and returns the boolean value it represents
     * @param ref the reference containing the value
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing or contain a null value, in this case <code>defaultValue</code> will be returned
     * @param defaultValue the defaultValue to return if the property is null and <code>mayBeNull==true</code>
     * @return the boolean value of the property
     * @throws NamingException if the property is missing and <code>mayBeNull==false</code>
     */
    private boolean getBooleanProperty(Reference ref,
                                       String propertyName,
                                       boolean mayBeNull,
                                       boolean defaultValue) throws NamingException {
        String content = propertyContent(ref, propertyName, mayBeNull);
        if (content == null) return defaultValue;
        return Boolean.valueOf(content);
    }

    /**
     * Reads a property from the reference and returns the int value it represents
     * @param ref the reference
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing, in which case <code>defaultValue</code> will be returned
     * @param defaultValue the default value to return if <code>mayBeNull</code> is set to true
     * @return the integer value representing the property value
     * @throws NamingException if the property is missing while mayBeNull is set to false, or a number format exception happened
     */
    private int getIntProperty(Reference ref,
                               String propertyName,
                               boolean mayBeNull,
                               int defaultValue) throws NamingException {
        String content = propertyContent(ref, propertyName, mayBeNull);
        if (content == null) return defaultValue;
        try {
            return Integer.parseInt(content);
        } catch (Exception x) {
            NamingException nx = new NamingException(String.format("Property [%s] is present but is not an integer value [%s]", propertyName, content));
            nx.setRootCause(x);
            throw nx;
        }
    }

    /**
     * Reads a property from the reference and returns the long integer value it represents
     * @param ref the reference
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing, in which case <code>defaultValue</code> will be returned
     * @param defaultValue the default value to return if <code>mayBeNull</code> is set to true
     * @return the long integer value representing the property value
     * @throws NamingException if the property is missing while mayBeNull is set to false, or a number format exception happened
     */
    private long getLongProperty(Reference ref,
                                 String propertyName,
                                 boolean mayBeNull,
                                 long defaultValue) throws NamingException {
        String content = propertyContent(ref, propertyName, mayBeNull);
        if (content == null) return defaultValue;
        try {
            return Long.parseLong(content);
        } catch (Exception x) {
            NamingException nx = new NamingException(String.format("Property [%s] is present but is not a long integer value [%s]", propertyName, content));
            nx.setRootCause(x);
            throw nx;
        }
    }

    private static String propertyStringContent(RefAddr ra) {
        return (ra == null ? null : ra.getContent() == null ? null : ra.getContent().toString());
    }

    private static String propertyContent(Reference ref, String propertyName, boolean mayBeNull) throws NamingException {
        RefAddr ra = ref.get(propertyName);
        if (!mayBeNull && ra == null) {
            throw new NamingException(String.format("Property [%s] may not be null.", propertyName));
        }
        String content = propertyStringContent(ra);
        if (content == null && !mayBeNull) {
            throw new NamingException(String.format("Property [%s] is present but is lacking a value.", propertyName));
        }
        return content;
    }
}
