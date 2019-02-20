package com.bopr.android.smailer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bopr.android.smailer.Database.PhoneEventCursor;
import com.bopr.android.smailer.mail.JavaMailTransport;
import com.sun.mail.util.MailConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import static com.bopr.android.smailer.Contacts.getContactName;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_MAIN;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_SERVER;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_MARK_SMS_AS_READ;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;

/**
 * Sends out email for phone events.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class CallProcessor {

    private static Logger log = LoggerFactory.getLogger("CallProcessor");

    private final Settings settings;
    private final Context context;
    private final JavaMailTransport transport;
    private final Cryptor cryptor;
    private final Notifications notifications;
    private final Database database;
    private final GeoLocator locator;

    CallProcessor(Context context, JavaMailTransport transport, Cryptor cryptor, Notifications notifications,
                  Database database, GeoLocator locator) {
        this.context = context;
        this.transport = transport;
        this.cryptor = cryptor;
        this.notifications = notifications;
        this.database = database;
        this.locator = locator;
        settings = new Settings(context);
    }

    CallProcessor(Context context, Database database, GeoLocator locator) {
        this(context, new JavaMailTransport(), new Cryptor(context), new Notifications(context), database, locator);
    }

    /**
     * Sends out a mail for event.
     *
     * @param event email event
     */
    void process(PhoneEvent event) {
        log.debug("Processing event: " + event);

        event.setLocation(locator.getLocation());
        database.putEvent(event);

        if (settings.getFilter().test(event)) {
            startMailSession();
            sendMail(event, false);
        } else {
            ignoreEvent(event);
        }

        database.notifyChanged();
    }

    /**
     * Sends out email for all pending events.
     */
    void processPending() {
        log.debug("Processing pending events");

        startMailSession();

        PhoneEventCursor events = database.getPendingEvents();
        if (events.getCount() == 0) {
            log.debug("No pending events found");
        } else {
            final PhoneEventFilter filter = settings.getFilter();

            events.iterate(new Consumer<PhoneEvent>() {

                @Override
                public void accept(PhoneEvent event) {
                    if (filter.test(event)) {
                        sendMail(event, true);
                    } else {
                        ignoreEvent(event);
                    }
                }
            });

            database.notifyChanged();
        }
    }

    private void ignoreEvent(PhoneEvent event) {
        event.setState(PhoneEvent.State.IGNORED);
        database.putEvent(event);

        log.debug("Event ignored: " + event);
    }

    private void startMailSession() {
        log.debug("Starting session");

        transport.startSession(
                settings.getString(KEY_PREF_SENDER_ACCOUNT, ""),
                cryptor.decrypt(settings.getString(KEY_PREF_SENDER_PASSWORD, "")),
                settings.getString(KEY_PREF_EMAIL_HOST, ""),
                settings.getString(KEY_PREF_EMAIL_PORT, ""));
    }

    /**
     * Sends out a event.
     *
     * @param event  email event
     * @param silent if true do not show notifications
     */
    private void sendMail(PhoneEvent event, boolean silent) {
        log.debug("Sending mail: " + event);

        try {
            MailFormatter formatter = createFormatter(event);
            transport.send(formatter.formatSubject(), formatter.formatBody(), null,
                    settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""));

            handleSuccess(event);
        } catch (AuthenticationFailedException x) {
            handleError(event, x, R.string.user_password_not_accepted, ACTION_SHOW_SERVER, silent);
        } catch (MailConnectException x) {
            handleError(event, x, R.string.cannot_connect_mail_server, ACTION_SHOW_SERVER, silent);
        } catch (MessagingException x) {
            handleError(event, x, R.string.unable_send_email, ACTION_SHOW_SERVER, silent);
        } catch (Throwable x) {
            handleError(event, x, R.string.internal_error, ACTION_SHOW_MAIN, silent);
        }
    }

    @NonNull
    private MailFormatter createFormatter(PhoneEvent event) {
        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(new Date());
        formatter.setContactName(getContactName(context, event.getPhone()));
        formatter.setDeviceName(settings.getDeviceName());
        formatter.setContentOptions(settings.getStringSet(KEY_PREF_EMAIL_CONTENT, null));

        String locale = settings.getString(KEY_PREF_EMAIL_LOCALE, null);
        if (locale != null) {
            formatter.setLocale(locale);
        }
        return formatter;
    }

    private void markSmsAsRead(final PhoneEvent event) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://sms/inbox");

        Cursor cursor = contentResolver.query(uri, null, "read = 0 AND address = ? AND date_sent = ?",
                new String[]{event.getPhone(), String.valueOf(event.getStartTime())}, null);
        if (cursor == null) {
            throw new NullPointerException("Cannot obtain cursor");
        }

        try {
            if (cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndex("_id"));

                ContentValues values = new ContentValues();
                values.put("read", true);
                values.put("seen", true);
                contentResolver.update(uri, values, "_id=" + id, null);

                log.debug("SMS marked as read. " + event);
            }
        } catch (Exception e) {
            log.error("Mark SMS as read failed. ", e);
        } finally {
            cursor.close();
        }
    }

    private void handleSuccess(PhoneEvent event) {
        event.setState(PhoneEvent.State.PROCESSED);
        database.putEvent(event);

        notifications.hideMailError();

        if (settings.getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false)) {
            notifications.showMailSuccess(event.getId());
        }
        if (settings.getBoolean(KEY_PREF_MARK_SMS_AS_READ, false)) {
            markSmsAsRead(event);
        }
    }

    private void handleError(PhoneEvent event, Throwable error, int notification, int action, boolean silent) {
        log.warn("Send failed: " + event, error);

        event.setState(PhoneEvent.State.PENDING);
        database.putEvent(event);

        if (!silent) {
            notifications.showMailError(notification, event.getId(), action);
        }
    }
}
