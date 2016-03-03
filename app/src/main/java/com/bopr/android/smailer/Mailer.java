package com.bopr.android.smailer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.bopr.android.smailer.settings.Settings;
import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.MailTransport;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.mail.AuthenticationFailedException;

import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_PROTOCOL;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_PASSWORD;


/**
 * Class Mailer.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Mailer {

    private static final String TAG = "bopr.Mailer";
    private static Mailer instance;

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
        MailerProperties properties = readProperties(context);

        MailTransport transport = new MailTransport(properties.getUser(),
//                EncryptUtil.decrypt(context, properties.getPassword()),
                properties.getPassword(), properties.getProtocol(), properties.getHost(),
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
        String pattern = r.getString(R.string.email_body_pattern);
        SimpleDateFormat timeFormat = new SimpleDateFormat(r.getString(R.string.email_time_pattern), Locale.getDefault());
        String time = timeFormat.format(message.getTime());
        return String.format(pattern, message.getBody(), DeviceUtil.getDeviceName(), time);
    }

    private MailerProperties readProperties(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Settings.PREFERENCES_STORAGE_NAME, Context.MODE_PRIVATE);
        MailerProperties properties = new MailerProperties();

        properties.setUser(preferences.getString(KEY_PREF_SENDER_ACCOUNT, ""));
        properties.setPassword(preferences.getString(KEY_PREF_SENDER_PASSWORD, ""));
        properties.setRecipients(preferences.getString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, ""));
        properties.setProtocol(preferences.getString(KEY_PREF_EMAIL_PROTOCOL, ""));
        properties.setHost(preferences.getString(KEY_PREF_EMAIL_HOST, ""));
        properties.setPort(preferences.getString(KEY_PREF_EMAIL_PORT, ""));

        return properties;
    }

}
