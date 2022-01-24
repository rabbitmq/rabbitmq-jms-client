// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2022 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.integration.tests;

import com.rabbitmq.TestUtils.DisabledIfTlsNotEnabled;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test for hostname verification with TLS.
 */
@DisabledIfTlsNotEnabled
public class SSLHostnameVerificationIT {

    static SSLContext sslContext;
    RMQConnectionFactory cf;

    @BeforeAll
    static void initCrypto() throws Exception {
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

    @BeforeEach
    public void init() {
        cf = new RMQConnectionFactory();
        cf.useSslProtocol(sslContext);
        cf.setHostnameVerification(true);
    }

    @Test
    public void hostnameVerificationEnabledShouldPassForLocalhost() throws JMSException {
        cf.setHost("localhost");
        Connection connection = null;
        try {
            connection = cf.createConnection();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test
    public void hostnameVerificationEnabledShouldFailForLoopbackInterface() throws JMSException {
        cf.setHost("127.0.0.1");
        assertThrows(JMSSecurityException.class, () -> cf.createConnection());
    }
}
