package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MessageListener;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.util.Abortable;

/**
 * Class implementing a RabbitMQ Consumer to receive
 * messages and propagate them to the calling client.
 */
class MessageListenerConsumer implements Consumer, Abortable {
    /**
     * The consumer tag for this RabbitMQ consumer
     */
    private volatile String consumerTag;

    private final RMQMessageConsumer messageConsumer;
    private final Channel channel;
    private final boolean autoAck;
    private final Completion completion = new Completion();
    private final long terminationTimeout;
    private volatile boolean rejecting = false;

    /**
     * Constructor
     * @param messageConsumer
     * @param channel
     * @param terminationTimeout
     */
    public MessageListenerConsumer(RMQMessageConsumer messageConsumer, Channel channel, long terminationTimeout) {
        this.messageConsumer = messageConsumer;
        this.channel = channel;
        this.autoAck = messageConsumer.isAutoAck();
        this.terminationTimeout = terminationTimeout;
    }

    /**
     * Returns the consumer tag used for this consumer
     * @return the consumer tag for this consumer
     */
    public String getConsumerTag() {
        return this.consumerTag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleConsumeOk(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCancelOk(String consumerTag) {
        this.consumerTag = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCancel(String consumerTag) throws IOException {
        this.consumerTag = null;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        /* Assign the consumer tag, we are not reusing Consumer objects for different subscriptions this is a safe
         * to do */
        if (this.consumerTag==null) this.consumerTag = consumerTag;
        if (this.rejecting) {
            this.channel.basicNack(envelope.getDeliveryTag(), false, true);
            return;
        }
        /* Wrap the incoming message in a GetResponse */
        GetResponse response = new GetResponse(envelope, properties, body, 0);
        try {
            MessageListener messageListener = this.messageConsumer.getMessageListener();
            if (messageListener != null) {
                boolean acked = this.autoAck;
                if (this.autoAck) {
                    try {
                        /* Subscriptions we never auto ack with RabbitMQ, so we have to do this ourselves. */
                        this.channel.basicAck(envelope.getDeliveryTag(), false);
                        /* Mark message as acked */
                        acked = true;
                    } catch (AlreadyClosedException x) {
                        //TODO logging impl warn message
                        //this is problematic, we have received a message, but we can't ACK it to the server
                        x.printStackTrace();
                        //TODO should we deliver the message at this time, knowing that we can't ack it?
                        //My recommendation is that we bail out here and not proceed
                    }
                }
                // Create a javax.jms.Message object and deliver it to the client
                messageListener.onMessage(messageConsumer.processMessage(response, acked));
            } else {
                try {
                    // We are unable to deliver the message, nack it
                    this.channel.basicNack(envelope.getDeliveryTag(), false, true);
                } catch (AlreadyClosedException x) {
                    //TODO logging impl debug message
                    //this is fine. we didn't ack the message in the first place
                }
            }
        } catch (JMSException x) {
            x.printStackTrace(); //TODO logging implementation
            throw new IOException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        // noop
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleRecoverOk(String consumerTag) {
        // noop
    }

    @Override
    public void abort() {
        try {
            this.channel.basicCancel(this.consumerTag);
        } catch (Exception _) {
        }
        this.rejecting = true;
        this.completion.setComplete();
    }

    @Override
    public void stop() {
        try {
            this.channel.basicCancel(this.consumerTag);
            this.completion.waitUntilComplete(this.terminationTimeout, TimeUnit.NANOSECONDS);
        } catch (AlreadyClosedException ace) {
            // TODO check if basicCancel really necessary in this case.
            if (!ace.isInitiatedByApplication()) {
                throw ace;
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        } catch (Exception _) {
        }
    }

    @Override
    public void start() {
        try {
            this.messageConsumer.basicConsume(this);
        } catch (Exception _) {
        }
    }

}