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

/**
 * Implementation of a one time Consumer used to handle the JMS semantics described in
 * {@link MessageConsumer#receive()} and {@link MessageConsumer#receive(long)}
 */
class SynchronousConsumer implements Consumer {
    private static final GetResponse ACCEPT_MSG = new GetResponse(null, null, null, 0);

    private final Exchanger<GetResponse> exchanger = new Exchanger<GetResponse>();
    private final long timeout;
    private final Channel channel;
    private final AtomicBoolean useOnce = new AtomicBoolean(false);
    private final AtomicBoolean oneReceived = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    SynchronousConsumer(Channel channel, long timeout) {
        this.timeout = timeout;
        this.channel = channel;
    }

    GetResponse receive() throws JMSException {
        if (!useOnce.compareAndSet(false, true)) {
            throw new JMSException("SynchronousConsumer.receive can be called only once.");
        }
        GetResponse response = null;
        try {
            response = exchanger.exchange(ACCEPT_MSG, timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException x) {
            // this is expected
        } catch (InterruptedException x) {
            /* Reset the thread interrupted status */
            Thread.currentThread().interrupt();
        }
        return response;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        // noop
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        // noop
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        // noop
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        GetResponse response = new GetResponse(envelope, properties, body, 1);
        handleDelivery(consumerTag,response);
    }

    final void handleDelivery(String consumerTag, GetResponse response) throws IOException {
        if (cancelled.compareAndSet(false, true)) {
            try {
                channel.basicCancel(consumerTag);
            } catch (Exception x) {
                x.printStackTrace();
                //TODO logging implementation
            }
        }

        // give a receive() thread enough time to arrive
        GetResponse waiter = null;
        try {
            if (oneReceived.compareAndSet(false, true)) {
                waiter = exchanger.exchange(response, Math.min(100, this.timeout), TimeUnit.MILLISECONDS);
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
            channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
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

    boolean cancel(String consumerTag) {
        boolean result = cancelled.compareAndSet(false, true);
        if (result) {
            try {
                if (channel.isOpen()) {
                    channel.basicCancel(consumerTag);
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
}
