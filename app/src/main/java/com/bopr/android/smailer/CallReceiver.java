package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER;
import static android.telephony.TelephonyManager.EXTRA_STATE;
import static android.telephony.TelephonyManager.EXTRA_STATE_IDLE;
import static android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.EXTRA_STATE_RINGING;
import static com.bopr.android.smailer.MailerService.createIncomingCallIntent;
import static com.bopr.android.smailer.MailerService.createMissedCallIntent;
import static com.bopr.android.smailer.MailerService.createOutgoingCallIntent;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.isTriggerEnabled;

/**
 * Receives phone call intents and starts mailer service.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallReceiver extends BroadcastReceiver {

    private static Logger log = LoggerFactory.getLogger("CallReceiver");

    private static String lastCallState = EXTRA_STATE_IDLE;
    private static long callStartTime;
    private static boolean isIncomingCall;
    private static String lastCallNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        log.debug("Received intent: " + intent);

        if (Settings.isServiceEnabled(context)) {
            switch (intent.getAction()) {
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
                log.debug("Call received");
            } else if (callState.equals(EXTRA_STATE_OFFHOOK)) {
                isIncomingCall = lastCallState.equals(EXTRA_STATE_RINGING);
                callStartTime = System.currentTimeMillis();
                log.debug(("Started " + (isIncomingCall ? "incoming" : "outgoing") + " call"));
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
        log.debug("Processing incoming call");
        if (isTriggerEnabled(context, VAL_PREF_TRIGGER_IN_CALLS)) {
            context.startService(createIncomingCallIntent(context, number, start, end));
        }
    }

    private void onOutgoingCall(Context context, String number, long start, long end) {
        log.debug("Processing outgoing call");
        if (isTriggerEnabled(context, VAL_PREF_TRIGGER_OUT_CALLS)) {
            context.startService(createOutgoingCallIntent(context, number, start, end));
        }
    }

    private void onMissedCall(Context context, String number, long start) {
        log.debug("Processing missed call");
        if (isTriggerEnabled(context, VAL_PREF_TRIGGER_MISSED_CALLS)) {
            context.startService(createMissedCallIntent(context, number, start));
        }
    }

}