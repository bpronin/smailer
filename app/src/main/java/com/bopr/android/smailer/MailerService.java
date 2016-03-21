package com.bopr.android.smailer;

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

    private static final String TAG = "MailerService";

    public static final String EXTRA_INCOMING = "direction";
    public static final String EXTRA_MISSED = "missed";
    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_END_TIME = "end_time";

    public static final String ACTION_SMS = "sms";
    public static final String ACTION_CALL = "call";

    private LocationProvider locationProvider;
    private Mailer mailer;

    public MailerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mailer = new Mailer(this);
        locationProvider = new LocationProvider(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        locationProvider.start();
        super.onStart(intent, startId);
        Log.d(TAG, "Service started");
    }

    @Override
    public void onDestroy() {
        locationProvider.stop();
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Processing mailer service intent");

        MailMessage message;
        if (intent.getAction().equals(ACTION_SMS)) {
            message = parseSmsIntent(intent);
        } else {
            message = parseCallIntent(intent);
        }

        if (message != null) {
            mailer.send(message);
        } else {
            Log.e(TAG, "Null message");
        }
    }

    private MailMessage parseCallIntent(Intent intent) {
        MailMessage message = new MailMessage();
        message.setPhone(intent.getStringExtra(EXTRA_PHONE_NUMBER));
        message.setIncoming(intent.getBooleanExtra(EXTRA_INCOMING, true));
        message.setStartTime(intent.getLongExtra(EXTRA_START_TIME, 0));
        message.setEndTime(intent.getLongExtra(EXTRA_END_TIME, 0));
        message.setMissed(intent.getBooleanExtra(EXTRA_MISSED, false));
        message.setSms(false);
        message.setLocation(locationProvider.getLocation());

        return message;
    }

    private MailMessage parseSmsIntent(Intent intent) {
        SmsMessage[] messages;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        } else {
            Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                byte[] pdu = (byte[]) pdus[i];
                //noinspection deprecation
                messages[i] = SmsMessage.createFromPdu(pdu);
            }
        }

        if (messages.length > 0) {
            String text = "";
            for (SmsMessage message : messages) {
                text += message.getDisplayMessageBody();
            }

            MailMessage message = new MailMessage();
            message.setPhone(messages[0].getDisplayOriginatingAddress());
            message.setIncoming(true);
            message.setStartTime(messages[0].getTimestampMillis());
            message.setSms(true);
            message.setText(text);
            message.setLocation(locationProvider.getLocation());

            return message;
        }
        return null;
    }

}
