package com.bopr.android.smailer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.app.PendingIntent.getBroadcast;
import static android.telephony.SmsManager.getDefault;

public class SmsTransport {

    private static final Logger log = LoggerFactory.getLogger("SmsTransport");

    public static final String ACTION_SMS_SENT = "sms_sent";
    public static final String ACTION_SMS_DELIVERED = "sms_delivered";

    private final Context context;

    public SmsTransport(Context context) {
        this.context = context;
    }

    public void send(String message, String phone) {
        SmsManager manager = getDefault();

        PendingIntent sentIntent = getBroadcast(context, 0, new Intent(ACTION_SMS_SENT), 0);
        PendingIntent deliveredIntent = getBroadcast(context, 0, new Intent(ACTION_SMS_DELIVERED), 0);
        for (String m : manager.divideMessage(message)) {
            manager.sendTextMessage(phone, null, m, sentIntent, deliveredIntent);

            log.debug("Sent SMS: " + message + " to " + phone);
        }
    }

}
