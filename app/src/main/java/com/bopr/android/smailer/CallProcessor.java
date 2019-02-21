package com.bopr.android.smailer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bopr.android.smailer.mail.GmailTransport;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import static com.bopr.android.smailer.Contacts.getContactName;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_MAIN;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_MARK_SMS_AS_READ;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;

/**
 * Sends out email for phone events.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class CallProcessor {

    private static Logger log = LoggerFactory.getLogger("CallProcessor");

    private final Settings settings;
    private final Context context;
    private final GmailTransport transport;
    private final Notifications notifications;
    private final Database database;
    private final GeoLocator locator;

    CallProcessor(Context context, GmailTransport transport, Cryptor cryptor, Notifications notifications,
                  Database database, GeoLocator locator) {
        this.context = context;
        this.transport = transport;
        this.notifications = notifications;
        this.database = database;
        this.locator = locator;
        settings = new Settings(context);
    }

    CallProcessor(Context context, Database database, GeoLocator locator) {
        this(context, new GmailTransport(context), new Cryptor(context), new Notifications(context), database, locator);
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
            if (startMailSession(false)) {
                sendMail(event, false);
            }
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

        final PhoneEventFilter filter = settings.getFilter();
        final List<PhoneEvent> events = new LinkedList<>();
        database.getPendingEvents().iterate(new Consumer<PhoneEvent>() {

            @Override
            public void accept(PhoneEvent event) {
                if (filter.test(event)) {
                    events.add(event);
                } else {
                    ignoreEvent(event);
                }
            }
        });

        if (!events.isEmpty()) {
            if (startMailSession(true)) {
                for (PhoneEvent event : events) {
                    sendMail(event, true);
                }
            }
        } else {
            log.debug("No pending events found");
        }

        database.notifyChanged();
    }

    private void ignoreEvent(PhoneEvent event) {
        event.setState(PhoneEvent.State.IGNORED);
        database.putEvent(event);

        log.debug("Event ignored: " + event);
    }

    private boolean startMailSession(boolean silent) {
        log.debug("Starting session");
        try {
            transport.init(settings.getString(KEY_PREF_SENDER_ACCOUNT, ""));
            return true;
        } catch (IllegalAccessException x) {
            log.error("Failed starting session: ", x);
            if (!silent) {
                notifications.showMailError(R.string.no_account_specified, null, ACTION_SHOW_MAIN);
            }
            return false;
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

        try {
            MailFormatter formatter = createFormatter(event);
            transport.send(formatter.formatSubject(), formatter.formatBody(), null,
                    settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""));

            handleSuccess(event);
//        } catch (AuthenticationFailedException x) {
//            handleError(event, x, R.string.user_password_not_accepted, ACTION_SHOW_SERVER, silent);
//        } catch (MailConnectException x) {
//            handleError(event, x, R.string.cannot_connect_mail_server, ACTION_SHOW_SERVER, silent);
//        } catch (MessagingException x) {
//            handleError(event, x, R.string.unable_send_email, ACTION_SHOW_SERVER, silent);
        } catch (UserRecoverableAuthIOException x) {
            AuthorizationHelper.removeSelectedAccount(context);
            handleError(event, x, R.string.need_google_permission, ACTION_SHOW_MAIN, silent);
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
