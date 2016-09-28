package com.bopr.android.smailer;

import android.content.Intent;

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
    public void testStartResend() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).sendAllUnsent();

        MailerService service = new MailerService();
        service.init(mailer, mock(Locator.class));

        Intent intent = MailerService.createResendIntent(getContext());
        service.onHandleIntent(intent);

        assertEquals(1, invocations.size());
    }

    /**
     * Checks that service calls {@link Mailer#send(MailMessage)}} method on incoming sms.
     *
     * @throws Exception when fails
     */
    public void testStartSendIncomingSms() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(MailMessage.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

        Intent intent = MailerService.createSmsIntent(getContext(), "123", (long) 100000, "Text", true);
        service.onHandleIntent(intent);

        MailMessage message = (MailMessage) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertEquals("Text", message.getText());
        assertNull(message.getEndTime());
        assertFalse(message.isSent());
        assertTrue(message.isIncoming());
        assertFalse(message.isMissed());
        assertTrue(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

    /**
     * Checks that service calls {@link Mailer#send(MailMessage)}} method on outgoing sms.
     *
     * @throws Exception when fails
     */
    public void testStartSendOutgoingSms() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(MailMessage.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

        Intent intent = MailerService.createSmsIntent(getContext(), "123", (long) 100000, "Text", false);
        service.onHandleIntent(intent);

        MailMessage message = (MailMessage) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertEquals("Text", message.getText());
        assertFalse(message.isSent());
        assertFalse(message.isIncoming());
        assertFalse(message.isMissed());
        assertTrue(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

    /**
     * Checks that service calls {@link Mailer#send(MailMessage)}} method on incoming call.
     *
     * @throws Exception when fails
     */
    public void testStartSendIncomingCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(MailMessage.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

        Intent intent = MailerService.createIncomingCallIntent(getContext(), "123", 100000, 200000);
        service.onHandleIntent(intent);

        MailMessage message = (MailMessage) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertEquals(200000, message.getEndTime().longValue());
        assertNull(message.getText());
        assertFalse(message.isSent());
        assertTrue(message.isIncoming());
        assertFalse(message.isMissed());
        assertFalse(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

    /**
     * Checks that service calls {@link Mailer#send(MailMessage)}} method on outgoing call.
     *
     * @throws Exception when fails
     */
    public void testStartSendOutgoingCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(MailMessage.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

        Intent intent = MailerService.createOutgoingCallIntent(getContext(), "123", 100000, 200000);
        service.onHandleIntent(intent);

        MailMessage message = (MailMessage) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertEquals(200000, message.getEndTime().longValue());
        assertNull(message.getText());
        assertFalse(message.isSent());
        assertFalse(message.isIncoming());
        assertFalse(message.isMissed());
        assertFalse(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

    /**
     * Checks that service calls {@link Mailer#send(MailMessage)}} method on missed call.
     *
     * @throws Exception when fails
     */
    public void testStartSendMissedCall() throws Exception {
        InvocationsCollector invocations = new InvocationsCollector();

        Mailer mailer = mock(Mailer.class);
        doAnswer(invocations).when(mailer).send(any(MailMessage.class));
        Locator locator = mock(Locator.class);
        when(locator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        MailerService service = new MailerService();
        service.init(mailer, locator);

        Intent intent = MailerService.createMissedCallIntent(getContext(), "123", 100000);
        service.onHandleIntent(intent);

        MailMessage message = (MailMessage) invocations.get(0)[0];
        assertEquals("123", message.getPhone());
        assertEquals(100000, message.getStartTime().longValue());
        assertNull(message.getEndTime());
        assertNull(message.getText());
        assertFalse(message.isSent());
        assertTrue(message.isIncoming());
        assertTrue(message.isMissed());
        assertFalse(message.isSms());
        assertNull(message.getDetails());
        assertEquals(new GeoCoordinates(60, 30), message.getLocation());
    }

}