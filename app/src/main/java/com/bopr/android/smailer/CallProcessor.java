package com.bopr.android.smailer;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.bopr.android.smailer.Notifications.TARGET_MAIN;
import static com.bopr.android.smailer.Notifications.TARGET_RECIPIENTS;
import static com.bopr.android.smailer.Notifications.Target;
import static com.bopr.android.smailer.PhoneEvent.REASON_ACCEPTED;
import static com.bopr.android.smailer.PhoneEvent.STATE_IGNORED;
import static com.bopr.android.smailer.PhoneEvent.STATE_PROCESSED;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.PREF_MARK_SMS_AS_READ;
import static com.bopr.android.smailer.Settings.PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.util.AddressUtil.isValidEmailAddressList;
import static com.bopr.android.smailer.util.AndroidUtil.getAccount;
import static com.bopr.android.smailer.util.ContentUtils.getContactName;
import static com.bopr.android.smailer.util.ContentUtils.markSmsAsRead;
import static com.bopr.android.smailer.util.TextUtil.isNullOrEmpty;
import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;

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
    private final GoogleMail transport;
    private final Notifications notifications;
    private final Database database;
    private final GeoLocator locator;

    CallProcessor(Context context, Database database) {
        this(context, new GoogleMail(context), new Notifications(context), database,
                new GeoLocator(context, database));
    }

    CallProcessor(Context context, GoogleMail transport, Notifications notifications,
                  Database database, GeoLocator locator) {
        this.context = context;
        this.transport = transport;
        this.notifications = notifications;
        this.database = database;
        this.locator = locator;
        settings = new Settings(context);
    }

    /**
     * Sends out a mail for event.
     *
     * @param event email event
     */
    public void process(@NonNull PhoneEvent event) {
        log.debug("Processing event: " + event);

        event.setLocation(locator.getLocation());
        event.setStateReason(settings.getFilter().test(event));
        if (event.getStateReason() != REASON_ACCEPTED) {
            event.setState(STATE_IGNORED);
        } else if (startMailSession(false) && sendMail(event, false)) {
            event.setState(STATE_PROCESSED);
        }

        database.putEvent(event);
        database.notifyChanged();
    }

    /**
     * Sends out email for all pending events.
     */
    public void processPending() {
        log.debug("Processing pending events");

        List<PhoneEvent> events = database.getPendingEvents().toList();

        if (events.isEmpty()) {
            log.debug("No pending events");
        } else {
            if (startMailSession(true)) {
                for (PhoneEvent event : events) {
                    if (sendMail(event, true)) {
                        event.setState(STATE_PROCESSED);
                        database.putEvent(event);
                    }
                }
            }

            database.notifyChanged();
        }
    }

    private boolean startMailSession(boolean silent) {
        log.debug("Starting session");

        try {
            requireRecipient(silent);
            transport.login(requireAccount(silent), GMAIL_SEND);
            return true;
        } catch (Exception x) {
            log.warn("Failed starting mail session: ", x);

            return false;
        }
    }

    private boolean sendMail(PhoneEvent event, boolean silent) {
        log.debug("Sending mail: " + event);

        try {
            sendMessage(event, requireRecipient(silent));

            notifications.hideAllErrors();

            if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS, false)) {
                notifications.showMessage(R.string.email_send, TARGET_MAIN);
            }

            if (settings.getBoolean(PREF_MARK_SMS_AS_READ, false)) {
                markSmsAsRead(context, event);
            }

            return true;
        } catch (UserRecoverableAuthIOException x) {
            log.warn("Failed sending mail: ", x);

            showErrorNotification(R.string.need_google_permission, TARGET_MAIN, silent);
            return false;
        } catch (Exception x) {
            log.warn("Failed sending mail: ", x);

            return false;
        }
    }

    private Account requireAccount(boolean silent) throws Exception {
        Account account = getAccount(context, settings.getString(PREF_SENDER_ACCOUNT));
        if (account == null) {
            showErrorNotification(R.string.sender_account_not_found, TARGET_MAIN, silent);
            throw new Exception("Account not found");
        }
        return account;
    }

    private String requireRecipient(boolean silent) throws Exception {
        String s = settings.getString(PREF_RECIPIENTS_ADDRESS);

        if (isNullOrEmpty(s)) {
            showErrorNotification(R.string.no_recipients_specified, TARGET_RECIPIENTS, silent);
            throw new Exception("Recipients not specified");
        }

        if (!isValidEmailAddressList(s)) {
            showErrorNotification(R.string.invalid_recipient, TARGET_RECIPIENTS, silent);
            throw new Exception("Recipients are invalid");
        }

        return s;
    }

    private void sendMessage(PhoneEvent event, String recipient) throws IOException {
        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(new Date());
        formatter.setContactName(getContactName(context, event.getPhone()));
        formatter.setDeviceName(settings.getDeviceName());
        formatter.setContentOptions(settings.getStringSet(PREF_EMAIL_CONTENT));
        formatter.setServiceAccount(settings.getString(PREF_REMOTE_CONTROL_ACCOUNT));
        formatter.setLocale(settings.getLocale());

        MailMessage message = new MailMessage();
        message.setSubject(formatter.formatSubject());
        message.setBody(formatter.formatBody());
        message.setRecipients(recipient);
        message.setReplyTo(settings.getString(PREF_REMOTE_CONTROL_ACCOUNT));

        transport.send(message);
    }

    private void showErrorNotification(@StringRes int reason, @Target int target,
                                       boolean silent) {
        if (!silent) {
            notifications.showMailError(reason, target);
        }
    }

}
