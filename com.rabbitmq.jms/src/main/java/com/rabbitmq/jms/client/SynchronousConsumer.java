package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.util.Abortable;
import com.rabbitmq.jms.util.AbortedException;
import com.rabbitmq.jms.util.RJMSLogger;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Implementation of a one-shot {@link Consumer}.
 * This is used to support the JMS semantics described in
 * {@link MessageConsumer#receive()} and {@link MessageConsumer#receive(long)}.
 */
class SynchronousConsumer implements Consumer, Abortable {
    private final static RJMSLogger LOGGER = new RJMSLogger("SynchronousConsumer");

    private static final GetResponse ACCEPT_MSG = new GetResponse(null, null, null, 0);
    private static final GetResponse REJECT_MSG = new GetResponse(null, null, null, 0);
    private final Exchanger<GetResponse> exchanger = new Exchanger<GetResponse>();

    private final long timeout;
    private final Channel channel;
    private final AtomicBoolean useOnce = new AtomicBoolean(false);
    private final AtomicBoolean oneReceived = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private volatile String consTag;
    private volatile boolean aborted = false;

    SynchronousConsumer(Channel channel, TimeTracker tt) {
        this.timeout = tt.remainingMillis();
        this.channel = channel;
    }

    GetResponse receive() throws JMSException, AbortedException {
        LOGGER.log("receive", this.consTag);

        if (!this.useOnce.compareAndSet(false, true)) {
            throw new JMSException("SynchronousConsumer.receive can be called only once.");
        }
        GetResponse response = null;
        try {
            response = this.exchanger.exchange(ACCEPT_MSG, this.timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException x) {
            LOGGER.log("receive(exchange)", x, this.consTag);
            return null;
        } catch (InterruptedException x) {
            LOGGER.log("receive(exchange)", x, this.consTag);
            /* Reset the thread interrupted status */
            Thread.currentThread().interrupt();
            return null;
        }
        if (response == REJECT_MSG) {
            throw new AbortedException();
        }
        LOGGER.log("receive(valid-response)", this.consTag);
        return response;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        LOGGER.log("handleConsumeOK", consumerTag);
        this.consTag = consumerTag;
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        LOGGER.log("handleCancelOK", consumerTag);
        this.consTag = null;
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        LOGGER.log("handleCancel", consumerTag);
        this.consTag = null;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        GetResponse response = new GetResponse(envelope, properties, body, 1);
        this.handleDelivery(consumerTag, response);
    }

    final void handleDelivery(String consumerTag, GetResponse response) throws IOException {
        LOGGER.log("handleDelivery", consumerTag);
        if (this.cancelled.compareAndSet(false, true)) {
            try {
                this.channel.basicCancel(consumerTag);
                LOGGER.log("handleDelivery(basicCancel)success", consumerTag);
            } catch (Exception x) {
                LOGGER.log("handleDelivery(basicCancel)", x, consumerTag);
                // x.printStackTrace();
                //TODO logging implementation
            }
        }

        GetResponse waiter = null;
        if (this.oneReceived.compareAndSet(false, true)) {
            try {
                // give a receive() thread enough time to arrive
                waiter = this.exchanger.exchange(response, Math.min(100, this.timeout), TimeUnit.MILLISECONDS);
                LOGGER.log("handleDelivery(exchange)success", this.consTag);
            } catch (InterruptedException x) {
                LOGGER.log("handleDelivery(exchange)", x, this.consTag);
                // this is ok, it means we had a message
                // but no one there to receive it and got
                // interrupted
                /* Reset the thread interrupted status anyway */
                Thread.currentThread().interrupt();
            } catch (TimeoutException x) {
                LOGGER.log("handleDelivery(exchange)", x, this.consTag);
            }
        }

        if (waiter == ACCEPT_MSG) {
            /* we never ack any message, that is the responsibility of the calling thread */
        } else {
            this.channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
        }
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOGGER.log("handleShutdownSignal", consumerTag);
        // noop
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        LOGGER.log("handleRecoverOK", consumerTag);
        // noop
    }

    boolean cancel() {
        LOGGER.log("cancel", this.consTag);

        boolean result = this.cancelled.compareAndSet(false, true);
        if (result) {
            try {
                if (this.channel.isOpen() && this.consTag != null) {
                    this.channel.basicCancel(this.consTag);
                    LOGGER.log("cancel(basicCancel)success", this.consTag);
                }
            } catch (ShutdownSignalException x) {
                LOGGER.log("cancel(basicCancel)", x, this.consTag);
                //do nothing
            } catch (IOException x) {
                LOGGER.log("cancel(basicCancel)", x, this.consTag);
                if (x.getCause() instanceof ShutdownSignalException) {
                    //TODO debug logging impl
                } else {
                    x.printStackTrace();
                    //TODO logging implementation
                }
            }
        }
        return result;
    }

    public void abort() {
        LOGGER.log("abort", this.consTag);
        if (this.aborted) return;
        try {
            this.aborted = true;
            this.exchanger.exchange(REJECT_MSG, 0, TimeUnit.MILLISECONDS);
            LOGGER.log("abort(exchange)success", this.consTag);
        } catch (InterruptedException e) {
            /* Reset the thread interrupted status */
            LOGGER.log("abort(exchange)", e, this.consTag);
            Thread.currentThread().interrupt();
        } catch (TimeoutException te) {
            LOGGER.log("abort(exchange)", te, this.consTag);
        }
    }

    @Override
    public void stop() {
        LOGGER.log("stop", this.consTag);
    }

    @Override
    public void start() {
        LOGGER.log("start", this.consTag);
    }
}
