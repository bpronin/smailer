package com.bopr.android.smailer;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Class MailerService.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailerService extends IntentService {

    private static final String TAG = "bopr.MailerService";

    public MailerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Service started");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Processing mailer service intent");

        Mailer mailer = Mailer.getInstance();
        for (SmsMessage smsMessage : getSmsMessages(intent)) {
            mailer.send(this,
                    new MailMessage(
                            smsMessage.getDisplayOriginatingAddress(),
                            smsMessage.getDisplayMessageBody(),
                            smsMessage.getTimestampMillis()
                    )
            );
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private SmsMessage[] getSmsMessages(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Telephony.Sms.Intents.getMessagesFromIntent(intent);
        } else {
            Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");
            String format = intent.getStringExtra("format");

            int pduCount = pdus.length;
            SmsMessage[] messages = new SmsMessage[pduCount];
            for (int i = 0; i < pduCount; i++) {
                byte[] pdu = (byte[]) pdus[i];
                messages[i] = SmsMessage.createFromPdu(pdu, format);
            }
            return messages;
        }
    }

}
