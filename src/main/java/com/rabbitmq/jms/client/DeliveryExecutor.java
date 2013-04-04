/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.MessageListener;

import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Class to deliver messages to the <code>onMessage()</code> callback. Handles execution on a different thread, timeout
 * if execution takes too long (set on instantiation), and interrupts execution on closure or timeout. Also serialises
 * calls. There is one instance of this executor per session.
 */
public class DeliveryExecutor {

    /** Timeout for onMessage executions */
    private final long onMessageTimeoutMs; // 3 seconds

    /** Executor allocated if/when onMessage calls are made; used to isolate us from potential hangs. */
    public ExecutorService onMessageExecutorService = null;
    public final Object lockOnMessageExecutorService = new Object();

    public DeliveryExecutor(long onMessageTimeoutMs) {
        this.onMessageTimeoutMs = onMessageTimeoutMs;
    }

    /**
     * Method to deliver message to the client, on a separate executor thread so we can abort this if it takes too long.
     * The executor service (with one thread) is allocated one per session, as and when needed, and we reserve the right
     * to interrupt it if it takes too long to process the <code>onMessage</code> call. Within a session there should be
     * only one <code>onMessage</code> call being executed at any one time.
     *
     * @param rmqMessage the message to deliver
     * @throws JMSException if the delivery takes too long and is aborted
     */
    public void deliverMessageWithProtection(final RMQMessage rmqMessage, final MessageListener messageListener) throws JMSException, InterruptedException {
        ExecutorService es = this.getExecutorService();
        final Eventual<JMSException, Boolean> arCompleted = new Eventual<JMSException, Boolean>();
        es.execute(new Runnable(){
            public void run() {
                try {
                    messageListener.onMessage(rmqMessage);
                    arCompleted.setValue(true);
                } catch (Exception e) {
                    arCompleted.setException(new RMQJMSException("Exception thrown on delivery", e));
                }
            };
        });
        try {
            arCompleted.get(new TimeTracker(this.onMessageTimeoutMs, TimeUnit.MILLISECONDS));
        } catch (TimeoutException _) {
            closeExecutorService(es);
            throw new RMQJMSException("onMessage took too long and was interrupted", null);
        }
    }

    public void close() {
        closeExecutorService(this.takeExecutorService());
    }

    private void closeExecutorService(ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdown();
            if (!this.waitForTerminatedExecutorService(executorService)) {
                executorService.shutdownNow();
            }
        }
    }

    private ExecutorService takeExecutorService() {
        synchronized (this.lockOnMessageExecutorService) {
            ExecutorService es = this.onMessageExecutorService;
            this.onMessageExecutorService = null;
            return es;
        }
    }

    private ExecutorService getExecutorService() {
        synchronized (this.lockOnMessageExecutorService) {
            if (this.onMessageExecutorService == null) {
                this.onMessageExecutorService = Executors.newSingleThreadExecutor();
            }
            return this.onMessageExecutorService;
        }
    }

    private boolean waitForTerminatedExecutorService(ExecutorService executorService) {
        try {
            return executorService.awaitTermination(this.onMessageTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }
}