package com.bopr.android.smailer;

import android.os.AsyncTask;
import android.util.Log;

import com.bopr.android.smailer.util.mail.GMailSender;

/**
 * Class MailSender.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailSender {

    private static final String TAG = "bo.MailSender";

    public void send(MailMessage message) {
        Log.d(TAG, "Processing message: " + message);
        new MailTask().execute(message);
    }

    private String formatBody(MailMessage message) {
        return message.getSender() + " " + message.getBody();
    }

    private void sendMail(MailMessage message) {
        String user = "xxxxxxx@gmail.com";
        String password = "xxxxxxx";
        String recipients = "xxxxxxx@yandex.ru";
        String subject = "SMS";
        String sender = "xxxxxxx@gmail.com";

        GMailSender mailSender = new GMailSender(user, password);
        try {
            mailSender.sendMail(subject, formatBody(message), sender, recipients);
        } catch (Exception x) {
            Log.e(TAG, "Error sending message: " + message);
        }
    }

    private class MailTask extends AsyncTask<MailMessage, Void, Void> {

        @Override
        protected Void doInBackground(MailMessage... messages) {
            sendMail(messages[0]);
            return null;
        }
    }

}
