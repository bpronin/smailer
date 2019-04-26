package com.bopr.android.smailer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
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
    private GoogleMailSupport transport;
    private Notifications notifications;
    private NetworkInfo networkInfo;
    private SharedPreferences preferences;
    private GeoLocator locator;

    @Override
    @SuppressWarnings("ResourceType")
    public void setUp() throws Exception {
        super.setUp();

        preferences = mock(SharedPreferences.class);
        when(preferences.getString(eq(KEY_PREF_SENDER_ACCOUNT), anyString())).thenReturn("sender@mail.com");
        when(preferences.getString(eq(KEY_PREF_RECIPIENTS_ADDRESS), anyString())).thenReturn("recipient@mail.com");
        when(preferences.getStringSet(eq(KEY_PREF_EMAIL_TRIGGERS), anySetOf(String.class))).thenReturn(DEFAULT_TRIGGERS);
        when(preferences.getStringSet(eq(KEY_PREF_EMAIL_CONTENT), anySetOf(String.class))).thenReturn(DEFAULT_CONTENT);

        networkInfo = mock(NetworkInfo.class);
        when(networkInfo.isConnected()).thenReturn(true);

        ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(networkInfo);

        context = mock(Context.class);
        when(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE))).thenReturn(connectivityManager);
        when(context.getContentResolver()).thenReturn(getContext().getContentResolver());
        when(context.getResources()).thenReturn(getContext().getResources());
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(preferences);

        database = new Database(getContext(), "test.sqlite"); /* not mock context */
        database.destroy();

        transport = mock(GoogleMailSupport.class);
        notifications = mock(Notifications.class);
        locator = mock(GeoLocator.class);
    }

    /**
     * Tests normal mailer behaviour.
     */
    @Test
    public void testSend() throws Exception {
        InvocationsCollector inits = new InvocationsCollector();
        InvocationsCollector sends = new InvocationsCollector();
        InvocationsCollector errors = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(inits).when(transport).init(anyString(), anyString());
        doAnswer(sends).when(transport).send(any(MailMessage.class));

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, new GeoCoordinates(30.0, 60.0), null, PhoneEvent.STATE_PENDING));

        assertTrue(errors.isEmpty());
        assertArrayEquals(new Object[]{"sender@mail.com", "decrypted password", "smtp.mail.com", "111"}, inits.invocation(0));
        assertEquals("[SMailer] Outgoing call to +12345678901", sends.invocation(0)[0]);
    }

    /**
     * Tests normal mailer behaviour with non-default locale.
     *
     * @throws Exception when fails
     */
    @Test
    public void testSendLocalized() throws Exception {
        InvocationsCollector inits = new InvocationsCollector();
        InvocationsCollector sends = new InvocationsCollector();
        InvocationsCollector errors = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(inits).when(transport).init(anyString(), anyString());
        doAnswer(sends).when(transport).send(any(MailMessage.class));

        when(preferences.getString(eq(KEY_PREF_EMAIL_LOCALE), anyString())).thenReturn("ru_RU");

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, new GeoCoordinates(30.0, 60.0), null, PhoneEvent.STATE_PENDING));

        assertTrue(errors.isEmpty());
        assertArrayEquals(new Object[]{"sender@mail.com", "decrypted password", "smtp.mail.com", "111"}, inits.invocation(0));
        assertEquals("[SMailer] Исходящий звонок на +12345678901", sends.invocation(0)[0]);
    }

    /**
     * Check that mailer produces notification without internet connection.
     *
     * @throws Exception when fails
     */
    @Test
    public void testErrorNotConnected() throws Exception {
        InvocationsCollector inits = new InvocationsCollector();
        InvocationsCollector sends = new InvocationsCollector();
        InvocationsCollector errors = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(inits).when(transport).init(anyString(), anyString());
        doAnswer(sends).when(transport).send(any(MailMessage.class));
        when(networkInfo.isConnected()).thenReturn(false);

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));

        assertTrue(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.no_internet_connection, errors.invocation(0)[0]);
    }

    /**
     * Check that mailer produces notification when user parameter is empty.
     *
     * @throws Exception when fails
     */
    @Test
    public void testErrorEmptyUser() throws Exception {
        InvocationsCollector inits = new InvocationsCollector();
        InvocationsCollector sends = new InvocationsCollector();
        InvocationsCollector errors = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(inits).when(transport).init(anyString(), anyString());
        doAnswer(sends).when(transport).send(any(MailMessage.class));

        when(preferences.getString(eq(KEY_PREF_SENDER_ACCOUNT), anyString())).thenReturn(null);

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));

        assertTrue(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.no_account_specified, errors.invocation(0)[0]);
    }

    /**
     * Check that mailer produces notification when recipient parameter is empty.
     *
     * @throws Exception when fails
     */
    @Test
    public void testErrorEmptyRecipients() throws Exception {
        InvocationsCollector inits = new InvocationsCollector();
        InvocationsCollector sends = new InvocationsCollector();
        InvocationsCollector errors = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(inits).when(transport).init(anyString(), anyString());
        doAnswer(sends).when(transport).send(any(MailMessage.class));

        when(preferences.getString(eq(KEY_PREF_RECIPIENTS_ADDRESS), anyString())).thenReturn(null);

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));

        assertTrue(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.no_recipients_specified, errors.invocation(0)[0]);
    }

    /**
     * Check that mailer produces notification on authorisation exceptions.
     *
     * @throws Exception when fails
     */
    @Test
    public void testErrorAuthenticationFailedException() throws Exception {
        InvocationsCollector inits = new InvocationsCollector();
        InvocationsCollector sends = new InvocationsCollector();
        InvocationsCollector errors = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(inits).when(transport).init(anyString(), anyString());
        doAnswer(sends).when(transport).send(any(MailMessage.class));
        doThrow(AuthenticationFailedException.class).when(transport).send(any(MailMessage.class));

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));

        assertFalse(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.user_password_not_accepted, errors.invocation(0)[0]);
    }

    /**
     * Check that mailer produces notification on other transport exceptions.
     *
     * @throws Exception when fails
     */
    @Test
    public void testErrorOtherExceptions() throws Exception {
        InvocationsCollector inits = new InvocationsCollector();
        InvocationsCollector sends = new InvocationsCollector();
        InvocationsCollector errors = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(inits).when(transport).init(anyString(), anyString());
        doAnswer(sends).when(transport).send(any(MailMessage.class));
        doThrow(MessagingException.class).when(transport).send(any(MailMessage.class));

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));

        assertFalse(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.unable_send_email, errors.invocation(0)[0]);
    }

    /**
     * When mailer parameters goes back to normal last notification should be removed.
     *
     * @throws Exception when fails
     */
    @Test
    public void testClearNotificationExceptions() throws Exception {
        InvocationsCollector errors = new InvocationsCollector();
        InvocationsCollector clears = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(clears).when(notifications).hideLastError();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String subject = (String) invocation.getArguments()[0];
                if (subject.equals("[SMailer] Outgoing call to bad_phone")) {
                    throw new MessagingException("bad_phone");
                }
                return null;
            }
        }).when(transport).send(any(MailMessage.class));

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);

        /* bad_phone produces notification */

        callProcessor.process(new PhoneEvent("bad_phone", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));
        assertEquals(R.string.unable_send_email, errors.invocation(0)[0]);
        assertTrue(clears.isEmpty());

        /* good_phone removes it */

        errors.clear();
        clears.clear();

        callProcessor.process(new PhoneEvent("good_phone", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));

        assertTrue(errors.isEmpty());
        assertFalse(clears.isEmpty());
    }

    /**
     * When {@link Settings#KEY_PREF_NOTIFY_SEND_SUCCESS} set to true success notification should be shown.
     */
    @Test
    public void testSuccessNotification() {
        InvocationsCollector errors = new InvocationsCollector();
        InvocationsCollector successes = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doAnswer(successes).when(notifications).showMessage(R.string.email_send, Notifications.ACTION_SHOW_MAIN);

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);

        /* settings is off */
        when(preferences.getBoolean(eq(KEY_PREF_NOTIFY_SEND_SUCCESS), anyBoolean())).thenReturn(false);

        callProcessor.process(new PhoneEvent("1", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));

        assertTrue(errors.isEmpty());
        assertTrue(successes.isEmpty());

        /* settings is on */
        when(preferences.getBoolean(eq(KEY_PREF_NOTIFY_SEND_SUCCESS), anyBoolean())).thenReturn(true);
        callProcessor.process(new PhoneEvent("1", false, null, null, false, null, null, null, PhoneEvent.STATE_PENDING));

        assertTrue(errors.isEmpty());
        assertFalse(successes.isEmpty());
    }

    /**
     * Test resending pending messages.
     *
     * @throws Exception when fails
     */
    @Test
    public void testSendUnsent() throws Exception {
        InvocationsCollector errors = new InvocationsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyInt());
        doThrow(MessagingException.class).when(transport).send(any(MailMessage.class));

        CallProcessor callProcessor = new CallProcessor(context, transport, notifications, database, locator);
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, new GeoCoordinates(30.0, 60.0), null, PhoneEvent.STATE_PENDING));
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, new GeoCoordinates(30.0, 60.0), null, PhoneEvent.STATE_PENDING));
        callProcessor.process(new PhoneEvent("+12345678901", false, null, null, false, null, new GeoCoordinates(30.0, 60.0), null, PhoneEvent.STATE_PENDING));

        assertEquals(3, database.getEvents().getCount());
        assertEquals(3, database.getPendingEvents().getCount());
        assertEquals(3, errors.size());

        /* try resend with transport still disabled */
        errors.clear();

        callProcessor.processPending();

        assertEquals(3, database.getEvents().getCount());
        assertEquals(3, database.getPendingEvents().getCount());
        assertTrue(errors.isEmpty()); /* no error notifications should be shown */

        /* enable transport an try again */
        doNothing().when(transport).send(any(MailMessage.class));
        errors.clear();

        callProcessor.processPending();

        assertEquals(3, database.getEvents().getCount());
        assertEquals(0, database.getPendingEvents().getCount());
        assertTrue(errors.isEmpty());
    }

}