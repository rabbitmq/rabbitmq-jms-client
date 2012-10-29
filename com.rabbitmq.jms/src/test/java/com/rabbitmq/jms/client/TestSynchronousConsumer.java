package com.rabbitmq.jms.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Explicit receive() tests
 */
public class TestSynchronousConsumer {

    private static final long TIMEOUT = 200; // ms
    private static final Envelope envelope = mock(Envelope.class);
    static {
        when(envelope.getDeliveryTag()).thenReturn(1l);
    }
    private static final GetResponse TEST_RESPONSE = new GetResponse(envelope, null, null, 0);

    private final Channel channel = mock(Channel.class);

    /**
     * An explicit message is exchanged successfully
     * @throws Exception if test error
     */
    @Test
    public void testSynchronousConsumerSuccess() throws Exception {
        SynchronousConsumer consumer = new SynchronousConsumer(channel, new TimeTracker(TIMEOUT, TimeUnit.MILLISECONDS));
        CountDownLatch senderLatch = new CountDownLatch(1);
        CountDownLatch receiverLatch = new CountDownLatch(1);
        DriveConsumerThread senderThread = new DriveConsumerThread(TEST_RESPONSE, consumer, senderLatch);
        ReceiverThread receiverThread = new ReceiverThread(TEST_RESPONSE, consumer, receiverLatch);
        senderThread.start();
        receiverThread.start();

        receiverLatch.countDown();
        senderLatch.countDown();

        receiverThread.join();
        senderThread.join();

        verify(channel, atLeastOnce()).basicNack(anyLong(),anyBoolean(),anyBoolean());
        verify(channel, atLeastOnce()).basicCancel(anyString());
        assertThreads(true, true, senderThread, receiverThread);
    }

    private static void assertThreads(boolean s, boolean r, DriveConsumerThread senderThread, ReceiverThread receiverThread) {
        String errMsg = "";
        if (!senderThread.isSuccess()) {
            Exception se = senderThread.getException();
            if (null != se) se.printStackTrace();
            if (s) errMsg += "Did not send (exception=" + se + "); ";
        } else {
            if (!s) errMsg += "Sent; ";
        }
        if (!receiverThread.isSuccess()) {
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
        Channel channel = mock(Channel.class);

        SynchronousConsumer consumer = new SynchronousConsumer(channel, new TimeTracker(10, TimeUnit.MILLISECONDS));
        CountDownLatch senderLatch = new CountDownLatch(1);
        CountDownLatch receiverLatch = new CountDownLatch(1);
        DriveConsumerThread senderThread = new DriveConsumerThread(TEST_RESPONSE, consumer, senderLatch);
        ReceiverThread receiverThread = new ReceiverThread(TEST_RESPONSE, consumer, receiverLatch);
        senderThread.start();
        receiverThread.start();

        receiverLatch.countDown();
        senderLatch.countDown();

        receiverThread.join();
        senderThread.join();

        verify(channel, atLeastOnce()).basicCancel(anyString());
        assertThreads(true, true, senderThread, receiverThread);
    }

    /**
     * In this test the receiver should timeout, since there
     * is a delay in sending, and the sender must NACK the message
     * @throws Exception if test error
     */
    @Test
    public void testSynchronousConsumerReceiverTimeout() throws Exception {
        Channel channel = mock(Channel.class);

        SynchronousConsumer consumer = new SynchronousConsumer(channel, new TimeTracker(TIMEOUT, TimeUnit.MILLISECONDS));
        CountDownLatch senderLatch = new CountDownLatch(1);
        CountDownLatch receiverLatch = new CountDownLatch(1);
        DriveConsumerThread senderThread = new DriveConsumerThread(TEST_RESPONSE, consumer, senderLatch);
        ReceiverThread receiverThread = new ReceiverThread(TEST_RESPONSE, consumer, receiverLatch);
        senderThread.start();
        receiverThread.start();

        receiverLatch.countDown();
        Thread.sleep(2*TIMEOUT);
        senderLatch.countDown();

        receiverThread.join();
        senderThread.join();


        verify(channel, atLeastOnce()).basicNack(anyLong(),anyBoolean(), anyBoolean());
        verify(channel, atLeastOnce()).basicCancel(anyString());
        assertThreads(true, false, senderThread, receiverThread);
    }

    /**
     * In this test the receiver should timeout, since there
     * is a no sending at all
     * @throws Exception if test error
     */
    @Test
    public void testSynchronousConsumerReceiverTimeoutNoSender() throws Exception {
        Channel channel = mock(Channel.class);

        SynchronousConsumer consumer = new SynchronousConsumer(channel, new TimeTracker(TIMEOUT, TimeUnit.MILLISECONDS));
        CountDownLatch receiverLatch = new CountDownLatch(1);
        ReceiverThread receiverThread = new ReceiverThread(TEST_RESPONSE, consumer, receiverLatch);
        receiverThread.start();

        receiverLatch.countDown();

        receiverThread.join();

        assertFalse("Received!", receiverThread.isSuccess());
    }


    private static class DriveConsumerThread extends Thread {
        final GetResponse response;
        final SynchronousConsumer consumer;
        final CountDownLatch latch;
        volatile boolean success = false;
        volatile Exception exception = null;

        public DriveConsumerThread(GetResponse response, SynchronousConsumer consumer, CountDownLatch latch) {
            this.response = response;
            this.consumer = consumer;
            this.latch = latch;
        }

        public void run() {
            final String fakeConsumerTag = "";
            try {
                this.latch.await();
                this.consumer.handleDelivery(fakeConsumerTag, this.response);
                this.success = true;
            } catch (Exception x) {
                x.printStackTrace(); //TODO logging implementation
                this.exception = x;
                this.success = false;
                return;
            }
            try {
                // this will work fine, and it should provoke a NACK
                this.consumer.handleDelivery(fakeConsumerTag, this.response);
            } catch (Exception x) {
                //this is unexpected
                this.exception = x;
                this.success = false;
                return;
            }
        }

        public boolean isSuccess() {
            return this.success;
        }

        public Exception getException() {
            return this.exception;
        }
    }

    private static class ReceiverThread extends Thread {
        final GetResponse response;
        final SynchronousConsumer consumer;
        final CountDownLatch latch;
        volatile boolean success = false;
        volatile Exception exception = null;

        public ReceiverThread(GetResponse response, SynchronousConsumer consumer, CountDownLatch latch) {
            this.response = response;
            this.consumer = consumer;
            this.latch = latch;
        }

        public void run() {
            try {
                this.latch.await();
                this.success = (this.consumer.receive() == this.response);
            } catch (Exception x) {
                this.exception = x;
                this.success = false;
                return;
            }
        }

        public boolean isSuccess() {
            return this.success;
        }

        public Exception getException() {
            return this.exception;
        }
    }
}
