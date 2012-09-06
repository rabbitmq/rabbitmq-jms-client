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
 * Implements a {@link ExecutorService} but allows the service to be paused
 * This has the method {@link #pause()} - this method, when invoked, will 
 * allow existing threads to finish, but it will not let new tasks to commence until
 * the {@link #resume()} method has been called.
 * <br/>
 * The {@link PausableExecutorService} will continue to accept runnable
 * tasks while paused.
 * 
 * This {@link PausableExecutorService} has an unbounded runnable queue, this is important to consider when 
 * pausing tasks as the 
 */

public class PausableExecutorService extends ThreadPoolExecutor implements ExecutorService {
    
    /**
     * Default timeout used when calling pause() the default value is 
     * 300000 milli seconds.
     */
    public final static long DEFAULT_PAUSE_TIMEOUT = Long.getLong("rabbit.jms.DEFAULT_PAUSE_TIMEOUT", 300000);
    /**
     * We simply use this object to name threads with a number
     * this is the suffix of the thread
     */
    private final static AtomicLong THREAD_COUNTER = new AtomicLong(0);
    
    /**
     * The latch that we use to pause, resume, and wait on resume
     */
    private final PauseLatch latch;
    
    /**
     * This latch keeps track of how many threads are currently executing 
     * tasks so that we can hold the {@link #pause()} call until 
     * they are finished
     */
    private final CountUpAndDownLatch clatch = new CountUpAndDownLatch(0);
    
    /**
     * The thread name prefix
     */
    private volatile String serviceId = "RabbitMQ JMS Thread #";
    
    /**
     * Creates a {@link PausableExecutorService} with an unbounded queue
     * @param maxThreads - number of threads at peek 
     * @param paused - initial state true means it starts paused
     */
    public PausableExecutorService(int maxThreads, boolean paused) {
        super(0,maxThreads,60, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
        latch = new PauseLatch(paused);
        ThreadFactory f = new RMQThreadFactory();
        this.setThreadFactory(f);
    }
    
    public PausableExecutorService(int maxThreads, boolean paused, BlockingQueue<Runnable> queue) {
        super(0,maxThreads,60, TimeUnit.SECONDS,queue);
        latch = new PauseLatch(paused);
        ThreadFactory f = new RMQThreadFactory();
        this.setThreadFactory(f);
    }
    
    /**
     * Returns the service ID 
     * @return
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



    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        clatch.countUp();
        super.beforeExecute(t, r);
        try {
            latch.await(DEFAULT_PAUSE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException x) {
            throw new RejectedExecutionException("Thread was interrupted before executing.");
        }
    }
    
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        clatch.countDown();
        super.afterExecute(r, t);
    }
    
    /**
     * returns true if this executor is paused
     */
    public boolean isPaused() {
        return latch.isPaused();
    }

    /**
     * pauses the executor, this method will not return until
     * all existing threads have finished running or the default timeout of
     * {@link #DEFAULT_PAUSE_TIMEOUT} in milliseconds has passed
     * @throws InterruptedException
     */
    public void pause() throws InterruptedException {
        pause(DEFAULT_PAUSE_TIMEOUT);
    }
    
    /**
     * pauses the executor, this method will not return until
     * all existing threads have finished running or the provided timeout of
     * {@link #DEFAULT_PAUSE_TIMEOUT} has passed
     * @param timeout time in milliseconds to wait for threads to finish
     * @throws InterruptedException
     */
    public void pause(long timeout) throws InterruptedException {
        //first we pause the pause latch 
        latch.pause();
        //then we make sure the threads do complete
        clatch.awaitZero(timeout, TimeUnit.MILLISECONDS);
    }

    public void resume()  {
        latch.resume();
    }
    
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
