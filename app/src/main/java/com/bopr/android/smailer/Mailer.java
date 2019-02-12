package com.bopr.android.smailer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.sun.mail.util.MailConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import static com.bopr.android.smailer.Contacts.getContactName;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_CONNECTION;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_MAIN;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_RECIPIENTS;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_SERVER;
import static com.bopr.android.smailer.Settings.KEY_PREF_MARK_SMS_AS_READ;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.getDeviceName;
import static com.bopr.android.smailer.Settings.getPreferences;
import static com.bopr.android.smailer.util.AndroidUtil.hasInternetConnection;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Sends out {@link PhoneEvent}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class Mailer {

    private static Logger log = LoggerFactory.getLogger("Mailer");

    private final Context context;
    private final MailTransport transport;
    private final Cryptor cryptor;
    private final Notifications notifications;
    private final Database database;

    Mailer(Context context, MailTransport transport, Cryptor cryptor,
           Notifications notifications,
           Database database) {
        this.context = context;
        this.transport = transport;
        this.cryptor = cryptor;
        this.notifications = notifications;
        this.database = database;
    }

    Mailer(Context context, Database database) {
        this(context, new MailTransport(), new Cryptor(context), new Notifications(context), database);
    }

    /**
     * Sends out a mail event.
     *
     * @param event email event
     */
    void send(PhoneEvent event) {
        doSend(event, false);
    }

    /**
     * Sends out all previously unsent messages.
     */
    void sendAllUnsent() {
        List<PhoneEvent> events = database.getUnsentEvents().getAll();

        log.debug("Resending " + events.size() + " messages");
        // TODO: 13.02.2019 send all in one transport session
        for (PhoneEvent event : events) {
            doSend(event, true);
        }
    }

    /**
     * Sends out a event.
     *
     * @param event  email event
     * @param silent if true do not show notifications
     */
    private void doSend(PhoneEvent event, boolean silent) {
        log.debug("Sending mail: " + event);

        MailerProperties pp = new MailerProperties(getPreferences(context));
        if (checkProperties(pp, event, silent) && checkConnection(event, silent)) {
            MailFormatter formatter = createFormatter(event, pp);

            transport.startSession(pp.getUser(), cryptor.decrypt(pp.getPassword()), pp.getHost(), pp.getPort());
            try {
                transport.send(formatter.formatSubject(), formatter.formatBody(), pp.getUser(), pp.getRecipients());

                success(event);
            } catch (AuthenticationFailedException x) {
                failed(x, x.toString(), event, R.string.notification_error_authentication, ACTION_SHOW_SERVER, silent);
            } catch (MailConnectException x) {
                failed(x, x.toString(), event, R.string.notification_error_connect, ACTION_SHOW_SERVER, silent);
            } catch (MessagingException x) {
                failed(x, x.toString(), event, R.string.notification_error_mail_general, ACTION_SHOW_SERVER, silent);
            } catch (Throwable x) {
                failed(x, x.toString(), event, R.string.notification_error_internal, ACTION_SHOW_MAIN, silent);
            }
        }
    }

    @NonNull
    private MailFormatter createFormatter(PhoneEvent event, MailerProperties mp) {
        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(new Date());
        formatter.setContactName(getContactName(context, event.getPhone()));
        formatter.setDeviceName(getDeviceName(context));
        formatter.setContentOptions(mp.getContentOptions());

        if (mp.getMessageLocale() != null) {
            formatter.setLocale(mp.getMessageLocale());
        }
        return formatter;
    }

    private boolean checkProperties(MailerProperties properties, PhoneEvent event, boolean silent) {
        if (isEmpty(properties.getHost())) {
            failed(null, "Host not specified", event, R.string.notification_error_no_host, ACTION_SHOW_SERVER, silent);
            return false;
        } else if (isEmpty(properties.getPort())) {
            failed(null, "Port not specified", event, R.string.notification_error_no_port, ACTION_SHOW_SERVER, silent);
            return false;
        } else if (isEmpty(properties.getUser())) {
            failed(null, "Account not specified", event, R.string.notification_error_no_account, ACTION_SHOW_SERVER, silent);
            return false;
        } else if (isEmpty(properties.getRecipients())) {
            failed(null, "Recipients not specified", event, R.string.notification_error_no_recipients, ACTION_SHOW_RECIPIENTS, silent);
            return false;
        }
        return true;
    }

    private boolean checkConnection(PhoneEvent event, boolean silent) {
        if (!hasInternetConnection(context)) {
            failed(null, "No internet connection", event, R.string.notification_error_no_connection, ACTION_SHOW_CONNECTION, silent);
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

    private void success(PhoneEvent event) {
        event.setState(PhoneEvent.State.PROCESSED);
        event.setDetails(null);
        database.putEvent(event);
        notifications.hideMailError();

        SharedPreferences preferences = getPreferences(context);
        if (preferences.getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false)) {
            notifications.showMailSuccess(event.getId());
        }
        if (preferences.getBoolean(KEY_PREF_MARK_SMS_AS_READ, false)) {
            markSmsAsRead(event);
        }
    }

    private void failed(Throwable error, String details, PhoneEvent event, int notification,
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
