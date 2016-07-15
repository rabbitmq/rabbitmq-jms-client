package com.rabbitmq.jms.admin;

import static org.junit.Assert.assertEquals;

import java.util.Enumeration;
import java.util.Properties;

import javax.naming.CompositeName;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.junit.Test;

public class RMQConnectionFactoryTest {

    private static final Properties defaultProps = new Properties();
    static {
        RMQConnectionFactory defaultFact = new RMQConnectionFactory();
        defaultProps.setProperty("uri", defaultFact.getUri());
        defaultProps.setProperty("queueBrowserReadMax", "0");
    }

    private static Properties getProps(Reference ref) {
        Enumeration<RefAddr> refEnum = ref.getAll();
        Properties props = new Properties();
        while (refEnum.hasMoreElements()) {
            RefAddr ra = refEnum.nextElement();
            props.setProperty(ra.getType(), (String)ra.getContent());
        }
        return props;
    }

    /**
     * Adds a String valued property to a Reference (as a RefAddr)
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static void addStringRefProperty(Reference ref,
                                             String propertyName,
                                             String value) {
        if (value==null || propertyName==null) return;
        removeRefProperty(ref, propertyName);
        RefAddr ra = new StringRefAddr(propertyName, value);
        ref.add(ra);
    }

    /**
     * Remove property from a Reference (as a RefAddr)
     * @param ref - the reference
     * @param propertyName - the name of the property to remove
     */
    private static void removeRefProperty(Reference ref,
                                          String propertyName) {
        if (propertyName==null) return;
        int numProps = ref.size();
        for (int i=0; i < numProps; ++i) {
            RefAddr ra = ref.get(i);
            if (ra.getType().equals(propertyName)) {
                ref.remove(i--); numProps--;
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
        connFactory.setSsl(true);
        connFactory.setTerminationTimeout(1234567890123456789L);
        connFactory.setUsername("fred");
        connFactory.setVirtualHost("bill");

        Reference ref = connFactory.getReference();
        Properties newProps = getProps(ref);

        assertEquals("Not the correct uri", "amqps://fred:my-password@sillyHost:42/bill", newProps.getProperty("uri"));
        assertEquals("Not the correct queueBrowserReadMax", "52", newProps.getProperty("queueBrowserReadMax"));
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

        RMQConnectionFactory newFactory = (RMQConnectionFactory) new RMQObjectFactory().createConnectionFactory(ref, new CompositeName("newOne"));

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
    public void testConnectionFactoryReferenceUpdated() throws Exception {
        RMQConnectionFactory connFactory = new RMQConnectionFactory();
        connFactory.setQueueBrowserReadMax(52);

        Reference ref = connFactory.getReference();

        addStringRefProperty(ref, "host", "sillyHost");
        addStringRefProperty(ref, "password", "my-password");
        addStringRefProperty(ref, "port", "42");
        addStringRefProperty(ref, "queueBrowserReadMax", "52"); // duplicates don't overwrite
        addStringRefProperty(ref, "ssl", "true");
        addStringRefProperty(ref, "terminationTimeout","1234567890123456789");
        addStringRefProperty(ref, "username", "fred");
        addStringRefProperty(ref, "virtualHost", "bill");

        RMQConnectionFactory newFactory = (RMQConnectionFactory) new RMQObjectFactory().createConnectionFactory(ref, new CompositeName("newOne"));

        assertEquals("Not the correct host", "sillyHost", newFactory.getHost());
        assertEquals("Not the correct password", "my-password", newFactory.getPassword());
        assertEquals("Not the correct port", 42, newFactory.getPort());
        assertEquals("Not the correct queueBrowserReadMax", 52, newFactory.getQueueBrowserReadMax());
        assertEquals("Not the correct ssl", true, newFactory.isSsl());

        assertEquals("Not the correct terminationTimeout", 1234567890123456789L, newFactory.getTerminationTimeout());

        assertEquals("Not the correct username", "fred", newFactory.getUsername());
        assertEquals("Not the correct virtualHost", "bill", newFactory.getVirtualHost());

        assertEquals("Not the correct uri", "amqps://fred:my-password@sillyHost:42/bill", newFactory.getUri());
    }
}
