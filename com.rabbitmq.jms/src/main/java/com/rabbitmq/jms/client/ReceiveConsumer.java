package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
 * A {@link Consumer} to feed messages into a receive buffer. It has control methods <code>register()</code>,
 * <code>cancel()</code>, <code>abort()</code>, <code>start()</code> and <code>stop()</code>.
 * <p>
 * This is used to support the JMS semantics described in {@link MessageConsumer#receive()} and
 * {@link MessageConsumer#receive(long)}.
 * </p>
 */
class ReceiveConsumer implements Consumer, Abortable {
    private static final GetResponse EOF_RESPONSE = new GetResponse(null, null, null, 0);

    private static final long CANCELLATION_TIMEOUT = 1000; // milliseconds

    private final int batchingSize;
    private final Channel channel;
    private final RMQMessageConsumer rmqMessageConsumer;
    private final BlockingQueue<GetResponse> buffer;

    private final Completion completion = new Completion(); // RabbitMQ called Cancel, or CancelOK.

    private final Object lock = new Object(); // synchronising lock
    @GuardedBy("lock") private String consumerTag;
    @GuardedBy("lock") private boolean aborted = false;
    @GuardedBy("lock") private boolean cancelled = false;

    ReceiveConsumer(RMQMessageConsumer rmqMessageConsumer, BlockingQueue<GetResponse> buffer, int batchingSize) {
        this.batchingSize = Math.max(batchingSize, 1); // must be at least 1
        this.rmqMessageConsumer = rmqMessageConsumer;
        this.buffer = buffer;
        this.channel = rmqMessageConsumer.getSession().getChannel();
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        synchronized (this.lock) {
            this.consumerTag = consumerTag;
        }
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        synchronized (this.lock) {
            this.consumerTag = null;
        }
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        synchronized (this.lock) {
            this.consumerTag = null;
            this.abort();
        }
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        GetResponse response = new GetResponse(envelope, properties, body, 1);
        this.handleDelivery(consumerTag, response);
    }

    final void handleDelivery(String consumerTag, GetResponse response) throws IOException {
        this.cancel(); // cancel immediately (idempotent)

        synchronized (this.lock) {
            if (!this.aborted && this.buffer.size() < this.batchingSize) {
                try {
                    this.buffer.put(response);
                    return;
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt(); // reinstate interrupt status
                    this.abort();                       // we abort if interrupted
                }
            }
            /* Drop through if we do not put message in buffer. */
            /* We never ACK any message, that is the responsibility of the caller. */
            try {
                this.channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
            } catch (IOException e) {
                this.abort();
                throw e; // RabbitMQ should close the channel
            }
        }
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        this.abort();
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        // noop
    }

    public void cancel() {
        this.cancel(false); // don't wait for completion
    }

    private void cancel(boolean wait) {
        synchronized (this.lock) {
            try {
                if (!this.cancelled) {
                    this.channel.basicCancel(this.consumerTag);
                    this.cancelled = true;
                }
            } catch (ShutdownSignalException x) {
                this.abort();
            } catch (IOException x) {
                if (!(x.getCause() instanceof ShutdownSignalException)) {
                    x.printStackTrace(); // TODO: log
                }
                this.abort();
            }
        }
        if (wait)
            try {
                completion.waitUntilComplete(new TimeTracker(CANCELLATION_TIMEOUT, TimeUnit.MILLISECONDS));
            } catch (TimeoutException e) {
                e.printStackTrace(); // TODO: log
            } catch (InterruptedException e) {
                e.printStackTrace(); // TODO: log
                this.abort();
            }
    }

    public void abort() {
        synchronized (this.lock) {
            if (this.aborted)
                return;
            this.aborted = true;
            try {
                this.buffer.put(EOF_RESPONSE);
            } catch (InterruptedException _) {
                // we are aborting anyway
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void start() {
    }

    void register() {
        try {
            this.channel.basicConsume(this.rmqMessageConsumer.rmqQueueName(), // queue we are listening on
                                      false, // no autoAck - we do all our own acking
                                      RMQMessageConsumer.newConsumerTag(), // generate a new unique consumer tag
                                      this.rmqMessageConsumer.getNoLocalNoException(), // noLocal option
                                      false, // not exclusive
                                      null, // no arguments
                                      this); // drive this consumer
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean isEOFMessage(GetResponse response) {
        return response==EOF_RESPONSE;
    }
}
