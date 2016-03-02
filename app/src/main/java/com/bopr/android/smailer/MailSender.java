package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.EncryptUtil;
import com.bopr.android.smailer.util.mail.GMailSender;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.mail.AuthenticationFailedException;


/**
 * Class MailSender.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailSender {

    private static final String TAG = "bopr.MailSender";
    private static MailSender instance;

    private MailSenderProperties properties = new MailSenderProperties();

    public static MailSender getInstance() {
        if (instance == null) {
            instance = new MailSender();
        }
        return instance;
    }

    private MailSender() {
    }

    public void setProperties(MailSenderProperties properties) {
        this.properties = properties;
        Log.d(TAG, "Mailer properties changed: " + properties);
    }

    public void send(Context context, MailMessage message) {
        Log.d(TAG, "Processing message: " + message);
        new MailTask(context).execute(message);
    }

    private void sendMail(Context context, MailMessage message) {
        Log.d(TAG, "Sending mail: " + message);

        GMailSender sender = new GMailSender(properties.getUser(),
//                EncryptUtil.decrypt(context, properties.getPassword()),
                properties.getPassword(),
                properties.getProtocol(), properties.getHost(), properties.getPort());
        try {
            sender.sendMail(formatSubject(context, message), formatBody(context, message),
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
        String time = timeFormat.format(message.getDate());
        return String.format(pattern, message.getBody(), DeviceUtil.getDeviceName(), time);
    }

    private class MailTask extends AsyncTask<MailMessage, Void, Void> {

        private Context context;

        public MailTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(MailMessage... messages) {
            sendMail(context, messages[0]);
            return null;
        }
    }

}
