/* Copyright (c) 2018 Pivotal Software, Inc. All rights reserved. */

package com.rabbitmq.jms.admin;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.AddressResolver;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.jms.client.AmqpConnectionFactoryPostProcessor;
import org.junit.Before;
import org.junit.Test;

import javax.naming.CompositeName;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class RMQConnectionFactoryTest {

    private static final Properties defaultProps = new Properties();

    static {
        RMQConnectionFactory defaultFact = new RMQConnectionFactory();
        defaultProps.setProperty("uri", defaultFact.getUri());
        defaultProps.setProperty("queueBrowserReadMax", "0");
        defaultProps.setProperty("onMessageTimeoutMs", "2000");
    }

    private static Properties getProps(Reference ref) {
        Enumeration<RefAddr> refEnum = ref.getAll();
        Properties props = new Properties();
        while (refEnum.hasMoreElements()) {
            RefAddr ra = refEnum.nextElement();
            props.setProperty(ra.getType(), (String) ra.getContent());
        }
        return props;
    }

    /**
     * Adds a String valued property to a Reference (as a RefAddr)
     *
     * @param ref          - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value        - the value to store with the property
     */
    private static void addStringRefProperty(Reference ref,
                                             String propertyName,
                                             String value) {
        if (value == null || propertyName == null) return;
        removeRefProperty(ref, propertyName);
        RefAddr ra = new StringRefAddr(propertyName, value);
        ref.add(ra);
    }

    /**
     * Remove property from a Reference (as a RefAddr)
     *
     * @param ref          - the reference
     * @param propertyName - the name of the property to remove
     */
    private static void removeRefProperty(Reference ref,
                                          String propertyName) {
        if (propertyName == null) return;
        int numProps = ref.size();
        for (int i = 0; i < numProps; ++i) {
            RefAddr ra = ref.get(i);
            if (ra.getType().equals(propertyName)) {
                ref.remove(i--);
                numProps--;
            }
        }
    }

    @Test
    public void testDefaultConnectionFactoryReference() throws Exception {
        RMQConnectionFactory connFactory = new RMQConnectionFactory();
        Reference ref = connFactory.getReference();

        assertEquals("Not the default properties", defaultProps, getProps(ref));
    }

    @Test
    public void testUpdatedConnectionFactoryReference() throws Exception {
        RMQConnectionFactory connFactory = new RMQConnectionFactory();

        connFactory.setHost("sillyHost");
        connFactory.setPassword("my-password");
        connFactory.setPort(42);
        connFactory.setQueueBrowserReadMax(52);
        connFactory.setOnMessageTimeoutMs(62);
        connFactory.setSsl(true);
        connFactory.setTerminationTimeout(1234567890123456789L);
        connFactory.setUsername("fred");
        connFactory.setVirtualHost("bill");

        Reference ref = connFactory.getReference();
        Properties newProps = getProps(ref);

        assertEquals("Not the correct uri", "amqps://fred:my-password@sillyHost:42/bill", newProps.getProperty("uri"));
        assertEquals("Not the correct queueBrowserReadMax", "52", newProps.getProperty("queueBrowserReadMax"));
        assertEquals("Not the correct onMessageTimeoutMs", "62", newProps.getProperty("onMessageTimeoutMs"));
    }

    @Test
    public void testConnectionFactoryRegeneration() throws Exception {
        RMQConnectionFactory connFactory = new RMQConnectionFactory();

        connFactory.setHost("sillyHost");
        connFactory.setPassword("my-password");
        connFactory.setPort(42);
        connFactory.setQueueBrowserReadMax(52);
        connFactory.setSsl(true);
        connFactory.setTerminationTimeout(1234567890123456789L);
        connFactory.setUsername("fred");
        connFactory.setVirtualHost("bill");

        Reference ref = connFactory.getReference();

        RMQConnectionFactory newFactory = (RMQConnectionFactory) new RMQObjectFactory().createConnectionFactory(ref, new Hashtable<Object, Object>(), new CompositeName("newOne"));

        assertEquals("Not the correct uri", "amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri());

        assertEquals("Not the correct host", "sillyHost", newFactory.getHost());
        assertEquals("Not the correct password", "my-password", newFactory.getPassword());
        assertEquals("Not the correct port", 42, newFactory.getPort());
        assertEquals("Not the correct queueBrowserReadMax", 52, newFactory.getQueueBrowserReadMax());
        assertEquals("Not the correct ssl", true, newFactory.isSsl());

        assertEquals("Not the correct terminationTimeout", 15000L, newFactory.getTerminationTimeout());

        assertEquals("Not the correct username", "fred", newFactory.getUsername());
        assertEquals("Not the correct virtualHost", "bill", newFactory.getVirtualHost());
    }

    @Test
    public void testConnectionFactoryRegenerationViaEnvironmentProperties() throws Exception {

        Hashtable<Object, Object> environment = new Hashtable<Object, Object>();


        environment.put("host", "sillyHost");
        environment.put("password", "my-password");
        environment.put("port", 42);
        environment.put("queueBrowserReadMax", 52);
        environment.put("ssl", true);
        environment.put("terminationTimeout", 1234567890123456789L);
        environment.put("username", "fred");
        environment.put("virtualHost", "bill");

        RMQConnectionFactory newFactory = (RMQConnectionFactory) new RMQObjectFactory().createConnectionFactory(null, environment, new CompositeName("newOne"));

        assertEquals("Not the correct uri", "amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri());

        assertEquals("Not the correct host", "sillyHost", newFactory.getHost());
        assertEquals("Not the correct password", "my-password", newFactory.getPassword());
        assertEquals("Not the correct port", 42, newFactory.getPort());
        assertEquals("Not the correct queueBrowserReadMax", 52, newFactory.getQueueBrowserReadMax());
        assertEquals("Not the correct ssl", true, newFactory.isSsl());

        assertEquals("Not the correct terminationTimeout", 1234567890123456789L, newFactory.getTerminationTimeout());

        assertEquals("Not the correct username", "fred", newFactory.getUsername());
        assertEquals("Not the correct virtualHost", "bill", newFactory.getVirtualHost());
    }

    @Test
    public void testConnectionFactoryReferenceUpdated() throws Exception {
        RMQConnectionFactory connFactory = new RMQConnectionFactory();
        connFactory.setQueueBrowserReadMax(52);

        Reference ref = connFactory.getReference();

        addStringRefProperty(ref, "host", "sillyHost");
        addStringRefProperty(ref, "password", "my-password");
        addStringRefProperty(ref, "port", "42");
        addStringRefProperty(ref, "queueBrowserReadMax", "52"); // duplicates don't overwrite
        addStringRefProperty(ref, "onMessageTimeoutMs", "62");
        addStringRefProperty(ref, "ssl", "true");
        addStringRefProperty(ref, "terminationTimeout", "1234567890123456789");
        addStringRefProperty(ref, "username", "fred");
        addStringRefProperty(ref, "virtualHost", "bill");

        RMQConnectionFactory newFactory = (RMQConnectionFactory) new RMQObjectFactory().createConnectionFactory(ref, new Hashtable<Object, Object>(), new CompositeName("newOne"));

        assertEquals("Not the correct host", "sillyHost", newFactory.getHost());
        assertEquals("Not the correct password", "my-password", newFactory.getPassword());
        assertEquals("Not the correct port", 42, newFactory.getPort());
        assertEquals("Not the correct queueBrowserReadMax", 52, newFactory.getQueueBrowserReadMax());
        assertEquals("Not the correct onMessageTimeoutMs", 62, newFactory.getOnMessageTimeoutMs());
        assertEquals("Not the correct ssl", true, newFactory.isSsl());

        assertEquals("Not the correct terminationTimeout", 1234567890123456789L, newFactory.getTerminationTimeout());

        assertEquals("Not the correct username", "fred", newFactory.getUsername());
        assertEquals("Not the correct virtualHost", "bill", newFactory.getVirtualHost());

        assertEquals("Not the correct uri", "amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri());
    }

    TestRmqConnectionFactory rmqCf;

    AddressResolver passedInAddressResolver;

    @Before
    public void init() {
        rmqCf = new TestRmqConnectionFactory();
        passedInAddressResolver = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenOneOfUrisIsInvalid() throws Exception {
        rmqCf.setUris(asList("amqp://localhost", "invalid-amqp-uri"));
    }

    @Test
    public void firstUriShouldBeAppliedToGlobalSettings() throws Exception {
        rmqCf.setUris(asList("amqp://user:pass@host1:10000/vhost", "amqp://user:pass@host2:10000/vhost"));
        assertEquals("host1", rmqCf.getHost());
        assertEquals("user", rmqCf.getUsername());
        assertEquals("pass", rmqCf.getPassword());
        assertEquals(10000, rmqCf.getPort());
        assertEquals("vhost", rmqCf.getVirtualHost());
        assertFalse(rmqCf.isSsl());
    }

    @Test
    public void firstUriShouldBeAppliedToGlobalSettingsTls() throws Exception {
        rmqCf.setUris(asList("amqps://user:pass@host1:10000/vhost", "amqps://user:pass@host2:10000/vhost"));
        assertEquals("host1", rmqCf.getHost());
        assertEquals("user", rmqCf.getUsername());
        assertEquals("pass", rmqCf.getPassword());
        assertEquals(10000, rmqCf.getPort());
        assertEquals("vhost", rmqCf.getVirtualHost());
        assertTrue(rmqCf.isSsl());
    }

    @Test
    public void shouldUseSingleResolvedAddressWhenSingleUri() throws Exception {
        rmqCf.setUri("amqp://localhost:10000");
        rmqCf.createConnection("guest", "guest");
        assertNotNull(passedInAddressResolver);
        List<Address> resolved = passedInAddressResolver.getAddresses();
        assertThat(resolved.size(), is(both(greaterThanOrEqualTo(1)).and(lessThanOrEqualTo(3))));
        // don't check host, as there can be some DNS resolution happening
        assertEquals(10000, resolved.get(0).getPort());
    }

    @Test
    public void shouldUseSeveralAddressesWhenUrisIsUsed() throws Exception {
        rmqCf.setUris(asList("amqps://user:pass@host1:10000/vhost", "amqps://user:pass@host2:10000/vhost"));
        rmqCf.createConnection("user", "pass");
        assertNotNull(passedInAddressResolver);
        assertEquals(2, passedInAddressResolver.getAddresses().size());
        assertEquals("host1", passedInAddressResolver.getAddresses().get(0).getHost());
        assertEquals(10000, passedInAddressResolver.getAddresses().get(0).getPort());
        assertEquals("host2", passedInAddressResolver.getAddresses().get(1).getHost());
        assertEquals(10000, passedInAddressResolver.getAddresses().get(1).getPort());
    }

    @Test public void amqpConnectionFactoryIsCalled() throws Exception {
        final AtomicInteger callCount = new AtomicInteger(0);
        rmqCf.setAmqpConnectionFactoryPostProcessor(new AmqpConnectionFactoryPostProcessor() {

            @Override
            public void postProcess(ConnectionFactory cf) {
                callCount.incrementAndGet();
            }
        });
        rmqCf.createConnection();
        assertEquals(1, callCount.get());
        rmqCf.createConnection();
        assertEquals(2, callCount.get());
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        RMQConnectionFactory cf = new RMQConnectionFactory();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(cf);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    class TestRmqConnectionFactory extends RMQConnectionFactory {

        @Override
        protected ConnectionFactory createConnectionFactory() {
            return new ConnectionFactory() {

                @Override
                public Connection newConnection(ExecutorService executor, AddressResolver addressResolver, String clientProvidedName) {
                    passedInAddressResolver = addressResolver;
                    return mock(Connection.class);
                }
            };
        }
    }
}
