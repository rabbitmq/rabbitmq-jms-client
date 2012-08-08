package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.jms.util.Util;

/**
 * Implementation of a one time
 */
public class SynchronousConsumer implements Consumer {
    private static final GetResponse REJECT_MSG = new GetResponse(null, null, null, 0);
    private static final GetResponse ACCEPT_MSG = new GetResponse(null, null, null, 0);

    private final Exchanger<GetResponse> exchanger = new Exchanger<GetResponse>();
    private final CountDownLatch latch = new CountDownLatch(1);
    private final long timeout;
    private final Channel channel;

    public SynchronousConsumer(Channel channel, long timeout) {
        super();
        this.timeout = timeout;
        this.channel = channel;
    }

    public GetResponse receive() throws JMSException {
        boolean timedout = true;
        try {
            timedout = !latch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException x) {
            // no op - timed out is set
        }
        GetResponse response = null;
        if (timedout) {
            try {
                exchanger.exchange(REJECT_MSG, 0, TimeUnit.MILLISECONDS);
            } catch (TimeoutException x) {
                // this is expected
            } catch (InterruptedException x) {
                // this we can ignore
            }
        } else {
            try {

                response = exchanger.exchange(ACCEPT_MSG, 0, TimeUnit.MILLISECONDS);
            } catch (TimeoutException x) {
                // this should never happen
                Util.util().handleException(x);
            } catch (InterruptedException x) {
                // this should never happen if we have 0 wait time
                Util.util().handleException(x);
            }
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
        if (latch.getCount() == 0)
            throw new IOException("Not accepting more than one message in this consumer.");
        try {
            latch.countDown();
            try {
                // give the other thread enough time to arrive
                GetResponse waiter = null;
                try {
                    waiter = exchanger.exchange(response, 100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException x) {
                    waiter = REJECT_MSG;
                }catch (TimeoutException x) {
                    waiter = REJECT_MSG;
                }
                if (waiter == ACCEPT_MSG) {
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                } else {
                    channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
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
