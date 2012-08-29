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

/**
 * JNDI Factory to create resources in containers such as <a href="http://tomcat.apache.org">Tomcat</a>
 * An example Tomcat configuration for a ConnectionFactory would look like:<br/>
 * &lt;Resource <br/>
 * &emsp;name=&quot;jms/ConnectionFactory&quot; <br/> 
 * &emsp;type=&quot;javax.jms.ConnectionFactory&quot;  <br/>
 * &emsp;factory=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot;  <br/>
 * &emsp;username=&quot;guest&quot; <br/>
 * &emsp;password=&quot;guest&quot; <br/>
 * &emsp;virtualHost=&quot;/&quot; <br/>
 * &emsp;host=&quot;localhost&quot; <br/>
 * &emsp;threadsPerConnection=&quot;2&quot;/&gt; <br/>
 * the type attribute can be <code>javax.jms.ConnectionFactory</code>, <code>javax.jms.QueueConnectionFactory</code>, <code>javax.jms.TopicConnectionFactory</code>
 * or the actual classname of the implementation, <code>com.rabbitmq.jms.admin.RMQConnectionFactory</code> <br/>
 * A destination, <code>Queue</code> or <code>Topic</code>, can be created using the following configuration (queue below) <br/>
 * &lt;Resource <br/>
 * &emsp;name=&quot;jms/Queue&quot; type=&quot;javax.jms.Queue&quot; <br/>
 * &emsp;factory=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot;<br/>
 * &emsp;destinationName=&quot;queueName&quot;/&gt; <br/>
 * and a Topic would be created using <br/>              
 * &lt;Resource <br/>
 * &emsp;name=&quot;jms/Topic&quot; type=&quot;javax.jms.Topic&quot; <br/>
 * &emsp;factory=&quot;com.rabbitmq.jms.admin.RMQObjectFactory&quot; <br/>
 * &emsp;destinationName=&quot;topicName&quot;/&gt; <br/>
 *              
 * Valid types are: <br/>
 * javax.jms.ConnectionFactory<br/>
 * javax.jms.QueueConnectionFactory<br/>
 * javax.jms.TopicConnectionFactory<br/>
 * javax.jms.Topic<br/>
 * javax.jms.Queue <br/>
 * Properties for a ConnectionFactory are:
 * <ul>
 *  <li>username</li>
 *  <li>password</li>
 *  <li>virtualHost</li>
 *  <li>host</li>
 *  <li>port</li>
 *  <li>threadsPerConnection</li>
 *  <li>threadPrefix</li>
 * </ul>  
 * Properties for a topic or a queue are:
 * <ul>
 *  <li>destinationName</li>
 * </ul> 
 */
public class RMQObjectFactory implements ObjectFactory {

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
            throw new NamingException("Unable to instantiate opbject, type has not been specified");
        }

        /*
         * Valid classnames are:
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
        RMQConnectionFactory f = new RMQConnectionFactory();

        String username = getStringProperty(ref, "username", true, "guest");
        String password = getStringProperty(ref, "password", true, "guest");
        String virtualHost = getStringProperty(ref, "virtualHost", true, "/");
        String host = getStringProperty(ref, "localhost", true, "localhost");

        int port = getIntProperty(ref, "port", true, 5672);
        int threadsPerConnection = getIntProperty(ref, "threadPerConnection", true, 2);
        String threadPrefix = getStringProperty(ref, "threadPrefix", true, "Rabbit JMS Thread #");

        f.setUsername(username);
        f.setPassword(password);
        f.setVirtualHost(virtualHost);
        f.setHost(host);
        f.setPort(port);
        f.setThreadsPerConnection(threadsPerConnection);
        f.setThreadPrefix(threadPrefix);
        
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
        String dname = getStringProperty(ref, "destinationName", false, null);
        RMQDestination d = new RMQDestination(dname, !topic);
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
        if (!mayBeNull && ra == null) {
            throw new NamingException("Property [" + propertyName + "] may not be null.");
        }
        String content = ra.getContent().toString();
        if (!mayBeNull && (content == null)) {
            throw new NamingException("Property [" + propertyName + "] is present but is lacking a value.");
        }

        if (content == null && mayBeNull) {
            return defaultValue;
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
        String content = ra.getContent().toString();
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
