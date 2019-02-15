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
import static com.bopr.android.smailer.CallProcessorService.startMailService;
import static java.lang.System.currentTimeMillis;

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
     * Incoming call: goes from IDLE to RINGING when it rings, to OFF HOOK when it's answered, to IDLE when its hung up.
     * Outgoing call: goes from IDLE to OFF HOOK when it dials out, to IDLE when hung up.
     *
     * @param context context
     * @param intent  call state intent
     */
    private void onCallStateChanged(Context context, Intent intent) {
        String callState = intent.getStringExtra(EXTRA_STATE);
        if (!lastCallState.equals(callState)) {
            if (callState.equals(EXTRA_STATE_RINGING)) {
                isIncomingCall = true;
                callStartTime = currentTimeMillis();
                lastCallNumber = intent.getStringExtra(EXTRA_INCOMING_NUMBER);
                log.debug("Call received");
            } else if (callState.equals(EXTRA_STATE_OFFHOOK)) {
                isIncomingCall = lastCallState.equals(EXTRA_STATE_RINGING);
                callStartTime = currentTimeMillis();
                log.debug(("Started " + (isIncomingCall ? "incoming" : "outgoing") + " call"));
            } else if (callState.equals(EXTRA_STATE_IDLE)) {
                if (lastCallState.equals(EXTRA_STATE_RINGING)) {
                    log.debug("Processing missed call");
                    processCall(context, true, true);
                } else if (isIncomingCall) {
                    log.debug("Processing incoming call");
                    processCall(context, true, false);
                } else {
                    log.debug("Processing outgoing call");
                    processCall(context, false, false);
                }
                lastCallNumber = null;
            }

            lastCallState = callState;
        }
    }

    private void processCall(Context context, boolean incoming, boolean missed) {
        PhoneEvent event = new PhoneEvent();
        event.setPhone(lastCallNumber);
        event.setStartTime(callStartTime);
        event.setEndTime(currentTimeMillis());
        event.setIncoming(incoming);
        event.setMissed(missed);

        startMailService(context, event);
    }

}