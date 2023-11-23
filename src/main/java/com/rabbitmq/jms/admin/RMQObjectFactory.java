/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.admin;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Hashtable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import com.rabbitmq.jms.client.AuthenticationMechanism;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JNDI Factory to create resources in containers such as <a href="https://tomcat.apache.org">Tomcat</a>.
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
 * <p>Alternatively, a <a href="https://www.rabbitmq.com/uri-spec.html">AMQP uri</a> can be used:
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
 * An example Wildfly configuration for a {@link ConnectionFactory} would look like:
 * </p>
 * <pre>
 * &lt;object-factory name=&quot;java:global/jms/ConnectionFactory&quot; module=&quot;org.jboss.genericjms.provider&quot; class=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot;&gt;
 *     &lt;environment&gt;
 *         &lt;property name=&quot;className&quot; value=&quot;javax.jms.ConnectionFactory&quot;/&gt;
 *         &lt;property name=&quot;username&quot; value=&quot;guest&quot;/&gt;
 *         &lt;property name=&quot;password&quot; value=&quot;guest&quot;/&gt;
 *         &lt;property name=&quot;virtualHost&quot; value=&quot;/&quot;/&gt;
 *         &lt;property name=&quot;host&quot; value=&quot;localhost&quot;/&gt;
 *     &lt;/environment&gt;
 * &lt;/object-factory&gt;
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
 * <li>onMessageTimeoutMs</li>
 * <li>channelsQos</li>
 * <li>ssl</li>
 * <li>terminationTimeout</li>
 * <li>username</li>
 * <li>virtualHost</li>
 * <li>className - only applies when properties are provided via environment HashTable</li>
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

    private static final String ENV_CLASS_NAME = "className";

    private static final Logger LOGGER = LoggerFactory.getLogger(RMQObjectFactory.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context ctx, Hashtable<?, ?> environment) throws Exception {

        if ((obj == null) ) {
            return null;
        }
        Reference ref = obj instanceof Reference ? (Reference) obj : null;

        if (ref == null && (environment == null || environment.isEmpty())) {
            throw new NamingException("Unable to instantiate object: obj is not a Reference instance and environment table is empty");
        }

        String className = ref!= null ? ref.getClassName(): (String) environment.get(ENV_CLASS_NAME);

        if (className == null || className.trim().length() == 0) {
            throw new NamingException("Unable to instantiate object: type has not been specified");
        }

        /*
         * Valid class names are:
         * javax.jms.ConnectionFactory
         * javax.jms.QueueConnectionFactory
         * javax.jms.TopicConnectionFactory
         * com.rabbitmq.jms.admin.RMQConnectionFactory
         * javax.jms.Topic
         * javax.jms.Queue
         * com.rabbitmq.jms.admin.RMQDestination
         *
         */
        boolean topic = false;
        boolean connectionFactory = false;
        if (isClassNameMatching(className)) {
            connectionFactory = true;
        } else if (javax.jms.Topic.class.getName().equals(className)) {
            topic = true;
        } else if (javax.jms.Queue.class.getName().equals(className)) {
        } else if (RMQDestination.class.getName().equals(className)) {
            topic = !getBooleanProperty(ref, environment, "isQueue", true, false);
        } else {
            throw new NamingException(String.format("Unknown class [%s]", className));
        }

        if (connectionFactory) {
            return createConnectionFactory(ref, environment, name);
        } else {
            return createDestination(ref, environment, name, topic);
        }
    }

    /**
     * Creates a RMQConnectionFactory from a Reference or environment Hashtable
     * @param ref the reference containing properties
     * @param environment the environment containing properties
     * @param name the name of the object
     * @return a {@link RMQConnectionFactory} object configured
     * @throws NamingException if a required property is missing or invalid
     */
    public Object createConnectionFactory(Reference ref, Hashtable<?, ?> environment, Name name) throws NamingException {
        LOGGER.trace("Creating connection factory ref '{}', name '{}'.", ref, name);
        RMQConnectionFactory f = new RMQConnectionFactory();

        try { // set uri first, which may fail if it doesn't parse
            f.setUri(getStringProperty(ref, environment, "uri", true, f.getUri()));
        } catch (JMSException e) {
            LOGGER.warn("Failed to set RMQConnectionFactory properties by URI--defaults taken initially.", e);
        }

        String urisString = getStringProperty(ref, environment, "uris", true, null);
        if (urisString != null) {
            try {
                f.setUris(Arrays.stream(urisString.split(",")).map(String::trim).collect(Collectors.toList()));
            } catch (JMSException e) {
                LOGGER.warn("Failed to set RMQConnectionFactory properties by URIs.", e);
            }
        }

        // explicit properties (these override the uri, if set)
        f.setHost               (getStringProperty (ref, environment, "host",                true, f.getHost()               ));
        f.setPassword           (getStringProperty (ref, environment, "password",            true, f.getPassword()           ));
        f.setPort               (getIntProperty    (ref, environment, "port",                true, f.getPort()               ));
        f.setQueueBrowserReadMax(getIntProperty    (ref, environment, "queueBrowserReadMax", true, f.getQueueBrowserReadMax()));
        f.setOnMessageTimeoutMs (getIntProperty    (ref, environment, "onMessageTimeoutMs",  true, f.getOnMessageTimeoutMs() ));
        f.setChannelsQos        (getIntProperty    (ref, environment, "channelsQos",         true, f.getChannelsQos()        ));
        if (getBooleanProperty(ref, environment, "ssl",                 true, f.isSsl())) {
            try {
                f.useSslProtocol();
            } catch (NoSuchAlgorithmException e) {
                throw new NamingException("Error while enabling TLS: " + e.getMessage());
            }
        }
        f.setTerminationTimeout (getLongProperty   (ref, environment, "terminationTimeout",  true, f.getTerminationTimeout() ));
        f.setUsername           (getStringProperty (ref, environment, "username",            true, f.getUsername()           ));
        f.setVirtualHost        (getStringProperty (ref, environment, "virtualHost",         true, f.getVirtualHost()        ));
        f.setCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose(getBooleanProperty(ref, environment, "cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose",                 true, f.isCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose()                 ));
        f.setDeclareReplyToDestination(getBooleanProperty(ref, environment, "declareReplyToDestination", true, true));
        f.setKeepTextMessageType(getBooleanProperty(ref, environment, "keepTextMessageType", true, false));
        f.setNackOnRollback(getBooleanProperty(ref, environment, "nackOnRollback", true, false));

        String authenticationMechanismString = getStringProperty(ref, environment, "authenticationMechanism", true, null);
        if (authenticationMechanismString != null) {
            try {
                f.setAuthenticationMechanism(AuthenticationMechanism.valueOf(authenticationMechanismString));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Failed to set AuthenticationMechanism on RMQConnectionFactory.", e);
            }
        }
        return f;
    }

    /**
     * Create a {@link RMQDestination} from a Reference of environment Hashtable
     * @param ref the reference containing the properties
     * @param environment the environment containing the properties
     * @param name the name
     * @param topic true if this is a topic, false if it is a queue (ignored if this is amqp-mapped)
     * @return a {@link RMQDestination} object with the destinationName configured
     * @throws NamingException if the <code>destinationName</code> property is missing
     */
    public Object createDestination(Reference ref, Hashtable<?, ?> environment, Name name, boolean topic) throws NamingException {
        LOGGER.trace("Creating destination ref '{}', name '{}' (topic={}).", ref, name, topic);
        String dname = getStringProperty(ref, environment, "destinationName", false, null);
        Map<String, Object> queueDeclareArguments = convertQueueDeclareArguments(getMapProperty(
          ref, environment, "queueDeclareArguments", true, null
        ));
        boolean amqp = getBooleanProperty(ref, environment, "amqp", true, false);
        if (amqp) {
            if (queueDeclareArguments != null) {
                LOGGER.warn("Queue declare arguments are ignored for AMQP destinations");
            }
            String amqpExchangeName = getStringProperty(ref, environment, "amqpExchangeName", true, null);
            String amqpRoutingKey = getStringProperty(ref, environment,"amqpRoutingKey", true, null);
            String amqpQueueName = getStringProperty(ref, environment, "amqpQueueName", true, null);
            return new RMQDestination(dname, amqpExchangeName, amqpRoutingKey, amqpQueueName);
        } else {
            return new RMQDestination(dname, !topic, false, queueDeclareArguments);
        }
    }

    /**
     * Returns the value of a set property in a reference
     * @param ref the Reference containing the value
     * @param environment the environment Hashtable containing the value
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing or contain a null value, in this case <code>defaultValue</code> will be returned
     * @param defaultValue the defaultValue to return if the property is null and <code>mayBeNull==true</code>
     * @return the String value for the property
     * @throws NamingException if the property is missing and <code>mayBeNull==false</code>
     */
    private static String getStringProperty(Reference ref,
                                     Hashtable<?, ?> environment,
                                     String propertyName,
                                     boolean mayBeNull,
                                     String defaultValue) throws NamingException {
        String content = propertyContent(ref, environment, propertyName, mayBeNull);
        if (content == null) return defaultValue;
        return content;
    }

    /**
     * Reads a property from the reference and returns the boolean value it represents
     * @param ref the Reference containing the value
     * @param environment the environment Hashtable containing the value
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing or contain a null value, in this case <code>defaultValue</code> will be returned
     * @param defaultValue the defaultValue to return if the property is null and <code>mayBeNull==true</code>
     * @return the boolean value of the property
     * @throws NamingException if the property is missing and <code>mayBeNull==false</code>
     */
    private static boolean getBooleanProperty(Reference ref,
                                       Hashtable<?, ?> environment,
                                       String propertyName,
                                       boolean mayBeNull,
                                       boolean defaultValue) throws NamingException {
        String content = propertyContent(ref, environment, propertyName, mayBeNull);
        if (content == null) return defaultValue;
        return Boolean.valueOf(content);
    }

    /**
     * Reads a property from the reference and returns the int value it represents
     * @param ref the Reference containing the value
     * @param environment the environment Hashtable containing the value
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing, in which case <code>defaultValue</code> will be returned
     * @param defaultValue the default value to return if <code>mayBeNull</code> is set to true
     * @return the integer value representing the property value
     * @throws NamingException if the property is missing while mayBeNull is set to false, or a number format exception happened
     */
    private static int getIntProperty(Reference ref,
                               Hashtable<?, ?> environment,
                               String propertyName,
                               boolean mayBeNull,
                               int defaultValue) throws NamingException {
        String content = propertyContent(ref, environment, propertyName, mayBeNull);
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
     * @param ref the Reference containing the value
     * @param environment the environment Hashtable containing the value
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing, in which case <code>defaultValue</code> will be returned
     * @param defaultValue the default value to return if <code>mayBeNull</code> is set to true
     * @return the long integer value representing the property value
     * @throws NamingException if the property is missing while mayBeNull is set to false, or a number format exception happened
     */
    private static long getLongProperty(Reference ref,
                                 Hashtable<?, ?> environment,
                                 String propertyName,
                                 boolean mayBeNull,
                                 long defaultValue) throws NamingException {
        String content = propertyContent(ref, environment, propertyName, mayBeNull);
        if (content == null) return defaultValue;
        try {
            return Long.parseLong(content);
        } catch (Exception x) {
            NamingException nx = new NamingException(String.format("Property [%s] is present but is not a long integer value [%s]", propertyName, content));
            nx.setRootCause(x);
            throw nx;
        }
    }

    /**
     * Reads a property from the reference and returns the map value it represents
     * @param ref the Reference containing the value
     * @param environment the environment Hashtable containing the value
     * @param propertyName the name of the property
     * @param mayBeNull true if the property may be missing, in which case <code>defaultValue</code> will be returned
     * @param defaultValue the default value to return if <code>mayBeNull</code> is set to true
     * @return the map value representing the property value
     * @throws NamingException if the property is missing while mayBeNull is set to false, or a conversion exception happened
     */
    private static Map<String, String> getMapProperty(Reference ref,
        Hashtable<?, ?> environment,
        String propertyName,
        boolean mayBeNull,
        Map<String, String> defaultValue) throws NamingException {
        String content = propertyContent(ref, environment, propertyName, mayBeNull);
        if (content == null) return defaultValue;
        try {
            Map<String, String> result = new LinkedHashMap<>();
            String [] entries = content.split(",");
            for (String entry : entries) {
                String [] keyValue = entry.split("=");
                result.put(keyValue[0], keyValue[1]);
            }
            return result;
        } catch (Exception x) {
            NamingException nx = new NamingException(String.format("Property [%s] is present but is not a map value [%s]", propertyName, content));
            nx.setRootCause(x);
            throw nx;
        }
    }

    private static String propertyStringContent(RefAddr ra) {
        return (ra == null ? null : ra.getContent() == null ? null : ra.getContent().toString());
    }

    private static String environmentPropertyStringContent(Object propValue) {
        return (propValue == null ? null : propValue.toString());
    }

    private static String propertyContent(Reference ref, Hashtable<?,?> environment, String propertyName, boolean mayBeNull) throws NamingException {
        if (!mayBeNull && (ref == null || ref.get(propertyName) == null) && (environment == null || environment.get(propertyName) == null)) {
            throw new NamingException(String.format("Property [%s] may not be null.", propertyName));
        }
        String content = ref != null ? propertyStringContent(ref.get(propertyName)) : environmentPropertyStringContent(environment.get(propertyName));
        if (content == null && !mayBeNull) {
            throw new NamingException(String.format("Property [%s] is present but is lacking a value.", propertyName));
        }
        return content;
    }

    // from rabbit_amqqueue:declare_args/0
    private static final Map<String, Function<String, Object>> QUEUE_DECLARE_ARGUMENTS_RULES =
                        new ConcurrentHashMap<String, Function<String, Object>>(){{
        put("x-expires", Integer::parseInt);
        put("x-message-ttl", Integer::parseInt);
        put("x-dead-letter-exchange", String::valueOf);
        put("x-dead-letter-routing-key", String::valueOf);
        put("x-dead-letter-strategy", String::valueOf);
        put("x-max-length", Integer::parseInt);
        put("x-max-length-bytes", Integer::parseInt);
        put("x-max-in-memory-length", Integer::parseInt);
        put("x-max-in-memory-bytes", Integer::parseInt);
        put("x-max-priority", Integer::parseInt);
        put("x-overflow", String::valueOf);
        put("x-queue-mode", String::valueOf);
        put("x-queue-version", Integer::valueOf);
        put("x-single-active-consumer", Boolean::valueOf);
        put("x-queue-type", String::valueOf);
        put("x-quorum-initial-group-size", Integer::parseInt);
        put("x-max-age", String::valueOf);
        put("x-stream-max-segment-size-bytes", Integer::parseInt);
        put("x-initial-cluster-size", Integer::parseInt);
        put("x-queue-leader-locator", String::valueOf);
    }};

    private static Map<String, Object> convertQueueDeclareArguments(
        Map<String, String> queueDeclareArguments) {
        if (queueDeclareArguments == null) {
            return null;
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>(queueDeclareArguments.size());
        for (Entry<String, String> entry : queueDeclareArguments.entrySet()) {
            String key = entry.getKey();
            String rawValue = entry.getValue();
            Function<String, Object> conversionRule = QUEUE_DECLARE_ARGUMENTS_RULES.get(key);
            Object convertedValue = rawValue;
            try {
                convertedValue = conversionRule == null ? rawValue : conversionRule.apply(rawValue);
            } catch (Exception e) {
                LOGGER.info("Could not convert queue declare argument {} = {} to appropriate type, "
                    + "using string value. Error message: {}", key, rawValue, e.getMessage());
            }
            result.put(key, convertedValue);
        }
        return result;
    }

    /**
     * Returns if the class name matches any of the connection factory names
     * @param className
     * @return the boolean value representing the result of conditional statement
     */
    private static boolean isClassNameMatching(String className) {
        return javax.jms.QueueConnectionFactory.class.getName().equals(className)
                || javax.jms.TopicConnectionFactory.class.getName().equals(className)
                || javax.jms.ConnectionFactory.class.getName().equals(className)
                || RMQConnectionFactory.class.getName().equals(className);
    }

}
