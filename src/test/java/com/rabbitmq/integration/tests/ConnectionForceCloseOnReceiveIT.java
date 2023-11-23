// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.client.Completion;
import com.rabbitmq.jms.util.Shell;
import org.junit.jupiter.api.Test;

import javax.jms.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration test of broker close while waiting for receive on a Topic destination.
 */
public class ConnectionForceCloseOnReceiveIT extends AbstractITTopic {

    private static final boolean TRACING = false;

    private static final String TOPIC_NAME = "test.topic." + ConnectionForceCloseOnReceiveIT.class.getCanonicalName();

    private static final long TEN_SECONDS = 10*1000; // ms
    private static final long ONE_SECOND = 1*1000; // ms
    private static final long FIVE_SECONDS = 5*1000; // ms
    private static final long ZERO_SECONDS = 0; // ms

    private AtomicBoolean atomBool = new AtomicBoolean();
    private AtomicReference<JMSException> atomJMSExceptionRef = new AtomicReference<JMSException>();

    @Test
    public void testConnectionFailDuringReceiveWithExceptionListener() throws Exception {
        ExceptionListener eListener = exception -> {
            t("onException called");
            atomBool.set(true);
            atomJMSExceptionRef.set(exception);
        };

        topicConn.setExceptionListener(eListener);

        topicConn.start(); t("started");

        TopicSession topicSession = topicConn.createTopicSession(true, Session.DUPS_OK_ACKNOWLEDGE);
        Topic topicDestination = topicSession.createTopic(TOPIC_NAME);
        MessageConsumer messageConsumer = topicSession.createConsumer(topicDestination);

        Completion receiveCompletion = new Completion();

        Thread receiver = new DelayedReceive(ZERO_SECONDS, messageConsumer, receiveCompletion);

        receiver.start(); t("started receiver");

        Thread.sleep(ONE_SECOND);

        Shell.restartNode();

        try {
            t("waiting for completion...");
            receiveCompletion.waitUntilComplete(TEN_SECONDS, TimeUnit.MILLISECONDS);
            t("...complete returned");
        } catch (TimeoutException e) {
            t("timeout waiting for receive");
            fail("Timeout before receive returns!");
        }

        if (!atomBool.get()) {
            t("exception listener was not driven during test");
            fail("ExceptionListener was not driven");
        }
    }

    private static class DelayedReceive extends Thread {
        private final long timeoutMillis;
        private final MessageConsumer subscr;
        private final Completion completion;
        private final AtomicReference<Message> result; // not expecting one

        public DelayedReceive(long timeoutMillis, MessageConsumer subscr, Completion completion) {
            this.timeoutMillis = timeoutMillis;
            this.subscr = subscr;
            this.result = new AtomicReference<>(null);
            this.completion = completion;
        }
        @Override
        public void run() {
            t("receiver running...");
            try {
                Thread.sleep(this.timeoutMillis);
                t("woken up, issuing receive");
                this.result.set(this.subscr.receive(FIVE_SECONDS));
                t("returned form receive normally");
            } catch (InterruptedException e) {
                t("interrupted");
                e.printStackTrace(); // for debugging
            } catch (JMSException e) {
                t("caught JMSException from receive");
                e.printStackTrace(); // for debugging
            } catch (Exception e) {
                t("caught Exception from receive");
                e.printStackTrace(); // for debugging
            }
            t("setting complete...");
            this.completion.setComplete();
            t("ended");
        }
    }

    private static final String t_pre = "t("+ConnectionForceCloseOnReceiveIT.class.getSimpleName()+")";

    private static void t(String format, Object...objs) {
        if (!TRACING) return;
        String threadName = Thread.currentThread().getName();
        System.out.println(String.format(String.format("%1$20d [%2$s] %3$s: %4$s", System.currentTimeMillis(), threadName, t_pre, format), objs));
    }
}
