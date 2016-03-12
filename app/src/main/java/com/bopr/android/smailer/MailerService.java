package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.bopr.android.smailer.util.LocationProvider;

/**
 * Class MailerService.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailerService extends IntentService {

    private static final String TAG = "bopr.MailerService";

    public static final String EXTRA_INCOMING = "direction";
    public static final String EXTRA_MISSED = "missed";
    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_END_TIME = "end_time";

    public static final String ACTION_SMS = "sms";
    public static final String ACTION_CALL = "call";

    private LocationProvider locationProvider;

    public MailerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
            new Mailer().send(this, message);
        } else {
            Log.e(TAG, "Null message");
        }
    }

    private MailMessage parseCallIntent(Intent intent) {
        return new MailMessage(
                intent.getStringExtra(EXTRA_PHONE_NUMBER),
                intent.getBooleanExtra(EXTRA_INCOMING, true),
                intent.getLongExtra(EXTRA_START_TIME, 0),
                intent.getLongExtra(EXTRA_END_TIME, 0),
                intent.getBooleanExtra(EXTRA_MISSED, false),
                false,
                null,
                locationProvider.getLocation()
        );
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

            return new MailMessage(
                    messages[0].getDisplayOriginatingAddress(),
                    true,
                    messages[0].getTimestampMillis(),
                    0,
                    false,
                    true,
                    text,
                    locationProvider.getLocation()
            );
        }
        return null;
    }

}
