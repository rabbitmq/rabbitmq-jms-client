package com.rabbitmq.jms.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;


public class CountUpAndDownLatch {

    private static final class CountSync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -1;

        CountSync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        public int tryAcquireShared(int acquires) {
            return getState() == 0? 1 : -1;
        }

        public boolean tryReleaseShared(int releases) {
            for (;;) {
                int c = getState();
                int nextc = c + releases;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final CountSync countSync;

    public CountUpAndDownLatch(int count) {
        this.countSync = new CountSync(count);
    }

    public boolean awaitZero(long timeout, TimeUnit unit) throws InterruptedException {
        return countSync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        countSync.releaseShared(-1);
    }
    
    public void countUp() {
        countSync.releaseShared(1);
    }

    public long getCount() {
        return countSync.getCount();
    }

}
