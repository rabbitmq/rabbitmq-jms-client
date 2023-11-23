// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.jms.admin;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.AddressResolver;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;
import com.rabbitmq.client.SaslConfig;
import com.rabbitmq.jms.client.AuthenticationMechanism;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.CompositeName;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class RMQConnectionFactoryTest {

    private static final Properties defaultProps = new Properties();

    static {
        RMQConnectionFactory defaultFact = new RMQConnectionFactory();
        defaultProps.setProperty("uri", defaultFact.getUri());
        defaultProps.setProperty("host", defaultFact.getHost());
        defaultProps.setProperty("password", defaultFact.getPassword());
        defaultProps.setProperty("port", "5672");
        defaultProps.setProperty("queueBrowserReadMax", "0");
        defaultProps.setProperty("onMessageTimeoutMs", "2000");
        defaultProps.setProperty("channelsQos", "-1");
        defaultProps.setProperty("ssl", "false");
        defaultProps.setProperty("terminationTimeout", "15000");
        defaultProps.setProperty("username", "guest");
        defaultProps.setProperty("virtualHost", "/");
        defaultProps.setProperty("cleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose", "false");
        defaultProps.setProperty("declareReplyToDestination", "true");
        defaultProps.setProperty("authenticationMechanism", AuthenticationMechanism.PLAIN.name());
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
        assertThat(getProps(ref)).hasSameSizeAs(defaultProps);
        assertEquals(defaultProps, getProps(ref), "Not the default properties");
    }

    @Test
    public void testUpdatedConnectionFactoryReference() throws Exception {
        RMQConnectionFactory connFactory = new RMQConnectionFactory();

        connFactory.setHost("sillyHost");
        connFactory.setPassword("my-password");
        connFactory.setPort(42);
        connFactory.setQueueBrowserReadMax(52);
        connFactory.setOnMessageTimeoutMs(62);
        connFactory.useSslProtocol();
        connFactory.setTerminationTimeout(1234567890123456789L);
        connFactory.setUsername("fred");
        connFactory.setVirtualHost("bill");
        connFactory.setAuthenticationMechanism(AuthenticationMechanism.EXTERNAL);

        Reference ref = connFactory.getReference();
        Properties newProps = getProps(ref);

        assertEquals("amqps://fred:my-password@sillyHost:42/bill", newProps.getProperty("uri"), "Not the correct uri");
        assertEquals("52", newProps.getProperty("queueBrowserReadMax"), "Not the correct queueBrowserReadMax");
        assertEquals("62", newProps.getProperty("onMessageTimeoutMs"), "Not the correct onMessageTimeoutMs");
        assertEquals(AuthenticationMechanism.EXTERNAL.name(), newProps.getProperty("authenticationMechanism"), "Not the correct authenticationMechanism");
    }

    @Test
    public void testConnectionFactoryRegeneration() throws Exception {
        RMQConnectionFactory connFactory = new RMQConnectionFactory();

        connFactory.setHost("sillyHost");
        connFactory.setPassword("my-password");
        connFactory.setPort(42);
        connFactory.setQueueBrowserReadMax(52);
        connFactory.setOnMessageTimeoutMs(66);
        connFactory.setChannelsQos(250);
        connFactory.useSslProtocol();
        connFactory.setTerminationTimeout(1234567890123456789L);
        connFactory.setUsername("fred");
        connFactory.setVirtualHost("bill");
        connFactory.setCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose(true);
        connFactory.setDeclareReplyToDestination(false);
        connFactory.setAuthenticationMechanism(AuthenticationMechanism.EXTERNAL);

        Reference ref = connFactory.getReference();

        RMQConnectionFactory newFactory = (RMQConnectionFactory) new RMQObjectFactory().createConnectionFactory(ref, new Hashtable<Object, Object>(), new CompositeName("newOne"));

        assertEquals("amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri(), "Not the correct uri");

        assertEquals("sillyHost", newFactory.getHost(), "Not the correct host");
        assertEquals("my-password", newFactory.getPassword(), "Not the correct password");
        assertEquals(42, newFactory.getPort(), "Not the correct port");
        assertEquals(52, newFactory.getQueueBrowserReadMax(), "Not the correct queueBrowserReadMax");
        assertEquals(66, newFactory.getOnMessageTimeoutMs());
        assertEquals(250, newFactory.getChannelsQos());
        assertEquals(true, newFactory.isSsl(), "Not the correct ssl");

        assertEquals(1234567890123456789L, newFactory.getTerminationTimeout(), "Not the correct terminationTimeout");

        assertEquals("fred", newFactory.getUsername(), "Not the correct username");
        assertEquals("bill", newFactory.getVirtualHost(), "Not the correct virtualHost");
        assertTrue(newFactory.isCleanUpServerNamedQueuesForNonDurableTopicsOnSessionClose());

        assertFalse((Boolean) getRMQConnectionFactoryFieldValue(newFactory, "declareReplyToDestination"));

        assertEquals(AuthenticationMechanism.EXTERNAL, getRMQConnectionFactoryFieldValue(newFactory, "authenticationMechanism"));
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
        environment.put("authenticationMechanism", AuthenticationMechanism.EXTERNAL);

        RMQConnectionFactory newFactory = (RMQConnectionFactory) new RMQObjectFactory().createConnectionFactory(null, environment, new CompositeName("newOne"));

        assertEquals("amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri(), "Not the correct uri");

        assertEquals("sillyHost", newFactory.getHost(), "Not the correct host");
        assertEquals("my-password", newFactory.getPassword(), "Not the correct password");
        assertEquals(42, newFactory.getPort(), "Not the correct port");
        assertEquals(52, newFactory.getQueueBrowserReadMax(), "Not the correct queueBrowserReadMax");
        assertEquals(true, newFactory.isSsl(), "Not the correct ssl");

        assertEquals(1234567890123456789L, newFactory.getTerminationTimeout(), "Not the correct terminationTimeout");

        assertEquals("fred", newFactory.getUsername(), "Not the correct username");
        assertEquals("bill", newFactory.getVirtualHost(), "Not the correct virtualHost");

        assertEquals(AuthenticationMechanism.EXTERNAL, getRMQConnectionFactoryFieldValue(newFactory, "authenticationMechanism"));
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
        addStringRefProperty(ref, "authenticationMechanism", AuthenticationMechanism.EXTERNAL.name());

        RMQConnectionFactory newFactory = (RMQConnectionFactory) new RMQObjectFactory().createConnectionFactory(ref, new Hashtable<Object, Object>(), new CompositeName("newOne"));

        assertEquals("sillyHost", newFactory.getHost(), "Not the correct host");
        assertEquals("my-password", newFactory.getPassword(), "Not the correct password");
        assertEquals(42, newFactory.getPort(), "Not the correct port");
        assertEquals(52, newFactory.getQueueBrowserReadMax(), "Not the correct queueBrowserReadMax");
        assertEquals(62, newFactory.getOnMessageTimeoutMs(), "Not the correct onMessageTimeoutMs");
        assertEquals(true, newFactory.isSsl(), "Not the correct ssl");

        assertEquals(1234567890123456789L, newFactory.getTerminationTimeout(), "Not the correct terminationTimeout");

        assertEquals("fred", newFactory.getUsername(), "Not the correct username");
        assertEquals("bill", newFactory.getVirtualHost(), "Not the correct virtualHost");

        assertEquals("amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri());

        assertEquals(AuthenticationMechanism.EXTERNAL, getRMQConnectionFactoryFieldValue(newFactory, "authenticationMechanism"));
    }

    @SuppressWarnings("unchecked")
    private <T> T getRMQConnectionFactoryFieldValue(RMQConnectionFactory factory, String fieldName) throws Exception {
        Field field = RMQConnectionFactory.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(factory);
    }

    TestRmqConnectionFactory rmqCf;

    AddressResolver passedInAddressResolver;

    @BeforeEach
    public void init() {
        rmqCf = new TestRmqConnectionFactory();
        passedInAddressResolver = null;
    }

    @Test
    public void shouldFailWhenOneOfUrisIsInvalid() {
        assertThrows(
            IllegalArgumentException.class,
            () -> rmqCf.setUris(asList("amqp://localhost", "invalid-amqp-uri"))
        );
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
        assertThat(resolved).hasSizeBetween(1, 3);
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
        AtomicInteger callCount = new AtomicInteger(0);
        rmqCf.setAmqpConnectionFactoryPostProcessor(cf -> callCount.incrementAndGet());
        rmqCf.createConnection();
        assertEquals(1, callCount.get());
        rmqCf.createConnection();
        assertEquals(2, callCount.get());
    }

    @Test
    public void saslConfigIsSet() throws Exception {
        AtomicReference<SaslConfig> saslConfigRef = new AtomicReference<>();
        rmqCf.setAuthenticationMechanism(AuthenticationMechanism.EXTERNAL);
        rmqCf.setAmqpConnectionFactoryPostProcessor(cf -> saslConfigRef.set(cf.getSaslConfig()));
        rmqCf.createConnection();
        assertEquals(DefaultSaslConfig.EXTERNAL, saslConfigRef.get());
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
