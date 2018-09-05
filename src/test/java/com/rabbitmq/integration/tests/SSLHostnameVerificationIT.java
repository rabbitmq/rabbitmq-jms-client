/* Copyright (c) 2018 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Integration test for hostname verification with TLS.
 */
@RunWith(Parameterized.class)
public class SSLHostnameVerificationIT {

    static SSLContext sslContext;
    static AtomicInteger hostnameVerifierCalls;
    @Parameterized.Parameter(0)
    public ConnectionFactoryCustomizer customizer;
    @Parameterized.Parameter(1)
    public Runnable assertion;
    RMQConnectionFactory cf;

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[] {
            new Object[] { enableHostnameVerification(), expectedCallsOnHostnameVerifierAssertion(0) },
            new Object[] { useCustomHostnameVerifier(), expectedCallsOnHostnameVerifierAssertion(1) }
        };
    }

    private static Runnable expectedCallsOnHostnameVerifierAssertion(final int expectedCount) {
        return new Runnable() {

            @Override
            public void run() {
                assertEquals(expectedCount, hostnameVerifierCalls.get());
            }
        };
    }

    private static ConnectionFactoryCustomizer useCustomHostnameVerifier() {
        return new ConnectionFactoryCustomizer() {

            @Override
            public void customize(RMQConnectionFactory connectionFactory) {
                final DefaultHostnameVerifier delegate = new DefaultHostnameVerifier();
                HostnameVerifier verifier = new HostnameVerifier() {

                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        hostnameVerifierCalls.incrementAndGet();
                        return delegate.verify(hostname, session);
                    }
                };
                connectionFactory.setHostnameVerifier(verifier);
            }
        };
    }

    private static ConnectionFactoryCustomizer enableHostnameVerification() {
        return new ConnectionFactoryCustomizer() {

            @Override
            public void customize(RMQConnectionFactory connectionFactory) {
                connectionFactory.setHostnameVerification(true);
            }
        };
    }

    @BeforeClass
    public static void initCrypto() throws Exception {
        String keystorePath = System.getProperty("test-keystore.ca");
        assertNotNull(keystorePath);
        String keystorePasswd = System.getProperty("test-keystore.password");
        assertNotNull(keystorePasswd);
        char[] keystorePassword = keystorePasswd.toCharArray();

        KeyStore tks = KeyStore.getInstance("JKS");
        tks.load(new FileInputStream(keystorePath), keystorePassword);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(tks);

        String p12Path = System.getProperty("test-client-cert.path");
        assertNotNull(p12Path);
        String p12Passwd = System.getProperty("test-client-cert.password");
        assertNotNull(p12Passwd);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        char[] p12Password = p12Passwd.toCharArray();
        ks.load(new FileInputStream(p12Path), p12Password);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, p12Password);

        sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    }

    @Before
    public void init() {
        cf = new RMQConnectionFactory();
        cf.useSslProtocol(sslContext);
        hostnameVerifierCalls = new AtomicInteger(0);
    }

    @Test
    public void hostnameVerificationEnabledShouldPassForLocalhost() throws JMSException {
        cf.setHost("localhost");
        customizer.customize(cf);
        Connection connection = null;
        try {
            connection = cf.createConnection();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        assertion.run();
    }

    @Test(expected = JMSSecurityException.class)
    public void hostnameVerificationEnabledShouldFailForLoopbackInterface() throws JMSException {
        cf.setHost("127.0.0.1");
        customizer.customize(cf);
        cf.createConnection();
    }

    interface ConnectionFactoryCustomizer {

        void customize(RMQConnectionFactory connectionFactory);
    }
}
