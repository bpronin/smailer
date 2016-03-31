package com.bopr.android.smailer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sun.mail.util.MailConnectException;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import static com.bopr.android.smailer.Contacts.getContactName;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.getDeviceName;
import static com.bopr.android.smailer.Settings.getPreferences;
import static com.bopr.android.smailer.util.Util.isAnyEmpty;


/**
 * Sends out {@link MailMessage}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Mailer {

    private static final String TAG = "Mailer";
    private final Context context;
    private final MailTransport transport;
    private final Cryptor cryptor;
    private final Notifications notifications;
    private final Database database;

    public Mailer(Context context, MailTransport transport, Cryptor cryptor,
                  Notifications notifications,
                  Database database) {
        this.context = context;
        this.transport = transport;
        this.cryptor = cryptor;
        this.notifications = notifications;
        this.database = database;
    }

    public Mailer(Context context, Database database) {
        this(context, new MailTransport(), new Cryptor(context), new Notifications(context),
                database);
    }

    /**
     * Sends out a message.
     *
     * @param message email message
     */
    public void send(MailMessage message) {
        Log.d(TAG, "Sending mail: " + message);

        MailerProperties mp = new MailerProperties(getPreferences(context));
        if (isAnyEmpty(mp.getHost(), mp.getPort(), mp.getUser(), mp.getRecipients())) {
            logError(null);
            failed(message, "Invalid parameters", R.string.notification_error_no_parameters);
        } else {
            MailFormatter formatter = createFormatter(message, mp);
            transport.init(mp.getUser(), cryptor.decrypt(mp.getPassword()), mp.getHost(), mp.getPort());
            try {
                transport.send(formatter.getSubject(), formatter.getBody(), mp.getUser(),
                        mp.getRecipients());

                success(message);
            } catch (AuthenticationFailedException x) {
                logError(x);
                failed(message, x.toString(), R.string.notification_error_authentication);
            } catch (MailConnectException x) {
                logError(x);
                failed(message, x.toString(), R.string.notification_error_connect);
            } catch (MessagingException x) {
                logError(x);
                failed(message, x.toString(), R.string.notification_error_mail_general);
            } catch (Throwable x) {
                logError(x);
                failed(message, x.toString(), R.string.notification_error_internal);
            }
        }
    }

    /**
     * Sends out all previously unsent messages.
     */
    public void sendAllUnsent() {
        for (MailMessage message : database.getUnsentMessages().getAll()) {
            send(message);
        }
    }

    @NonNull
    private MailFormatter createFormatter(MailMessage message, MailerProperties mp) {
        MailFormatter formatter = new MailFormatter(message, context.getResources(),
                getContactName(context, message.getPhone()), getDeviceName());
        formatter.setContentOptions(mp.getContentOptions());
        if (mp.getMessageLocale() != null) {
            formatter.setLocale(mp.getMessageLocale());
        }
        return formatter;
    }

    private void success(MailMessage message) {
        message.setSent(true);
        message.setDetails(null);
        database.updateMessage(message);
        notifications.hideMailError();
        if (getPreferences(context).getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false)) {
            notifications.showMailSuccess();
        }
    }

    private void failed(MailMessage message, String details, int notificationMessage) {
        message.setSent(false);
        message.setDetails(details);
        database.updateMessage(message);
        notifications.showMailError(notificationMessage, message.getId());
    }

    private void logError(Throwable x) {
        Log.e(TAG, "Send failed", x);
    }

}
