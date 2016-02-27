package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;

import static android.provider.Telephony.Sms;

/**
 * Class SmsReceiver.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "bo.SmsReceiver";

    private MailSender mailer = new MailSender();

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Got SMS intent: " + intent);

//        for (SmsMessage smsMessage : getMessagesFromIntent(intent)) {
//            mailer.send(new MailMessage(
//                    smsMessage.getDisplayOriginatingAddress(),
//                    smsMessage.getDisplayMessageBody()
//            ));
//        }
    }

    private SmsMessage[] getMessagesFromIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Sms.Intents.getMessagesFromIntent(intent);
        } else {
            Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
            String format = intent.getStringExtra("format");

//            int subId = intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY,
//                    SubscriptionManager.getDefaultSmsSubId());

            int pduCount = messages.length;
            SmsMessage[] msgs = new SmsMessage[pduCount];
            for (int i = 0; i < pduCount; i++) {
                byte[] pdu = (byte[]) messages[i];
                msgs[i] = SmsMessage.createFromPdu(pdu, format);
//                msgs[i].setSubId(subId);
            }
            return msgs;
        }
    }

}
