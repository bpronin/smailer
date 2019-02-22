package com.bopr.android.smailer;

import android.util.Log;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * {@link CallProcessorService} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallProcessorServiceTest extends BaseTest {

    /**
     * Checks that service calls {@link CallProcessor#processPending()} method.
     */
    @Test
    public void testStartResend() {
        final CountDownLatch latch = new CountDownLatch(1);

        CallProcessor callProcessor = mock(CallProcessor.class);
        doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) {
                Log.d(TAG, "Method invoked");
                latch.countDown();
                return null;
            }
        }).when(callProcessor).processPending();

        CallProcessorService service = new CallProcessorService();
        Whitebox.setInternalState(service, "callProcessor", callProcessor);

        service.onHandleIntent(CallProcessorService.createResendIntent(getContext()));

        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }
    }

    /**
     * Checks that service calls {@link CallProcessor#process(PhoneEvent)}} method on incoming sms.
     */
    @Test
    public void testStart() {
        CallProcessor callProcessor = mock(CallProcessor.class);

        BlockingAnswer answer = new BlockingAnswer() ;
        doAnswer(answer).when(callProcessor).process(any(PhoneEvent.class));

        CallProcessorService service = new CallProcessorService();
        Whitebox.setInternalState(service, "callProcessor", callProcessor);

        PhoneEvent event = new PhoneEvent();
        event.setId(100L);
        event.setPhone("32121");

        service.onHandleIntent(CallProcessorService.createIntent(getContext(), event));

        try {
            assertTrue(answer.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail();
        }

        PhoneEvent result = (PhoneEvent) answer.invocation().getArguments()[0];
        assertEquals(event, result);
    }

}