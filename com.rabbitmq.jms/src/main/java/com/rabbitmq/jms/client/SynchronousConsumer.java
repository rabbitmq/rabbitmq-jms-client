package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Implementation of a one time
 */
public class SynchronousConsumer implements Consumer {
    private static final GetResponse ACCEPT_MSG = new GetResponse(null, null, null, 0);

    private final Exchanger<GetResponse> exchanger = new Exchanger<GetResponse>();
    private final long timeout;
    private final Channel channel;
    private final AtomicBoolean useOnce = new AtomicBoolean(false);
    private final AtomicBoolean oneReceived = new AtomicBoolean(false);

    public SynchronousConsumer(Channel channel, long timeout) {
        super();
        this.timeout = timeout;
        this.channel = channel;
    }

    public GetResponse receive() throws JMSException {
        if (!useOnce.compareAndSet(false, true)) {
            throw new JMSException("SynchronousConsumer.receive can only be used once.");
        }
        GetResponse response = null;
        try {
            response = exchanger.exchange(ACCEPT_MSG, timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException x) {
            // this is expected
        } catch (InterruptedException x) {
            // this we can ignore
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

    public synchronized void handleDelivery(String consumerTag, GetResponse response) throws IOException {
        boolean success = false;
        try {
            // give the other thread enough time to arrive
            GetResponse waiter = null;
            try {
                if (oneReceived.compareAndSet(false, true)) {
                    success = true;
                    waiter = exchanger.exchange(response, Math.min(100, this.timeout), TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException x) {
                //this is ok, it means we had a message
                //but no one there to receive it and got
                //interrupted
            }catch (TimeoutException x) {
                
            }
            if (waiter == ACCEPT_MSG) {
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            } else {
                channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
            }
            //this shouldn't happen since handleDelivery is synchronized
            //but if it does, we need to NACK the message
            //and throw back an IOException to Rabbit
            if (!success) throw new IOException("multiple invocations of SynchronousConsumer.handleDelivery");
        } finally {
            channel.basicCancel(consumerTag);
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

    public long getTimeout() {
        return timeout;
    }

    public Channel getChannel() {
        return channel;
    }

    

}
