package com.bopr.android.smailer;

import android.content.Context;
import android.util.Log;

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

    public Mailer(Context context, MailTransport transport, Cryptor cryptor, Notifications notifications) {
        this.context = context;
        this.transport = transport;
        this.cryptor = cryptor;
        this.notifications = notifications;
    }

    public Mailer(Context context) {
        this(context, new MailTransport(), new Cryptor(context), new Notifications());
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

                notifications.removeMailError(context);
            } catch (AuthenticationFailedException x) {
                Log.e(TAG, "Error sending message: " + message, x);
                notifications.showMailError(context, R.string.message_error_authentication);
            } catch (Exception x) {
                Log.e(TAG, "Error sending message: " + message, x);
                notifications.showMailError(context, R.string.message_error_sending_email);
            }
        }
    }

}
