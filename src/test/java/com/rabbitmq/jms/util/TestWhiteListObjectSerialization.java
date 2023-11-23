// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.util;

import com.rabbitmq.jms.client.message.RMQObjectMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class TestWhiteListObjectSerialization {
    @Test
    public void testSuccessfulSerializationWithWildcard() throws IOException, JMSException {
        expectSuccessWith(Collections.singletonList("*"));
        // effectively acts as a wildcard. "Trust no package"
        // doesn't seem to be terribly useful to support.
        expectSuccessWith(Collections.singletonList(""));
    }

    @Test
    public void testSuccessfulSerializationWithWhitelistedClass() throws IOException, JMSException {
        expectSuccessWith(Collections.singletonList("com.rabbitmq.jms.util"));
    }

    @Test
    public void testSuccessfulSerializationWithMultipleWhitelistedClasses() throws IOException, JMSException {
        expectSuccessWith(Arrays.asList("io.doesnt-match", "jp.co.doesnt-match", "com.rabbitmq.jms.util"));
    }

    @Test
    public void testSuccessfulSerializationWithNonWhitelistedClass() throws IOException, JMSException {
        expecteFailureWith(Collections.singletonList("io.l337h4x0r"), new TestKlazz("abc"));
        expecteFailureWith(Collections.singletonList("com.whatever"), new TestKlazz("abc"));
        expecteFailureWith(Collections.singletonList("---"), new TestKlazz("abc"));
        expecteFailureWith(Collections.singletonList("1234567890"), new TestKlazz("abc"));
    }

    @Test
    public void testSuccessWithPrimitiveTypes() throws IOException, JMSException {
        RMQObjectMessage om = new RMQObjectMessage();
        om.setObject("JMS");

        RMQObjectMessage.recreate(om, Collections.singletonList("/does/not/match/any/package"));
    }

    private void expecteFailureWith(List<String> patterns, TestKlazz serializedValue) throws JMSException {
        RMQObjectMessage om = new RMQObjectMessage();
        om.setObject(serializedValue);
        try {
            RMQObjectMessage.recreate(om, patterns);
            fail("Expected an exception");
        } catch (JMSException ignored) {
        }
    }


    private void expectSuccessWith(List<String> prefixes) throws JMSException {
        TestKlazz k = new TestKlazz("abc");
        RMQObjectMessage om = new RMQObjectMessage();
        om.setObject(k);

        RMQObjectMessage.recreate(om, prefixes);
    }


    @SuppressWarnings("serial")
    private static class TestKlazz implements Serializable {
        private final String text;

        TestKlazz(String text) {
            if (text == null) {
                throw new IllegalArgumentException("text must not be null");
            }
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestKlazz testKlazz = (TestKlazz) o;

            return text.equals(testKlazz.text);

        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }
    }
}
