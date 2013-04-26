/* Copyright © 2013 VMware, Inc. All rights reserved. */
package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import com.rabbitmq.jms.util.RJMSLogger;
import com.rabbitmq.jms.util.RJMSLogger.LogTemplate;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Class implementing a RabbitMQ {@link Consumer} to receive
 * messages and propagate them to the calling client.
 */
class MessageListenerConsumer implements Consumer, Abortable {
    private final RJMSLogger LOGGER = new RJMSLogger(new LogTemplate(){
        @Override
        public String template() {
            return "MessageListenerConsumer(consumerTag="+MessageListenerConsumer.this.getConsTag()+")";
        }
    });

    private final Object tagLock = new Object();
    /** The consumer tag for this RabbitMQ consumer */
    private String consTag = null; // @GuardedBy(tagLock);

    private final RMQMessageConsumer messageConsumer;
    private final Channel channel;
    private final MessageListener messageListener;
    private final boolean autoAck;
    private volatile Completion completion;
    private final long terminationTimeout;
    private volatile boolean rejecting;

    /**
     * Constructor
     * @param messageConsumer to which this Rabbit Consumer belongs
     * @param channel Rabbit channel this Consumer uses
     * @param messageListener to call {@link MessageListener#onMessage(javax.jms.Message) onMessage(Message)} with received messages
     * @param terminationTimeout wait time (in nanoseconds) for cancel to take effect
     */
    public MessageListenerConsumer(RMQMessageConsumer messageConsumer, Channel channel, MessageListener messageListener, long terminationTimeout) {
        this.messageConsumer = messageConsumer;
        this.channel = channel;
        this.messageListener = messageListener;
        this.autoAck = messageConsumer.isAutoAck();
        this.terminationTimeout = terminationTimeout;
        this.completion = new Completion();  // completed when cancelled.
        this.rejecting = this.messageConsumer.getSession().getConnection().isStopped();
    }

    private String getConsTag() {
        synchronized(tagLock) {
            if (this.consTag == null)
                this.consTag = RMQMessageConsumer.newConsumerTag();  // new consumer tag
            return this.consTag;
        }
    }

    private void clearConsTag() {
        synchronized(tagLock) {
            this.consTag = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleConsumeOk(String consumerTag) {
        LOGGER.log("handleConsumeOK");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCancelOk(String consumerTag) {
        LOGGER.log("handleCancelOK");
        this.completion.setComplete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCancel(String consumerTag) throws IOException {
        LOGGER.log("handleCancel");
        this.completion.setComplete();
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        LOGGER.log("handleDelivery", envelope);
        if (this.rejecting) {
            long dtag = envelope.getDeliveryTag();
            LOGGER.log("handleDelivery", "basicNack:rejecting", dtag);
            this.channel.basicNack(dtag, false, true);
            return;
        }
        /* Wrap the incoming message in a GetResponse */
        GetResponse response = new GetResponse(envelope, properties, body, 0); // last parameter is remaining message count, which we don't know.
        try {
            long dtag = envelope.getDeliveryTag();
            if (this.messageListener != null) {
                if (this.autoAck) { // we do the acknowledging
                    /* Subscriptions do not autoAck with RabbitMQ, so we have to do this ourselves. */
                    LOGGER.log("handleDelivery", "basicAck:", dtag);
                    this.messageConsumer.explicitAck(dtag);
                }
                // Create a javax.jms.Message object and deliver it to the listener
                this.messageConsumer.getSession().deliverMessage(this.messageConsumer.processMessage(response, this.autoAck), this.messageListener);
            } else {
                // We are unable to deliver the message, nack it
                LOGGER.log("handleDelivery", "basicNack:nullMessageListener", dtag);
                this.messageConsumer.explicitNack(dtag);
            }
        } catch (JMSException x) {
            x.printStackTrace();
            throw new IOException(x);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            throw new IOException("Interrupted while delivering message", ie);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOGGER.log("handleShutdownSignal");
        // noop
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleRecoverOk(String consumerTag) {
        LOGGER.log("handleRecoverOk");
        // noop
    }

    @Override
    public void abort() {
        LOGGER.log("abort");
        try {
            if (!this.completion.isComplete()) { // not yet cancelled
                LOGGER.log("abort", "basicCancel:");
                this.channel.basicCancel(this.getConsTag());
            }
        } catch (Exception e) {
            LOGGER.log("abort", e, "basicCancel");
            e.printStackTrace(); // for diagnostics
        }
        this.rejecting = true;
        this.completion.setComplete();
    }

    @Override
    public void stop() {
        LOGGER.log("stop");
        TimeTracker tt = new TimeTracker(this.terminationTimeout, TimeUnit.NANOSECONDS);
        try {
            if (!this.completion.isComplete()) {
                LOGGER.log("stop", "basicCancel:");
                this.channel.basicCancel(this.getConsTag());
                this.completion.waitUntilComplete(tt);
                this.clearConsTag();
            }
        } catch (TimeoutException te) {
            Thread.currentThread().interrupt();
        } catch (AlreadyClosedException acx) {
            LOGGER.log("stop", acx, "basicCancel");
            // TODO check if basicCancel really necessary in this case.
            if (!acx.isInitiatedByApplication()) {
                throw acx;
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.log("stop", e, "basicCancel");
        }
    }

    @Override
    public void start() {
        LOGGER.log("start");
        this.rejecting = false;
        this.completion = new Completion();  // need a new completion object
        try {
            this.messageConsumer.basicConsume(this, this.getConsTag());
        } catch (Exception e) {
            this.completion.setComplete();  // just in case someone is waiting on it
            e.printStackTrace(); // diagnostics
        }
    }

}