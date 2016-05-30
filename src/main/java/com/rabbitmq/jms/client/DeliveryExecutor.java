/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.MessageListener;

import com.rabbitmq.jms.util.RMQJMSException;

/**
 * Class to deliver messages to the <code>onMessage()</code> callback. Handles execution on a different thread, timeout
 * if execution takes too long (set on instantiation), and interrupts execution on closure or timeout. Also serialises
 * calls. There is one instance of this executor per session.
 */
public class DeliveryExecutor {

    private final class CallOnMessage implements Callable<Boolean> {
        private final RMQMessage rmqMessage;
        private final MessageListener messageListener;

        private CallOnMessage(RMQMessage rmqMessage, MessageListener messageListener) {
            this.rmqMessage = rmqMessage;
            this.messageListener = messageListener;
        }

        public Boolean call() throws Exception {
            this.messageListener.onMessage(this.rmqMessage);
            return true;
        }
    }

    /** Timeout for onMessage executions */
    private final long onMessageTimeoutMs;

    /** Executor allocated if/when onMessage calls are made; used to isolate us from potential hangs. */
    private ExecutorService onMessageExecutorService = null;
    private final Object lockOnMessageExecutorService = new Object();

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
    public void deliverMessageWithProtection(RMQMessage rmqMessage, MessageListener messageListener) throws JMSException, InterruptedException {
        try {
            this.getExecutorService().submit(new CallOnMessage(rmqMessage, messageListener)).get(this.onMessageTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            this.closeAbruptly();
            throw new RMQJMSException("onMessage took too long and was interrupted", null);
        } catch (ExecutionException e) {
            throw new RMQJMSException("onMessage threw exception", e.getCause());
        }
    }

    public void close() {
        closeExecutorService(this.takeExecutorService());
    }

    private void closeAbruptly() {
        this.takeExecutorService().shutdownNow();
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
