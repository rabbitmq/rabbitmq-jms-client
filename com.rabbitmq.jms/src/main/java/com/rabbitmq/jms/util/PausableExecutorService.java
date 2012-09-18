package com.rabbitmq.jms.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements a {@link ExecutorService} but allows the service to be paused.
 * <p>
 * This has the method {@link #pause()} - this method, when invoked, will allow existing threads to finish, but it will
 * not let new tasks commence until the {@link #resume()} method has been called.
 * </p>
 * <p>
 * The {@link PausableExecutorService} will continue to accept runnable tasks while paused.
 * </p>
 * <p>
 * This {@link PausableExecutorService} has an unbounded runnable queue, this is important to consider when pausing as
 * the queue can build up indefinitely.
 * </p>
 */

public class PausableExecutorService extends ThreadPoolExecutor implements ExecutorService {

    /** Timeout used when calling pause(). Default is 300000 ms == 300 s == 5 mins */
    private final static long DEFAULT_PAUSE_TIMEOUT = Long.getLong("rabbit.jms.DEFAULT_PAUSE_TIMEOUT", 300000);

    /** We suffix thread names with a number. */
    private final static AtomicLong THREAD_COUNTER = new AtomicLong(0);

    /** The latch that we use to pause, resume, and wait on resume */
    private final PauseLatch latch;

    /**
     * This latch keeps track of how many threads are currently executing
     * tasks so that we can hold the {@link #pause()} call until
     * they are finished.
     */
    private final CountUpAndDownLatch clatch = new CountUpAndDownLatch(0);

    /** The thread name prefix. */
    private volatile String serviceId = "RabbitMQ JMS Thread #";

    /**
     * Creates a {@link PausableExecutorService} with an unbounded queue
     * @param maxThreads - number of threads at peek
     * @param paused - initial state true means it starts paused
     */
    public PausableExecutorService(int maxThreads, boolean paused) {
        super(0, maxThreads, 60, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
        latch = new PauseLatch(paused);
        ThreadFactory f = new RMQThreadFactory();
        this.setThreadFactory(f);
    }

    /**
     * Creates a {@link PausableExecutorService} with the provided queue
     * @param maxThreads - number of threads at peek
     * @param paused - initial state true means it starts paused
     * @param queue -
     */
    public PausableExecutorService(int maxThreads, boolean paused, BlockingQueue<Runnable> queue) {
        super(0, maxThreads, 60, TimeUnit.SECONDS, queue);
        latch = new PauseLatch(paused);
        ThreadFactory f = new RMQThreadFactory();
        this.setThreadFactory(f);
    }

    /**
     * @return the service ID
     */
    public String getServiceId() {
        return serviceId;
    }


    /**
     * Sets the service ID. The service ID is used to name the threads
     * @param serviceId the ID to be used in naming threads, for example &quot;Rabbit JMS Thread #&quot;
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        try {
            /*
             * If we are paused, let's wait here until we resume
             */
            latch.await(DEFAULT_PAUSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException x) {
            throw new RejectedExecutionException("Thread was interrupted before executing.");
        }
        /*
         * Notify that a thread is running
         */
        clatch.countUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        /*
         * Notify that the task has completed
         */
        clatch.countDown();
        super.afterExecute(r, t);
    }

    /**
     * @return true if this executor is paused
     */
    public boolean isPaused() {
        return latch.isPaused();
    }

    /**
     * Pauses the executor. This method will not return until
     * all existing threads have finished running or the default timeout has passed.
     * @throws InterruptedException if interrupted during timed wait.
     */
    public void pause() throws InterruptedException {
        pause(DEFAULT_PAUSE_TIMEOUT);
    }

    /**
     * Pauses the executor. This method will not return until
     * all existing threads have finished running or the provided timeout has passed.
     * @param timeout time (in ms) to wait for threads to finish
     * @throws InterruptedException if interrupted during timed wait.
     */
    public void pause(long timeout) throws InterruptedException {
        //first we pause the pause latch
        latch.pause();
        //then we make sure the threads do complete
        clatch.awaitZero(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Signals that the executor service can resume.
     * If the executor is not paused, this call returns without effect.
     */
    public void resume()  {
        latch.resume();
    }

    /**
     * Private thread factory so that we can name and make threads daemon status
     */
    private final class RMQThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(getServiceId() + THREAD_COUNTER.incrementAndGet());
            return t;
        }
    }
}
