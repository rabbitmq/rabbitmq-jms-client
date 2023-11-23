// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rabbitmq.TestUtils.SkipIfTlsNotActivated;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

/** Integration test for hostname verification with TLS. */
@SkipIfTlsNotActivated
@EnabledForJreRange(min = JRE.JAVA_11)
public class SSLHostnameVerificationIT {

  static SSLContext sslContext;
  RMQConnectionFactory cf;

  @BeforeAll
  static void initCrypto() throws Exception {
    sslContext = SSLContext.getInstance("TLSv1.2");

    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(null, null);
    ks.setCertificateEntry("default", caCertificate());
    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(ks);
    sslContext.init(null, tmf.getTrustManagers(), null);
  }

  static X509Certificate caCertificate() throws Exception {
    return loadCertificate(caCertificateFile());
  }

  static String caCertificateFile() {
    return System.getProperty("test-tls-certs.dir", "tls-gen/basic") + "/testca/cacert.pem";
  }

  static X509Certificate loadCertificate(String file) throws Exception {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      CertificateFactory fact = CertificateFactory.getInstance("X.509");
      X509Certificate certificate = (X509Certificate) fact.generateCertificate(inputStream);
      return certificate;
    }
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
