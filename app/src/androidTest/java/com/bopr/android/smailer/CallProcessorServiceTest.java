package com.bopr.android.smailer;

import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CallProcessorService} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallProcessorServiceTest extends BaseTest {

    /**
     * Checks that service calls {@link CallProcessor#processAll()} method.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartResend() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        CallProcessor callProcessor = mock(CallProcessor.class);
        doAnswer(invocations).when(callProcessor).processAll();

        CallProcessorService service = new CallProcessorService();
//        service.init(callProcessor, mock(GeoLocator.class));

/*
        Intent intent = CallProcessorService.createResendIntent(getContext());
        service.onHandleIntent(intent);
*/

        assertEquals(1, invocations.size());
    }

    /**
     * Checks that service calls {@link CallProcessor#process(PhoneEvent)}} method on incoming sms.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendIncomingSms() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        CallProcessor callProcessor = mock(CallProcessor.class);
        doAnswer(invocations).when(callProcessor).process(any(PhoneEvent.class));
        GeoLocator locator = mock(GeoLocator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        CallProcessorService service = new CallProcessorService();
//        service.init(callProcessor, locator);

        //"123", (long) 100000, "Text", true
        PhoneEvent event1 = new PhoneEvent();
/*
        Intent intent = CallProcessorService.createPhoneEventIntent(getContext(), event1);
        service.onHandleIntent(intent);
*/

        PhoneEvent event = (PhoneEvent) invocations.get(0)[0];
        assertEquals("123", event.getPhone());
        assertEquals(100000, event.getStartTime().longValue());
        assertEquals("Text", event.getText());
        assertNull(event.getEndTime());
        assertEquals(PhoneEvent.State.PENDING, event.getState());
        assertTrue(event.isIncoming());
        assertFalse(event.isMissed());
        assertTrue(event.isSms());
        assertNull(event.getDetails());
        assertEquals(new GeoCoordinates(60, 30), event.getLocation());
    }

    /**
     * Checks that service calls {@link CallProcessor#process(PhoneEvent)}} method on outgoing sms.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendOutgoingSms() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        CallProcessor callProcessor = mock(CallProcessor.class);
        doAnswer(invocations).when(callProcessor).process(any(PhoneEvent.class));
        GeoLocator locator = mock(GeoLocator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        CallProcessorService service = new CallProcessorService();
//        service.init(callProcessor, locator);

//        Intent intent = CallProcessorService.createSmsIntent(getContext(), "123", (long) 100000, "Text", false);
/*
        PhoneEvent event1 = new PhoneEvent();
        Intent intent = CallProcessorService.createPhoneEventIntent(getContext(), event1);
        service.onHandleIntent(intent);
*/

        PhoneEvent message = (PhoneEvent) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertEquals("Text", message.getText());
//        assertFalse(message.isProcessed());
        assertFalse(message.isIncoming());
        assertFalse(message.isMissed());
        assertTrue(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

    /**
     * Checks that service calls {@link CallProcessor#process(PhoneEvent)}} method on incoming call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendIncomingCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        CallProcessor callProcessor = mock(CallProcessor.class);
        doAnswer(invocations).when(callProcessor).process(any(PhoneEvent.class));
        GeoLocator locator = mock(GeoLocator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        CallProcessorService service = new CallProcessorService();
//        service.init(callProcessor, locator);

//        Intent intent = CallProcessorService.createIncomingCallIntent(getContext(), "123", 100000, 200000);
/*
        PhoneEvent event1 = new PhoneEvent();
        Intent intent = CallProcessorService.createPhoneEventIntent(getContext(), event1);
        service.onHandleIntent(intent);
*/

        PhoneEvent message = (PhoneEvent) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertEquals(200000, message.getEndTime().longValue());
        assertNull(message.getText());
//        assertFalse(message.isProcessed());
        assertTrue(message.isIncoming());
        assertFalse(message.isMissed());
        assertFalse(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

    /**
     * Checks that service calls {@link CallProcessor#process(PhoneEvent)}} method on outgoing call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendOutgoingCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        CallProcessor callProcessor = mock(CallProcessor.class);
        doAnswer(invocations).when(callProcessor).process(any(PhoneEvent.class));
        GeoLocator locator = mock(GeoLocator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        CallProcessorService service = new CallProcessorService();
//        service.init(callProcessor, locator);

//        Intent intent = CallProcessorService.createOutgoingCallIntent(getContext(), "123", 100000, 200000);
        PhoneEvent event1 = new PhoneEvent();
/*
        Intent intent = CallProcessorService.createPhoneEventIntent(getContext(), event1);
        service.onHandleIntent(intent);
*/

        PhoneEvent message = (PhoneEvent) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertEquals(200000, message.getEndTime().longValue());
        assertNull(message.getText());
//        assertFalse(message.isProcessed());
        assertFalse(message.isIncoming());
        assertFalse(message.isMissed());
        assertFalse(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

    /**
     * Checks that service calls {@link CallProcessor#process(PhoneEvent)}} method on missed call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendMissedCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        CallProcessor callProcessor = mock(CallProcessor.class);
        doAnswer(invocations).when(callProcessor).process(any(PhoneEvent.class));
        GeoLocator locator = mock(GeoLocator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        CallProcessorService service = new CallProcessorService();
//        service.init(callProcessor, locator);

//        Intent intent = CallProcessorService.createMissedCallIntent(getContext(), "123", 100000);
        PhoneEvent event1 = new PhoneEvent();
/*
        Intent intent = CallProcessorService.createPhoneEventIntent(getContext(), event1);
        service.onHandleIntent(intent);
*/

        PhoneEvent message = (PhoneEvent) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertNull(message.getEndTime());
        assertNull(message.getText());
//        assertFalse(message.isProcessed());
        assertTrue(message.isIncoming());
        assertTrue(message.isMissed());
        assertFalse(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

}