package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsMessage;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static android.content.Intent.ACTION_NEW_OUTGOING_CALL;
import static android.provider.Telephony.Sms.Intents.getMessagesFromIntent;
import static android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED;
import static android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER;
import static android.telephony.TelephonyManager.EXTRA_STATE;
import static android.telephony.TelephonyManager.EXTRA_STATE_IDLE;
import static android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK;
import static android.telephony.TelephonyManager.EXTRA_STATE_RINGING;
import static com.bopr.android.smailer.CallProcessorService.startCallProcessingService;
import static com.bopr.android.smailer.util.AndroidUtil.deviceName;
import static java.lang.System.currentTimeMillis;

/**
 * Receives phone call and sms intents and starts mailer service.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallReceiver extends BroadcastReceiver {

    private static Logger log = LoggerFactory.getLogger("CallReceiver");

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    private static String lastCallState = EXTRA_STATE_IDLE;
    private static long callStartTime;
    private static boolean isIncomingCall;
    private static String lastCallNumber;

    @Override
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // TODO: 06.02.2020 deprecated
    public void onReceive(Context context, Intent intent) {
        log.debug("Received intent: " + intent);

        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_NEW_OUTGOING_CALL:
                    lastCallNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    break;
                case ACTION_PHONE_STATE_CHANGED:
                    onCallStateChanged(context, intent);
                    break;
                case SMS_RECEIVED:
                    onSmsReceived(context, intent);
                    break;
            }
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
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // TODO: 06.02.2020 deprecated
    private void onCallStateChanged(@NonNull Context context, @NonNull Intent intent) {
        String callState = intent.getStringExtra(EXTRA_STATE);
        if (callState != null && !lastCallState.equals(callState)) {
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

    /**
     * Processes sms intent.
     */
    private void onSmsReceived(@NonNull Context context, @NonNull Intent intent) {
        SmsMessage[] messages;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            messages = getMessagesFromIntent(intent);
        } else {
            messages = parseMessageLegacy(intent);
        }

        PhoneEvent event = new PhoneEvent();
        event.setIncoming(true);
        if (messages.length > 0) {
            StringBuilder text = new StringBuilder();
            for (SmsMessage message : messages) {
                text.append(message.getDisplayMessageBody());
            }

            event.setPhone(messages[0].getDisplayOriginatingAddress());
            event.setRecipient(deviceName());
            event.setStartTime(messages[0].getTimestampMillis()); /* time zone on emulator may be incorrect */
            event.setEndTime(event.getStartTime());
            event.setText(text.toString());
        }

        startCallProcessingService(context, event);
    }

    private void processCall(@NonNull Context context, boolean incoming, boolean missed) {
        PhoneEvent event = new PhoneEvent();
        event.setRecipient(deviceName());
        event.setPhone(lastCallNumber);
        event.setStartTime(callStartTime);
        event.setEndTime(currentTimeMillis());
        event.setIncoming(incoming);
        event.setMissed(missed);

        startCallProcessingService(context, event);
    }

    @NonNull
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    private SmsMessage[] parseMessageLegacy(Intent intent) {
        Object[] pdus = (Object[]) Objects.requireNonNull(intent.getSerializableExtra("pdus"));
        SmsMessage[] messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            byte[] pdu = (byte[]) pdus[i];
            messages[i] = SmsMessage.createFromPdu(pdu);
        }
        return messages;
    }
}