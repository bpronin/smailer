package com.bopr.android.smailer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sun.mail.util.MailConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import androidx.annotation.NonNull;

import static com.bopr.android.smailer.Contacts.getContactName;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_CONNECTION;
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
import static com.bopr.android.smailer.util.AndroidUtil.hasInternetConnection;

/**
 * Sends out {@link PhoneEvent}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class CallProcessor {

    private static Logger log = LoggerFactory.getLogger("CallProcessor");

    private final Settings settings;
    private final Context context;
    private final MailTransport transport;
    private final Cryptor cryptor;
    private final Notifications notifications;
    private final Database database;
    private final GeoLocator locator;

    CallProcessor(Context context, MailTransport transport, Cryptor cryptor, Notifications notifications,
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
        this(context, new MailTransport(), new Cryptor(context), new Notifications(context), database, locator);
    }

    /**
     * Sends out a mail event.
     *
     * @param event email event
     */
    void process(PhoneEvent event) {
        log.debug("Processing event: " + event);

        event.setLocation(locator.getLocation());
        database.putEvent(event);

        if (settings.getFilter().accept(event)) {
            sendMail(event, false);
        } else {
            event.setState(PhoneEvent.State.IGNORED);
            database.putEvent(event);

            log.debug("Event ignored");
        }
    }

    /**
     * Sends out all previously unsent messages.
     */
    void processAll() {
        List<PhoneEvent> events = database.getUnsentEvents().findAll();

        log.debug("Sending " + events.size() + " messages");
        // TODO: 13.02.2019 send all in one transport session
        for (PhoneEvent event : events) {
            sendMail(event, true);
        }
    }

    /**
     * Sends out a event.
     *
     * @param event  email event
     * @param silent if true do not show notifications
     */
    private void sendMail(PhoneEvent event, boolean silent) {
        log.debug("Sending mail: " + event);

        if (checkInternetConnection(event, silent) && checkPreferences(event, silent)) {
            MailFormatter formatter = createFormatter(event);

            String host = settings.getString(KEY_PREF_EMAIL_HOST, "");
            String port = settings.getString(KEY_PREF_EMAIL_PORT, "");
            String user = settings.getString(KEY_PREF_SENDER_ACCOUNT, "");
            String password = settings.getString(KEY_PREF_SENDER_PASSWORD, "");
            String recipients = settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, "");

            transport.startSession(user, cryptor.decrypt(password), host, port);
            try {
                transport.send(formatter.formatSubject(), formatter.formatBody(), user, recipients);
                handleSuccess(event);
            } catch (AuthenticationFailedException x) {
                handleError(x, x.toString(), event, R.string.notification_error_authentication, ACTION_SHOW_SERVER, silent);
            } catch (MailConnectException x) {
                handleError(x, x.toString(), event, R.string.notification_error_connect, ACTION_SHOW_SERVER, silent);
            } catch (MessagingException x) {
                handleError(x, x.toString(), event, R.string.notification_error_mail_general, ACTION_SHOW_SERVER, silent);
            } catch (Throwable x) {
                handleError(x, x.toString(), event, R.string.notification_error_internal, ACTION_SHOW_MAIN, silent);
            }
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

    private boolean checkPreferences(PhoneEvent event, boolean silent) {
//        if (!settings.contains(KEY_PREF_EMAIL_HOST)) {
//            handleError(null, "Host not specified", event, R.string.notification_error_no_host, ACTION_SHOW_SERVER, silent);
//            return false;
//        } else if (!settings.contains(KEY_PREF_EMAIL_PORT)) {
//            handleError(null, "Port not specified", event, R.string.notification_error_no_port, ACTION_SHOW_SERVER, silent);
//            return false;
//        } else if (!settings.contains(KEY_PREF_SENDER_ACCOUNT)) {
//            handleError(null, "Account not specified", event, R.string.notification_error_no_account, ACTION_SHOW_SERVER, silent);
//            return false;
//        } else if (!settings.contains(KEY_PREF_RECIPIENTS_ADDRESS)) {
//            handleError(null, "Recipients not specified", event, R.string.notification_error_no_recipients, ACTION_SHOW_RECIPIENTS, silent);
//            return false;
//        }
        return true;
    }

    private boolean checkInternetConnection(PhoneEvent event, boolean silent) {
        if (!hasInternetConnection(context)) {
            handleError(null, "No internet connection", event, R.string.notification_error_no_connection, ACTION_SHOW_CONNECTION, silent);
            return false;
        }
        return true;
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
        event.setDetails(null);
        database.putEvent(event);
        notifications.hideMailError();

        if (settings.getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false)) {
            notifications.showMailSuccess(event.getId());
        }
        if (settings.getBoolean(KEY_PREF_MARK_SMS_AS_READ, false)) {
            markSmsAsRead(event);
        }
    }

    private void handleError(Throwable error, String details, PhoneEvent event, int notification,
                             int action, boolean silent) {
        log.error("Send failed. " + event, error);

        event.setState(PhoneEvent.State.PENDING);
        event.setDetails(details);
        database.putEvent(event);

        if (!silent) {
            notifications.showMailError(notification, event.getId(), action);
        }
    }
}
