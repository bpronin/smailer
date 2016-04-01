package com.bopr.android.smailer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.bopr.android.smailer.util.AndroidUtil;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Settings.getPreferences;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link Mailer} tester.
 */
@SuppressWarnings("ResourceType")
public class MailerTest extends BaseTest {

    private Database database;
    private Context context;
    private Cryptor cryptor;
    private MailTransport transport;
    Notifications notifications;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getContext();

        database = new Database(context, "test.sqlite"); /* not mock context */
        database.destroy();

        cryptor = mock(Cryptor.class);
        when(cryptor.decrypt(anyString())).then(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return "decrypted " + invocation.getArguments()[0];
            }
        });

        transport = mock(MailTransport.class);
        notifications = mock(Notifications.class);

        getPreferences(context)
                .edit()
                .clear()
                .putBoolean(KEY_PREF_SERVICE_ENABLED, true)
                .putString(KEY_PREF_SENDER_ACCOUNT, "user")
                .putString(KEY_PREF_SENDER_PASSWORD, "password")
                .putString(KEY_PREF_RECIPIENTS_ADDRESS, "recipient")
                .putString(KEY_PREF_EMAIL_HOST, DEFAULT_HOST)
                .putString(KEY_PREF_EMAIL_PORT, DEFAULT_PORT)
                .putStringSet(KEY_PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
                .putStringSet(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT)
                .commit();
    }

    /**
     * Tests normal mailer behaviour.
     *
     * @throws Exception when fails
     */
    public void testSend() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());

        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, start, end, false, false, null, new GeoCoordinates(30.0, 60.0), true, null));

        assertTrue(errors.isEmpty());
        assertArrayEquals(new Object[]{"user", "decrypted password", "smtp.gmail.com", "465"}, inits.get(0));
        assertEquals("[SMailer] Outgoing call to +12345678901", sends.get(0)[0]);
    }

    /**
     * Tests normal mailer behaviour with non-default locale.
     *
     * @throws Exception when fails
     */
    public void testSendLocalized() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());

        getPreferences(context).edit().putString(KEY_PREF_EMAIL_LOCALE, "ru_RU").commit();

        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, start, end, false, false, null, new GeoCoordinates(30.0, 60.0), true, null));

        assertTrue(errors.isEmpty());
        assertArrayEquals(new Object[]{"user", "decrypted password", "smtp.gmail.com", "465"}, inits.get(0));
        assertEquals("[SMailer] Исходящий звонок на +12345678901", sends.get(0)[0]);
    }

    /**
     * Check that mailer produces notification when no internet connection.
     *
     * @throws Exception when fails
     */
    public void testErrorNotConnected() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());

        NetworkInfo networkInfo = mock(NetworkInfo.class);
        when(networkInfo.isConnected()).thenReturn(false);

        ConnectivityManager manager = mock(ConnectivityManager.class);
        when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);

        Context context = mock(Context.class);
        when(context.getSystemService(eq(Context.CONNECTIVITY_SERVICE))).thenReturn(manager);
        when(context.getResources()).thenReturn(getContext().getResources());
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(getPreferences(getContext()));

        assertFalse(AndroidUtil.hasInternetConnection(context));

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, null, null, false, false, null, null, true, null));

        assertTrue(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.notification_error_no_connection, errors.get(0)[0]);
    }

    /**
     * Check that mailer produces notification when user parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testErrorEmptyUser() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());

        getPreferences(context).edit().putString(KEY_PREF_SENDER_ACCOUNT, null).commit();

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, null, null, false, false, null, null, true, null));

        assertTrue(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.notification_error_no_parameters, errors.get(0)[0]);
    }

    /**
     * Check that mailer produces notification when recipient parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testErrorEmptyRecipients() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());

        getPreferences(context).edit().putString(KEY_PREF_RECIPIENTS_ADDRESS, null).commit();

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, null, null, false, false, null, null, true, null));

        assertTrue(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.notification_error_no_parameters, errors.get(0)[0]);
    }

    /**
     * Check that mailer produces notification when host parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testErrorEmptyHost() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());

        getPreferences(context).edit().putString(KEY_PREF_EMAIL_HOST, null).commit();

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, null, null, false, false, null, null, true, null));

        assertTrue(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.notification_error_no_parameters, errors.get(0)[0]);
    }

    /**
     * Check that mailer produces notification when port parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testErrorEmptyPort() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());

        getPreferences(context).edit().putString(KEY_PREF_EMAIL_PORT, null).commit();

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, null, null, false, false, null, null, true, null));

        assertTrue(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.notification_error_no_parameters, errors.get(0)[0]);
    }

    /**
     * Check that mailer produces notification on authorisation exceptions.
     *
     * @throws Exception when fails
     */
    public void testErrorAuthenticationFailedException() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());
        doThrow(AuthenticationFailedException.class).when(transport).send(anyString(), anyString(), anyString(), anyString());

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, null, null, false, false, null, null, true, null));

        assertFalse(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.notification_error_authentication, errors.get(0)[0]);
    }

    /**
     * Check that mailer produces notification on other transport exceptions.
     *
     * @throws Exception when fails
     */
    public void testErrorOtherExceptions() throws Exception {
        ArgumentsCollector inits = new ArgumentsCollector();
        ArgumentsCollector sends = new ArgumentsCollector();
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(inits).when(transport).init(anyString(), anyString(), anyString(), anyString());
        doAnswer(sends).when(transport).send(anyString(), anyString(), anyString(), anyString());
        doThrow(MessagingException.class).when(transport).send(anyString(), anyString(), anyString(), anyString());

        /* test start */

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, null, null, false, false, null, null, true, null));

        assertFalse(inits.isEmpty());
        assertTrue(sends.isEmpty());
        assertEquals(R.string.notification_error_mail_general, errors.get(0)[0]);
    }

    /**
     * When mailer parameters goes back to normal last notification should be removed.
     *
     * @throws Exception when fails
     */
    public void testClearNotificationExceptions() throws Exception {
        ArgumentsCollector errors = new ArgumentsCollector();
        ArgumentsCollector clears = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(clears).when(notifications).hideMailError();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String subject = invocation.getArgumentAt(0, String.class);
                if (subject.equals("[SMailer] Outgoing call to bad_phone")) {
                    throw new MessagingException("bad_phone");
                }
                return null;
            }
        }).when(transport).send(anyString(), anyString(), anyString(), anyString());

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);

        /* bad_phone produces notification */

        mailer.send(new MailMessage("bad_phone", false, null, null, false, false, null, null, true, null));
        assertEquals(R.string.notification_error_mail_general, errors.get(0)[0]);
        assertTrue(clears.isEmpty());

        /* good_phone removes it */

        errors.clear();
        clears.clear();

        mailer.send(new MailMessage("good_phone", false, null, null, false, false, null, null, true, null));

        assertTrue(errors.isEmpty());
        assertFalse(clears.isEmpty());
    }

    /**
     * When {@link Settings#KEY_PREF_NOTIFY_SEND_SUCCESS} set to true success notification should be shown.
     *
     * @throws Exception when fails
     */
    public void testSuccessNotification() throws Exception {
        ArgumentsCollector errors = new ArgumentsCollector();
        ArgumentsCollector successes = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());
        doAnswer(successes).when(notifications).showMailSuccess();

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);

        /* settings is on */
        getPreferences(context).edit().putBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false).commit();
        mailer.send(new MailMessage("1", false, null, null, false, false, null, null, true, null));

        assertTrue(errors.isEmpty());
        assertTrue(successes.isEmpty());

        /* settings is off */
        getPreferences(context).edit().putBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, true).commit();
        mailer.send(new MailMessage("1", false, null, null, false, false, null, null, true, null));

        assertTrue(errors.isEmpty());
        assertFalse(successes.isEmpty());
    }

    /**
     * Test resending unsent messages.
     *
     * @throws Exception when fails
     */
    public void testSendUnsent() throws Exception {
        ArgumentsCollector errors = new ArgumentsCollector();

        doAnswer(errors).when(notifications).showMailError(anyInt(), anyLong());

        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        /* test start */

        doThrow(MessagingException.class).when(transport).send(anyString(), anyString(), anyString(), anyString());

        Mailer mailer = new Mailer(context, transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, start, end, false, false, null, new GeoCoordinates(30.0, 60.0), false, null));
        mailer.send(new MailMessage("+12345678901", false, start, end, false, false, null, new GeoCoordinates(30.0, 60.0), false, null));
        mailer.send(new MailMessage("+12345678901", false, start, end, false, false, null, new GeoCoordinates(30.0, 60.0), false, null));

        assertEquals(3, database.getMessages().getCount());
        assertEquals(3, database.getUnsentMessages().getCount());
        assertEquals(3, errors.size());

        errors.clear();
        doNothing().when(transport).send(anyString(), anyString(), anyString(), anyString());

        mailer.sendAllUnsent();

        assertEquals(3, database.getMessages().getCount());
        assertEquals(0, database.getUnsentMessages().getCount());
        assertTrue(errors.isEmpty());
    }

    private class ArgumentsCollector implements Answer {

        private final List<Object[]> arguments = new ArrayList<>();

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            arguments.add(invocation.getArguments());
            return null;
        }

        public Object[] get(int index) {
            return arguments.get(index);
        }

        public boolean isEmpty() {
            return arguments.isEmpty();
        }

        public void clear() {
            arguments.clear();
        }

        public int size() {
            return arguments.size();
        }
    }

}