package com.bopr.android.smailer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;

/**
 * Class SmsParser.
 *
 * @author Boris Pronin (<a href="mailto:bpronin@bttprime.com">bpronin@bttprime.com</a>)
 */
class SmsParser {

    /**
     * Parses sms intent into plain object.
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
            event.setStartTime(messages[0].getTimestampMillis());
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
