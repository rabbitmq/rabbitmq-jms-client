package com.rabbitmq.jms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class CountUpAndDownLatchTest {

    @Test
    public void testCountLatch() throws Exception {
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

}
