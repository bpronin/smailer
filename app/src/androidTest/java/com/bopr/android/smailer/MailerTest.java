package com.bopr.android.smailer;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.test.ApplicationTestCase;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.atomic.AtomicReference;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Settings.getPreferences;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link Mailer} tester.
 */
public class MailerTest extends ApplicationTestCase<Application> {

    public MailerTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
    }

    @Override
    protected void tearDown() throws Exception {
        getPreferences(getContext()).edit().clear().commit();
        super.tearDown();
    }

    private void populatePreferences() {
        getPreferences(getContext())
                .edit()
                .clear()
                .putBoolean(KEY_PREF_SERVICE_ENABLED, true)
                .putString(KEY_PREF_SENDER_ACCOUNT, "user")
                .putString(KEY_PREF_SENDER_PASSWORD, "password")
                .putString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, "recipient")
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
    private MailTransport createMailTransport(final AtomicReference<Object[]> initArgs,
                                              final AtomicReference<Object[]> sendArgs) throws MessagingException {
        MailTransport transport = mock(MailTransport.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                initArgs.set(invocation.getArguments());
                return null;
            }
        }).when(transport).init(anyString(), anyString(), anyString(), anyString());

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                sendArgs.set(invocation.getArguments());
                return null;
            }
        }).when(transport).send(anyString(), anyString(), anyString(), anyString());
        return transport;
    }

    @NonNull
    private Notifications createNotifications(final AtomicReference<Object[]> errorArgs,
                                              final AtomicReference<Object[]> clearArgs) {
        Notifications notifications = mock(Notifications.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (errorArgs != null) {
                    errorArgs.set(invocation.getArguments());
                }
                return null;
            }
        }).when(notifications).showMailError(any(Context.class), anyInt());

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (clearArgs != null) {
                    clearArgs.set(invocation.getArguments());
                }
                return null;
            }
        }).when(notifications).removeMailError(any(Context.class));

        return notifications;
    }

    /**
     * Tests normal mailer behaviour.
     *
     * @throws Exception when fails
     */
    public void testSend() throws Exception {
        AtomicReference<Object[]> initArgs = new AtomicReference<>();
        AtomicReference<Object[]> sendArgs = new AtomicReference<>();
        AtomicReference<Object[]> errorArgs = new AtomicReference<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(null, null);
        MailTransport transport = createMailTransport(initArgs, sendArgs);
        ActivityLog log = mock(ActivityLog.class);

        populatePreferences();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, log);
        mailer.send(new MailMessage("+12345678901", false, 0, 0, false, false, null, 0, 0));

        assertNull(errorArgs.get());
        assertArrayEquals(new Object[]{"user", "decrypted password", "smtp.gmail.com", "465"}, initArgs.get());
        assertArrayEquals(new Object[]{"[SMailer] Outgoing call to +12345678901",
                "<html><head>" +
                        "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                        "You had an outgoing call of 0:00:00 duration. <hr style=\"border: none; background-color: #cccccc; height: 1px;\"> " +
                        "Called: <a href=\"tel:+12345678901\">+12345678901 (John Dou)</a><br>" +
                        "Last known device location: <a href=\"http://maps.google.com/maps/place/0.0,0.0\">0&#176;0'0\"S, 0&#176;0'0\"E</a><br>" +
                        "Sent from Unknown Custom Phone - 5.1.0 - API 22 - 768x1280 at Dec 31, 1969 7:00:00 PM" +
                        "</body></html>",
                "user", "recipient"}, sendArgs.get());
    }

    /**
     * Check that mailer produces notification when user parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testEmptyUser() throws Exception {
        AtomicReference<Object[]> initArgs = new AtomicReference<>();
        AtomicReference<Object[]> sendArgs = new AtomicReference<>();
        AtomicReference<Object[]> errorArgs = new AtomicReference<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errorArgs, null);
        MailTransport transport = createMailTransport(initArgs, sendArgs);
        ActivityLog log = mock(ActivityLog.class);

        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_SENDER_ACCOUNT, null).commit();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, log);
        mailer.send(new MailMessage("+12345678901", false, 0, 0, false, false, null, 0, 0));

        assertNull(initArgs.get());
        assertNull(sendArgs.get());
        assertEquals(R.string.message_error_no_parameters, errorArgs.get()[1]);
    }

    /**
     * Check that mailer produces notification when recipient parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testEmptyRecipients() throws Exception {
        AtomicReference<Object[]> initArgs = new AtomicReference<>();
        AtomicReference<Object[]> sendArgs = new AtomicReference<>();
        AtomicReference<Object[]> errorArgs = new AtomicReference<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errorArgs, null);
        MailTransport transport = createMailTransport(initArgs, sendArgs);
        ActivityLog log = mock(ActivityLog.class);


        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, null).commit();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, log);
        mailer.send(new MailMessage("+12345678901", false, 0, 0, false, false, null, 0, 0));

        assertNull(initArgs.get());
        assertNull(sendArgs.get());
        assertEquals(R.string.message_error_no_parameters, errorArgs.get()[1]);
    }

    /**
     * Check that mailer produces notification when host parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testEmptyHost() throws Exception {
        AtomicReference<Object[]> initArgs = new AtomicReference<>();
        AtomicReference<Object[]> sendArgs = new AtomicReference<>();
        AtomicReference<Object[]> errorArgs = new AtomicReference<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errorArgs, null);
        MailTransport transport = createMailTransport(initArgs, sendArgs);
        ActivityLog log = mock(ActivityLog.class);

        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_EMAIL_HOST, null).commit();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, log);
        mailer.send(new MailMessage("+12345678901", false, 0, 0, false, false, null, 0, 0));

        assertNull(initArgs.get());
        assertNull(sendArgs.get());
        assertEquals(R.string.message_error_no_parameters, errorArgs.get()[1]);
    }

    /**
     * Check that mailer produces notification when port parameter is empty.
     *
     * @throws Exception when fails
     */
    public void testEmptyPort() throws Exception {
        AtomicReference<Object[]> initArgs = new AtomicReference<>();
        AtomicReference<Object[]> sendArgs = new AtomicReference<>();
        AtomicReference<Object[]> errorArgs = new AtomicReference<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errorArgs, null);
        MailTransport transport = createMailTransport(initArgs, sendArgs);
        ActivityLog log = mock(ActivityLog.class);

        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_EMAIL_PORT, null).commit();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, log);
        mailer.send(new MailMessage("+12345678901", false, 0, 0, false, false, null, 0, 0));

        assertNull(initArgs.get());
        assertNull(sendArgs.get());
        assertEquals(R.string.message_error_no_parameters, errorArgs.get()[1]);
    }

    /**
     * Check that mailer produces notification on authorisation exceptions.
     *
     * @throws Exception when fails
     */
    public void testAuthenticationFailedException() throws Exception {
        AtomicReference<Object[]> initArgs = new AtomicReference<>();
        AtomicReference<Object[]> sendArgs = new AtomicReference<>();
        AtomicReference<Object[]> errorArgs = new AtomicReference<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errorArgs, null);
        MailTransport transport = createMailTransport(initArgs, sendArgs);
        doThrow(AuthenticationFailedException.class).when(transport).send(anyString(), anyString(), anyString(), anyString());
        ActivityLog log = mock(ActivityLog.class);

        populatePreferences();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, log);
        mailer.send(new MailMessage("+12345678901", false, 0, 0, false, false, null, 0, 0));

        assertNotNull(initArgs.get());
        assertNull(sendArgs.get());
        assertEquals(R.string.message_error_authentication, errorArgs.get()[1]);
    }

    /**
     * Check that mailer produces notification on other transport exceptions.
     *
     * @throws Exception when fails
     */
    public void testOtherExceptions() throws Exception {
        AtomicReference<Object[]> initArgs = new AtomicReference<>();
        AtomicReference<Object[]> sendArgs = new AtomicReference<>();
        AtomicReference<Object[]> errorArgs = new AtomicReference<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errorArgs, null);
        MailTransport transport = createMailTransport(initArgs, sendArgs);
        doThrow(Exception.class).when(transport).send(anyString(), anyString(), anyString(), anyString());
        ActivityLog log = mock(ActivityLog.class);

        populatePreferences();

        /* test start */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, log);
        mailer.send(new MailMessage("+12345678901", false, 0, 0, false, false, null, 0, 0));

        assertNotNull(initArgs.get());
        assertNull(sendArgs.get());
        assertEquals(R.string.message_error_general, errorArgs.get()[1]);
    }

    /**
     * When mailer parameters goes back to normal last notification should be removed.
     *
     * @throws Exception when fails
     */
    public void testClearNotificationExceptions() throws Exception {
        AtomicReference<Object[]> initArgs = new AtomicReference<>();
        AtomicReference<Object[]> sendArgs = new AtomicReference<>();
        AtomicReference<Object[]> errorArgs = new AtomicReference<>();
        AtomicReference<Object[]> clearArgs = new AtomicReference<>();

        Cryptor cryptor = createCryptor();
        Notifications notifications = createNotifications(errorArgs, clearArgs);

        MailTransport transport = createMailTransport(initArgs, sendArgs);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String subject = invocation.getArgumentAt(0, String.class);
                if (subject.equals("[SMailer] Outgoing call to bad_phone")) {
                    throw new Exception("bad_phone");
                }
                return null;
            }
        }).when(transport).send(anyString(), anyString(), anyString(), anyString());
        ActivityLog log = mock(ActivityLog.class);

        populatePreferences();
        getPreferences(getContext()).edit().putString(KEY_PREF_EMAIL_HOST, "good host").commit();

        /* bad_phone produces notification */

        Mailer mailer = new Mailer(getContext(), transport, cryptor, notifications, log);

        mailer.send(new MailMessage("bad_phone", false, 0, 0, false, false, null, 0, 0));
        assertEquals(R.string.message_error_general, errorArgs.get()[1]);
        assertNull(clearArgs.get());

        /* good_phone removes it */

        errorArgs.set(null);
        clearArgs.set(null);

        mailer.send(new MailMessage("good_phone", false, 0, 0, false, false, null, 0, 0));

        assertNull(errorArgs.get());
        assertNotNull(clearArgs.get());
    }

}