package com.bopr.android.smailer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bopr.android.smailer.util.AndroidUtil;
import com.sun.mail.util.MailConnectException;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import static com.bopr.android.smailer.Contacts.getContactName;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_CONNECTION;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_MAIN;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_RECIPIENTS;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_SERVER;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.getDeviceName;
import static com.bopr.android.smailer.Settings.getPreferences;
import static com.bopr.android.smailer.util.Util.isEmpty;


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
     * Sends out a mail message.
     *
     * @param message email message
     */
    public void send(MailMessage message) {
        doSend(message, false);
    }

    /**
     * Sends out all previously unsent messages.
     */
    public void sendAllUnsent() {
        for (MailMessage message : database.getUnsentMessages().getAll()) {
            doSend(message, true);
        }
    }

    /**
     * Sends out a message.
     *
     * @param message email message
     * @param silent  if true do not show notifications
     */
    private void doSend(MailMessage message, boolean silent) {
        Log.d(TAG, "Sending mail: " + message);

        MailerProperties pp = new MailerProperties(getPreferences(context));
        if (checkProperties(pp, message, silent) && checkConnection(message, silent)) {
            MailFormatter formatter = createFormatter(message, pp);

            transport.init(pp.getUser(), cryptor.decrypt(pp.getPassword()), pp.getHost(), pp.getPort());
            try {
                transport.send(formatter.getSubject(), formatter.getBody(), pp.getUser(), pp.getRecipients());

                success(message);
            } catch (AuthenticationFailedException x) {
                failed(x, x.toString(), message, R.string.notification_error_authentication, ACTION_SHOW_SERVER, silent);
            } catch (MailConnectException x) {
                failed(x, x.toString(), message, R.string.notification_error_connect, ACTION_SHOW_SERVER, silent);
            } catch (MessagingException x) {
                failed(x, x.toString(), message, R.string.notification_error_mail_general, ACTION_SHOW_SERVER, silent);
            } catch (Throwable x) {
                failed(x, x.toString(), message, R.string.notification_error_internal, ACTION_SHOW_MAIN, silent);
            }
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

    private boolean checkProperties(MailerProperties properties, MailMessage message,
                                    boolean silent) {
        if (isEmpty(properties.getHost())) {
            failed(null, "Host not specified", message, R.string.notification_error_no_host, ACTION_SHOW_SERVER, silent);
            return false;
        } else if (isEmpty(properties.getPort())) {
            failed(null, "Port not specified", message, R.string.notification_error_no_port, ACTION_SHOW_SERVER, silent);
            return false;
        } else if (isEmpty(properties.getUser())) {
            failed(null, "Account not specified", message, R.string.notification_error_no_account, ACTION_SHOW_SERVER, silent);
            return false;
        } else if (isEmpty(properties.getRecipients())) {
            failed(null, "Recipients not specified", message, R.string.notification_error_no_recipients, ACTION_SHOW_RECIPIENTS, silent);
            return false;
        }
        return true;
    }

    private boolean checkConnection(MailMessage message, boolean silent) {
        if (!AndroidUtil.hasInternetConnection(context)) {
            failed(null, "No internet connection", message, R.string.notification_error_no_connection, ACTION_SHOW_CONNECTION, silent);
            return false;
        }
        return true;
    }

    private void success(MailMessage message) {
        message.setSent(true);
        message.setDetails(null);
        database.updateMessage(message);
        notifications.hideMailError();
        if (getPreferences(context).getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false)) {
            notifications.showMailSuccess(message.getId());
        }
    }

    private void failed(Throwable error, String details, MailMessage message, int notification,
                        int action, boolean silent) {
        Log.e(TAG, "Send failed. " + message, error);

        message.setSent(false);
        message.setDetails(details);
        database.updateMessage(message);
        if (!silent) {
            notifications.showMailError(notification, message.getId(), action);
        }
    }

}
