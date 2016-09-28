package com.bopr.android.smailer;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;

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

        if (intent.getAction().equals(SMS_RECEIVED_ACTION)
                && Settings.isServiceEnabled(context)
                && Settings.isTriggerEnabled(context, VAL_PREF_TRIGGER_IN_SMS)) {
            log.debug("Processing incoming sms");
            Sms sms = parse(intent);
            context.startService(MailerService.createSmsIntent(context, sms.phone, sms.time, sms.text, true));
        }
    }

    /**
     * Parses sms intent into plain object.
     */
    @NonNull
    private Sms parse(@NonNull Intent intent) {
        SmsMessage[] messages;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            messages = parseSmsMessages(intent);
        } else {
            messages = parseSmsMessagesLegacy(intent);
        }

        Sms sms = new Sms();
        if (messages.length > 0) {

            StringBuilder text = new StringBuilder();
            for (SmsMessage message : messages) {
                text.append(message.getDisplayMessageBody());
            }

            sms.phone = messages[0].getDisplayOriginatingAddress();
            sms.time = messages[0].getTimestampMillis();
            sms.text = text.toString();
        }
        return sms;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private SmsMessage[] parseSmsMessages(Intent intent) {
        return Telephony.Sms.Intents.getMessagesFromIntent(intent);
    }

    @NonNull
    @SuppressWarnings("deprecation")
    private SmsMessage[] parseSmsMessagesLegacy(Intent intent) {
        SmsMessage[] messages;
        Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");
        messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            byte[] pdu = (byte[]) pdus[i];
            messages[i] = SmsMessage.createFromPdu(pdu);
        }
        return messages;
    }

    private class Sms {

        private String phone;
        private long time;
        private String text;
    }

}