package com.bopr.android.smailer;

import android.support.annotation.NonNull;

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
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Settings.getPreferences;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link Mailer} tester.
 */
public class MailerTest extends BaseTest {

    private Database database;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        database = new Database(getContext(), "test.sqlite");
        database.destroy();
    }

    @Override
    protected void tearDown() throws Exception {
        getPreferences(getContext()).edit().clear().commit();
        super.tearDown();
    }

    private Database getMockDatabase() {
//        return mock(Database.class);
        return database;
    }

    private void populatePreferences() {
        getPreferences(getContext())
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

    @NonNull
    private Cryptor createCryptor() {
        Cryptor cryptor = mock(Cryptor.class);
        when(cryptor.decrypt(anyString())).then(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return "decrypted " + invocation.getArguments()[0];
            }
        });
        return cryptor;
    }

    @NonNull
    private MailTransport createMailTransport(final List<Object[]> inits,
                                              final List<Object[]> sends) throws MessagingException {
        MailTransport transport = mock(MailTransport.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (inits != null) {
                    inits.add(invocation.getArguments());
                }
                return null;
            }
        }).when(transport).init(anyString(), anyString(), anyString(), anyString());

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (sends != null) {
                    sends.add(invocation.getArguments());
                }
                return null;
            }
        }).when(transport).send(anyString(), anyString(), anyString(), anyString());
        return transport;
    }

    @NonNull
    private Notifications createNotifications(final List<Object[]> errors,
                                              final List<Object[]> clears,
                                              final List<Object[]> successes) {
        Notifications notifications = mock(Notifications.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (errors != null) {
                    errors.add(invocation.getArguments());
                }
                return null;
            }
        }).when(notifications).showMailError(anyInt(), anyLong());

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (clears != null) {
                    clears.add(invocation.getArguments());
                }
                return null;
            }
        }).when(notifications).hideMailError();

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (successes != null) {
                    successes.add(invocation.getArguments());
                }
                return null;
            }
        }).when(notifications).showMailSuccess();

        return notifications;
    }

    /**
     * Tests normal mailer behaviour.
     *
     * @throws Exception when fails
     */
    public void testSend() throws Exception {
        List<Object[]> inits = new ArrayList<>();
        List<Object[]> sends = new ArrayList<>();
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);
        MailTransport transport = createMailTransport(inits, sends);
        Database database = getMockDatabase();

        populatePreferences();

        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
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
        List<Object[]> inits = new ArrayList<>();
        List<Object[]> sends = new ArrayList<>();
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);
        MailTransport transport = createMailTransport(inits, sends);
        Database database = getMockDatabase();

        populatePreferences();
        getPreferences(getContext())
                .edit()
                .putString(KEY_PREF_EMAIL_LOCALE, "ru_RU")
                .commit();

        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
        mailer.send(new MailMessage("+12345678901", false, start, end, false, false, null, new GeoCoordinates(30.0, 60.0), true, null));

        assertTrue(errors.isEmpty());
        assertArrayEquals(new Object[]{"user", "decrypted password", "smtp.gmail.com", "465"}, inits.get(0));
        assertEquals("[SMailer] Исходящий звонок на +12345678901", sends.get(0)[0]);
    }

    /**
     * Check that mailer produces notification when user parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testEmptyUser() throws Exception {
        List<Object[]> inits = new ArrayList<>();
        List<Object[]> sends = new ArrayList<>();
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);
        MailTransport transport = createMailTransport(inits, sends);
        Database database = getMockDatabase();

        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_SENDER_ACCOUNT, null).commit();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
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
    public void testEmptyRecipients() throws Exception {
        List<Object[]> inits = new ArrayList<>();
        List<Object[]> sends = new ArrayList<>();
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);
        MailTransport transport = createMailTransport(inits, sends);
        Database database = getMockDatabase();


        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_RECIPIENTS_ADDRESS, null).commit();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
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
    public void testEmptyHost() throws Exception {
        List<Object[]> inits = new ArrayList<>();
        List<Object[]> sends = new ArrayList<>();
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);
        MailTransport transport = createMailTransport(inits, sends);
        Database database = getMockDatabase();

        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_EMAIL_HOST, null).commit();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
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
    public void testEmptyPort() throws Exception {
        List<Object[]> inits = new ArrayList<>();
        List<Object[]> sends = new ArrayList<>();
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);
        MailTransport transport = createMailTransport(inits, sends);
        Database database = getMockDatabase();

        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_EMAIL_PORT, null).commit();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
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
    public void testAuthenticationFailedException() throws Exception {
        List<Object[]> inits = new ArrayList<>();
        List<Object[]> sends = new ArrayList<>();
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);
        MailTransport transport = createMailTransport(inits, sends);
        doThrow(AuthenticationFailedException.class).when(transport).send(anyString(), anyString(), anyString(), anyString());
        Database database = getMockDatabase();

        populatePreferences();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
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
    public void testOtherExceptions() throws Exception {
        List<Object[]> inits = new ArrayList<>();
        List<Object[]> sends = new ArrayList<>();
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);
        MailTransport transport = createMailTransport(inits, sends);
        doThrow(MessagingException.class).when(transport).send(anyString(), anyString(), anyString(), anyString());
        Database database = getMockDatabase();

        populatePreferences();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
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
        List<Object[]> errors = new ArrayList<>();
        List<Object[]> clears = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, clears, null);

        MailTransport transport = createMailTransport(null, null);
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
        Database database = getMockDatabase();

        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_EMAIL_HOST, "good host").commit();

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);

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
        List<Object[]> errors = new ArrayList<>();
        List<Object[]> successes = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, successes);

        MailTransport transport = createMailTransport(null, null);
        Database database = getMockDatabase();

        populatePreferences();
        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);

        /* settings is on */
        getPreferences(getContext()).edit().putBoolean(Settings.KEY_PREF_NOTIFY_SEND_SUCCESS, false).commit();
        mailer.send(new MailMessage("1", false, null, null, false, false, null, null, true, null));

        assertTrue(errors.isEmpty());
        assertTrue(successes.isEmpty());

        /* settings is off */
        getPreferences(getContext()).edit().putBoolean(Settings.KEY_PREF_NOTIFY_SEND_SUCCESS, true).commit();
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
        List<Object[]> errors = new ArrayList<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errors, null, null);

        MailTransport transport = createMailTransport(null, null);

        populatePreferences();

        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        /* test start */

        doThrow(MessagingException.class).when(transport).send(anyString(), anyString(), anyString(), anyString());

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, database);
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


}