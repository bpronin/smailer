package com.bopr.android.smailer;

import android.accounts.AccountsException;
import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Test;

import java.io.IOException;

import static com.bopr.android.smailer.PhoneEvent.REASON_ACCEPTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_TRIGGER_OFF;
import static com.bopr.android.smailer.PhoneEvent.STATE_IGNORED;
import static com.bopr.android.smailer.PhoneEvent.STATE_PENDING;
import static com.bopr.android.smailer.PhoneEvent.STATE_PROCESSED;
import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.PREF_SENDER_ACCOUNT;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CallProcessor} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallProcessorTest extends BaseTest {

    private Database database;
    private Context context;
    private GoogleMail transport;
    private Notifications notifications;
    private SharedPreferences preferences;
    private GeoLocator geoLocator;

    @Override
    @SuppressWarnings("ResourceType")
    public void setUp() throws Exception {
        super.setUp();

        preferences = mock(SharedPreferences.class);
        when(preferences.getString(eq(PREF_SENDER_ACCOUNT), anyString())).thenReturn("sender@mail.com");
        when(preferences.getString(eq(PREF_RECIPIENTS_ADDRESS), anyString())).thenReturn("recipient@mail.com");
        when(preferences.getStringSet(eq(PREF_EMAIL_TRIGGERS), anySetOf(String.class))).thenReturn(DEFAULT_TRIGGERS);
        when(preferences.getStringSet(eq(PREF_EMAIL_CONTENT), anySetOf(String.class))).thenReturn(DEFAULT_CONTENT);

        context = mock(Context.class);
        when(context.getContentResolver()).thenReturn(getContext().getContentResolver());
        when(context.getResources()).thenReturn(getContext().getResources());
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(preferences);

        geoLocator = mock(GeoLocator.class);
        when(geoLocator.getLocation()).thenReturn(new GeoCoordinates(60, 30));

        transport = mock(GoogleMail.class);
        notifications = mock(Notifications.class);

        database = new Database(getContext(), "test.sqlite"); /* not a mock context here! */
        database.destroy();
    }

    private PhoneEvent newPhoneEvent(boolean missed) {
        long time = currentTimeMillis();
        return new PhoneEvent("+123", true, time, time + 1000, missed,
                "SMS TEXT", null, null, STATE_PENDING, "device", REASON_ACCEPTED, false);
    }

    /**
     * Tests successful processing - mail sent.
     */
    @Test
    public void testProcessMailSent() throws Exception {
        MethodInvocationsCollector initInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector sendInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector showErrorInvocations = new MethodInvocationsCollector();

        doAnswer(initInvocations).when(transport).startSession(anyString(), anyString());
        doAnswer(sendInvocations).when(transport).send(any(MailMessage.class));
        doAnswer(showErrorInvocations).when(notifications).showMailError(anyInt(), anyInt());

        PhoneEvent event = newPhoneEvent(true);

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);
        processor.process(event);

        assertTrue(showErrorInvocations.isEmpty());

        assertEquals(1, initInvocations.count());
        String sender = initInvocations.getArgument(0, 0);
        assertEquals("sender@mail.com", sender);

        assertEquals(1, sendInvocations.count());
        MailMessage message = sendInvocations.getArgument(0, 0);
        assertEquals("[SMailer] Missed call from \"+123\"", message.getSubject());

        PhoneEvent savedEvent = database.getEvents().findFirst();
        assertEquals(event.getAcceptor(), savedEvent.getAcceptor());
        assertEquals(event.getStartTime(), savedEvent.getStartTime());
        assertEquals(event.getPhone(), savedEvent.getPhone());
        assertEquals(STATE_PROCESSED, savedEvent.getState());
        assertEquals(REASON_ACCEPTED, savedEvent.getStateReason());
        assertEquals(new GeoCoordinates(60, 30), event.getLocation());
    }

    /**
     * Tests successful processing - event ignored.
     */
    @Test
    public void testProcessIgnored() throws Exception {
        MethodInvocationsCollector initInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector sendInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector showErrorInvocations = new MethodInvocationsCollector();

        doAnswer(initInvocations).when(transport).startSession(anyString(), anyString());
        doAnswer(sendInvocations).when(transport).send(any(MailMessage.class));
        doAnswer(showErrorInvocations).when(notifications).showMailError(anyInt(), anyInt());

        PhoneEvent event = newPhoneEvent(false); /* make it not missed (default filter denies it) */

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);
        processor.process(event);

        assertTrue(showErrorInvocations.isEmpty());
        assertTrue(initInvocations.isEmpty());
        assertTrue(sendInvocations.isEmpty());

        PhoneEvent savedEvent = database.getEvents().findFirst();
        assertEquals(event.getAcceptor(), savedEvent.getAcceptor());
        assertEquals(event.getStartTime(), savedEvent.getStartTime());
        assertEquals(event.getPhone(), savedEvent.getPhone());
        assertEquals(STATE_IGNORED, savedEvent.getState());
        assertEquals(REASON_TRIGGER_OFF, savedEvent.getStateReason());
        assertEquals(new GeoCoordinates(60, 30), event.getLocation());
    }

    /**
     * Test processing when sender is not specified.
     */
    @Test
    public void testProcessNoSender() throws Exception {
        MethodInvocationsCollector initInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector sendInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector showErrorInvocations = new MethodInvocationsCollector();

        doAnswer(initInvocations).when(transport).startSession(anyString(), anyString());
        doAnswer(sendInvocations).when(transport).send(any(MailMessage.class));
        doAnswer(showErrorInvocations).when(notifications).showMailError(anyInt(), anyInt());

        when(preferences.getString(eq(PREF_SENDER_ACCOUNT), anyString())).thenReturn(null);

        PhoneEvent event = newPhoneEvent(true);

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);
        processor.process(event);

        assertEquals(1, showErrorInvocations.count());
        int notificationText = showErrorInvocations.getArgument(0, 0);
        assertEquals(R.string.no_account_specified, notificationText);

        assertTrue(initInvocations.isEmpty());
        assertTrue(sendInvocations.isEmpty());

        PhoneEvent savedEvent = database.getEvents().findFirst();
        assertEquals(event.getAcceptor(), savedEvent.getAcceptor());
        assertEquals(event.getStartTime(), savedEvent.getStartTime());
        assertEquals(event.getPhone(), savedEvent.getPhone());
        assertEquals(STATE_PENDING, savedEvent.getState());
        assertEquals(REASON_ACCEPTED, savedEvent.getStateReason());
        assertEquals(new GeoCoordinates(60, 30), event.getLocation());
    }

    /**
     * Test processing when no recipients specified.
     */
    @Test
    public void testProcessNoRecipients() throws Exception {
        MethodInvocationsCollector initInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector sendInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector showErrorInvocations = new MethodInvocationsCollector();

        doAnswer(initInvocations).when(transport).startSession(anyString(), anyString());
        doAnswer(sendInvocations).when(transport).send(any(MailMessage.class));
        doAnswer(showErrorInvocations).when(notifications).showMailError(anyInt(), anyInt());

        when(preferences.getString(eq(PREF_RECIPIENTS_ADDRESS), anyString())).thenReturn(null);

        PhoneEvent event = newPhoneEvent(true);

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);
        processor.process(event);

        assertEquals(1, showErrorInvocations.count());
        int notificationText = showErrorInvocations.getArgument(0, 0);
        assertEquals(R.string.no_recipients_specified, notificationText);

        assertTrue(initInvocations.isEmpty());
        assertTrue(sendInvocations.isEmpty());

        PhoneEvent savedEvent = database.getEvents().findFirst();
        assertEquals(event.getAcceptor(), savedEvent.getAcceptor());
        assertEquals(event.getStartTime(), savedEvent.getStartTime());
        assertEquals(event.getPhone(), savedEvent.getPhone());
        assertEquals(STATE_PENDING, savedEvent.getState());
        assertEquals(REASON_ACCEPTED, savedEvent.getStateReason());
        assertEquals(new GeoCoordinates(60, 30), event.getLocation());
    }

    /**
     * Tests processing when mail transport produces init error.
     */
    @Test
    public void testProcessTransportInitFailed() throws Exception {
        MethodInvocationsCollector showErrorInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector sendInvocations = new MethodInvocationsCollector();

        doAnswer(sendInvocations).when(transport).send(any(MailMessage.class));
        doAnswer(showErrorInvocations).when(notifications).showMailError(anyInt(), anyInt());
        doThrow(new AccountsException("Test error")).when(transport).startSession(anyString(), anyString());

        PhoneEvent event = newPhoneEvent(true);

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);
        processor.process(event);

        assertTrue(sendInvocations.isEmpty());

        assertEquals(1, showErrorInvocations.count());
        int notificationText = showErrorInvocations.getArgument(0, 0);
        assertEquals(R.string.account_not_registered, notificationText);

        PhoneEvent savedEvent = database.getEvents().findFirst();
        assertEquals(event.getAcceptor(), savedEvent.getAcceptor());
        assertEquals(event.getStartTime(), savedEvent.getStartTime());
        assertEquals(event.getPhone(), savedEvent.getPhone());
        assertEquals(STATE_PENDING, savedEvent.getState());
        assertEquals(REASON_ACCEPTED, savedEvent.getStateReason());
        assertEquals(new GeoCoordinates(60, 30), event.getLocation());
    }

    /**
     * Tests processing when mail transport produces send error.
     */
    @Test
    public void testProcessTransportSendFailed() throws Exception {
        MethodInvocationsCollector initInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector sendInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector showErrorInvocations = new MethodInvocationsCollector();

        doAnswer(initInvocations).when(transport).startSession(anyString(), anyString());
        doAnswer(sendInvocations).when(transport).send(any(MailMessage.class));
        doAnswer(showErrorInvocations).when(notifications).showMailError(anyInt(), anyInt());
        doThrow(new IOException("Test error")).when(transport).send(any(MailMessage.class));

        PhoneEvent event = newPhoneEvent(true);

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);
        processor.process(event);

        assertEquals(1, initInvocations.count());
        assertTrue(sendInvocations.isEmpty());
        assertTrue(showErrorInvocations.isEmpty());

        PhoneEvent savedEvent = database.getEvents().findFirst();
        assertEquals(event.getAcceptor(), savedEvent.getAcceptor());
        assertEquals(event.getStartTime(), savedEvent.getStartTime());
        assertEquals(event.getPhone(), savedEvent.getPhone());
        assertEquals(STATE_PENDING, savedEvent.getState());
        assertEquals(REASON_ACCEPTED, savedEvent.getStateReason());
        assertEquals(new GeoCoordinates(60, 30), event.getLocation());
    }

    /**
     * When settings goes back to normal last error notification should be removed.
     */
    @Test
    public void testClearNotifications() throws Exception {
        MethodInvocationsCollector showInvocations = new MethodInvocationsCollector();
        MethodInvocationsCollector hideInvocations = new MethodInvocationsCollector();

        doAnswer(showInvocations).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(hideInvocations).when(notifications).hideAllErrors();

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);

        /* error while sending produces error notification */
        doThrow(new IOException("Test error")).when(transport).send(any(MailMessage.class));
        processor.process(newPhoneEvent(true));

        assertTrue(showInvocations.isEmpty());
        assertTrue(hideInvocations.isEmpty());

        showInvocations.reset();
        hideInvocations.reset();

        /* sending without errors hides all previous error notifications */
        doNothing().when(transport).send(any(MailMessage.class));
        processor.process(newPhoneEvent(true));

        assertTrue(showInvocations.isEmpty());
        assertEquals(1, hideInvocations.count());
    }

    /**
     * When {@link Settings#PREF_NOTIFY_SEND_SUCCESS} setting is set to true success notification should be shown.
     */
    @Test
    public void testSuccessNotification() {
        MethodInvocationsCollector showSuccessInvocation = new MethodInvocationsCollector();

        doAnswer(showSuccessInvocation).when(notifications).showMessage(R.string.email_send, Notifications.ACTION_SHOW_MAIN);

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);

        /* the setting is OFF */
        when(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyBoolean())).thenReturn(false);

        processor.process(newPhoneEvent(true));

        assertTrue(showSuccessInvocation.isEmpty());

        /* the setting is ON */
        when(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyBoolean())).thenReturn(true);

        processor.process(newPhoneEvent(true));

        assertEquals(1, showSuccessInvocation.count());
        int notificationText = showSuccessInvocation.getArgument(0, 0);
        assertEquals(R.string.email_send, notificationText);
    }

    /**
     * Test resending pending messages.
     */
    @Test
    public void testProcessPending() throws Exception {
        MethodInvocationsCollector showErrorInvocations = new MethodInvocationsCollector();

        doAnswer(showErrorInvocations).when(notifications).showMailError(anyInt(), anyInt());

        /* disable transport */
        doThrow(new IOException("Test error")).when(transport).send(any(MailMessage.class));

        CallProcessor processor = new CallProcessor(context, transport, notifications, database, geoLocator);

        processor.process(newPhoneEvent(true));
        processor.process(newPhoneEvent(true));
        processor.process(newPhoneEvent(true));

        assertEquals(3, database.getEvents().getCount());
        assertEquals(3, database.getPendingEvents().getCount());
        assertTrue(showErrorInvocations.isEmpty());

        /* try resend with disabled transport */

        showErrorInvocations.reset();

        processor.processPending();

        assertEquals(3, database.getEvents().getCount());
        assertEquals(3, database.getPendingEvents().getCount());
        assertTrue(showErrorInvocations.isEmpty()); /* no error notifications should be shown */

        /* enable transport an try again */

        doNothing().when(transport).send(any(MailMessage.class));

        showErrorInvocations.reset();

        processor.processPending();

        assertEquals(3, database.getEvents().getCount());
        assertEquals(0, database.getPendingEvents().getCount());
        assertTrue(showErrorInvocations.isEmpty());
    }

}