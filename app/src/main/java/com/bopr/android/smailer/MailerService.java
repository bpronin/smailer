package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Class MailerService.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailerService extends IntentService {

    private static final String TAG = "MailerService";

    private static final String EXTRA_INCOMING = "direction";
    private static final String EXTRA_MISSED = "missed";
    private static final String EXTRA_PHONE_NUMBER = "phone_number";
    private static final String EXTRA_START_TIME = "start_time";
    private static final String EXTRA_END_TIME = "end_time";
    private static final String ACTION_SMS = "sms";
    private static final String ACTION_CALL = "call";
    private static final String ACTION_RESEND = "resend";

    private LocationProvider locationProvider;
    private Mailer mailer;

    public MailerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Database database = new Database(this);
        mailer = new Mailer(this, database);
        locationProvider = new LocationProvider(this, database);
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
        Log.d(TAG, "Processing mailer service intent:" + intent);

        switch (intent.getAction()) {
            case ACTION_SMS:
                mailer.send(parseSmsIntent(intent));
                break;
            case ACTION_CALL:
                mailer.send(parseCallIntent(intent));
                break;
            case ACTION_RESEND:
                mailer.sendAllUnsent();
                break;
        }
    }

    @Nullable
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

    @Nullable
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

    public static void startForSms(Context context, Intent smsIntent) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_SMS);
        intent.fillIn(smsIntent, Intent.FILL_IN_DATA);

        context.startService(intent);
    }

    public static void startForMissingCall(Context context, String number, long start) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_CALL);
        intent.putExtra(EXTRA_MISSED, true);
        intent.putExtra(EXTRA_PHONE_NUMBER, number);
        intent.putExtra(EXTRA_START_TIME, start);

        context.startService(intent);
    }

    public static void startForIncomingCall(Context context, String number, long start, long end) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_CALL);
        intent.putExtra(EXTRA_PHONE_NUMBER, number);
        intent.putExtra(EXTRA_INCOMING, true);
        intent.putExtra(EXTRA_START_TIME, start);
        intent.putExtra(EXTRA_END_TIME, end);

        context.startService(intent);
    }

    public static void startForOutgoingCall(Context context, String number, long start, long end) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_CALL);
        intent.putExtra(EXTRA_PHONE_NUMBER, number);
        intent.putExtra(EXTRA_INCOMING, false);
        intent.putExtra(EXTRA_START_TIME, start);
        intent.putExtra(EXTRA_END_TIME, end);

        context.startService(intent);
    }

    public static void startForResendPostponed(Context context) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_RESEND);

        context.startService(intent);
    }
}