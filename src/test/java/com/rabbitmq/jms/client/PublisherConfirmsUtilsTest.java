// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2019-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import com.rabbitmq.client.Channel;
import javax.jms.CompletionListener;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PublisherConfirmsUtilsTest {

    private static final CompletionListener NO_OP_COMPLETION_LISTENER = new CompletionListener() {
        @Override
        public void onCompletion(Message message) {

        }

        @Override
        public void onException(Message message, Exception exception) {

        }
    };

    static TextMessage message(String body) throws Exception {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(body);
        return message;
    }

    static int toInt(Message message) {
        try {
            return Integer.valueOf(((TextMessage) message).getText());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void configurePublisherConfirmsSupport() throws Exception {
        Channel channel = mock(Channel.class);
        AtomicReference<com.rabbitmq.client.ConfirmListener> amqpConfirmListener = new AtomicReference<>();
        doAnswer(invocation -> {
            amqpConfirmListener.set(invocation.getArgument(0, com.rabbitmq.client.ConfirmListener.class));
            return null;
        }).when(channel).addConfirmListener(any(com.rabbitmq.client.ConfirmListener.class));

        int messageCount = 50;

        List<Integer> acked = Collections.synchronizedList(new ArrayList<>());
        List<Integer> nacked = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(messageCount);
        ConfirmListener confirmListener = context -> {
            if (context.isAck()) {
                acked.add(toInt(context.getMessage()));
            } else {
                nacked.add(toInt(context.getMessage()));
            }
            latch.countDown();
        };

        BlockingQueue<TextMessage> messagesSentToServer = new LinkedBlockingQueue<>();
        new Thread(() -> {
            AtomicLong serverPublishingSequence = new AtomicLong(1);
            while (true) {
                try {
                    TextMessage message = messagesSentToServer.poll(10, TimeUnit.SECONDS);
                    int body = Integer.valueOf(message.getText());
                    long sequenceNumber = serverPublishingSequence.getAndIncrement();
                    if (body <= 10) {
                        // ack 1-10 individually
                        amqpConfirmListener.get().handleAck(sequenceNumber, false);
                    } else if (body == 20) {
                        // ack 11-20 in batch
                        amqpConfirmListener.get().handleAck(sequenceNumber, true);
                    } else if (body == 25) {
                        // nack 25 individually
                        amqpConfirmListener.get().handleNack(sequenceNumber, false);
                    } else if (body == 30) {
                        // ack 21-30 in batch (- 25, which has been nack-ed)
                        amqpConfirmListener.get().handleAck(sequenceNumber, true);
                    } else if (body == 35) {
                        // ack 35 individually
                        amqpConfirmListener.get().handleAck(sequenceNumber, false);
                    } else if (body == 40) {
                        // nack 31-40 in batch (- 35, which has been ack-ed)
                        amqpConfirmListener.get().handleNack(sequenceNumber, true);
                    } else if (body >= 41) {
                        // nack 41-50 individually
                        amqpConfirmListener.get().handleNack(sequenceNumber, false);
                    }
                    if (body == messageCount) {
                        return;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        PublishingListener publishingListener = PublisherConfirmsUtils.configurePublisherConfirmsSupport(
                channel, confirmListener
        );
        AtomicLong clientPublishingSequence = new AtomicLong(1);

        for (int i = 1; i <= messageCount; i++) {
            TextMessage message = message(String.valueOf(i));
            publishingListener.publish(message, NO_OP_COMPLETION_LISTENER,
                clientPublishingSequence.getAndIncrement());
            messagesSentToServer.offer(message);
        }

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(acked.size() + nacked.size()).isEqualTo(messageCount);
        assertThat(acked).hasSize(30)
                .containsAll(IntStream.range(1, 30).filter(i -> i != 25).boxed().collect(Collectors.toList()))
                .contains(30, 35);
        assertThat(nacked).hasSize(20)
                .contains(25)
                .containsAll(IntStream.range(31, 50).filter(i -> i != 35).boxed().collect(Collectors.toList()));
    }

}
