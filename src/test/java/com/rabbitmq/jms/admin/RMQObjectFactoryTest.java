// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2020 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.admin;

import org.junit.jupiter.api.Test;

import javax.jms.ConnectionFactory;
import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.Hashtable;

import static com.rabbitmq.jms.client.RMQConnection.NO_CHANNEL_QOS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * RMQObjectFactory unit test
 *
 * @author Rémi Bantos
 */
public class RMQObjectFactoryTest {

    private RMQObjectFactory rmqObjectFactory = new RMQObjectFactory();

    @Test
    public void getObjectInstanceShouldCreateAMQPConnectionFactoryViaReference() throws Exception {

        Reference ref = new Reference(ConnectionFactory.class.getName());

        Object createdObject = rmqObjectFactory.getObjectInstance(ref, new CompositeName("java:global/jms/TestConnectionFactory"), null, null);

        assertNotNull(createdObject);
        assertEquals(RMQConnectionFactory.class, createdObject.getClass());

        RMQConnectionFactory createdConFactory = (RMQConnectionFactory) createdObject;

        assertEquals("guest", createdConFactory.getUsername());
        assertEquals("guest", createdConFactory.getPassword());
        assertEquals("/", createdConFactory.getVirtualHost());
        assertEquals("localhost", createdConFactory.getHost());
        assertEquals(NO_CHANNEL_QOS, createdConFactory.getChannelsQos());

    }


    @Test
    public void getObjectInstanceShouldCreateAMQPConnectionFactoryViaEnvironment() throws Exception {

        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.ConnectionFactory");
            put("username", "remi");
            put("password", "1234");
            put("virtualHost", "/fake");
            put("host", "fakeHost");
            put("channelsQos", 10);
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);

        assertNotNull(createdObject);
        assertEquals(RMQConnectionFactory.class, createdObject.getClass());

        RMQConnectionFactory createdConFactory = (RMQConnectionFactory) createdObject;

        assertEquals("remi", createdConFactory.getUsername());
        assertEquals("1234", createdConFactory.getPassword());
        assertEquals("/fake", createdConFactory.getVirtualHost());
        assertEquals("fakeHost", createdConFactory.getHost());
        assertEquals(10, createdConFactory.getChannelsQos());

    }

    @Test
    public void getObjectInstanceShouldCreateAMQPDestinationQUEUEViaEnvironment() throws Exception {

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
    public void getObjectInstanceShouldCreateAMQPDestinationTOPICViaEnvironment() throws Exception {

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
    public void getObjectInstanceShouldThrowNamingExceptionWhenMissingRequiredPropertyViaEnvironment() throws Exception {

        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.Queue");
            put("amqp", "true");
        }};

        try {
            rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);
            fail("Should have thrown a NamingException");
        } catch (NamingException ne) {
            assertEquals("Property [destinationName] may not be null.", ne.getMessage());
        }

    }

    @Test
    public void getObjectInstanceShouldReturnNullObjectWhenObjectArgIsNull() throws Exception {

        assertNull(rmqObjectFactory.getObjectInstance(null, new CompositeName("java:global/jms/TestConnectionFactory"), null, null));
    }

    @Test
    public void getObjectInstanceShouldThrowNamingExceptionWhenObjectArgIsNotAReferenceAndEnvironmentIsNull() throws Exception {

        try {
            rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, null);
            fail("Should have thrown a NamingException");
        } catch (NamingException ne) {
            assertEquals("Unable to instantiate object: obj is not a Reference instance and environment table is empty", ne.getMessage());
        }
    }

    @Test
    public void getObjectInstanceShouldThrowNamingExceptionWhenObjectArgIsNotAReferenceAndEnvironmentIsempty() throws Exception {

        try {
            rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, new Hashtable<Object, Object>());
            fail("Should have thrown a NamingException");
        } catch (NamingException ne) {
            assertEquals("Unable to instantiate object: obj is not a Reference instance and environment table is empty", ne.getMessage());
        }
    }

    @Test
    public void getObjectInstanceShouldThrowNamingExceptionWhenObjectArgIsNotAReferenceAndEnvironmentClassNameIsMissing() throws Exception {

        try {
            Hashtable<Object, Object> environment = new Hashtable<Object, Object>() {{
                put("anything but className", "some value");
            }};
            rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);
            fail("Should have thrown a NamingException");
        } catch (NamingException ne) {
            assertEquals("Unable to instantiate object: type has not been specified", ne.getMessage());
        }
    }

}
