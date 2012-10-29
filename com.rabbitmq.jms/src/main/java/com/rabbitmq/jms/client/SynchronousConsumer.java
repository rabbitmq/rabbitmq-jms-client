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
    private final Completion completion = new Completion();
    private final AtomicBoolean useOnce = new AtomicBoolean(false);
    private final AtomicBoolean oneReceived = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private volatile String consumerTag;

    SynchronousConsumer(Channel channel, TimeTracker tt) {
        this.timeout = TimeUnit.NANOSECONDS.toMillis(tt.remaining());
        this.channel = channel;
    }

    GetResponse receive() throws JMSException, AbortedException {
        if (!this.useOnce.compareAndSet(false, true)) {
            throw new JMSException("SynchronousConsumer.receive can be called only once.");
        }
        GetResponse response = null;
        try {
            response = this.exchanger.exchange(ACCEPT_MSG, this.timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException x) {
            return null;
        } catch (InterruptedException x) {
            /* Reset the thread interrupted status */
            Thread.currentThread().interrupt();
        }
        if (response == REJECT_MSG) {
            throw new AbortedException();
        }
        return response;
    }

    /**
     * Returns the consumer tag used for this consumer
     * @return the consumer tag for this consumer
     */
    public String getConsumerTag() {
        return this.consumerTag;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        this.completion.setComplete();
        this.consumerTag = null;
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        this.completion.setComplete();
        this.consumerTag = null;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        GetResponse response = new GetResponse(envelope, properties, body, 1);
        this.handleDelivery(consumerTag, response);
    }

    final void handleDelivery(String consumerTag, GetResponse response) throws IOException {
        if (this.cancelled.compareAndSet(false, true)) {
            try {
                this.channel.basicCancel(consumerTag);
            } catch (Exception x) {
                x.printStackTrace();
                //TODO logging implementation
            }
        }

        GetResponse waiter = null;
        try {
            if (this.oneReceived.compareAndSet(false, true)) { // give a receive() thread enough time to arrive
                waiter = this.exchanger.exchange(response, Math.min(100, this.timeout), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException x) {
            // this is ok, it means we had a message
            // but no one there to receive it and got
            // interrupted
            /* Reset the thread interrupted status anyway */
            Thread.currentThread().interrupt();
        } catch (TimeoutException x) {

        }
        if (waiter == ACCEPT_MSG) {
            /* we never ack any message, that is the responsibility of the calling thread */
        } else {
            this.channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
        }
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        // noop
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        // noop
    }

    boolean cancel() {
        boolean result = this.cancelled.compareAndSet(false, true);
        if (result) {
            try {
                if (this.channel.isOpen() && this.consumerTag != null) {
                    this.channel.basicCancel(this.consumerTag);
                }
            } catch (ShutdownSignalException x) {
                //do nothing
            } catch (IOException x) {
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
        try {
            this.exchanger.exchange(REJECT_MSG, 1000, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            /* Reset the thread interrupted status */
            Thread.currentThread().interrupt();
        } catch (TimeoutException _) {
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void start() {
    }
}
