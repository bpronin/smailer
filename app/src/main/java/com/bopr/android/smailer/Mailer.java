package com.bopr.android.smailer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bopr.android.smailer.util.ContactUtil;
import com.bopr.android.smailer.util.Cryptor;
import com.bopr.android.smailer.util.DeviceUtil;
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
    private final Context context;

    public Mailer(Context context) {
        this.context = context;
    }

    public void send(MailMessage message) {
        Log.d(TAG, "Sending mail: " + message);

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE);
        MailerProperties properties = new MailerProperties(preferences);

        MailTransport transport = new MailTransport(properties.getUser(),
                Cryptor.decrypt(properties.getPassword(), context),
                properties.getHost(), properties.getPort());

        MailFormatter formatter = new MailFormatter(message, context.getResources(),
                properties, ContactUtil.getContactName(context, message.getPhone()),
                DeviceUtil.getDeviceName());

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
