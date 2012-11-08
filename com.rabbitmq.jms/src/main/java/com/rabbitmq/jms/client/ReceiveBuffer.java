package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Intermediate buffer between RabbitMQ queue and JMS client.
 * <p>
 * The buffer offers a blocking get, with timeout, where the buffer is populated by
 * an asynchronous RabbitMQ {@link Consumer}.  The buffer fills with messages from
 * a RabbitMQ queue, up to a certain size, prompted to do so whenever the buffer is empty.
 * A new Consumer is constructed whenever needed to populate the buffer.
 * </p>
 * <p>
 * The buffer is dedicated to a particular queue, and has a fixed 'fill size' when created.
 * If more messages than the 'fill size' try to get put in the buffer the excess messages
 * are NACKed back to RabbitMQ.
 * </p>
 * <p>
 * The blocking method <code>get()</code> only returns with <code>null</code> when either the buffer is closed,
 * or the timeout expires.
 * </p>
 * <p>
 * When the buffer is <code>close()</code>d, the messages remaining in the buffer are NACKed.
 * </p>
 */
class ReceiveBuffer {

    private final BlockingQueue<GetResponse> buffer = new LinkedBlockingQueue<GetResponse>();
    private final int batchingSize;
    private final Channel channel;
    private final RMQMessageConsumer rmqMessageConsumer;

    private final static GetResponse TIMEOUT_RESPONSE = new GetResponse(null, null, null, 0);

    private final AtomicReference<ReceiveConsumer> receiveConsumer = new AtomicReference<ReceiveConsumer>(null);
    /**
     * @param batchingSize - the intended limit of messages that can remain in the buffer.
     * @param channel - the RabbitMQ channel to use for consumers.
     * @param rmqMessageConsumer - the JMS MessageConsumer we are serving.
     */
    public ReceiveBuffer(int batchingSize, Channel channel, RMQMessageConsumer rmqMessageConsumer) {
        this.batchingSize = batchingSize;
        this.channel = channel;
        this.rmqMessageConsumer = rmqMessageConsumer;
    }

    /**
     * Get a message if one arrives in the time available.
     * @param tt - keeps track of the time
     * @return message gotten, or <code>null</code> if timeout or connection closed.
     */
    public GetResponse get(TimeTracker tt) {
        GetResponse resp = this.buffer.poll();
        while (TIMEOUT_RESPONSE==resp) { // drain old TIMEOUTs
            resp = this.buffer.poll();
        }
        if (null!=resp)
            return resp;
        this.getSomeMore(tt);
        try {
            resp = this.buffer.take(); // timeouts will pop this
            if (resp!=TIMEOUT_RESPONSE) {
                return resp;
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
        // timeouts (or interruptions) drop through to here
        return null;
    }

    private void getSomeMore(TimeTracker tt) {
        // TODO: set up a Consumer to put messages in the buffer, and die after timeout or if buffer is filled.
        // TODO: If there is a Consumer already setup, and not cancelling, adjust the timeout.
        // TODO: If the existing Consumer is cancelling, wait for it to finish before creating a new one.
        ReceiveConsumer callback = new ReceiveConsumer(this.channel, tt, this.batchingSize);
        if (receiveConsumer.compareAndSet(null, callback)) {
            try {
                this.channel.basicConsume(rmqMessageConsumer.rmqQueueName(), // queue we are listening on
                                          false, // no autoAck - we do all our own acking
                                          RMQMessageConsumer.newConsumerTag(), // generate a new unique consumer tag
                                          rmqMessageConsumer.getNoLocalNoException(), // noLocal option
                                          false, // not exclusive
                                          null, // no arguments
                                          callback); // drive this consumer
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
