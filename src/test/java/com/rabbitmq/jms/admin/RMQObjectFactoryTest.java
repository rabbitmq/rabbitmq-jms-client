// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.admin;

import org.junit.jupiter.api.Test;

import javax.jms.ConnectionFactory;
import javax.naming.CompositeName;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.util.Hashtable;

import static com.rabbitmq.jms.client.RMQConnection.NO_CHANNEL_QOS;
import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(createdConFactory).hasFieldOrPropertyWithValue("keepTextMessageType", false);

    }

    @Test
    public void keepTextMessageTypePropertyIsSetOnConnectionFactory() throws Exception {
        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.ConnectionFactory");
            put("keepTextMessageType", "true");
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);

        assertNotNull(createdObject);
        assertEquals(RMQConnectionFactory.class, createdObject.getClass());
        RMQConnectionFactory createdConFactory = (RMQConnectionFactory) createdObject;
        assertThat(createdConFactory).hasFieldOrPropertyWithValue("keepTextMessageType", true);
    }

    @Test
    public void nackOnRollbackPropertyIsSetOnConnectionFactory() throws Exception {
        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.ConnectionFactory");
            put("nackOnRollback", "true");
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);

        assertNotNull(createdObject);
        assertEquals(RMQConnectionFactory.class, createdObject.getClass());
        RMQConnectionFactory createdConFactory = (RMQConnectionFactory) createdObject;
        assertThat(createdConFactory).hasFieldOrPropertyWithValue("nackOnRollback", true);
    }

    @Test
    public void urisOnRmqObjectFactoryShouldBeEnforced() throws Exception {
        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.ConnectionFactory");
            put("uris", "amqp://user:pass@host-0:10000/vhost,amqp://user:pass@host-1:10000/vhost");
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance("anything but a javax.naming.Reference", new CompositeName("java:global/jms/TestConnectionFactory"), null, environment);

        assertNotNull(createdObject);
        assertEquals(RMQConnectionFactory.class, createdObject.getClass());

        RMQConnectionFactory createdConFactory = (RMQConnectionFactory) createdObject;

        assertEquals("user", createdConFactory.getUsername());
        assertEquals("pass", createdConFactory.getPassword());
        assertEquals("vhost", createdConFactory.getVirtualHost());
        assertEquals("host-0", createdConFactory.getHost());

        assertThat(createdConFactory.getUris())
            .hasSize(2)
            .containsExactly("amqp://user:pass@host-0:10000/vhost", "amqp://user:pass@host-1:10000/vhost");
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

    @Test
    void getObjectInstanceShouldCreateDestinationWithConvertedQueueDeclareArguments() throws Exception {
        String queueDeclareArguments = "x-expires=10000,"
            + "x-message-ttl=5000,"
            + "x-dead-letter-exchange=dlq,"
            + "x-dead-letter-routing-key=dlrq,"
            + "x-dead-letter-strategy=at-least-once,"
            + "x-max-length=42,"
            + "x-max-length-bytes=420,"
            + "x-max-in-memory-length=33,"
            + "x-max-in-memory-bytes=330,"
            + "x-max-priority=5,"
            + "x-overflow=drop-head,"
            + "x-queue-mode=default,"
            + "x-queue-version=2,"
            + "x-single-active-consumer=true,"
            + "x-queue-type=stream,"
            + "x-quorum-initial-group-size=3,"
            + "x-max-age=7D,"
            + "x-stream-max-segment-size-bytes=7,"
            + "x-initial-cluster-size=5,"
            + "x-queue-leader-locator=client-local,"
            + "random-key=random-value"
            ;
        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.Queue");
            put("destinationName", "TEST_QUEUE");
            put("queueDeclareArguments", queueDeclareArguments);
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance(
            "anything but a javax.naming.Reference",
            new CompositeName("java:global/jms/TestQueue"), null, environment
        );

        assertNotNull(createdObject);
        assertEquals(RMQDestination.class, createdObject.getClass());

        RMQDestination createdDestination = (RMQDestination) createdObject;

        assertEquals("TEST_QUEUE", createdDestination.getDestinationName());
        assertEquals("TEST_QUEUE", createdDestination.getQueueName());
        assertThat(createdDestination.getQueueDeclareArguments())
            .containsEntry("x-expires", 10_000)
            .containsEntry("x-message-ttl", 5_000)
            .containsEntry("x-dead-letter-exchange", "dlq")
            .containsEntry("x-dead-letter-routing-key", "dlrq")
            .containsEntry("x-dead-letter-strategy", "at-least-once")
            .containsEntry("x-max-length", 42)
            .containsEntry("x-max-length-bytes", 420)
            .containsEntry("x-max-in-memory-length", 33)
            .containsEntry("x-max-in-memory-bytes", 330)
            .containsEntry("x-max-priority", 5)
            .containsEntry("x-overflow", "drop-head")
            .containsEntry("x-queue-mode", "default")
            .containsEntry("x-queue-version", 2)
            .containsEntry("x-single-active-consumer", true)
            .containsEntry("x-queue-type", "stream")
            .containsEntry("x-quorum-initial-group-size", 3)
            .containsEntry("x-max-age", "7D")
            .containsEntry("x-stream-max-segment-size-bytes", 7)
            .containsEntry("x-initial-cluster-size", 5)
            .containsEntry("x-queue-leader-locator", "client-local")
            .containsEntry("random-key", "random-value")
        ;
    }

    @Test
    void getObjectInstanceQueueDeclareArgumentsShouldKeepOriginalValueIfConversionFails() throws Exception {
        // the conversion will fail but we keep the original value
        String queueDeclareArguments = "x-expires=bad-value";
        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.Queue");
            put("destinationName", "TEST_QUEUE");
            put("queueDeclareArguments", queueDeclareArguments);
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance(
            "anything but a javax.naming.Reference",
            new CompositeName("java:global/jms/TestQueue"), null, environment
        );

        assertNotNull(createdObject);
        assertEquals(RMQDestination.class, createdObject.getClass());

        RMQDestination createdDestination = (RMQDestination) createdObject;

        assertEquals("TEST_QUEUE", createdDestination.getDestinationName());
        assertEquals("TEST_QUEUE", createdDestination.getQueueName());
        assertThat(createdDestination.getQueueDeclareArguments())
            .containsEntry("x-expires", "bad-value");
    }

    @Test
    void getObjectInstanceQueueDeclareArgumentsAreIgnoredForAmqpDestination() throws Exception {
        String queueDeclareArguments = "x-expires=1000";
        Hashtable<?, ?> environment = new Hashtable<Object, Object>() {{
            put("className", "javax.jms.Queue");
            put("destinationName", "TEST_QUEUE");
            put("amqp", "true");
            put("amqpExchangeName", "ex");
            put("amqpRoutingKey", "rk");
            put("amqpQueueName", "queue");
            put("queueDeclareArguments", queueDeclareArguments);
        }};

        Object createdObject = rmqObjectFactory.getObjectInstance(
            "anything but a javax.naming.Reference",
            new CompositeName("java:global/jms/TestQueue"), null, environment
        );

        assertNotNull(createdObject);
        assertEquals(RMQDestination.class, createdObject.getClass());

        RMQDestination createdDestination = (RMQDestination) createdObject;

        assertEquals("TEST_QUEUE", createdDestination.getDestinationName());
        assertEquals("TEST_QUEUE", createdDestination.getQueueName());
        assertThat(createdDestination.getQueueDeclareArguments()).isNull();
    }
}
