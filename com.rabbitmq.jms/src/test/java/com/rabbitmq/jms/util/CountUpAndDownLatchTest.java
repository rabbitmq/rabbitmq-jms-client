package com.rabbitmq.jms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class CountUpAndDownLatchTest {

    @Test
    public void testCountLatch0() throws Exception {
        CountUpAndDownLatch latch = new CountUpAndDownLatch(0);
        assertEquals(0, latch.getCount());
        assertTrue(latch.awaitZero(0, TimeUnit.MILLISECONDS));
        latch.countUp();
        assertEquals(1, latch.getCount());
        assertFalse(latch.awaitZero(0, TimeUnit.MILLISECONDS));
        latch.countDown();
        assertEquals(0, latch.getCount());
        assertTrue(latch.awaitZero(0, TimeUnit.MILLISECONDS));
    }
    
    @Test
    public void testCountLatch1() throws Exception {
        CountUpAndDownLatch latch = new CountUpAndDownLatch(1);
        assertEquals(1, latch.getCount());
        TestThread t = new TestThread(latch);
        t.start();
        assertFalse(t.isComplete());
        latch.countDown();
        t.join();
        assertTrue(t.isComplete());
        assertTrue(t.isSuccess());
        assertFalse(t.isError());
        latch.countUp();
        assertEquals(1, latch.getCount());
        t = new TestThread(latch);
        t.start();
        assertFalse(t.isComplete());
        latch.countDown();
        t.join();
        assertTrue(t.isComplete());
        assertTrue(t.isSuccess());
        assertFalse(t.isError());
        latch.countDown();
        assertEquals(-1, latch.getCount());
        t = new TestThread(latch);
        t.start();
        assertFalse(t.isComplete());
        latch.countUp();
        t.join();
        assertTrue(t.isComplete());
        assertTrue(t.isSuccess());
        assertFalse(t.isError());
    }
    
    @Test
    public void testCountLatch2() throws Exception {
        CountUpAndDownLatch latch = new CountUpAndDownLatch(2);
        assertEquals(2, latch.getCount());
        TestThread t = new TestThread(latch);
        t.start();
        assertFalse(t.isComplete());
        latch.countDown();
        t.join();
        assertTrue(t.isComplete());
        assertFalse(t.isSuccess());
        assertFalse(t.isError());
    }
    
    
    public static class TestThread extends Thread {
        private volatile boolean success = false;
        private final CountUpAndDownLatch latch;
        private volatile boolean complete = false;
        private volatile boolean error = false;
        public TestThread(CountUpAndDownLatch latch) {
            this.latch = latch;
        }
        
        public void run() {
            try {
                success = latch.awaitZero(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException x) {
                error = true;
            } finally {
                complete = true;
            }
        }
        
        public boolean isSuccess() {
            return success;
        }
        public boolean isComplete() {
            return complete;
        }
        public boolean isError() {
            return error;
        }
    }

}
