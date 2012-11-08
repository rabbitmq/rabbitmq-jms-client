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
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Implementation of a one-shot {@link Consumer}.
 * This is used to support the JMS semantics described in
 * {@link MessageConsumer#receive()} and {@link MessageConsumer#receive(long)}.
 */
class SynchronousConsumer implements Consumer, Abortable {
    private static final GetResponse ACCEPT_MSG = new GetResponse(null, null, null, 0);
    private static final GetResponse REJECT_MSG = new GetResponse(null, null, null, 0);
    private final Exchanger<GetResponse> exchanger = new Exchanger<GetResponse>();

    private final long timeout;
    private final Channel channel;
    private final AtomicBoolean useOnce = new AtomicBoolean(false);
    private final AtomicBoolean oneReceived = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private volatile String consumerTag;
    private volatile boolean aborted = false;

    SynchronousConsumer(Channel channel, TimeTracker tt) {
        this.timeout = tt.remainingMillis();
        this.channel = channel;
    }

    GetResponse receive() throws JMSException, AbortedException {
        log(this.consumerTag, "receive");

        if (!this.useOnce.compareAndSet(false, true)) {
            throw new JMSException("SynchronousConsumer.receive can be called only once.");
        }
        GetResponse response = null;
        try {
            response = this.exchanger.exchange(ACCEPT_MSG, this.timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException x) {
            log(this.consumerTag, x, "receive(exchange)");
            return null;
        } catch (InterruptedException x) {
            log(this.consumerTag, x, "receive(exchange)");
            /* Reset the thread interrupted status */
            Thread.currentThread().interrupt();
            return null;
        }
        if (response == REJECT_MSG) {
            throw new AbortedException();
        }
        log(this.consumerTag, "receive(valid-response)");
        return response;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        log(consumerTag, "handleConsumeOK");
        this.consumerTag = consumerTag;
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        log(consumerTag, "handleCancelOK");
        this.consumerTag = null;
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        log(consumerTag, "handleCancel");
        this.consumerTag = null;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        GetResponse response = new GetResponse(envelope, properties, body, 1);
        this.handleDelivery(consumerTag, response);
    }

    final void handleDelivery(String consumerTag, GetResponse response) throws IOException {
        log(consumerTag, "handleDelivery");
        if (this.cancelled.compareAndSet(false, true)) {
            try {
                this.channel.basicCancel(consumerTag);
                log(consumerTag, "handleDelivery(basicCancel)success");
            } catch (Exception x) {
                log(consumerTag, x, "handleDelivery(basicCancel)");
                // x.printStackTrace();
                //TODO logging implementation
            }
        }

        GetResponse waiter = null;
        if (this.oneReceived.compareAndSet(false, true)) {
            try {
                // give a receive() thread enough time to arrive
                waiter = this.exchanger.exchange(response, Math.min(100, this.timeout), TimeUnit.MILLISECONDS);
                log(this.consumerTag, "handleDelivery(exchange)success");
            } catch (InterruptedException x) {
                log(this.consumerTag, x, "handleDelivery(exchange)");
                // this is ok, it means we had a message
                // but no one there to receive it and got
                // interrupted
                /* Reset the thread interrupted status anyway */
                Thread.currentThread().interrupt();
            } catch (TimeoutException x) {
                log(this.consumerTag, x, "handleDelivery(exchange)");
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
        log(consumerTag, "handleShutdownSignal");
        // noop
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        log(consumerTag, "handleRecoverOK");
        // noop
    }

    boolean cancel() {
        log(this.consumerTag, "cancel");

        boolean result = this.cancelled.compareAndSet(false, true);
        if (result) {
            try {
                if (this.channel.isOpen() && this.consumerTag != null) {
                    this.channel.basicCancel(this.consumerTag);
                    log(this.consumerTag, "cancel(basicCancel)success");
                }
            } catch (ShutdownSignalException x) {
                log(this.consumerTag, x, "cancel(basicCancel)");
                //do nothing
            } catch (IOException x) {
                log(this.consumerTag, x, "cancel(basicCancel)");
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
        log(this.consumerTag, "abort");
        if (this.aborted) return;
        try {
            this.aborted = true;
            this.exchanger.exchange(REJECT_MSG, 0, TimeUnit.MILLISECONDS);
            log(this.consumerTag, "abort(exchange)success");
        } catch (InterruptedException e) {
            /* Reset the thread interrupted status */
            log(this.consumerTag, e, "abort(exchange)");
            Thread.currentThread().interrupt();
        } catch (TimeoutException te) {
            log(this.consumerTag, te, "abort(exchange)");
        }
    }

    @Override
    public void stop() {
        log(this.consumerTag, "stop");
    }

    @Override
    public void start() {
        log(this.consumerTag, "start");
    }

    private static final boolean LOGGING = true;

    private static final void log(String ctag, Exception x, String s) {
        if (LOGGING)
            log(ctag, "Exception ("+x+") in "+s);
    }

    private static final void log(String ctag, String s) {
        if (LOGGING)
            log(s+"("+ctag+")");
    }

    private static final void log(String s) {
        if (LOGGING)
            System.err.println("--->SynchronousConsumer: " + s);
    }
}
