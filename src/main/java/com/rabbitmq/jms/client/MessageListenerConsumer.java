// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.MessageListener;

import com.rabbitmq.jms.util.RMQJMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.util.Abortable;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Class implementing a RabbitMQ {@link Consumer} to receive
 * messages and propagate them to the calling client.
 */
class MessageListenerConsumer implements Consumer, Abortable {
    private final Logger logger = LoggerFactory.getLogger(MessageListenerConsumer.class);

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
    private final boolean requeueOnMessageListenerException;
    private final boolean requeueOnTimeout;

    /**
     * True when AMQP auto-ack is true as well. Happens
     * only when the consumer listens on direct reply to
     * pseudo-queue. No ack or nack operation is then performed.
     */
    private final boolean skipAck;

    private final ReceivingContextConsumer receivingContextConsumer;

    /**
     * Constructor
     * @param messageConsumer to which this Rabbit Consumer belongs
     * @param channel Rabbit channel this Consumer uses
     * @param messageListener to call {@link MessageListener#onMessage(javax.jms.Message) onMessage(Message)} with received messages
     * @param terminationTimeout wait time (in nanoseconds) for cancel to take effect
     */
    MessageListenerConsumer(RMQMessageConsumer messageConsumer, Channel channel, MessageListener messageListener, long terminationTimeout,
                boolean requeueOnMessageListenerException, ReceivingContextConsumer receivingContextConsumer,
                boolean requeueOnTimeout) {
        if (requeueOnTimeout && !requeueOnMessageListenerException) {
            throw new IllegalArgumentException("requeueOnTimeout can be true only if requeueOnMessageListenerException is true as well");
        }
        this.messageConsumer = messageConsumer;
        this.channel = channel;
        this.messageListener = messageListener;
        this.autoAck = messageConsumer.isAutoAck();
        this.terminationTimeout = terminationTimeout;
        this.completion = new Completion();  // completed when cancelled.
        this.rejecting = this.messageConsumer.getSession().getConnection().isStopped();
        this.requeueOnMessageListenerException = requeueOnMessageListenerException;
        this.skipAck = messageConsumer.amqpAutoAck();
        this.receivingContextConsumer = receivingContextConsumer;
        this.requeueOnTimeout = requeueOnTimeout;
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
        logger.trace("consumerTag='{}'", consumerTag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCancelOk(String consumerTag) {
        logger.trace("consumerTag='{}'", consumerTag);
        this.completion.setComplete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCancel(String consumerTag) {
        logger.trace("consumerTag='{}'", consumerTag);
        this.completion.setComplete();
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        logger.trace("consumerTag='{}' envelope='{}'", consumerTag, envelope);
        if (this.rejecting) {
            long dtag = envelope.getDeliveryTag();
            logger.debug("basicNack: dtag='{}'", dtag);
            nack(dtag);
            return;
        }
        /* Wrap the incoming message in a GetResponse */
        GetResponse response = new GetResponse(envelope, properties, body, 0); // last parameter is remaining message count, which we don't know.
        try {
            long dtag = envelope.getDeliveryTag();
            if (this.messageListener != null) {
                if (this.requeueOnMessageListenerException) {
                    // requeuing in case of RuntimeException from the listener
                    // see https://github.com/rabbitmq/rabbitmq-jms-client/issues/23
                    // see section 4.5.2 of JMS 1.1 specification
                    RMQMessage msg = RMQMessage.convertMessage(this.messageConsumer.getSession(), this.messageConsumer.getDestination(),
                        response, this.receivingContextConsumer);
                    this.messageConsumer.getSession().addUncommittedTag(dtag);
                    boolean alreadyNacked = false;
                    try {
                        this.messageConsumer.getSession().deliverMessage(msg, this.messageListener);
                    } catch (DeliveryExecutor.DeliveryProcessingTimeoutException timeoutException) {
                        // happens only if requeueOnTimeout is true
                        logger.debug("nacking {} because of timeout", dtag);
                        alreadyNacked = true;
                        nack(dtag);
                    } catch(RMQMessageListenerExecutionJMSException e) {
                        if (e.getCause() instanceof RuntimeException) {
                            alreadyNacked = true;
                            nack(dtag);
                            this.abort();
                        } else {
                            throw e;
                        }
                    }
                    if (!alreadyNacked) {
                        dealWithAcknowledgments(dtag);
                    }
                } else {
                    // this is the "historical" behavior, not compliant with the spec
                    dealWithAcknowledgments(dtag);
                    RMQMessage msg = RMQMessage.convertMessage(this.messageConsumer.getSession(), this.messageConsumer.getDestination(),
                        response, this.receivingContextConsumer);
                    this.messageConsumer.getSession().addUncommittedTag(dtag);
                    this.messageConsumer.getSession().deliverMessage(msg, this.messageListener);
                }
            } else {
                // We are unable to deliver the message, nack it
                logger.debug("basicNack: dtag='{}' (null MessageListener)", dtag);
                nack(dtag);
            }
        } catch (JMSException x) {
            logger.error("Error while delivering message", x);
            throw new IOException(x);
        } catch (InterruptedException ie) {
            logger.warn("Message delivery has been interrupted", ie);
            throw new IOException("Interrupted while delivering message", ie);
        }
    }

    private void nack(long dtag) {
        if (!skipAck) {
            this.messageConsumer.getSession().explicitNack(dtag);
        }
    }

    private void dealWithAcknowledgments(long dtag) {
        if (!skipAck) {
            this.messageConsumer.dealWithAcknowledgements(this.autoAck, dtag);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        logger.trace("consumerTag='{}'", consumerTag, sig);
        // noop
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleRecoverOk(String consumerTag) {
        logger.trace("consumerTag='{}'", consumerTag);
        // noop
    }

    @Override
    public void abort() {
        try {
            if (!this.completion.isComplete()) { // not yet cancelled
                String cT = this.getConsTag();
                logger.debug("basicCancel: consumerTag='{}'", cT);
                this.channel.basicCancel(cT);
            }
        } catch (Exception e) {
            logger.debug("basicCancel threw exception", e);
        }
        this.rejecting = true;
        this.completion.setComplete();
    }

    @Override
    public void stop() {
        String cT = this.getConsTag();
        logger.trace("consumerTag='{}'", cT);
        TimeTracker tt = new TimeTracker(this.terminationTimeout, TimeUnit.NANOSECONDS);
        try {
            if (!this.completion.isComplete()) {
                logger.debug("consumerTag='{}' basicCancel:", cT);
                this.channel.basicCancel(cT);
                this.completion.waitUntilComplete(tt);
                this.clearConsTag();
            }
        } catch (TimeoutException te) {
            Thread.currentThread().interrupt();
        } catch (ShutdownSignalException sse) {
            // TODO check if basicCancel really necessary in this case.
            if (!sse.isInitiatedByApplication()) {
                logger.error("basicCancel (consumerTag='{}') threw exception", cT, sse);
                throw sse;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            if (! e.getMessage().equals("Unknown consumerTag")) {
                logger.error("basicCancel (consumerTag='{}') threw unexpected exception", cT, e);
            }
        }
    }

    @Override
    public void start() throws Exception {
        String cT = this.getConsTag();
        logger.trace("consumerTag='{}'", cT);
        this.rejecting = false;
        this.completion = new Completion();  // need a new completion object
        try {
            this.messageConsumer.basicConsume(this, cT);
        } catch (Exception e) {
            this.completion.setComplete();  // just in case someone is waiting on it
            logger.error("basicConsume (consumerTag='{}') threw exception", cT, e);
            throw new RMQJMSException("Error while starting consumer", e);
        }
    }

}
