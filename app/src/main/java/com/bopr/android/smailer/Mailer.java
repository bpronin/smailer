package com.bopr.android.smailer;

import android.content.Context;
import android.util.Log;

import com.sun.mail.util.MailConnectException;

import javax.mail.AuthenticationFailedException;

import static com.bopr.android.smailer.util.StringUtil.isEmpty;


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

    public Mailer(Context context) {
        this(context, new MailTransport(), new Cryptor(context), new Notifications(),
                new Database(context));
    }

    /**
     * Sends out a message.
     *
     * @param message email message
     */
    public void send(MailMessage message) {
        Log.d(TAG, "Sending mail: " + message);

        MailerProperties properties = new MailerProperties(Settings.getPreferences(context));
        if (isEmpty(properties.getHost()) || isEmpty(properties.getPort()) ||
                isEmpty(properties.getUser()) || isEmpty(properties.getRecipients())) {
            notifications.showMailError(context, R.string.message_error_no_parameters);
        } else {
            transport.init(properties.getUser(), cryptor.decrypt(properties.getPassword()),
                    properties.getHost(), properties.getPort());

            MailFormatter formatter = new MailFormatter(message, context.getResources(),
                    properties, Contacts.getContactName(context, message.getPhone()),
                    Settings.getDeviceName());

            try {
                transport.send(formatter.getSubject(), formatter.getBody(), properties.getUser(),
                        properties.getRecipients());

                message.setSent(true);
                database.addMessage(message, null);
                notifications.removeMailError(context);
            } catch (AuthenticationFailedException x) {
                handleError(message, x, R.string.message_error_authentication);
            } catch (MailConnectException x) {
                handleError(message, x, R.string.message_error_connect);
            } catch (Exception x) {
                handleError(message, x, R.string.message_error_general);
            }
        }
    }

    private void handleError(MailMessage message, Exception x, int notificationMessage) {
        Log.e(TAG, "Error sending message: " + message, x);
        database.addMessage(message, x);
        notifications.showMailError(context, notificationMessage);
    }

}
