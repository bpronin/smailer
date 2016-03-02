package com.bopr.android.smailer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Date;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static android.provider.Telephony.Sms;

/**
 * Class SmsReceiver.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "bopr.SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Got SMS intent: " + intent);

        for (SmsMessage smsMessage : getMessagesFromIntent(intent)) {
            MailSender.getInstance().send(context, new MailMessage(
                    smsMessage.getDisplayOriginatingAddress(),
                    smsMessage.getDisplayMessageBody(),
                    new Date(smsMessage.getTimestampMillis())
            ));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private SmsMessage[] getMessagesFromIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Sms.Intents.getMessagesFromIntent(intent);
        } else {
            Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");
            String format = intent.getStringExtra("format");

            int pduCount = pdus.length;
            SmsMessage[] messages = new SmsMessage[pduCount];
            for (int i = 0; i < pduCount; i++) {
                byte[] pdu = (byte[]) pdus[i];
                messages[i] = SmsMessage.createFromPdu(pdu, format);
            }
            return messages;
        }
    }

    /**
     * Set the enabled setting for broadcast receiver component.
     */
    public static void enableComponent(Activity activity, boolean enabled) {
        ComponentName component = new ComponentName(activity, SmsReceiver.class);
        int state = (enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED);
        activity.getPackageManager().setComponentEnabledSetting(component, state, DONT_KILL_APP);

        Log.d(TAG, "SMS broadcast receiver state is: " + (enabled ? "ENABLED" : "DISABLED"));
    }
}
