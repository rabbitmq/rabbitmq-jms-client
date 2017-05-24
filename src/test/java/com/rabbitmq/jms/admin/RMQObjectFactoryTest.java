package com.rabbitmq.jms.admin;

import org.junit.Test;

import javax.jms.ConnectionFactory;
import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.Hashtable;

import static org.junit.Assert.*;

/**
 * RMQObjectFactory unit test
 *
 * @author Rémi Bantos
 */
public class RMQObjectFactoryTest {

    private RMQObjectFactory rmqObjectFactory = new RMQObjectFactory();

    @Test
    public void getObjectInstance_should_create_a_RMQConnectionFactory_via_Reference() throws Exception {

        Reference ref = new Reference(ConnectionFactory.class.getName());

        Object createdObject = rmqObjectFactory.getObjectInstance(ref, new CompositeName("java:global/jms/TestConnectionFactory"), null, null);

        assertNotNull(createdObject);
        assertEquals(RMQConnectionFactory.class, createdObject.getClass());

        RMQConnectionFactory createdConFactory = (RMQConnectionFactory) createdObject;

        assertEquals("guest", createdConFactory.getUsername());
        assertEquals("guest", createdConFactory.getPassword());
        assertEquals("/", createdConFactory.getVirtualHost());
        assertEquals("localhost", createdConFactory.getHost());

    }


    @Test
    public void getObjectInstance_should_create_a_RMQConnectionFactory_via_environment() throws Exception {

        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.ConnectionFactory");
            put("username", "remi");
            put("password", "1234");
            put("virtualHost", "/fake");
            put("host", "fakeHost");
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);

        assertNotNull(createdObject);
        assertEquals(RMQConnectionFactory.class, createdObject.getClass());

        RMQConnectionFactory createdConFactory = (RMQConnectionFactory) createdObject;

        assertEquals("remi", createdConFactory.getUsername());
        assertEquals("1234", createdConFactory.getPassword());
        assertEquals("/fake", createdConFactory.getVirtualHost());
        assertEquals("fakeHost", createdConFactory.getHost());

    }

    @Test
    public void getObjectInstance_should_create_a_RMQDestination_QUEUE_via_environment() throws Exception {

        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.Queue");
            put("destinationName", "TEST_QUEUE");
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestQueue"), null, environment);

        assertNotNull(createdObject);
        assertEquals(RMQDestination.class, createdObject.getClass());

        RMQDestination createdDestination = (RMQDestination) createdObject;

        assertEquals("TEST_QUEUE", createdDestination.getDestinationName());
        assertEquals("TEST_QUEUE", createdDestination.getQueueName());

    }


    @Test
    public void getObjectInstance_should_create_a_RMQDestination_TOPIC_via_environment() throws Exception {

        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.Topic");
            put("destinationName", "TEST_TOPIC");
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestTopic"), null, environment);

        assertNotNull(createdObject);
        assertEquals(RMQDestination.class, createdObject.getClass());

        RMQDestination createdDestination = (RMQDestination) createdObject;

        assertEquals("TEST_TOPIC", createdDestination.getDestinationName());
        assertEquals("TEST_TOPIC", createdDestination.getTopicName());

    }


    @Test
    public void getObjectInstance_should_throw_NamingException_when_missing_required_property_via_environment() throws Exception {

        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.Queue");
            put("destinationName", "TEST_QUEUE");
            put("amqp", "true");
        }};

        try {
            rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);
        } catch (NamingException ne) {
            assertEquals("Property [amqpExchangeName] may not be null.", ne.getMessage());
        }

    }

    @Test
    public void getObjectInstance_should_return_null_object_arg_is_null() throws Exception {

        assertNull(rmqObjectFactory.getObjectInstance(null, new CompositeName("java:global/jms/TestConnectionFactory"), null, null));
    }

    @Test
    public void getObjectInstance_should_throw_NamingException_when_object_arg_is_not_a_Reference_and_environment_is_null() throws Exception {

        try {
            rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, null);
        } catch (NamingException ne) {
            assertEquals("Unable to instantiate object: obj is not a Reference instance and environment table is empty", ne.getMessage());
        }
    }

    @Test
    public void getObjectInstance_should_throw_NamingException_when_object_arg_is_not_a_Reference_and_environment_is_empty() throws Exception {

        try {
            rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, new Hashtable<Object, Object>());
        } catch (NamingException ne) {
            assertEquals("Unable to instantiate object: obj is not a Reference instance and environment table is empty", ne.getMessage());
        }
    }

    @Test
    public void getObjectInstance_should_throw_NamingException_when_object_arg_is_not_a_Reference_and_environment_className_is_missing() throws Exception {

        try {
            Hashtable<Object, Object> environment = new Hashtable<Object, Object>() {{
                put("anything but className", "some value");
            }};
            rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);
        } catch (NamingException ne) {
            assertEquals("Unable to instantiate object: type has not been specified", ne.getMessage());
        }
    }

}