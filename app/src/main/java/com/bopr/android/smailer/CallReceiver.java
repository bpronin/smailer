package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.telephony.TelephonyManager.*;
import static com.bopr.android.smailer.MailerService.createEventIntent;

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

        //noinspection ConstantConditions
        switch (intent.getAction()) {
            case Intent.ACTION_NEW_OUTGOING_CALL:
                lastCallNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                break;
            case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                onCallStateChanged(context, intent);
                break;
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

        PhoneEvent event = new PhoneEvent();
        event.setIncoming(true);
        event.setPhone(number);
        event.setStartTime(start);
        event.setEndTime(end);

        context.startService(createEventIntent(context, event));
    }

    private void onOutgoingCall(Context context, String number, long start, long end) {
        PhoneEvent event = new PhoneEvent();
        event.setIncoming(false);
        event.setPhone(number);
        event.setStartTime(start);
        event.setEndTime(end);

        context.startService(createEventIntent(context, event));
    }

    private void onMissedCall(Context context, String number, long start) {
        log.debug("Processing missed call");

        PhoneEvent event = new PhoneEvent();
        event.setMissed(true);
        event.setPhone(number);
        event.setStartTime(start);

        context.startService(createEventIntent(context, event));
    }

}