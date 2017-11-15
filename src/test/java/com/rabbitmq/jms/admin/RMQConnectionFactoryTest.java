package com.rabbitmq.jms.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.CompositeName;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.junit.jupiter.api.Test;

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
        connFactory.setSsl(true);
        connFactory.setTerminationTimeout(1234567890123456789L);
        connFactory.setUsername("fred");
        connFactory.setVirtualHost("bill");

        Reference ref = connFactory.getReference();
        Properties newProps = getProps(ref);

        assertEquals("amqps://fred:my-password@sillyHost:42/bill", newProps.getProperty("uri"), "Not the correct uri");
        assertEquals("52", newProps.getProperty("queueBrowserReadMax"), "Not the correct queueBrowserReadMax");
        assertEquals("62", newProps.getProperty("onMessageTimeoutMs"), "Not the correct onMessageTimeoutMs");
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

        assertEquals("amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri(), "Not the correct uri");

        assertEquals("sillyHost", newFactory.getHost(), "Not the correct host");
        assertEquals("my-password", newFactory.getPassword(), "Not the correct password");
        assertEquals(42, newFactory.getPort(), "Not the correct port");
        assertEquals(52, newFactory.getQueueBrowserReadMax(), "Not the correct queueBrowserReadMax");
        assertEquals(true, newFactory.isSsl(), "Not the correct ssl");

        assertEquals(15000L, newFactory.getTerminationTimeout(), "Not the correct terminationTimeout");

        assertEquals("fred", newFactory.getUsername(), "Not the correct username");
        assertEquals("bill", newFactory.getVirtualHost(), "Not the correct virtualHost");
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

        assertEquals("amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri(), "Not the correct uri");

        assertEquals("sillyHost", newFactory.getHost(), "Not the correct host");
        assertEquals("my-password", newFactory.getPassword(), "Not the correct password");
        assertEquals(42, newFactory.getPort(), "Not the correct port");
        assertEquals(52, newFactory.getQueueBrowserReadMax(), "Not the correct queueBrowserReadMax");
        assertEquals(true, newFactory.isSsl(), "Not the correct ssl");

        assertEquals(1234567890123456789L, newFactory.getTerminationTimeout(), "Not the correct terminationTimeout");

        assertEquals("fred", newFactory.getUsername(), "Not the correct username");
        assertEquals("bill", newFactory.getVirtualHost(), "Not the correct virtualHost");
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
    }
}
