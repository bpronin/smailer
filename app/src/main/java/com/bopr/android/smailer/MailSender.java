package com.bopr.android.smailer;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.bopr.android.smailer.settings.SettingsActivity;
import com.bopr.android.smailer.util.mail.GMailSender;

/**
 * Class MailSender.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailSender {

    private static final String TAG = "bo.MailSender";
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

    public MailSenderProperties getProperties() {
        return properties;
    }

    public void setProperties(MailSenderProperties properties) {
        this.properties = properties;
        Log.d(TAG, "Mailer properties changed");
    }

    public void send(Context context, MailMessage message) {
        Log.d(TAG, "Processing message: " + message);
        new MailTask(context).execute(message);
    }

    private void sendMail(Context context, MailMessage message) {
        Log.d(TAG, "Sending mail: " + message);

        GMailSender mailSender = new GMailSender(properties.getUser(), properties.getPassword());
        try {
            mailSender.sendMail(properties.getSubject(), formatBody(message),
                    properties.getSender(), properties.getRecipients());
        } catch (Exception x) {
            Log.e(TAG, "Error sending message: " + message, x);
            Notifications.showMailError(context);
        }
    }

    private String formatBody(MailMessage message) {
        return message.getSender() + " " + message.getBody();
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
