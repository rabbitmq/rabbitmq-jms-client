/* Copyright (c) 2016 Pivotal Software, Inc. All rights reserved. */

package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SslContextIT {

    Connection connection = null;

    @AfterEach public void tearDown() throws JMSException {
        if(connection != null) {
            connection.close();
        }
    }

    // https://github.com/rabbitmq/rabbitmq-jms-client/issues/12
    // the set SSLContext isn't overridden
    @Test public void sslContextShouldBeUsedWhenExplicitlySet() throws Exception {
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory(true, 0)
            .getConnectionFactory();
        connectionFactory.setUri("amqps://guest:guest@localhost:5671/%2f");
        SSLContext sslContext = createSslContext();
        AlwaysTrustTrustManager trustManager = new AlwaysTrustTrustManager();
        sslContext.init(null, new TrustManager[] {trustManager}, null);
        connectionFactory.useSslProtocol(sslContext);
        connection = connectionFactory.createConnection();
        assertTrue(trustManager.checkServerTrustedCallCount.get() >= 1, "TrustManager#checkServerTrusted must be called");
    }

    @Test public void useDefaultSslContextWhenOptionIsEnabled() throws Exception {
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory(true, 0)
            .getConnectionFactory();
        SSLContext defaultSslContext = createSslContext();
        AlwaysTrustTrustManager defaultTrustManager = new AlwaysTrustTrustManager();
        defaultSslContext.init(null, new TrustManager[] {defaultTrustManager}, null);
        SSLContext.setDefault(defaultSslContext);
        connectionFactory.useDefaultSslContext(true);
        connection = connectionFactory.createConnection();
        assertTrue(defaultTrustManager.checkServerTrustedCallCount.get() >= 1, "TrustManager#checkServerTrusted must be called");
    }

    @Test public void defaultSslContextOverridesSetSslContext() throws Exception {
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory(true, 0)
            .getConnectionFactory();
        SSLContext defaultSslContext = createSslContext();
        AlwaysTrustTrustManager defaultTrustManager = new AlwaysTrustTrustManager();
        defaultSslContext.init(null, new TrustManager[] {defaultTrustManager}, null);
        SSLContext.setDefault(defaultSslContext);
        connectionFactory.useDefaultSslContext(true);

        SSLContext sslContext = createSslContext();
        AlwaysTrustTrustManager trustManager = new AlwaysTrustTrustManager();
        sslContext.init(null, new TrustManager[] {trustManager}, null);
        connectionFactory.useSslProtocol(sslContext);

        connection = connectionFactory.createConnection();
        assertTrue(defaultTrustManager.checkServerTrustedCallCount.get() >= 1, "TrustManager#checkServerTrusted must be called");
        assertEquals(0, trustManager.checkServerTrustedCallCount.get());
    }

    private static SSLContext createSslContext() throws NoSuchAlgorithmException {
        String[] protocols = SSLContext.getDefault().getSupportedSSLParameters().getProtocols();
        String protocol = com.rabbitmq.client.ConnectionFactory.computeDefaultTlsProcotol(protocols);
        return SSLContext.getInstance(protocol);
    }

    private static class AlwaysTrustTrustManager implements X509TrustManager {

        private final AtomicInteger checkServerTrustedCallCount = new AtomicInteger();

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            checkServerTrustedCallCount.incrementAndGet();
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
