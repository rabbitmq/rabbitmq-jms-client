package com.rabbitmq.jms.client;

import java.io.IOException;

import javax.jms.MessageConsumer;

import net.jcip.annotations.GuardedBy;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.util.Abortable;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Implementation of a {@link Consumer} to feed messages into a receive buffer.
 * This is used to support the JMS semantics described in
 * {@link MessageConsumer#receive()} and {@link MessageConsumer#receive(long)}.
 */
class ReceiveConsumer implements Consumer, Abortable {
    private final int batchingSize;
    private final long timeout;
    private final Channel channel;

    private final Object lock = new Object(); // synchronising lock
    @GuardedBy("lock") private String consumerTag;
    @GuardedBy("lock") private boolean aborted = false;

    ReceiveConsumer(Channel channel, TimeTracker tt, int batchingSize) {
        this.timeout = tt.remainingMillis();
        this.channel = channel;
        this.batchingSize = batchingSize;
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        this.consumerTag = null;
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        this.consumerTag = null;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        GetResponse response = new GetResponse(envelope, properties, body, 1);
        this.handleDelivery(consumerTag, response);
    }

    final void handleDelivery(String consumerTag, GetResponse response) throws IOException {
            try {
                this.channel.basicCancel(consumerTag);
            } catch (Exception x) {
                x.printStackTrace();
                //TODO logging implementation
            }

        GetResponse waiter = null;
        // give a receive() thread enough time to arrive
        // exchanger

        if (waiter == /*ACCEPT_MSG*/null) {
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
        if (true) {
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
        return true;
    }

    public void abort() {
        if (this.aborted) return;
        this.aborted = true;
    }

    @Override
    public void stop() {
    }

    @Override
    public void start() {
    }
}
