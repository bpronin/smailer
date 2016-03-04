package com.bopr.android.smailer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.MailTransport;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
    private static Mailer instance;
    private MailerProperties properties;

    public static Mailer getInstance() {
        if (instance == null) {
            instance = new Mailer();
        }
        return instance;
    }

    private Mailer() {
    }

    public void send(Context context, MailMessage message) {
        Log.d(TAG, "Sending mail: " + message);

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE);
        properties = new MailerProperties(preferences);

        MailTransport transport = new MailTransport(properties.getUser(),
//                EncryptUtil.decrypt(context, properties.getPassword()),
                properties.getPassword(),  properties.getHost(),
                properties.getPort());

        try {
            transport.send(formatSubject(context, message), formatBody(context, message),
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

    private String formatSubject(Context context, MailMessage message) {
        Resources r = context.getResources();
        return r.getString(R.string.email_subject_prefix) + " "
                + String.format(r.getString(R.string.email_subject_incoming_sms_pattern), message.getPhone());
    }

    private String formatBody(Context context, MailMessage message) {
        Resources r = context.getResources();

        StringBuilder builder = new StringBuilder();
        builder.append(message.getBody());
        builder.append("\n");
        builder.append(r.getString(R.string.email_content_body_delimiter));
        builder.append("\n");
        builder.append(r.getString(R.string.email_content_sent_prefix));

        if (properties.isContentDeviceName()){
            builder.append(r.getString(R.string.email_content_from_prefix));
            builder.append(" ");
            builder.append(DeviceUtil.getDeviceName());
            builder.append("\n");
        }

        if (properties.isContentTime()){
            SimpleDateFormat format = new SimpleDateFormat(r.getString(R.string.email_content_time_pattern), Locale.getDefault());
            builder.append(r.getString(R.string.email_content_time_prefix));
            builder.append(" ");
            builder.append(format.format(message.getTime()));
            builder.append("\n");
        }

        return builder.toString();
    }

}
