package com.bopr.android.smailer;

import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MailerService} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailerServiceTest extends BaseTest {

    /**
     * Checks that service calls {@link Mailer#sendAllUnsent()} method.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartResend() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).sendAllUnsent();

        MailerService service = new MailerService();
        service.init(mailer, mock(Locator.class));

/*
        Intent intent = MailerService.createResendIntent(getContext());
        service.onHandleIntent(intent);
*/

        assertEquals(1, invocations.size());
    }

    /**
     * Checks that service calls {@link Mailer#send(PhoneEvent)}} method on incoming sms.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendIncomingSms() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(PhoneEvent.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

        //"123", (long) 100000, "Text", true
        PhoneEvent event1 = new PhoneEvent();
/*
        Intent intent = MailerService.createPhoneEventIntent(getContext(), event1);
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
     * Checks that service calls {@link Mailer#send(PhoneEvent)}} method on outgoing sms.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendOutgoingSms() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(PhoneEvent.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

//        Intent intent = MailerService.createSmsIntent(getContext(), "123", (long) 100000, "Text", false);
/*
        PhoneEvent event1 = new PhoneEvent();
        Intent intent = MailerService.createPhoneEventIntent(getContext(), event1);
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
     * Checks that service calls {@link Mailer#send(PhoneEvent)}} method on incoming call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendIncomingCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(PhoneEvent.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

//        Intent intent = MailerService.createIncomingCallIntent(getContext(), "123", 100000, 200000);
/*
        PhoneEvent event1 = new PhoneEvent();
        Intent intent = MailerService.createPhoneEventIntent(getContext(), event1);
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
     * Checks that service calls {@link Mailer#send(PhoneEvent)}} method on outgoing call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendOutgoingCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(PhoneEvent.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

//        Intent intent = MailerService.createOutgoingCallIntent(getContext(), "123", 100000, 200000);
        PhoneEvent event1 = new PhoneEvent();
/*
        Intent intent = MailerService.createPhoneEventIntent(getContext(), event1);
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
     * Checks that service calls {@link Mailer#send(PhoneEvent)}} method on missed call.
     *
     * @throws Exception when fails
     */
    @Test
    public void testStartSendMissedCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(PhoneEvent.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

//        Intent intent = MailerService.createMissedCallIntent(getContext(), "123", 100000);
        PhoneEvent event1 = new PhoneEvent();
/*
        Intent intent = MailerService.createPhoneEventIntent(getContext(), event1);
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