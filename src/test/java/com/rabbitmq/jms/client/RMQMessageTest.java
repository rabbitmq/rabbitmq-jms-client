// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2014-2022 VMware, Inc. or its affiliates. All rights reserved.
package com.rabbitmq.jms.client;

import com.rabbitmq.jms.client.message.RMQTextMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class RMQMessageTest {

    @Test
    @DisplayName("RMQMessage::toAmqpHeaders should convert all properties to amqp headers")
    void convertsAllTypesToAmqpHeaders() throws JMSException, IOException {
        RMQMessage textMessage = new RMQTextMessage();
        textMessage.setJMSDeliveryMode(1);
        textMessage.setJMSPriority(1);
        textMessage.setStringProperty("string", "string");
        textMessage.setIntProperty("int", 42);
        textMessage.setFloatProperty("float", 1337f);
        textMessage.setDoubleProperty("double", 3.14d);
        textMessage.setLongProperty("long", 1L);
        textMessage.setShortProperty("short", (short) 4);
        textMessage.setByteProperty("byte", (byte) 2);
        textMessage.setBooleanProperty("boolean", true);

        Map<String, Object> amqpHeaders = textMessage.toAmqpHeaders();

        assertThat(amqpHeaders).contains(
                entry("string", "string"),
                entry("int", 42),
                entry("float", 1337f),
                entry("double", 3.14d),
                entry("long", 1L),
                entry("short", (short) 4),
                entry("byte", (byte) 2),
                entry("boolean", true)
        );
    }

}