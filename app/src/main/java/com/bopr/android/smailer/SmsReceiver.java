package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * SMS receiver. Receives SMS intents and starts mailer service.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "bopr.SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Processing SMS intent: " + intent);

        Intent serviceIntent = new Intent(context, MailerService.class);
        serviceIntent.fillIn(intent, Intent.FILL_IN_DATA);
        context.startService(serviceIntent);
    }

}
