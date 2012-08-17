package com.rabbitmq.jms.util;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
public class PauseLatchTest {
    
    @Test
    public void testPauseLatch() throws Exception {
        PauseLatch latch = new PauseLatch(false);
        assertFalse(latch.isPaused());
        if (latch.pause()) {
            assertTrue(latch.isPaused());
        } else {
            fail("We should be in a pause state");
        }
        if (latch.resume()) {
            assertFalse(latch.isPaused());
        } else {
            fail("Resume should have succeeded");
        }
    }
    
    @Test
    public void testPauseLatch2() throws Exception {
        PauseLatch latch = new PauseLatch(true);
        assertTrue(latch.isPaused());
        if (latch.pause()) {
            assertTrue(latch.isPaused());
        } else {
            fail("We should not be in a resume state");
        }
        if (latch.resume()) {
            assertFalse(latch.isPaused());
        } else {
            fail("Resume should have succeeded");
        }
    }

    @Test
    public void testPauseLatch3() throws Exception {
        PauseLatch latch = new PauseLatch(true);
        assertFalse(latch.await(1,TimeUnit.MILLISECONDS));
        if (latch.resume()) {
            assertFalse(latch.isPaused());
        } else {
            fail("Resume should have succeeded");
        }
        assertTrue(latch.await(1, TimeUnit.MILLISECONDS));
    }
}
