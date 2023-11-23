// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.

package com.rabbitmq.jms.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.jms.admin.RMQDestination;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import javax.jms.JMSRuntimeException;

class DelayedMessageService {
    static final String X_DELAYED_JMS_EXCHANGE = "x-delayed-jms-message";
    static final String X_DELAY_HEADER = "x-delay";
    static final String X_DELAYED_JMS_EXCHANGE_HEADER = "delayed-exchange";

    /** Cache of exchanges which have been bound to the delayed exchange */
    private final Map<String, Boolean> delayedDestinations;
    private volatile boolean delayedExchangeDeclared;
    private Semaphore declaring = new Semaphore(1);

    public DelayedMessageService() {
        this.delayedDestinations = new ConcurrentHashMap<>();
    }
    void close() {
        delayedDestinations.clear();
    }

    String delayMessage(Channel channel, RMQDestination destination, Map<String, Object> messageHeaders, long deliveryDelayMs) {
        if (deliveryDelayMs <= 0L) return destination.getAmqpExchangeName();

        declareDelayedExchange(channel);
        delayedDestinations.computeIfAbsent(destination.getAmqpExchangeName(), destination1 -> {
            bindDestinationToDelayedExchange(channel, destination);
            return true;
        });
        messageHeaders.put(X_DELAY_HEADER, deliveryDelayMs);
        messageHeaders.put(X_DELAYED_JMS_EXCHANGE_HEADER, destination.getAmqpExchangeName());
        return X_DELAYED_JMS_EXCHANGE;
    }
    private void declareDelayedExchange(Channel channel) {
        if (delayedExchangeDeclared) {
            return;
        }

        try {
            declaring.acquire();
            if (delayedExchangeDeclared) {
                return;
            }

            Map<String, Object> args = new HashMap<>();
            args.put("x-delayed-type", "headers");
            channel.exchangeDeclare(X_DELAYED_JMS_EXCHANGE, "x-delayed-message", true,
                    false, // autoDelete
                    false, // internal
                    args); // object properties
            delayedExchangeDeclared = true;
        } catch (Exception x) {
            throw new JMSRuntimeException("Failed to declare exchange " + X_DELAYED_JMS_EXCHANGE,
                "", x);
        } finally {
            declaring.release();
        }
    }
    private void bindDestinationToDelayedExchange(Channel channel, RMQDestination destination) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(X_DELAYED_JMS_EXCHANGE_HEADER, destination.getAmqpExchangeName());
        try {
            channel.exchangeBind(destination.getAmqpExchangeName(), X_DELAYED_JMS_EXCHANGE, "", map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
