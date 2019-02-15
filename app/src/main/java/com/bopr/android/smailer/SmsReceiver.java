package com.bopr.android.smailer;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import androidx.annotation.NonNull;

import static com.bopr.android.smailer.CallProcessorService.startMailService;
import static com.bopr.android.smailer.util.Util.safeEquals;

/**
 * Receives SMS intents and starts mailer service.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class SmsReceiver extends BroadcastReceiver {

    private static Logger log = LoggerFactory.getLogger("SmsReceiver");
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        log.debug("Received intent: " + intent);

        if (safeEquals(intent.getAction(), SMS_RECEIVED_ACTION)) {
            startMailService(context, parse(intent));
        }
    }

    /**
     * Parses sms intent into event object.
     */
    @NonNull
    PhoneEvent parse(@NonNull Intent intent) {
        SmsMessage[] messages;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            messages = parseMessage(intent);
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
            event.setStartTime(messages[0].getTimestampMillis()); /* time zone on emulator may be incorrect */
            event.setEndTime(event.getStartTime());
            event.setText(text.toString());
        }
        return event;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private SmsMessage[] parseMessage(Intent intent) {
        return Telephony.Sms.Intents.getMessagesFromIntent(intent);
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private SmsMessage[] parseMessageLegacy(Intent intent) {
        SmsMessage[] messages;
        Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");
        messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            byte[] pdu = (byte[]) pdus[i];
            messages[i] = SmsMessage.createFromPdu(pdu);
        }
        return messages;
    }
}