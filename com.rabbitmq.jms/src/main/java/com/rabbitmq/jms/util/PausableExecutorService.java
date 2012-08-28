package com.rabbitmq.jms.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class PausableExecutorService extends ThreadPoolExecutor implements ExecutorService {

    private final static long DEFAULT_PAUSE_TIMEOUT = Long.getLong("rabbit.jms.DEFAULT_PAUSE_TIMEOUT", 300000);
    private final static AtomicLong THREAD_COUNTER = new AtomicLong(0);
    
    private final PauseLatch latch = new PauseLatch(false);
    private final CountUpAndDownLatch clatch = new CountUpAndDownLatch(0);
    private volatile String serviceId = "RabbitMQ JMS Thread #";
    
    public PausableExecutorService(int maxThreads) {
        super(0,maxThreads,60, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
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
    
    
    public boolean isPaused() {
        return latch.isPaused();
    }

    public void pause() throws InterruptedException {
        //first we pause the pause latch 
        latch.pause();
        //then we make sure the threads do complete
        clatch.awaitZero(DEFAULT_PAUSE_TIMEOUT, TimeUnit.MILLISECONDS);
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
