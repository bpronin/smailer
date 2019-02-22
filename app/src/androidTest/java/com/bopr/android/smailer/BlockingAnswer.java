package com.bopr.android.smailer;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class BlockingAnswer implements Answer {

    private final CountDownLatch latch = new CountDownLatch(1);
    private InvocationOnMock invocation;

    @Override
    public Object answer(InvocationOnMock invocation) {
        latch.countDown();
        this.invocation = invocation;
        return null;
    }

    public InvocationOnMock invocation() {
        return invocation;
    }

    public boolean await(int timeout, TimeUnit unit) throws InterruptedException {
        return latch.await(timeout, unit);
    }
}
