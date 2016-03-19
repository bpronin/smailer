package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Set;

import static android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER;
import static android.telephony.TelephonyManager.EXTRA_STATE;
import static android.telephony.TelephonyManager.EXTRA_STATE_IDLE;
import static android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.EXTRA_STATE_RINGING;
import static com.bopr.android.smailer.MailerService.ACTION_CALL;
import static com.bopr.android.smailer.MailerService.ACTION_SMS;
import static com.bopr.android.smailer.MailerService.EXTRA_END_TIME;
import static com.bopr.android.smailer.MailerService.EXTRA_INCOMING;
import static com.bopr.android.smailer.MailerService.EXTRA_MISSED;
import static com.bopr.android.smailer.MailerService.EXTRA_PHONE_NUMBER;
import static com.bopr.android.smailer.MailerService.EXTRA_START_TIME;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;

/**
 * Receives SMS and phone call intents and starts mailer service.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "CallReceiver";

    private static String lastCallState = EXTRA_STATE_IDLE;
    private static long callStartTime;
    private static boolean isIncomingCall;
    private static String lastCallNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received telephony intent: " + intent);

        if (Settings.getPreferences(context).getBoolean(KEY_PREF_SERVICE_ENABLED, false)) {
            switch (intent.getAction()) {
                case Telephony.Sms.Intents.SMS_RECEIVED_ACTION:
                    onIncomingSms(context, intent);
                    break;
                case Intent.ACTION_NEW_OUTGOING_CALL:
                    lastCallNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    break;
                case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                    onCallStateChanged(context, intent);
                    break;
            }
        }
    }

    /**
     * Deals with actual events.
     * Incoming call: goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up.
     * Outgoing call: goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up.
     *
     * @param context context
     * @param intent  call state intent
     */
    private void onCallStateChanged(Context context, Intent intent) {
        String callState = intent.getStringExtra(EXTRA_STATE);
        if (!lastCallState.equals(callState)) {
            if (callState.equals(EXTRA_STATE_RINGING)) {
                isIncomingCall = true;
                callStartTime = System.currentTimeMillis();
                lastCallNumber = intent.getStringExtra(EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "Call received");
            } else if (callState.equals(EXTRA_STATE_OFFHOOK)) {
                isIncomingCall = lastCallState.equals(EXTRA_STATE_RINGING);
                callStartTime = System.currentTimeMillis();
                Log.d(TAG, ("Started " + (isIncomingCall ? "incoming" : "outgoing") + " call"));
            } else if (callState.equals(EXTRA_STATE_IDLE)) {
                if (lastCallState.equals(EXTRA_STATE_RINGING)) {
                    onMissedCall(context, lastCallNumber, callStartTime);
                } else if (isIncomingCall) {
                    onIncomingCall(context, lastCallNumber, callStartTime, System.currentTimeMillis());
                } else {
                    onOutgoingCall(context, lastCallNumber, callStartTime, System.currentTimeMillis());
                }
                lastCallNumber = null;
            }

            lastCallState = callState;
        }
    }

    private void onIncomingCall(Context context, String number, long start, long end) {
        Log.d(TAG, "Processing incoming call");
        if (isSourceEnabled(context, VAL_PREF_TRIGGER_IN_CALLS)) {
            Intent intent = new Intent(context, MailerService.class);
            intent.setAction(ACTION_CALL);
            intent.putExtra(EXTRA_PHONE_NUMBER, number);
            intent.putExtra(EXTRA_INCOMING, true);
            intent.putExtra(EXTRA_START_TIME, start);
            intent.putExtra(EXTRA_END_TIME, end);

            context.startService(intent);
        }
    }

    private void onOutgoingCall(Context context, String number, long start, long end) {
        Log.d(TAG, "Processing outgoing call");
        if (isSourceEnabled(context, VAL_PREF_TRIGGER_OUT_CALLS)) {
            Intent intent = new Intent(context, MailerService.class);
            intent.setAction(ACTION_CALL);
            intent.putExtra(EXTRA_PHONE_NUMBER, number);
            intent.putExtra(EXTRA_INCOMING, false);
            intent.putExtra(EXTRA_START_TIME, start);
            intent.putExtra(EXTRA_END_TIME, end);

            context.startService(intent);
        }
    }

    private void onMissedCall(Context context, String number, long start) {
        Log.d(TAG, "Processing missed call");
        if (isSourceEnabled(context, VAL_PREF_TRIGGER_MISSED_CALLS)) {
            Intent intent = new Intent(context, MailerService.class);
            intent.setAction(ACTION_CALL);
            intent.putExtra(EXTRA_MISSED, true);
            intent.putExtra(EXTRA_PHONE_NUMBER, number);
            intent.putExtra(EXTRA_START_TIME, start);

            context.startService(intent);
        }
    }

    private void onIncomingSms(Context context, Intent smsIntent) {

        Log.d(TAG, "Processing incoming sms");
        if (isSourceEnabled(context, VAL_PREF_TRIGGER_IN_SMS)) {
            Intent intent = new Intent(context, MailerService.class);
            intent.setAction(ACTION_SMS);
            intent.fillIn(smsIntent, Intent.FILL_IN_DATA);
            context.startService(intent);
        }
    }

    private boolean isSourceEnabled(Context context, String source) {
        Set<String> options = Settings.getPreferences(context).getStringSet(KEY_PREF_EMAIL_TRIGGERS, null);
        return options != null && options.contains(source);
    }

}