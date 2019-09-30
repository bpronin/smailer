package com.bopr.android.smailer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.gmail.GmailScopes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.bopr.android.smailer.Notifications.ACTION_SHOW_MAIN;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_RECIPIENTS;
import static com.bopr.android.smailer.PhoneEvent.REASON_ACCEPT;
import static com.bopr.android.smailer.PhoneEvent.STATE_IGNORED;
import static com.bopr.android.smailer.PhoneEvent.STATE_PENDING;
import static com.bopr.android.smailer.PhoneEvent.STATE_PROCESSED;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.PREF_MARK_SMS_AS_READ;
import static com.bopr.android.smailer.Settings.PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.util.AndroidUtil.isValidEmailAddressList;
import static com.bopr.android.smailer.util.ContentUtils.getContactName;
import static com.bopr.android.smailer.util.ContentUtils.markSmsAsRead;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Sends out email for phone events.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess") /* tests mocking required public */
public class CallProcessor {

    private static Logger log = LoggerFactory.getLogger("CallProcessor");

    private final Settings settings;
    private final Context context;
    private final GoogleMailSupport transport;
    private final Notifications notifications;
    private final Database database;
    private final GeoLocator locator;

    CallProcessor(Context context, GoogleMailSupport transport, Notifications notifications,
                  Database database, GeoLocator locator) {
        this.context = context;
        this.transport = transport;
        this.notifications = notifications;
        this.database = database;
        this.locator = locator;
        settings = new Settings(context);
    }

    CallProcessor(Context context, Database database, GeoLocator locator) {
        this(context, new GoogleMailSupport(context), new Notifications(context), database, locator);
    }

    /**
     * Sends out a mail for event.
     *
     * @param event email event
     */
    public void process(PhoneEvent event) {
        log.debug("Processing event: " + event);

        event.setLocation(locator.getLocation());
        event.setStateReason(settings.getFilter().test(event));
        database.putEvent(event);

        if (event.getStateReason() == REASON_ACCEPT) {
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
    public void processPending() {
        log.debug("Processing pending events");

        final List<PhoneEvent> events = new LinkedList<>();
        database.getPendingEvents().forEach(new Consumer<PhoneEvent>() {

            @Override
            public void accept(PhoneEvent event) {
                if (event.getStateReason() == REASON_ACCEPT) {
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
        event.setState(STATE_IGNORED);
        database.putEvent(event);

        log.debug("Event ignored: " + event);
    }

    private boolean startMailSession(boolean silent) {
        log.debug("Starting session");

        try {
            requireRecipient(silent);
        } catch (Exception x) {
            log.warn("Invalid recipients");
            return false;
        }

        try {
            transport.init(requireSender(silent), GmailScopes.GMAIL_SEND);
            return true;
        } catch (IllegalArgumentException x) {
            log.error("Failed starting session: ", x);
            if (!silent) {
                notifications.showMailError(R.string.account_not_registered, ACTION_SHOW_MAIN);
            }
            return false;
        }
    }

    private void sendMail(PhoneEvent event, boolean silent) {
        log.debug("Sending mail: " + event);

        try {
            MailFormatter formatter = createFormatter(event);

            MailMessage message = new MailMessage();
            message.setSubject(formatter.formatSubject());
            message.setBody(formatter.formatBody());
            message.setRecipients(requireRecipient(silent));
            message.setReplyTo(settings.getString(PREF_REMOTE_CONTROL_ACCOUNT, null));

            transport.send(message);

            handleSendSuccess(event);
        } catch (UserRecoverableAuthIOException x) {
            removeSelectedAccount();
            handleSendError(event, x, R.string.need_google_permission, silent);
        } catch (Throwable x) {
            handleSendError(event, x, R.string.check_your_settings, silent);
        }
    }

    @NonNull
    private String requireSender(boolean silent) {
        String s = settings.getString(PREF_SENDER_ACCOUNT, null);
        if (isEmpty(s)) {
            if (!silent) {
                notifications.showMailError(R.string.no_account_specified, ACTION_SHOW_MAIN);
            }
            throw new IllegalArgumentException("Account not specified");
        }
        return s;
    }

    @NonNull
    private String requireRecipient(boolean silent) {
        String s = settings.getString(PREF_RECIPIENTS_ADDRESS, null);

        if (isEmpty(s)) {
            if (!silent) {
                notifications.showMailError(R.string.no_recipients_specified, ACTION_SHOW_RECIPIENTS);
            }
            throw new IllegalArgumentException("Recipients not specified");
        }

        if (!isValidEmailAddressList(s)) {
            if (!silent) {
                notifications.showMailError(R.string.invalid_recipient, ACTION_SHOW_RECIPIENTS);
            }
            throw new IllegalArgumentException("Recipients are invalid");
        }

        return s;
    }

    @NonNull
    private MailFormatter createFormatter(PhoneEvent event) {
        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(new Date());
        formatter.setContactName(getContactName(context, event.getPhone()));
        formatter.setDeviceName(settings.getDeviceName());
        formatter.setContentOptions(settings.getStringSet(PREF_EMAIL_CONTENT, null));
        formatter.setServiceAccount(settings.getString(PREF_REMOTE_CONTROL_ACCOUNT, null));

        String locale = settings.getString(PREF_EMAIL_LOCALE, null);
        if (locale != null) {   //todo: why not to set null locale?
            formatter.setLocale(locale);
        }
        return formatter;
    }

    private void handleSendSuccess(PhoneEvent event) {
        event.setState(STATE_PROCESSED);
        database.putEvent(event);

        notifications.hideLastError();

        if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS, false)) {
            notifications.showMessage(R.string.email_send, ACTION_SHOW_MAIN);
        }

        if (settings.getBoolean(PREF_MARK_SMS_AS_READ, false)) {
            markSmsAsRead(context, event);
        }
    }

    private void handleSendError(PhoneEvent event, Throwable error, int notification, boolean silent) {
        log.warn("Send failed: " + event, error);

        event.setState(STATE_PENDING);
        database.putEvent(event);

        if (!silent) {
            notifications.showMailError(notification, Notifications.ACTION_SHOW_MAIN);
        }
    }

    private void removeSelectedAccount() {
        settings.edit().putString(PREF_SENDER_ACCOUNT, null).apply();
    }
}
