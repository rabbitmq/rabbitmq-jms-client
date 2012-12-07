package com.rabbitmq.jms.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;

/**
 * Explicit receive() tests
 */
public class TestReceiveConsumer {

    private static final long TIMEOUT = 200; // ms
    private static final Envelope envelope = mock(Envelope.class);
    static {
        when(envelope.getDeliveryTag()).thenReturn(1l);
    }
    private static final Envelope anotherEnvelope = mock(Envelope.class);
    static {
        when(anotherEnvelope.getDeliveryTag()).thenReturn(2l);
    }

    private static final Channel channel = mock(Channel.class);
    private static final RMQSession session = mock(RMQSession.class);
    static {
        when(session.getChannel()).thenReturn(channel);
    }

    private static final RMQDestination destination = mock(RMQDestination.class);
    private static final RMQMessageConsumer rmqMessageConsumer = new RMQMessageConsumer(session, destination, "", false);

    private final BlockingQueue<GetResponse> blockingQueue = new LinkedBlockingQueue<GetResponse>();

    /**
     * An explicit message is exchanged successfully
     * @throws Exception if test error
     */
    @Test
    public void testReceiveConsumerSuccess() throws Exception {
//        ReceiveConsumer(Channel channel, String rmqQueueName, boolean noLocal, BlockingQueue<GetResponse> buffer, int batchingSize) {

        ReceiveConsumer consumer = new ReceiveConsumer(channel, rmqMessageConsumer.rmqQueueName(), rmqMessageConsumer.getNoLocalNoException(), blockingQueue, 1);

        DriveConsumerThread senderThread = new DriveConsumerThread(envelope, consumer);
        ReceiverThread receiverThread = new ReceiverThread(blockingQueue, envelope);
        senderThread.start();
        receiverThread.start();

        senderThread.openLatch();
        receiverThread.openLatch();

        receiverThread.join();
        senderThread.join();

        assertThreads(true, true, senderThread, receiverThread);
        verify(channel, atLeastOnce()).basicCancel(anyString());
    }

    private static void assertThreads(boolean s, boolean r, DriveConsumerThread senderThread, ReceiverThread receiverThread) {
        String errMsg = "";
        if (senderThread!=null && !senderThread.isSuccess()) {
            Exception se = senderThread.getException();
            if (null != se) se.printStackTrace();
            if (s) errMsg += "Did not send (exception=" + se + "); ";
        } else {
            if (!s) errMsg += "Sent; ";
        }
        if (receiverThread!=null && !receiverThread.isSuccess()) {
            Exception re = receiverThread.getException();
            if (null != re) re.printStackTrace();
            if (r) errMsg += "Did not receive (exception=" + re + "); ";
        } else {
            if (!r) errMsg += "Received; ";
        }
        if (!"".equals(errMsg)) fail(errMsg);
    }

    /**
     * Success even if it has to wait
     * @throws Exception if test error
     */
    @Test
    public void testSynchronousConsumerSuccessShortTimeout() throws Exception {
        ReceiveConsumer consumer = new ReceiveConsumer(channel, rmqMessageConsumer.rmqQueueName(), rmqMessageConsumer.getNoLocalNoException(), blockingQueue, 1);

        DriveConsumerThread senderThread = new DriveConsumerThread(envelope, consumer);
        ReceiverThread receiverThread = new ReceiverThread(blockingQueue, envelope);

        senderThread.start();
        receiverThread.start();

        receiverThread.openLatch();
        senderThread.openLatch();

        receiverThread.join();
        senderThread.join();

        assertThreads(true, true, senderThread, receiverThread);
        verify(channel, atLeastOnce()).basicCancel(anyString());
    }

    /**
     * In this test the receiver should timeout, since there
     * is a delay in sending, and the sender might NACK the message
     * @throws Exception if test error
     */
    @Test
    public void testSynchronousConsumerReceiverTimeout() throws Exception {

        ReceiveConsumer consumer = new ReceiveConsumer(channel, rmqMessageConsumer.rmqQueueName(), rmqMessageConsumer.getNoLocalNoException(), blockingQueue, 1);

        DriveConsumerThread senderThread = new DriveConsumerThread(envelope, consumer);
        ReceiverThread receiverThread = new ReceiverThread(blockingQueue, envelope);
        senderThread.start();
        receiverThread.start();

        receiverThread.openLatch();
        Thread.sleep(2*TIMEOUT);
        senderThread.openLatch();

        receiverThread.join();
        senderThread.join();

        assertThreads(true, false, senderThread, receiverThread);
        verify(channel, atLeastOnce()).basicCancel(anyString());
    }

    /**
     * In this test the receiver should timeout, since there
     * is a no sending at all.
     * @throws Exception if test error
     */
    @Test
    public void testSynchronousConsumerReceiverTimeoutNoSender() throws Exception {

        ReceiverThread receiverThread = new ReceiverThread(blockingQueue, envelope);
        receiverThread.start();
        receiverThread.openLatch();
        receiverThread.join();

        assertFalse("Received!", receiverThread.isSuccess());
        assertThreads(true, false, null, receiverThread);
    }

    private static class DriveConsumerThread extends Thread {
        private final Envelope env;
        private final ReceiveConsumer consumer;
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile boolean success = false;
        private volatile Exception exception = null;

        public DriveConsumerThread(Envelope envelope, ReceiveConsumer consumer) {
            this.env = envelope;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            final String fakeConsumerTag = "";
            try {
                this.latch.await();
                this.consumer.handleDelivery(fakeConsumerTag, this.env, null, null);
                this.success = true;
            } catch (Exception x) {
                x.printStackTrace(); //TODO logging implementation
                this.exception = x;
                this.success = false;
                return;
            }
            try {
                // this should work fine, and it may provoke a NACK because of buffer limitation
                this.consumer.handleDelivery(fakeConsumerTag, anotherEnvelope, null, null);
                this.consumer.handleCancelOk(fakeConsumerTag); // simulates cancellation trigger
                this.consumer.cancel();  // shouldn't have to wait for cancellation
            } catch (Exception x) {
                //this is unexpected
                this.exception = x;
                this.success = false;
                return;
            }
        }

        public void openLatch() {
            this.latch.countDown();
        }

        public boolean isSuccess() {
            return this.success;
        }

        public Exception getException() {
            return this.exception;
        }
    }

    private static class ReceiverThread extends Thread {
        private final BlockingQueue<GetResponse> blockingQueue;
        private final Envelope env;
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile boolean success = false;
        private volatile Exception exception = null;

        public ReceiverThread(BlockingQueue<GetResponse> blockingQueue, Envelope envelope) {
            this.blockingQueue = blockingQueue;
            this.env = envelope;
        }

        @Override
        public void run() {
            try {
                this.latch.await();
                GetResponse resp = this.blockingQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
                this.success = (resp!=null && resp.getEnvelope() == this.env);
            } catch (Exception x) {
                this.exception = x;
                this.success = false;
                return;
            }
        }

        public void openLatch() {
            this.latch.countDown();
        }

        public boolean isSuccess() {
            return this.success;
        }

        public Exception getException() {
            return this.exception;
        }
    }
}
