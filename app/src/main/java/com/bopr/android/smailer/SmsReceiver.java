package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

import static android.provider.Telephony.Sms;

/**
 * Class SmsReceiver.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "bo.SmsReceiver";

    private MailSender mailer = new MailSender();

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Got SMS intent: " + intent);

        SmsMessage[] messages = Sms.Intents.getMessagesFromIntent(intent);
        for (SmsMessage smsMessage : messages) {
            mailer.send(new MailMessage(
                    smsMessage.getDisplayOriginatingAddress(),
                    smsMessage.getDisplayMessageBody()
            ));
        }
    }

}
