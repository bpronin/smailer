package com.bopr.android.smailer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bopr.android.smailer.util.MailTransport;

import javax.mail.AuthenticationFailedException;

import static android.content.Context.MODE_PRIVATE;
import static com.bopr.android.smailer.settings.Settings.PREFERENCES_STORAGE_NAME;


/**
 * Class Mailer.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Mailer {

    private static final String TAG = "bopr.Mailer";

    public Mailer() {
    }

    public void send(Context context, MailMessage message) {
        Log.d(TAG, "Sending mail: " + message);

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE);
        MailerProperties properties = new MailerProperties(preferences);

        MailTransport transport = new MailTransport(properties.getUser(),
//                EncryptUtil.decrypt(context, properties.getPassword()),
                properties.getPassword(), properties.getHost(),
                properties.getPort());

        MailFormatter formatter = new MailFormatter(context, properties, message);
        try {
            transport.send(formatter.getSubject(), formatter.getBody(),
                    properties.getUser(), properties.getRecipients());

            Notifications.removeMailError(context);
        } catch (AuthenticationFailedException x) {
            Log.e(TAG, "Error sending message: " + message, x);
            Notifications.showMailAuthenticationError(context);
        } catch (Exception x) {
            Log.e(TAG, "Error sending message: " + message, x);
            Notifications.showMailError(context);
        }
    }

}
