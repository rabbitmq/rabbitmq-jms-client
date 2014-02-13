/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;

class BrowsingConsumer extends DefaultConsumer {

    private final CountDownLatch latch = new CountDownLatch(1);

    private int messagesExpected;
    private final java.util.Queue<GetResponse> msgQueue;

    public BrowsingConsumer(Channel channel, int messagesExpected, java.util.Queue<GetResponse> msgQueue) {
        super(channel);
        this.messagesExpected = messagesExpected;
        this.msgQueue = msgQueue;
    }

    public boolean finishesInTime(int browsingConsumerTimeout) {
        try {
            return this.latch.await(browsingConsumerTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // reset interrupted status
            return false;
        }
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        this.latch.countDown();
    }

    @Override
    public void handleCancel(String consumerTag) {
        this.latch.countDown();
    }

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body)
    throws IOException {
        this.msgQueue.add(new GetResponse(envelope, properties, body, --this.messagesExpected));
        if (this.messagesExpected == 0) {
            this.getChannel().basicCancel(consumerTag);
        }
    }
}